package com.abdallah.powertrack.ui.theme.screens.usage

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.edit
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UsageScreen(onDone: () -> Unit) {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("powertrack", Context.MODE_PRIVATE)
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()

    var input by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isSaving by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Update Usage", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onDone) {
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
                .background(Color(0xFFF8FAFC))
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Speed,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.secondary
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                "Manual Calibration",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                "Enter the current remaining units from your meter to calibrate the daily usage estimation and improve tracking accuracy.",
                textAlign = TextAlign.Center,
                fontSize = 14.sp,
                color = Color.Gray,
                modifier = Modifier.padding(vertical = 12.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = input,
                onValueChange = { 
                    input = it
                    errorMessage = null
                },
                label = { Text("Remaining Units (kWh)") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                isError = errorMessage != null,
                supportingText = { errorMessage?.let { Text(it) } },
                enabled = !isSaving,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.secondary,
                    unfocusedBorderColor = Color.LightGray,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                )
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    val newRemaining = input.toFloatOrNull()
                    if (newRemaining != null && newRemaining >= 0) {
                        isSaving = true
                        val oldRemaining = prefs.getFloat("remaining_units", 0f)
                        val lastUpdateTime = prefs.getLong("last_update_time", System.currentTimeMillis())
                        val userId = auth.currentUser?.uid
                        
                        val used = oldRemaining - newRemaining
                        var smoothedDaily = prefs.getFloat("daily_usage", 1.0f)
                        
                        if (used > 0) {
                            val now = System.currentTimeMillis()
                            val diffHours = (now - lastUpdateTime) / (1000f * 60 * 60)
                            val diffDays = diffHours / 24f
                            
                            if (diffDays > 0.04f) { 
                                val calculatedDaily = used / diffDays
                                val oldDaily = prefs.getFloat("daily_usage", 1.0f)
                                smoothedDaily = (oldDaily * 0.7f) + (calculatedDaily * 0.3f)
                            }
                        }

                        if (userId != null) {
                            val updateData = mapOf(
                                "remainingUnits" to newRemaining,
                                "dailyUsage" to smoothedDaily
                            )
                            db.collection("users").document(userId).update(updateData)
                                .addOnSuccessListener {
                                    prefs.edit {
                                        putFloat("remaining_units", newRemaining)
                                        putFloat("daily_usage", smoothedDaily)
                                        putLong("last_update_time", System.currentTimeMillis())
                                    }
                                    onDone()
                                }
                                .addOnFailureListener {
                                    isSaving = false
                                    errorMessage = "Failed to sync with cloud"
                                }
                        } else {
                            prefs.edit {
                                putFloat("remaining_units", newRemaining)
                                putFloat("daily_usage", smoothedDaily)
                                putLong("last_update_time", System.currentTimeMillis())
                            }
                            onDone()
                        }
                    } else {
                        errorMessage = "Please enter a valid number"
                    }
                },
                modifier = Modifier.fillMaxWidth().height(60.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                enabled = !isSaving && input.isNotBlank()
            ) {
                if (isSaving) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onSecondary)
                } else {
                    Text("UPDATE READING", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF7ED))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Speed,
                        contentDescription = null,
                        tint = Color(0xFFC2410C),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        "Updating this will recalculate your daily usage estimate based on the time since your last update.",
                        fontSize = 12.sp,
                        color = Color(0xFF9A3412)
                    )
                }
            }
        }
    }
}
