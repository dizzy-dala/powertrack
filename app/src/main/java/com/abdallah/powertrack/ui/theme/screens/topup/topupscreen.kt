package com.abdallah.powertrack.ui.theme.screens.topup

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Money
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.abdallah.powertrack.data.PaymentViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopUpScreen(navController: NavController) {
    val paymentViewModel: PaymentViewModel = viewModel()
    val context = LocalContext.current

    var amount by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var cardNumber by remember { mutableStateOf("") }
    var pin by remember { mutableStateOf("") }
    var selectedMethod by remember { mutableStateOf("M-Pesa") }
    var showConfirmDialog by remember { mutableStateOf(false) }
    var snackbarHostState = remember { SnackbarHostState() }
    val isProcessing by paymentViewModel.isProcessing
    val scope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Buy Electricity Token") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.Payment,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(24.dp))

            // Amount Field
            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it },
                label = { Text("Enter Amount (KES)") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                prefix = { Text("KES ") }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Conditional Fields based on method
            if (selectedMethod == "M-Pesa" || selectedMethod == "Airtel Money") {
                OutlinedTextField(
                    value = phoneNumber,
                    onValueChange = { phoneNumber = it },
                    label = { Text("Mobile Number (07... / 01...)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    leadingIcon = { Icon(Icons.Default.Smartphone, contentDescription = null) }
                )
            } else if (selectedMethod == "Visa Card" || selectedMethod == "MasterCard") {
                OutlinedTextField(
                    value = cardNumber,
                    onValueChange = { cardNumber = it },
                    label = { Text("Card Number (16 digits)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    leadingIcon = { Icon(Icons.Default.CreditCard, contentDescription = null) }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // PIN Field (Mandatory for all)
            OutlinedTextField(
                value = pin,
                onValueChange = { if (it.length <= 4) pin = it },
                label = { Text("Enter PIN") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                supportingText = { Text("Enter your secret 4-digit transaction PIN") }
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                "Select Payment Method",
                modifier = Modifier.align(Alignment.Start),
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            val methods = listOf(
                PaymentMethod("M-Pesa", Icons.Default.Smartphone),
                PaymentMethod("Airtel Money", Icons.Default.Smartphone),
                PaymentMethod("Visa Card", Icons.Default.CreditCard),
                PaymentMethod("MasterCard", Icons.Default.CreditCard),
                PaymentMethod("Bank Transfer", Icons.Default.Money)
            )

            methods.forEach { method ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable { selectedMethod = method.name },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (selectedMethod == method.name) 
                            MaterialTheme.colorScheme.primaryContainer 
                        else 
                            MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(method.icon, contentDescription = null, tint = if (selectedMethod == method.name) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(method.name, modifier = Modifier.padding(start = 16.dp), fontWeight = if (selectedMethod == method.name) FontWeight.Bold else FontWeight.Normal)
                        Spacer(modifier = Modifier.weight(1f))
                        RadioButton(
                            selected = selectedMethod == method.name,
                            onClick = { selectedMethod = method.name }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            
            // Where the money goes
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f))
            ) {
                Text(
                    "Payments are securely processed. Funds will be used to generate your electricity token and credited to your PowerTrack wallet.",
                    modifier = Modifier.padding(12.dp),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            val isDataValid = amount.isNotBlank() && 
                    pin.length == 4 && 
                    ((selectedMethod in listOf("M-Pesa", "Airtel Money") && phoneNumber.length >= 10) || 
                     (selectedMethod in listOf("Visa Card", "MasterCard") && cardNumber.length >= 13) ||
                     (selectedMethod == "Bank Transfer"))

            Button(
                onClick = {
                    showConfirmDialog = true
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                enabled = isDataValid && !isProcessing
            ) {
                if (isProcessing) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                } else {
                    Text("PROCEED TO PAY", fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    if (showConfirmDialog) {
        val target = if (selectedMethod in listOf("M-Pesa", "Airtel Money")) phoneNumber else if (selectedMethod in listOf("Visa Card", "MasterCard")) "Card ending in ${cardNumber.takeLast(4)}" else "Bank account"
        
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = { Text("Confirm Transaction") },
            text = { 
                Text("Confirm payment of KES $amount from $selectedMethod ($target).") 
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showConfirmDialog = false
                        val amountFloat = amount.toFloatOrNull() ?: 0f
                        paymentViewModel.processTopUp(
                            amountFloat, 
                            selectedMethod, 
                            pin, 
                            context, 
                            onSuccess = {
                                navController.popBackStack()
                            },
                            onError = { message ->
                                scope.launch {
                                    snackbarHostState.showSnackbar(message)
                                }
                            }
                        )
                    }
                ) {
                    Text("AUTHORIZE")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) {
                    Text("CANCEL")
                }
            }
        )
    }
}

data class PaymentMethod(val name: String, val icon: ImageVector)
