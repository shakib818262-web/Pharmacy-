package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LocalPharmacy
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.PharmacyProfile
import com.example.ui.PharmacyViewModel
import com.example.ui.theme.IbmPlexMonoFontFamily
import com.example.ui.theme.TealPrimary
import com.example.util.BengaliUtils
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(viewModel: PharmacyViewModel) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val coroutineScope = rememberCoroutineScope()

    val activePharmacy by viewModel.activePharmacy.collectAsState()
    val pharmacies by viewModel.pharmacies.collectAsState()
    val useBengali = activePharmacy.useBengaliDigits

    var showEditProfileDialog by remember { mutableStateOf(false) }
    var showAddPharmacyDialog by remember { mutableStateOf(false) }
    var showDemoConfirmDialog by remember { mutableStateOf(false) }
    var showBackupRestoreDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Active Pharmacy Info Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = TealPrimary.copy(alpha = 0.1f),
                                modifier = Modifier.size(44.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(imageVector = Icons.Default.LocalPharmacy, contentDescription = null, tint = TealPrimary)
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = activePharmacy.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "বর্তমান সক্রিয় ফার্মেসি",
                                    fontSize = 12.sp,
                                    color = TealPrimary,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        IconButton(onClick = { showEditProfileDialog = true }) {
                            Icon(imageVector = Icons.Default.Edit, contentDescription = "এডিট", tint = TealPrimary)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(10.dp))

                    Text("ঠিকানা: ${activePharmacy.address}", fontSize = 13.sp)
                    Text("মোবাইল: ${activePharmacy.phone}", fontSize = 13.sp, fontFamily = IbmPlexMonoFontFamily)
                    if (activePharmacy.drugLicense.isNotBlank()) {
                        Text("ড্রাগ লাইসেন্স নং: ${activePharmacy.drugLicense}", fontSize = 13.sp, fontFamily = IbmPlexMonoFontFamily)
                    }
                }
            }
        }

        // Multiple Pharmacy Branches Switcher
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "একাধিক ফার্মেসি শাখা (Multi-Pharmacy)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "ট্যাপ করে অন্য ফার্মেসিতে সুইচ করুন",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }

                        Button(
                            onClick = { showAddPharmacyDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("নতুন শাখা", fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    pharmacies.forEach { p ->
                        val isActive = p.id == activePharmacy.id
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable { viewModel.switchPharmacy(p.id) },
                            shape = RoundedCornerShape(10.dp),
                            color = if (isActive) Color(0xFFE0F2F1) else Color(0xFFF7FAF9),
                            border = if (isActive) androidx.compose.foundation.BorderStroke(1.5.dp, TealPrimary) else null
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if (isActive) Icons.Default.CheckCircle else Icons.Default.Store,
                                        contentDescription = null,
                                        tint = if (isActive) TealPrimary else Color.Gray,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = p.name,
                                            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
                                            fontSize = 13.sp,
                                            color = if (isActive) TealPrimary else Color.Black
                                        )
                                        Text(p.address, fontSize = 11.sp, color = Color.Gray)
                                    }
                                }

                                if (pharmacies.size > 1 && !isActive) {
                                    IconButton(
                                        onClick = { viewModel.deletePharmacy(p.id) },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(imageVector = Icons.Default.Delete, contentDescription = "মুছুন", tint = Color.LightGray, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Number Language Toggle
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFFE8F5E9),
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(imageVector = Icons.Default.Language, contentDescription = null, tint = Color(0xFF2E7D32))
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "সংখ্যা প্রদর্শন ফরম্যাট",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = if (useBengali) "বাংলা সংখ্যা সক্রিয় (১, ২, ৩)" else "ইংরেজি সংখ্যা সক্রিয় (1, 2, 3)",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }
                    }

                    Switch(
                        checked = useBengali,
                        onCheckedChange = { viewModel.toggleBengaliDigits(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = TealPrimary
                        )
                    )
                }
            }
        }

        // Demo Data & JSON Backup / Restore Actions
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "ডাটা ব্যাকআপ ও ডেমো সেটিংস",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    // 1-Click Demo Data Loader
                    Button(
                        onClick = { showDemoConfirmDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00897B))
                    ) {
                        Icon(imageVector = Icons.Default.RestartAlt, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("নমুনা ডেটা লোড করুন (Load Demo Data)")
                    }

                    // JSON Backup & Restore Button
                    OutlinedButton(
                        onClick = { showBackupRestoreDialog = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(imageVector = Icons.Default.Download, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("সম্পূর্ণ ডাটাবেস ব্যাকআপ ও রিস্টোর (JSON)")
                    }
                }
            }
        }

        // App Footer / Info
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "ফার্মেসি খাতা - আধুনিক ফার্মেসি ম্যানেজমেন্ট",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray
                    )
                    Text(
                        text = "ভার্সন ১.০.০ • অফলাইন-ফার্স্ট এবং ১০০% সুরক্ষিত",
                        fontSize = 11.sp,
                        color = Color.LightGray
                    )
                }
            }
        }
    }

    // Edit Profile Dialog
    if (showEditProfileDialog) {
        EditPharmacyProfileDialog(
            current = activePharmacy,
            onSave = { updated ->
                viewModel.savePharmacyProfile(updated)
                showEditProfileDialog = false
            },
            onDismiss = { showEditProfileDialog = false }
        )
    }

    // Add Pharmacy Dialog
    if (showAddPharmacyDialog) {
        EditPharmacyProfileDialog(
            current = PharmacyProfile(
                id = java.util.UUID.randomUUID().toString(),
                name = "নতুন শাখা ফার্মেসি",
                address = "",
                phone = "",
                drugLicense = ""
            ),
            isNew = true,
            onSave = { newProfile ->
                viewModel.savePharmacyProfile(newProfile)
                viewModel.switchPharmacy(newProfile.id)
                showAddPharmacyDialog = false
            },
            onDismiss = { showAddPharmacyDialog = false }
        )
    }

    // Demo Data Confirm Dialog
    if (showDemoConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDemoConfirmDialog = false },
            title = { Text("নমুনা ডেটা লোড করতে চান?") },
            text = { Text("এটি বর্তমান ফার্মেসিতে বাংলাদেশের জনপ্রিয় ওষুধ (Napa, Seclo, Maxpro ইত্যাদি), ডেমো বিক্রি এবং কাস্টমার ডাটা লোড করবে।") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.loadDemoData()
                        showDemoConfirmDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
                ) {
                    Text("হ্যাঁ, লোড করুন")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDemoConfirmDialog = false }) {
                    Text("বাতিল")
                }
            }
        )
    }

    // Backup & Restore Dialog
    if (showBackupRestoreDialog) {
        BackupRestoreDialog(
            onExport = {
                coroutineScope.launch {
                    val json = viewModel.exportJson()
                    clipboardManager.setText(AnnotatedString(json))
                    BengaliUtils.shareText(context, json, "ফার্মেসি খাতা ডাটা ব্যাকআপ")
                    Toast.makeText(context, "ব্যাকআপ ডাটা কপি ও শেয়ার করা হয়েছে", Toast.LENGTH_SHORT).show()
                }
            },
            onImport = { jsonContent ->
                coroutineScope.launch {
                    viewModel.importJson(jsonContent)
                    showBackupRestoreDialog = false
                }
            },
            onDismiss = { showBackupRestoreDialog = false }
        )
    }
}

