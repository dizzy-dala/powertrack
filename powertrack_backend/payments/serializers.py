from rest_framework import serializers
import re

class BuyTokenSerializer(serializers.Serializer):
    meter_number = serializers.CharField(max_length=20)
    phone_number = serializers.CharField(max_length=15)
    amount = serializers.DecimalField(max_digits=10, decimal_places=2)

    def validate_phone_number(self, value):
        # Remove any spaces
        value = value.strip().replace(" ", "")

        # Check patterns: 07..., 01..., 2547..., 2541...
        pattern = r'^(07|01)\d{8}$|^(2547|2541)\d{8}$'
        if not re.match(pattern, value):
            raise serializers.ValidationError(
                "Invalid phone number. Use 07..., 01..., 2547... or 2541... format."
            )
        return value
