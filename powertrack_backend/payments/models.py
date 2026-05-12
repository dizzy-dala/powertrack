from django.db import models

class Transaction(models.Model):
    meter_number = models.CharField(max_length=20)
    phone_number = models.CharField(max_length=15)
    amount = models.DecimalField(max_digits=10, decimal_places=2)
    checkout_request_id = models.CharField(max_length=100, unique=True)
    merchant_request_id = models.CharField(max_length=100)
    status = models.CharField(max_length=20, default='Pending') # Pending, Success, Failed
    result_code = models.IntegerField(null=True, blank=True)
    result_desc = models.TextField(null=True, blank=True)
    mpesa_receipt_number = models.CharField(max_length=20, blank=True, null=True)
    token = models.CharField(max_length=100, blank=True, null=True)
    units = models.DecimalField(max_digits=10, decimal_places=2, null=True, blank=True)
    created_at = models.DateTimeField(auto_now_add=True)
    updated_at = models.DateTimeField(auto_now=True)

    def __str__(self):
        return f"{self.phone_number} - {self.amount} - {self.status}"
