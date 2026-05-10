from django.shortcuts import render
from django.http import HttpResponse, JsonResponse
import requests
from requests.auth import HTTPBasicAuth
import json
import base64
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
        res.raise_for_status()
        mpesa_access_token = res.json()
        return mpesa_access_token.get("access_token")
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

        # Format phone number to 254...
        formatted_phone = phone_number
        if formatted_phone.startswith('0'):
            formatted_phone = '254' + formatted_phone[1:]
        elif formatted_phone.startswith('+'):
            formatted_phone = formatted_phone[1:]

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
            "CallBackURL": "https://your-domain.com/payments/callback/", # You need a public URL for this to work
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

@csrf_exempt
def mpesa_callback(request):
    if request.method == 'POST':
        data = json.loads(request.body)
        stk_callback = data.get('Body', {}).get('stkCallback', {})
        result_code = stk_callback.get('ResultCode')
        checkout_request_id = stk_callback.get('CheckoutRequestID')

        try:
            transaction = Transaction.objects.get(checkout_request_id=checkout_request_id)
            if result_code == 0:
                # Success
                transaction.status = 'Success'
                # Extract receipt number from CallbackMetadata
                items = stk_callback.get('CallbackMetadata', {}).get('Item', [])
                for item in items:
                    if item.get('Name') == 'MpesaReceiptNumber':
                        transaction.mpesa_receipt_number = item.get('Value')
                        break
            else:
                # Failed or Cancelled
                transaction.status = 'Failed'

            transaction.save()
            return JsonResponse({"ResultCode": 0, "ResultDesc": "Accepted"})
        except Transaction.DoesNotExist:
            return JsonResponse({"ResultCode": 1, "ResultDesc": "Transaction not found"})

    return HttpResponse("Invalid request")

# Kept for backward compatibility or other uses
def home(request):
    return render(request, 'home.html', {'navbar':'home'})

def token(request):
    return render(request, 'token.html', {"token": get_access_token()})

def pay(request):
    return HttpResponse("Use /buy-token/ API endpoint")

def stk(request):
    return render(request, 'pay.html', {'navbar':'stk'})
