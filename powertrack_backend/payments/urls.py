from django.urls import path
from payments import views

app_name = "stkpush"

urlpatterns = [
    path('', views.home, name="home"),
    path('token', views.token, name='token'),
    path('pay', views.pay, name='pay'),
    path('stk', views.stk, name="stk"),
    path('buy-token/', views.buy_token, name='buy-token'),
    path('check-status/<str:checkout_request_id>/', views.check_status, name='check-status'),
    path('callback/', views.mpesa_callback, name='mpesa-callback'),
    path('history/<str:meter_number>/', views.get_history, name='get-history'),
    path('balance/<str:meter_number>/', views.get_balance, name='get-balance'),
]
