package com.educode.app.features.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.educode.app.domain.models.NotificationSettings
import com.educode.app.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBackClick: () -> Unit,
    onLogout: () -> Unit,
    onNavigateToBugReport: () -> Unit,
    viewModel: SettingsViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showNameDialog by remember { mutableStateOf(false) }
    var tempName by remember { mutableStateOf("") }

    LaunchedEffect(uiState.isLoggedOut) {
        if (uiState.isLoggedOut) onLogout()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("الإعدادات والتفضيلات ⚙️", color = NeonCyan, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceDark)
            )
        },
        containerColor = DarkBackground
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = NeonCyan)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                // Account Section
                SettingsHeader("الحساب والأمان")
                SettingsCard {
                    AccountInfoItem(
                        title = "الاسم الشخصي",
                        value = uiState.user?.name ?: "",
                        onClick = {
                            tempName = uiState.user?.name ?: ""
                            showNameDialog = true
                        }
                    )
                    Divider(color = Color.White.copy(alpha = 0.05f))
                    AccountInfoItem(
                        title = "كلمة المرور",
                        value = "••••••••",
                        onClick = { /* Change password */ }
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Appearance Section
                SettingsHeader("المظهر")
                SettingsCard {
                    SelectionItem("وضع العرض", "داكن (Cyberpunk)", Icons.Default.Brightness4)
                    Divider(color = Color.White.copy(alpha = 0.05f))
                    SelectionItem("لغة التطبيق", "العربية (Arabic)", Icons.Default.Language)
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Notifications
                SettingsHeader("التنبيهات والبحث الذكي")
                SettingsCard {
                    val settings = uiState.user?.notificationSettings ?: NotificationSettings()
                    NotificationToggle(
                        title = "تفعيل الإشعارات",
                        description = "تشغيل أو إيقاف جميع الإشعارات",
                        checked = settings.enabled,
                        onCheckedChange = { viewModel.updateNotificationSettings(settings.copy(enabled = it)) }
                    )
                    if (settings.enabled) {
                        Divider(color = Color.White.copy(alpha = 0.05f))
                        NotificationToggle(
                            title = "التذكير اليومي",
                            description = "تذكير يومي للمتابعة (وقت حالي: ${settings.reminderTime})",
                            checked = settings.dailyReminder,
                            onCheckedChange = { viewModel.updateNotificationSettings(settings.copy(dailyReminder = it)) }
                        )
                        if (settings.dailyReminder) {
                            val context = androidx.compose.ui.platform.LocalContext.current
                            TextButton(
                                onClick = {
                                    val currentHour = settings.reminderTime.split(":")[0].toIntOrNull() ?: 9
                                    val currentMin = settings.reminderTime.split(":")[1].toIntOrNull() ?: 0
                                    android.app.TimePickerDialog(context, { _, h, m ->
                                        val newTime = String.format("%02d:%02d", h, m)
                                        viewModel.updateNotificationSettings(settings.copy(reminderTime = newTime))
                                    }, currentHour, currentMin, true).show()
                                },
                                modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
                            ) {
                                Text("تغيير وقت التذكير ⏰", color = NeonCyan, fontSize = 12.sp)
                            }
                        }
                        Divider(color = Color.White.copy(alpha = 0.05f))
                        NotificationToggle(
                            title = "إشعارات الدروس",
                            description = "إكمال الدروس والمحتوى التعليمي",
                            checked = settings.lessonReminders,
                            onCheckedChange = { viewModel.updateNotificationSettings(settings.copy(lessonReminders = it)) }
                        )
                        Divider(color = Color.White.copy(alpha = 0.05f))
                        NotificationToggle(
                            title = "تحديات ومكافآت",
                            description = "تنبيهات التحديات والمكافآت اليومية",
                            checked = settings.challengeReminders,
                            onCheckedChange = { viewModel.updateNotificationSettings(settings.copy(challengeReminders = it)) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Data management
                SettingsHeader("البيانات والتخزين")
                SettingsCard {
                    ActionItem("نسخ احتياطي للبيانات", Icons.Default.Backup) { /* Backup */ }
                    Divider(color = Color.White.copy(alpha = 0.05f))
                    ActionItem("مسح التخزين المؤقت", Icons.Default.DeleteSweep) { viewModel.clearCache() }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Barmuj AI
                SettingsHeader("Barmuj AI")
                SettingsCard {
                    ActionItem("مسح سجل المحادثات", Icons.Default.Chat) { /* Clear AI history */ }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Support
                SettingsHeader("الدعم والمساعدة")
                SettingsCard {
                    ActionItem("الإبلاغ عن مشكلة (Report Bug)", Icons.Default.BugReport) { onNavigateToBugReport() }
                    Divider(color = Color.White.copy(alpha = 0.05f))
                    ActionItem("عن التطبيق", Icons.Default.Info) { /* About logic */ }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Sign out / Delete
                Button(
                    onClick = { viewModel.logout() },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = SurfaceDark),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Logout, contentDescription = null, tint = NeonYellow)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("تسجيل الخروج", color = NeonYellow)
                }

                TextButton(
                    onClick = { viewModel.deleteAccount() },
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                ) {
                    Text("حذف الحساب نهائياً", color = Color.Red.copy(alpha = 0.7f), fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.height(40.dp))
            }
        }

        // Edit Name Dialog
        if (showNameDialog) {
            AlertDialog(
                onDismissRequest = { showNameDialog = false },
                title = { Text("تعديل الاسم", color = Color.White) },
                text = {
                    OutlinedTextField(
                        value = tempName,
                        onValueChange = { tempName = it },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonCyan,
                            unfocusedBorderColor = Color.Gray,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                },
                confirmButton = {
                    Button(onClick = {
                        viewModel.updateUserName(tempName)
                        showNameDialog = false
                    }) {
                        Text("حفظ")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showNameDialog = false }) {
                        Text("إلغاء", color = Color.Gray)
                    }
                },
                containerColor = SurfaceDark
            )
        }

        uiState.message?.let { msg ->
            LaunchedEffect(msg) {
                // In a real app we'd use snackbar
            }
            AlertDialog(
                onDismissRequest = { viewModel.clearMessage() },
                title = { Text("تنبيه", color = NeonCyan) },
                text = { Text(msg, color = Color.White) },
                confirmButton = {
                    Button(onClick = { viewModel.clearMessage() }) {
                        Text("حسناً")
                    }
                },
                containerColor = SurfaceDark
            )
        }
    }
}

@Composable
fun SettingsHeader(title: String) {
    Text(
        text = title,
        color = NeonCyan,
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
    )
}

@Composable
fun SettingsCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark)
    ) {
        Column(modifier = Modifier.padding(4.dp)) {
            content()
        }
    }
}

@Composable
fun AccountInfoItem(title: String, value: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(title, color = TextSecondary, fontSize = 12.sp)
            Text(value, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        }
        Icon(Icons.Default.ChevronLeft, contentDescription = null, tint = Color.Gray)
    }
}

@Composable
fun SelectionItem(title: String, value: String, icon: ImageVector) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { /* Selection Logic */ }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Text(title, color = Color.White, fontSize = 16.sp)
        }
        Text(value, color = NeonYellow, fontSize = 14.sp)
    }
}

@Composable
fun ActionItem(title: String, icon: ImageVector, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Text(title, color = Color.White, fontSize = 16.sp)
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = TextSecondary, fontSize = 14.sp)
        Text(value, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun NotificationToggle(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text(description, color = TextSecondary, fontSize = 12.sp)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = NeonCyan,
                checkedTrackColor = NeonCyan.copy(alpha = 0.5f)
            )
        )
    }
}
