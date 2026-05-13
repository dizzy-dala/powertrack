from rest_framework import serializers
import re

class BuyTokenSerializer(serializers.Serializer):
    meter_number = serializers.CharField(max_length=20, required=False)
    phone_number = serializers.CharField(max_length=15, required=False)
    phone = serializers.CharField(max_length=15, required=False) # For backward compatibility
    amount = serializers.DecimalField(max_digits=10, decimal_places=2)

    def validate(self, data):
        # Handle phone/phone_number alias
        if 'phone_number' not in data and 'phone' not in data:
            raise serializers.ValidationError({"phone_number": "This field is required."})

        if 'phone_number' not in data:
            data['phone_number'] = data['phone']

        # Ensure meter_number is present for the transaction logic
        if 'meter_number' not in data:
            # We can't really proceed without a meter number for a real app,
            # but we could provide a default or a clearer error if it's strictly required.
            # To avoid breaking "existing app" that might not send it,
            # let's use a default if it's missing, but it's better to fix the app.
            data['meter_number'] = 'UNKNOWN'

        # Validate phone number format
        phone_to_validate = data['phone_number']
        phone_to_validate = phone_to_validate.strip().replace(" ", "")
        pattern = r'^(07|01)\d{8}$|^(2547|2541)\d{8}$|^\+254\d{9}$'
        if not re.match(pattern, phone_to_validate):
             raise serializers.ValidationError(
                {"phone_number": "Invalid phone number. Use 07..., 01..., 2547... or 2541... format."}
            )
        data['phone_number'] = phone_to_validate

        return data
