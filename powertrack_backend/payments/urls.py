from django.urls import path
from payments import views

app_name = "payments"

urlpatterns = [
    path('', views.home, name="home"),
    path('token', views.token, name='token'),
    path('pay', views.pay, name='pay'),
    path('stk', views.stk, name="stk"),
    path('buy-token/', views.buy_token, name='buy-token'),
    path('callback/', views.mpesa_callback, name='mpesa-callback'),
]
