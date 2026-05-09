package com.abdallah.powertrack.ui.theme.screens.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.abdallah.powertrack.data.DashboardViewModel
import com.abdallah.powertrack.navigation.*
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(navController: NavController) {
    val dashboardViewModel: DashboardViewModel = viewModel()
    val context = LocalContext.current
    
    LaunchedEffect(Unit) {
        dashboardViewModel.startListening(context)
    }

    val units by dashboardViewModel.units
    val daily by dashboardViewModel.dailyUsage
    val userName by dashboardViewModel.userName

    val daysLeft = if (daily > 0) units / daily else 0f

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text("Hello, $userName", fontSize = 14.sp, fontWeight = FontWeight.Normal, color = Color.White.copy(alpha = 0.7f))
                        Text("PowerTrack", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { navController.navigate(ROUTE_SETTINGS) }) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { navController.navigate(ROUTE_ADD_TOKEN) },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Manual Token") },
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = Color.White
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFF8FAFC)) // Light gray background
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Main Balance Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp),
                shape = RoundedCornerShape(28.dp),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(MaterialTheme.colorScheme.primary, Color(0xFF0369A1))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("REMAINING BALANCE", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                String.format(Locale.getDefault(), "%.1f", units),
                                color = Color.White,
                                fontSize = 56.sp,
                                fontWeight = FontWeight.Black
                            )
                            Text("kWh", color = Color.White.copy(alpha = 0.8f), fontSize = 18.sp, modifier = Modifier.padding(bottom = 12.dp, start = 4.dp))
                        }
                        
                        LinearProgressIndicator(
                            progress = { (units / 50f).coerceIn(0f, 1f) },
                            modifier = Modifier
                                .width(200.dp)
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = if (units < 10) Color(0xFFFACC15) else Color.White,
                            trackColor = Color.White.copy(alpha = 0.2f),
                        )
                        
                        if (units < 5) {
                            Text(
                                "CRITICAL BALANCE",
                                color = Color(0xFFFACC15),
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.padding(top = 12.dp),
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Quick Info Row
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                InfoCard(
                    modifier = Modifier.weight(1f),
                    title = "Estimated Life",
                    value = "${daysLeft.toInt()} Days",
                    icon = Icons.Default.Timer,
                    color = Color(0xFF0EA5E9)
                )
                InfoCard(
                    modifier = Modifier.weight(1f),
                    title = "Daily Average",
                    value = String.format(Locale.getDefault(), "%.2f", daily),
                    icon = Icons.Default.TrendingDown,
                    color = Color(0xFF8B5CF6)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Action Section
            Text(
                "Quick Actions", 
                modifier = Modifier.align(Alignment.Start), 
                fontWeight = FontWeight.Bold, 
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(12.dp))

            ActionTile(
                title = "Buy Electricity Token",
                subtitle = "Top up via M-Pesa or Card",
                icon = Icons.Default.Bolt,
                color = Color(0xFFF59E0B),
                onClick = { navController.navigate(ROUTE_TOPUP) }
            )
            
            ActionTile(
                title = "Usage Analytics",
                subtitle = "View historical trends",
                icon = Icons.Default.BarChart,
                color = Color(0xFF10B981),
                onClick = { navController.navigate(ROUTE_HISTORY) }
            )
            
            ActionTile(
                title = "Sync Meter Reading",
                subtitle = "Calibrate sensor manualy",
                icon = Icons.Default.Sync,
                color = Color(0xFF6366F1),
                onClick = { navController.navigate(ROUTE_USAGE) }
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun InfoCard(modifier: Modifier, title: String, value: String, icon: ImageVector, color: Color) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(color.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(title, fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
            Text(value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
        }
    }
}

@Composable
fun ActionTile(title: String, subtitle: String, icon: ImageVector, color: Color, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = color)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(subtitle, fontSize = 13.sp, color = Color.Gray)
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.LightGray)
        }
    }
}