@Composable
fun EditPharmacyProfileDialog(
    current: PharmacyProfile,
    isNew: Boolean = false,
    onSave: (PharmacyProfile) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(current.name) }
    var address by remember { mutableStateOf(current.address) }
    var phone by remember { mutableStateOf(current.phone) }
    var drugLicense by remember { mutableStateOf(current.drugLicense) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = if (isNew) "নতুন ফার্মেসি শাখা যোগ" else "ফার্মেসির তথ্য পরিবর্তন",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TealPrimary
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("ফার্মেসির নাম*") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("ঠিকানা*") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("মোবাইল নম্বর*") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = drugLicense,
                    onValueChange = { drugLicense = it },
                    label = { Text("ড্রাগ লাইসেন্স নম্বর (ঐচ্ছিক)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) { Text("বাতিল") }
                    Button(
                        onClick = {
                            if (name.isNotBlank()) {
                                onSave(current.copy(
                                    name = name.trim(),
                                    address = address.trim(),
                                    phone = phone.trim(),
                                    drugLicense = drugLicense.trim()
                                ))
                            }
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
                    ) { Text("সংরক্ষণ") }
                }
            }
        }
    }
}

@Composable
fun BackupRestoreDialog(
    onExport: () -> Unit,
    onImport: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var importJsonText by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                Text(
                    text = "ডাটাবেস ব্যাকআপ ও রিস্টোর",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TealPrimary
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "১. ব্যাকআপ ডাউনলোড / শেয়ার করুন:",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp
                )
                Button(
                    onClick = onExport,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
                ) {
                    Icon(imageVector = Icons.Default.Share, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("ব্যাকআপ কপি ও শেয়ার করুন (JSON)")
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "২. ব্যাকআপ ডাটা রিস্টোর করুন:",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp
                )
                Text(
                    text = "পূর্বে সংরক্ষিত JSON ডাটা নিচে পেস্ট করুন:",
                    fontSize = 11.sp,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = importJsonText,
                    onValueChange = { importJsonText = it },
                    placeholder = { Text("JSON ডাটা এখানে পেস্ট করুন...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = {
                        if (importJsonText.isNotBlank()) {
                            onImport(importJsonText.trim())
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
                ) {
                    Icon(imageVector = Icons.Default.Upload, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("রিস্টোর সম্পন্ন করুন")
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                    Text("বন্ধ করুন")
                }
            }
        }
    }
}
