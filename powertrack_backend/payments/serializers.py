from rest_framework import serializers

class BuyTokenSerializer(serializers.Serializer):
    meter_number = serializers.CharField(max_length=20)
    phone_number = serializers.CharField(max_length=15)
    amount = serializers.DecimalField(max_digits=10, decimal_places=2)
