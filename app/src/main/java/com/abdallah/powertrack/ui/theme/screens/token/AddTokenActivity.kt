package com.abdallah.powertrack.ui.theme.screens.token

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
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.edit
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTokenScreen(onDone: () -> Unit) {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("powertrack", Context.MODE_PRIVATE)
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()

    var input by remember { mutableStateOf("") }
    var pin by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isSaving by remember { mutableStateOf(false) }
    var showConfirmDialog by remember { mutableStateOf(false) }
    
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Manual Entry", fontWeight = FontWeight.Bold) },
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
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.FlashOn,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                "Sync Your Meter",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                "If you bought a token from a physical vendor, enter the units here to keep your dashboard accurate.",
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
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
                label = { Text("Units to Add (kWh)") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                isError = errorMessage != null,
                supportingText = { errorMessage?.let { Text(it) } },
                enabled = !isSaving,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = Color.LightGray,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = pin,
                onValueChange = { if (it.length <= 4) pin = it },
                label = { Text("Confirm Transaction PIN") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                enabled = !isSaving,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = Color.Gray) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = Color.LightGray,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                )
            )

            if (input.isNotEmpty()) {
                val units = input.toFloatOrNull() ?: 0f
                if (units > 0) {
                    Card(
                        modifier = Modifier.padding(vertical = 24.dp).fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF))
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text("Transaction Summary", fontWeight = FontWeight.Bold, color = Color(0xFF1E40AF), fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("New Units:", color = Color.Gray)
                                Text("+ $units kWh", fontWeight = FontWeight.Bold, color = Color(0xFF047857))
                            }
                            val current = prefs.getFloat("remaining_units", 0f)
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Estimated New Balance:", color = Color.Gray)
                                Text("${String.format("%.1f", current + units)} kWh", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    val savedPin = prefs.getString("transaction_pin", "1234")
                    if (pin != savedPin) {
                        scope.launch { snackbarHostState.showSnackbar("Incorrect Transaction PIN") }
                        return@Button
                    }
                    showConfirmDialog = true
                },
                modifier = Modifier.fillMaxWidth().height(60.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                enabled = !isSaving && input.isNotBlank() && pin.length == 4
            ) {
                if (isSaving) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                } else {
                    Text("ADD TO BALANCE", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                }
            }
        }
    }

    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = { Text("Confirm Update") },
            text = { Text("Are you sure you want to add $input kWh to your current meter balance?") },
            confirmButton = {
                Button(
                    onClick = {
                        showConfirmDialog = false
                        val unitsAdded = input.toFloatOrNull() ?: 0f
                        isSaving = true
                        val userId = auth.currentUser?.uid
                        
                        val sdf = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault())
                        val date = sdf.format(Date())
                        val historyEntry = "$date: Manually added $unitsAdded units\n"
                        val oldHistory = prefs.getString("history", "") ?: ""

                        if (userId != null) {
                            val updateData = mapOf(
                                "remainingUnits" to FieldValue.increment(unitsAdded.toDouble())
                            )
                            db.collection("users").document(userId).update(updateData)
                                .addOnSuccessListener {
                                    val currentUnits = prefs.getFloat("remaining_units", 0f)
                                    prefs.edit {
                                        putFloat("remaining_units", currentUnits + unitsAdded)
                                        putString("history", historyEntry + oldHistory)
                                    }
                                    onDone()
                                }
                                .addOnFailureListener {
                                    isSaving = false
                                    errorMessage = "Cloud sync failed. Check connection."
                                }
                        } else {
                            val currentUnits = prefs.getFloat("remaining_units", 0f)
                            prefs.edit {
                                putFloat("remaining_units", currentUnits + unitsAdded)
                                putString("history", historyEntry + oldHistory)
                            }
                            onDone()
                        }
                    }
                ) { Text("SYNC NOW") }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) { Text("CANCEL") }
            }
        )
    }
}
