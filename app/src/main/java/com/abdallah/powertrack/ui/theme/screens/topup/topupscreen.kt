package com.abdallah.powertrack.ui.theme.screens.topup

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.abdallah.powertrack.data.DashboardViewModel
import com.abdallah.powertrack.data.HistoryViewModel
import com.abdallah.powertrack.data.PaymentViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopUpScreen(navController: NavController) {
    val paymentViewModel: PaymentViewModel = viewModel()
    val dashboardViewModel: DashboardViewModel = viewModel()
    val historyViewModel: HistoryViewModel = viewModel()
    val context = LocalContext.current

    // Start listening for balance updates from Firebase
    LaunchedEffect(Unit) {
        dashboardViewModel.startListening(context)
    }

    var amount by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var meterNumber by remember { mutableStateOf("") }
    var selectedMethod by remember { mutableStateOf("M-Pesa") }
    var showConfirmDialog by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var generatedToken by remember { mutableStateOf("") }
    var unitsPurchased by remember { mutableFloatStateOf(0f) }
    
    val currentUnits by dashboardViewModel.units
    val snackbarHostState = remember { SnackbarHostState() }
    val isProcessing by paymentViewModel.isProcessing
    val scope = rememberCoroutineScope()

    val quickAmounts = listOf("200", "500", "1000", "2000", "5000")

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Buy Electricity Token", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Balance Card (Shows live updates)
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Current Balance", style = MaterialTheme.typography.labelMedium)
                        Text(
                            "%.2f kWh".format(currentUnits),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Icon(
                        Icons.Default.Bolt,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Amount Header Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                ),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "How much electricity do you need?",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    OutlinedTextField(
                        value = amount,
                        onValueChange = { if (it.all { char -> char.isDigit() }) amount = it },
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = MaterialTheme.typography.headlineMedium.copy(
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        ),
                        placeholder = { 
                            Text(
                                "0", 
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.headlineMedium
                            ) 
                        },
                        prefix = { Text("KES", style = MaterialTheme.typography.titleLarge) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                            cursorColor = MaterialTheme.colorScheme.primary
                        ),
                        singleLine = true
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Quick Amount Selection
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 4.dp)
            ) {
                items(quickAmounts) { item ->
                    FilterChip(
                        selected = amount == item,
                        onClick = { amount = item },
                        label = { Text("KES $item") },
                        shape = RoundedCornerShape(12.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Meter and Phone Details
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Delivery Details",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    OutlinedTextField(
                        value = meterNumber,
                        onValueChange = { meterNumber = it },
                        label = { Text("Meter Number") },
                        placeholder = { Text("e.g. 14253647589") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        leadingIcon = { Icon(Icons.Default.Bolt, contentDescription = null) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    OutlinedTextField(
                        value = phoneNumber,
                        onValueChange = { phoneNumber = it },
                        label = { Text("Phone Number") },
                        placeholder = { Text("07xx xxx xxx") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        leadingIcon = { Icon(Icons.Default.PhoneAndroid, contentDescription = null) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Payment Method Selection (M-Pesa Only)
            Text(
                "Payment Method",
                modifier = Modifier.align(Alignment.Start),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f),
                border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary),
                shadowElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF4CAF50).copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Smartphone,
                            contentDescription = null,
                            tint = Color(0xFF4CAF50),
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Column(modifier = Modifier.padding(start = 16.dp)) {
                        Text(
                            "M-Pesa Express",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Instant token delivery",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    RadioButton(
                        selected = true,
                        onClick = { /* Always M-Pesa */ }
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Action Button
            val isDataValid = amount.isNotBlank() && 
                    meterNumber.isNotBlank() &&
                    phoneNumber.length >= 10

            Button(
                onClick = { showConfirmDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                shape = RoundedCornerShape(16.dp),
                enabled = isDataValid && !isProcessing,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
            ) {
                if (isProcessing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp), 
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 3.dp
                    )
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "PAY KES ${amount.ifEmpty { "0" }}", 
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(Icons.Default.ChevronRight, contentDescription = null)
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(40.dp))
        }
    }

    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            icon = { Icon(Icons.Default.Security, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
            title = { Text("Confirm Payment", textAlign = TextAlign.Center) },
            text = { 
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Text("You are about to pay")
                    Text(
                        "KES $amount", 
                        style = MaterialTheme.typography.headlineMedium, 
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text("to Meter: $meterNumber", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("An M-Pesa PIN prompt will appear on your phone ($phoneNumber).", textAlign = TextAlign.Center, style = MaterialTheme.typography.bodySmall)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showConfirmDialog = false
                        val amountFloat = amount.toFloatOrNull() ?: 0f
                        
                        paymentViewModel.processTopUp(
                            amountFloat, 
                            "M-Pesa", 
                            phoneNumber,
                            meterNumber,
                            context, 
                            onSuccess = { token, units ->
                                generatedToken = token
                                unitsPurchased = units
                                showSuccessDialog = true
                                
                                // Save meter number for other screens
                                val prefs = context.getSharedPreferences("powertrack", 0)
                                prefs.edit().putString("meter_number", meterNumber).apply()
                                
                                // Refresh ViewModels
                                dashboardViewModel.refreshFromBackend(meterNumber)
                                historyViewModel.fetchHistory(meterNumber)
                            },
                            onError = { message ->
                                scope.launch {
                                    snackbarHostState.showSnackbar("Payment Failed: $message")
                                }
                            }
                        )
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("PAY NOW")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showConfirmDialog = false },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("CANCEL")
                }
            },
            shape = RoundedCornerShape(28.dp)
        )
    }

    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { 
                showSuccessDialog = false
                navController.popBackStack()
            },
            icon = { Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF4CAF50), modifier = Modifier.size(48.dp)) },
            title = { Text("Token Generated Successfully", textAlign = TextAlign.Center) },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Text("Your Token Number:", style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            generatedToken,
                            modifier = Modifier.padding(12.dp),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            letterSpacing = 1.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Row {
                        Text("Units: ", fontWeight = FontWeight.Bold)
                        Text("%.2f kWh".format(unitsPurchased))
                    }
                    Text("Amount: KES $amount", style = MaterialTheme.typography.bodySmall)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSuccessDialog = false
                        navController.popBackStack()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("DONE")
                }
            },
            shape = RoundedCornerShape(28.dp)
        )
    }
}

data class PaymentMethod(val name: String, val icon: ImageVector, val color: Color)
