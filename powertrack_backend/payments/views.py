from django.shortcuts import render
from django.http import HttpResponse, JsonResponse
import requests
from requests.auth import HTTPBasicAuth
import json
import base64
import random
import os
from datetime import datetime
from django.views.decorators.csrf import csrf_exempt
from rest_framework.decorators import api_view
from rest_framework.response import Response
from rest_framework import status

from .credentials import MpesaC2bCredential, LipanaMpesaPpassword
from .serializers import BuyTokenSerializer
from .models import Transaction

# Helper function to get Access Token
def get_access_token():
    try:
        res = requests.get(
            MpesaC2bCredential.api_URL,
            auth=HTTPBasicAuth(MpesaC2bCredential.consumer_key, MpesaC2bCredential.consumer_secret),
            timeout=10
        )

        # Printing for debugging
        print(f"DEBUG: Request URL: {MpesaC2bCredential.api_URL}")
        print(f"DEBUG: Status Code: {res.status_code}")
        print(f"DEBUG: Response Text: {res.text}")

        res.raise_for_status()
        mpesa_access_token = res.json()
        return mpesa_access_token.get("access_token")
    except requests.exceptions.HTTPError as errh:
        print(f"Http Error: {errh}")
    except requests.exceptions.ConnectionError as errc:
        print(f"Error Connecting: {errc}")
    except requests.exceptions.Timeout as errt:
        print(f"Timeout Error: {errt}")
    except requests.exceptions.RequestException as err:
        print(f"OOps: Something Else: {err}")
    except Exception as e:
        print(f"Error fetching access token: {e}")
    return None

@api_view(['POST'])
def buy_token(request):
    serializer = BuyTokenSerializer(data=request.data)
    if serializer.is_valid():
        meter_number = serializer.validated_data['meter_number']
        phone_number = serializer.validated_data['phone_number']
        amount = int(serializer.validated_data['amount'])

        # Mpesa STK Push Logic
        access_token = get_access_token()
        if not access_token:
            return Response({
                "status": "error",
                "message": "Could not generate M-Pesa access token. Check your credentials or internet connection."
            }, status=status.HTTP_500_INTERNAL_SERVER_ERROR)

        api_url = "https://sandbox.safaricom.co.ke/mpesa/stkpush/v1/processrequest"
        headers = {"Authorization": "Bearer %s" % access_token}

        # Format phone number to 254... for M-Pesa Daraja API
        formatted_phone = phone_number
        if formatted_phone.startswith('0'):
            formatted_phone = '254' + formatted_phone[1:]
        elif formatted_phone.startswith('+'):
            formatted_phone = formatted_phone[1:]
        # Numbers starting with 254 already are handled by default

        timestamp = datetime.now().strftime('%Y%m%d%H%M%S')
        data_to_encode = LipanaMpesaPpassword.Business_short_code + LipanaMpesaPpassword.passkey + timestamp
        password = base64.b64encode(data_to_encode.encode()).decode('utf-8')

        stk_request = {
            "BusinessShortCode": LipanaMpesaPpassword.Business_short_code,
            "Password": password,
            "Timestamp": timestamp,
            "TransactionType": "CustomerPayBillOnline",
            "Amount": amount,
            "PartyA": formatted_phone,
            "PartyB": LipanaMpesaPpassword.Business_short_code,
            "PhoneNumber": formatted_phone,
            "CallBackURL": os.environ.get('MPESA_CALLBACK_URL', "https://opposite-violet-shy.ngrok-free.dev/callback/"),
            "AccountReference": meter_number,
            "TransactionDesc": "Token Purchase"
        }

        try:
            response = requests.post(api_url, json=stk_request, headers=headers)
            res_data = response.json()

            if res_data.get('ResponseCode') == '0':
                # Save transaction to DB
                Transaction.objects.create(
                    meter_number=meter_number,
                    phone_number=formatted_phone,
                    amount=amount,
                    checkout_request_id=res_data.get('CheckoutRequestID'),
                    merchant_request_id=res_data.get('MerchantRequestID'),
                    status='Pending'
                )
                return Response({
                    "status": "success",
                    "message": "STK Push sent successfully. Please enter your PIN.",
                    "checkout_request_id": res_data.get('CheckoutRequestID')
                }, status=status.HTTP_200_OK)
            else:
                return Response({
                    "status": "error",
                    "message": res_data.get('ResponseDescription', 'STK Push failed')
                }, status=status.HTTP_400_BAD_REQUEST)

        except Exception as e:
            return Response({"status": "error", "message": str(e)}, status=status.HTTP_500_INTERNAL_SERVER_ERROR)

    return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)

