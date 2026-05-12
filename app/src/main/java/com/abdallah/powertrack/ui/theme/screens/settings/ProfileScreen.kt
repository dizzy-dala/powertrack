package com.abdallah.powertrack.ui.theme.screens.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.abdallah.powertrack.data.ProfileViewModel
import com.abdallah.powertrack.models.UserProfile

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(navController: NavController) {
    val profileViewModel: ProfileViewModel = viewModel()
    val context = LocalContext.current
    val userProfile by profileViewModel.userProfile
    val isUpdating by profileViewModel.isUpdating
    val errorMessage by profileViewModel.errorMessage

    var isEditMode by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var meterNumber by remember { mutableStateOf("") }

    // Fetch profile data when screen loads
    LaunchedEffect(Unit) {
        profileViewModel.fetchProfile(context)
    }

    // Update local state when userProfile changes
    LaunchedEffect(userProfile) {
        userProfile?.let {
            name = it.name
            phoneNumber = it.phoneNumber
            meterNumber = it.meterNumber
        }
    }

    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Profile Settings", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (!isEditMode) {
                        TextButton(onClick = { isEditMode = true }) {
                            Text("Edit", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Profile Image Header
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier.size(60.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = userProfile?.name ?: "User",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = userProfile?.phoneNumber ?: "No phone number",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Info Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    ProfileField(
                        label = "Full Name",
                        value = name,
                        onValueChange = { name = it },
                        isEditMode = isEditMode,
                        icon = Icons.Default.Badge
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), thickness = 0.5.dp)

                    ProfileField(
                        label = "Phone Number",
                        value = phoneNumber,
                        onValueChange = { phoneNumber = it },
                        isEditMode = isEditMode,
                        icon = Icons.Default.Phone,
                        keyboardType = KeyboardType.Phone
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), thickness = 0.5.dp)

                    ProfileField(
                        label = "Default Meter Number",
                        value = meterNumber,
                        onValueChange = { meterNumber = it },
                        isEditMode = isEditMode,
                        icon = Icons.Default.Bolt,
                        keyboardType = KeyboardType.Number
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            if (isEditMode) {
                Button(
                    onClick = {
                        val updatedProfile = UserProfile(
                            name = name,
                            phoneNumber = phoneNumber,
                            meterNumber = meterNumber
                        )
                        profileViewModel.updateProfile(context, updatedProfile) {
                            isEditMode = false
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !isUpdating
                ) {
                    if (isUpdating) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                    } else {
                        Icon(Icons.Default.Save, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Save Changes", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }

                TextButton(
                    onClick = { 
                        isEditMode = false
                        // Reset fields to original values from userProfile state
                        userProfile?.let {
                            name = it.name
                            phoneNumber = it.phoneNumber
                            meterNumber = it.meterNumber
                        }
                    },
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Text("Cancel")
                }
            } else {
                // Settings Section
                Text(
                    "Security",
                    modifier = Modifier.align(Alignment.Start),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))
                
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(16.dp))
                            Text("Change Password", modifier = Modifier.weight(1f))
                            Icon(Icons.Default.ChevronRight, contentDescription = null)
                        }
                    }
                }
            }

            if (errorMessage != null) {
                Text(
                    text = errorMessage!!,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 16.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun ProfileField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    isEditMode: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
            if (isEditMode) {
                TextField(
                    value = value,
                    onValueChange = onValueChange,
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = MaterialTheme.colorScheme.primary
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                    singleLine = true
                )
            } else {
                Text(
                    text = value.ifEmpty { "Not set" },
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
