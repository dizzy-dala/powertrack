from django.contrib import admin
from .models import Transaction

@admin.register(Transaction)
class TransactionAdmin(admin.ModelAdmin):
    list_display = ('meter_number', 'phone_number', 'amount', 'status', 'mpesa_receipt_number', 'created_at')
    list_filter = ('status', 'created_at')
    search_fields = ('meter_number', 'phone_number', 'checkout_request_id', 'mpesa_receipt_number')
    readonly_fields = ('checkout_request_id', 'merchant_request_id', 'result_code', 'result_desc')