@api_view(['GET'])
def check_status(request, checkout_request_id):
    try:
        transaction = Transaction.objects.get(checkout_request_id=checkout_request_id)
        return Response({
            "status": transaction.status.lower(), # 'pending', 'success', or 'failed'
            "message": transaction.result_desc or "Processing...",
            "checkout_request_id": transaction.checkout_request_id,
            "amount": transaction.amount,
            "token": transaction.token,
            "units": transaction.units
        }, status=status.HTTP_200_OK)
    except Transaction.DoesNotExist:
        return Response({"status": "error", "message": "Transaction not found"}, status=status.HTTP_404_NOT_FOUND)

@csrf_exempt
def mpesa_callback(request):
    if request.method == 'POST':
        try:
            data = json.loads(request.body)
            stk_callback = data.get('Body', {}).get('stkCallback', {})
            result_code = stk_callback.get('ResultCode')
            result_desc = stk_callback.get('ResultDesc')
            checkout_request_id = stk_callback.get('CheckoutRequestID')

            try:
                transaction = Transaction.objects.get(checkout_request_id=checkout_request_id)
                transaction.result_code = result_code
                transaction.result_desc = result_desc

                if result_code == 0:
                    # Success
                    transaction.status = 'Success'

                    # Generate fake token (e.g., 20 digits: 4-4-4-4-4)
                    transaction.token = "-".join(["".join([str(random.randint(0, 9)) for _ in range(4)]) for _ in range(5)])

                    # Rough calculation of units (KES 25 per unit for simulation)
                    transaction.units = float(transaction.amount) / 25.0

                    # Extract receipt number and other metadata if needed
                    items = stk_callback.get('CallbackMetadata', {}).get('Item', [])
                    for item in items:
                        if item.get('Name') == 'MpesaReceiptNumber':
                            transaction.mpesa_receipt_number = item.get('Value')
                        # You can also capture 'Amount' here if you want to verify it
                else:
                    # Failed or Cancelled (e.g., 1032 for Request cancelled by user)
                    transaction.status = 'Failed'

                transaction.save()
                return JsonResponse({"ResultCode": 0, "ResultDesc": "Accepted"})
            except Transaction.DoesNotExist:
                # If transaction is not found, we still return success to Safaricom
                # to acknowledge receipt of the callback
                return JsonResponse({"ResultCode": 0, "ResultDesc": "Transaction not found but callback received"})
        except Exception as e:
            return JsonResponse({"ResultCode": 1, "ResultDesc": str(e)})

    return HttpResponse("Invalid request")

@api_view(['GET'])
def get_history(request, meter_number):
    transactions = Transaction.objects.filter(meter_number=meter_number).order_by('-created_at')
    data = []
    for tx in transactions:
        data.append({
            "id": tx.id,
            "amount": float(tx.amount),
            "units": float(tx.units) if tx.units else 0.0,
            "token": tx.token or "",
            "status": tx.status,
            "date": tx.created_at.strftime("%Y-%m-%d %H:%M:%S")
        })
    return Response(data)

@api_view(['GET'])
def get_balance(request, meter_number):
    from django.db.models import Sum
    total_units = Transaction.objects.filter(meter_number=meter_number, status='Success').aggregate(Sum('units'))['units__sum'] or 0.0
    return Response({"meter_number": meter_number, "balance": float(total_units)})

# Kept for backward compatibility or other uses
def home(request):
    return render(request, 'home.html', {'navbar':'home'})

def token(request):
    return render(request, 'token.html', {"token": get_access_token()})

def pay(request):
    return HttpResponse("Use /buy-token/ API endpoint")

def stk(request):
    return render(request, 'pay.html', {'navbar':'stk'})
