from django.db import models

class Transaction(models.Model):
    meter_number = models.CharField(max_length=20)
    phone_number = models.CharField(max_length=15)
    amount = models.DecimalField(max_digits=10, decimal_places=2)
    checkout_request_id = models.CharField(max_length=100, unique=True)
    merchant_request_id = models.CharField(max_length=100)
    status = models.CharField(max_length=20, default='Pending') # Pending, Success, Failed
    mpesa_receipt_number = models.CharField(max_length=20, blank=True, null=True)
    created_at = models.DateTimeField(auto_now_add=True)
    updated_at = models.DateTimeField(auto_now=True)

    def __str__(self):
        return f"{self.phone_number} - {self.amount} - {self.status}"
