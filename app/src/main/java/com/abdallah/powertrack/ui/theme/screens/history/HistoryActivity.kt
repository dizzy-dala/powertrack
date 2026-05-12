package com.abdallah.powertrack.ui.theme.screens.history

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.abdallah.powertrack.data.HistoryViewModel
import com.abdallah.powertrack.models.TransactionHistoryItem
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val viewModel: HistoryViewModel = viewModel()
    
    val prefs = context.getSharedPreferences("powertrack", 0)
    // We need a meter number to fetch history. In a real app, this is saved in prefs after setup.
    val meterNumber = prefs.getString("meter_number", "14253647589") ?: "14253647589"

    LaunchedEffect(Unit) {
        viewModel.fetchHistory(meterNumber)
    }

    val transactions by viewModel.transactions
    val isLoading by viewModel.isLoading
    val errorMessage by viewModel.error

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Transaction History", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (errorMessage != null) {
                Text(errorMessage ?: "Error", color = MaterialTheme.colorScheme.error, modifier = Modifier.align(Alignment.Center))
            } else if (transactions.isEmpty()) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.History, 
                        contentDescription = null, 
                        modifier = Modifier.size(80.dp), 
                        tint = MaterialTheme.colorScheme.outlineVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("No transactions yet", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.outline)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(transactions) { entry ->
                        HistoryItem(entry)
                    }
                }
            }
        }
    }
}

@Composable
fun HistoryItem(item: TransactionHistoryItem) {
    val isSuccess = item.status.lowercase() == "success"
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(if (isSuccess) Color(0xFFE1F5FE) else Color(0xFFFFF3E0)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    if (isSuccess) Icons.Default.Payments else Icons.Default.ElectricBolt,
                    contentDescription = null,
                    tint = if (isSuccess) Color(0xFF0288D1) else Color(0xFFF57C00),
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(item.date, fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
                Text(
                    if (isSuccess) "Purchased ${item.units} kWh" else "Payment ${item.status}", 
                    fontSize = 15.sp, 
                    fontWeight = FontWeight.Medium
                )
                if (item.token.isNotBlank()) {
                    Text("Token: ${item.token}", fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
                }
            }
            
            Text(
                "KES ${item.amount}", 
                fontWeight = FontWeight.Bold,
                color = if (isSuccess) Color(0xFF2E7D32) else Color.DarkGray
            )
        }
    }
}
