package com.abdallah.powertrack.ui.theme.screens.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
                        Text("PowerTrack", fontWeight = FontWeight.Bold)
                    }
                },
                actions = {
                    IconButton(onClick = { navController.navigate(ROUTE_SETTINGS) }) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate(ROUTE_ADD_TOKEN) },
                containerColor = MaterialTheme.colorScheme.secondary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Token", tint = Color.White)
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Hero Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.tertiary)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(
                                progress = { (units / 50f).coerceIn(0f, 1f) },
                                modifier = Modifier.size(120.dp),
                                color = if (units < 5) Color.Yellow else Color.White,
                                strokeWidth = 8.dp,
                                trackColor = Color.White.copy(alpha = 0.2f),
                            )
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    String.format(Locale.getDefault(), "%.1f", units),
                                    color = Color.White,
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.Black
                                )
                                Text("UNITS", color = Color.White.copy(alpha = 0.7f), fontSize = 10.sp)
                            }
                        }
                        if (units < 5) {
                            Text(
                                "LOW BALANCE",
                                color = Color.Yellow,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(top = 8.dp),
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
                    title = "Days Left",
                    value = "${daysLeft.toInt()} Days",
                    icon = Icons.Default.DateRange,
                    color = MaterialTheme.colorScheme.secondary
                )
                InfoCard(
                    modifier = Modifier.weight(1f),
                    title = "Avg. Daily",
                    value = String.format(Locale.getDefault(), "%.1f", daily),
                    icon = Icons.Default.Info,
                    color = MaterialTheme.colorScheme.tertiary
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Navigation Menu
            Text("Actions", modifier = Modifier.align(Alignment.Start), fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(modifier = Modifier.height(8.dp))

            MenuButton(
                text = "Buy Units (Top Up)",
                icon = Icons.Default.ShoppingCart,
                onClick = { navController.navigate(ROUTE_TOPUP) }
            )
            MenuButton(
                text = "Usage History",
                icon = Icons.Default.List,
                onClick = { navController.navigate(ROUTE_HISTORY) }
            )
            MenuButton(
                text = "Update Current Reading",
                icon = Icons.Default.Edit,
                onClick = { navController.navigate(ROUTE_USAGE) }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Usage Trend Placeholder
            Text("Weekly Trend", modifier = Modifier.align(Alignment.Start), fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                modifier = Modifier.fillMaxWidth().height(120.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.Bottom
                ) {
                    val heights = listOf(0.4f, 0.7f, 0.5f, 0.9f, 0.6f, 0.8f, 0.3f)
                    heights.forEach { h ->
                        Box(
                            modifier = Modifier
                                .width(20.dp)
                                .fillMaxHeight(h)
                                .background(MaterialTheme.colorScheme.secondary, RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun InfoCard(modifier: Modifier, title: String, value: String, icon: ImageVector, color: Color) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Icon(icon, contentDescription = null, tint = color)
            Spacer(modifier = Modifier.height(8.dp))
            Text(title, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = color)
        }
    }
}

@Composable
fun MenuButton(text: String, icon: ImageVector, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onSurfaceVariant)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null)
            Spacer(modifier = Modifier.width(16.dp))
            Text(text, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.weight(1f))
            Icon(Icons.Default.KeyboardArrowRight, contentDescription = null)
        }
    }
}
