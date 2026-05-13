import os

class MpesaC2bCredential:
    consumer_key = os.environ.get('MPESA_CONSUMER_KEY', '6YAHAG00wGSOCq3WZTOGcSUYyR1Dif9niiaIAmX9i8nvjfbF')
    consumer_secret = os.environ.get('MPESA_CONSUMER_SECRET', 'BmsbTiHARXGAWDXYJB4LqLnHkX2QLRfQvqCc4zGcur6XXArPQVl8oDgYjvk3v9Pu')
    api_URL = 'https://sandbox.safaricom.co.ke/oauth/v1/generate?grant_type=client_credentials'

class LipanaMpesaPpassword:
    Business_short_code = os.environ.get('MPESA_SHORTCODE', '174379')
    passkey = os.environ.get('MPESA_PASSKEY', 'bfb279f9aa9bdbcf158e97dd71a467cd2e0c893059b10f78e6b72ada1ed2c919')
