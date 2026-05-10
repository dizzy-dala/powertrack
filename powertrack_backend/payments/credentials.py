import requests
import json
from requests.auth import HTTPBasicAuth
from datetime import datetime
import base64

class MpesaC2bCredential:
    consumer_key = '6YAHAG00wGSOCq3WZTOGcSUYyR1Dif9niiaIAmX9i8nvjfbF'
    consumer_secret = 'BmsbTiHARXGAWDXYJB4LqLnHkX2QLRfQvqCc4zGcur6XXArPQVl8oDgYjvk3v9Pus'
    api_URL = 'https://sandbox.safaricom.co.ke/oauth/v1/generate?grant_type=client_credentials'

class LipanaMpesaPpassword:
    Business_short_code = "174379"
    passkey = 'bfb279f9aa9bdbcf158e97dd71a467cd2e0c893059b10f78e6b72ada1ed2c919'
