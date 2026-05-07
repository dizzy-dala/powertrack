package com.abdallah.powertrack.ui.theme.screens.setup

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.edit
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

@Composable
fun SetupScreen(onComplete: () -> Unit) {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("powertrack", Context.MODE_PRIVATE)
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    
    var units by remember { mutableStateOf("") }
    var dailyUsage by remember { mutableStateOf("") }
    var transactionPin by remember { mutableStateOf("") }
    var isSaving by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            Icons.Default.Bolt,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary,
        )
        
        Text(
            "Welcome to PowerTrack",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        
        Text(
            "Let's get you set up to track your electricity efficiently.",
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(vertical = 16.dp)
        )

        OutlinedTextField(
            value = units,
            onValueChange = { units = it },
            label = { Text("Current Units on Meter") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            enabled = !isSaving,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
        )
        
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = dailyUsage,
            onValueChange = { dailyUsage = it },
            label = { Text("Est. Daily Usage (e.g., 2.5)") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            enabled = !isSaving,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = transactionPin,
            onValueChange = { if (it.length <= 4) transactionPin = it },
            label = { Text("Set 4-digit Transaction PIN") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            enabled = !isSaving,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

        if (errorMessage != null) {
            Text(
                text = errorMessage!!,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 16.dp)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                val u = units.toFloatOrNull() ?: 0f
                val d = dailyUsage.toFloatOrNull() ?: 1.2f
                val pin = transactionPin.ifBlank { "1234" }
                val userId = auth.currentUser?.uid
                
                if (userId != null) {
                    isSaving = true
                    errorMessage = null
                    val updateData = mapOf(
                        "remainingUnits" to u,
                        "dailyUsage" to d,
                        "transactionPin" to pin,
                        "isSetupComplete" to true
                    )
                    
                    db.collection("users").document(userId).set(updateData, SetOptions.merge())
                        .addOnSuccessListener {
                            prefs.edit {
                                putFloat("remaining_units", u)
                                putFloat("daily_usage", d)
                                putString("transaction_pin", pin)
                                putBoolean("is_setup_complete", true)
                            }
                            onComplete()
                        }
                        .addOnFailureListener {
                            isSaving = false
                            errorMessage = "Connection error. Saving locally..."
                            // Fallback to local
                            prefs.edit {
                                putFloat("remaining_units", u)
                                putFloat("daily_usage", d)
                                putString("transaction_pin", pin)
                                putBoolean("is_setup_complete", true)
                            }
                            onComplete()
                        }
                } else {
                    prefs.edit {
                        putFloat("remaining_units", u)
                        putFloat("daily_usage", d)
                        putString("transaction_pin", pin)
                        putBoolean("is_setup_complete", true)
                    }
                    onComplete()
                }
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(12.dp),
            enabled = !isSaving && units.isNotBlank() && dailyUsage.isNotBlank() && transactionPin.length == 4
        ) {
            if (isSaving) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
            } else {
                Text("GET STARTED", fontWeight = FontWeight.Bold)
            }
        }
    }
}
