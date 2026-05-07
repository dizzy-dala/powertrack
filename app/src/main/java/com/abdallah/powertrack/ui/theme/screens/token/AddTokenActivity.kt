package com.abdallah.powertrack.ui.theme.screens.token

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Lock
import android.content.Context
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
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
                title = { Text("Add Token Manually") },
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
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.FlashOn,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Top Up Your Units",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                "Enter the amount of units you just purchased from a vendor.",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            OutlinedTextField(
                value = input,
                onValueChange = { 
                    input = it
                    errorMessage = null
                },
                label = { Text("Units Amount") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                isError = errorMessage != null,
                supportingText = { errorMessage?.let { Text(it) } },
                enabled = !isSaving,
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal)
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = pin,
                onValueChange = { if (it.length <= 4) pin = it },
                label = { Text("Transaction PIN") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                enabled = !isSaving,
                visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) }
            )

            if (input.isNotEmpty()) {
                val units = input.toFloatOrNull() ?: 0f
                if (units > 0) {
                    Card(
                        modifier = Modifier.padding(vertical = 16.dp).fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Summary", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.tertiary)
                            Text("Adding: $units units", fontSize = 16.sp)
                            val current = prefs.getFloat("remaining_units", 0f)
                            Text("New Balance: ${current + units} units", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    val savedPin = prefs.getString("transaction_pin", "1234")
                    if (pin != savedPin) {
                        scope.launch { snackbarHostState.showSnackbar("Incorrect Transaction PIN") }
                        return@Button
                    }
                    showConfirmDialog = true
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(12.dp),
                enabled = !isSaving && input.isNotBlank() && pin.length == 4
            ) {
                if (isSaving) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text("SAVE TOKEN", fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = { Text("Confirm Addition") },
            text = { Text("You are adding $input units to your balance. This action cannot be undone. Continue?") },
            confirmButton = {
                TextButton(
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
                                    errorMessage = "Failed to sync with cloud"
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
                ) { Text("CONFIRM") }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) { Text("CANCEL") }
            }
        )
    }
}
