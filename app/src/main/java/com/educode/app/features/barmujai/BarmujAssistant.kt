package com.educode.app.features.barmujai

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.drawToBitmap
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.educode.app.R
import com.educode.app.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*

val LocalBarmujViewModel = staticCompositionLocalOf<BarmujViewModel?> { null }

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun BarmujAssistant(
    content: @Composable () -> Unit
) {
    val viewModel: BarmujViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsState()
    val isOpen = uiState.isAssistantOpen
    val context = LocalContext.current
    val view = LocalView.current

    val permissionState = rememberPermissionState(android.Manifest.permission.RECORD_AUDIO)

    CompositionLocalProvider(LocalBarmujViewModel provides viewModel) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Main App Content
            content()

            // Persistent FAB at bottom-right (End)
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 16.dp, bottom = 90.dp) // Offset above Bottom Nav
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .shadow(12.dp, CircleShape)
                        .clip(CircleShape)
                        .background(Brush.linearGradient(listOf(NeonCyan, NeonPurple)))
                        .border(1.5.dp, Color.White.copy(alpha = 0.3f), CircleShape)
                        .clickable { viewModel.toggleAssistant(!isOpen) },
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_barmuj_robot_final_1781762659553), 
                        contentDescription = "برموج",
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            // Smart Floating Window
            AnimatedVisibility(
                visible = isOpen,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 16.dp, bottom = 90.dp)
                    .widthIn(max = 340.dp) // Adjusted width as requested "وسع عرضك شوية"
                    .fillMaxWidth(0.9f)
                    .heightIn(max = if (uiState.isMinimized) 60.dp else if (uiState.isExpanded) 850.dp else 700.dp) // Longer upwards
            ) {
                if (!uiState.isSetupComplete) {
                    BarmujSetupScreen(viewModel)
                } else {
                    BarmujWindow(viewModel, uiState, permissionState, view, context)
                }
            }
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun BarmujWindow(
    viewModel: BarmujViewModel,
    uiState: BarmujUIState,
    permissionState: com.google.accompanist.permissions.PermissionState,
    view: android.view.View,
    context: android.content.Context
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = SurfaceDark,
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, Brush.verticalGradient(listOf(NeonCyan.copy(alpha=0.3f), Color.Transparent))),
        shadowElevation = 24.dp
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White.copy(alpha = 0.03f))
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(id = R.drawable.img_barmuj_robot_final_1781762659553),
                        contentDescription = null,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .border(1.dp, NeonCyan.copy(alpha = 0.5f), CircleShape),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("برموج", color = NeonCyan, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { viewModel.toggleSettings(!uiState.isSettingsOpen) }) {
                        Icon(
                            if (uiState.isSettingsOpen) Icons.Default.Chat else Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = if (uiState.isSettingsOpen) NeonCyan else TextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(onClick = { viewModel.toggleMinimize(!uiState.isMinimized) }) {
                        Icon(
                            if (uiState.isMinimized) Icons.Default.ExpandLess else Icons.Default.Minimize,
                            contentDescription = "Minimize",
                            tint = TextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(onClick = { viewModel.toggleExpand(!uiState.isExpanded) }) {
                        Icon(
                            if (uiState.isExpanded) Icons.Default.Compress else Icons.Default.OpenInFull,
                            contentDescription = "Expand",
                            tint = TextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(onClick = { viewModel.toggleAssistant(false) }) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary, modifier = Modifier.size(18.dp))
                    }
                }
            }

            if (!uiState.isMinimized) {
                if (uiState.isSettingsOpen) {
                    BarmujSettingsView(viewModel, uiState)
                } else {
                    // Quick Actions
                    androidx.compose.foundation.lazy.LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(horizontal = 4.dp)
                    ) {
                        item { ActionChip(Modifier, "تلميح 💡", Icons.Default.Lightbulb) { viewModel.giveHint() } }
                        item { ActionChip(Modifier, "اشرح الخطأ 🔍", Icons.Default.BugReport) { viewModel.explainError() } }
                        item { ActionChip(Modifier, "بسط الفكرة 🤔", Icons.Default.HelpCenter) { viewModel.understandQuestion() } }
                    }

                    Divider(color = Color.White.copy(alpha = 0.05f))

                    // Chat
                    val listState = rememberLazyListState()
                    LaunchedEffect(uiState.messages.size) {
                        if (uiState.messages.isNotEmpty()) {
                            listState.animateScrollToItem(uiState.messages.size - 1)
                        }
                    }

                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp), // Added bottom padding
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(uiState.messages) { message ->
                            BarmujMessageBubble(message)
                        }
                        if (uiState.isTyping) {
                            item { BarmujTypingIndicator() }
                        }
                    }

                    // Footer
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(DarkBackground.copy(alpha = 0.5f))
                            .padding(12.dp)
                    ) {
                        var text by remember { mutableStateOf("") }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                IconButton(onClick = { 
                                    if (!permissionState.status.isGranted) permissionState.launchPermissionRequest()
                                    else /* Voice logic */ {}
                                }, modifier = Modifier.size(32.dp)) {
                                    Icon(Icons.Default.Mic, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(20.dp))
                                }
                                IconButton(onClick = { /* Image logic */ }, modifier = Modifier.size(32.dp)) {
                                    Icon(Icons.Default.Image, contentDescription = null, tint = NeonPurple, modifier = Modifier.size(20.dp))
                                }
                                IconButton(onClick = {
                                    val bitmap = try { view.drawToBitmap() } catch (e: Exception) { null }
                                    if (bitmap != null) viewModel.sendMessage("التقط لي الشاشة وحللها.", bitmap)
                                }, modifier = Modifier.size(32.dp)) {
                                    Icon(Icons.Default.Screenshot, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(20.dp))
                                }
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            OutlinedTextField(
                                value = text,
                                onValueChange = { text = it },
                                modifier = Modifier.weight(1f),
                                placeholder = { Text("اسأل برموج...", fontSize = 12.sp, color = TextSecondary) },
                                shape = RoundedCornerShape(20.dp),
                                maxLines = 2,
                                textStyle = androidx.compose.ui.text.TextStyle(fontSize = 13.sp, color = Color.White),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = NeonCyan.copy(alpha = 0.5f),
                                    unfocusedBorderColor = SurfaceVariantDark,
                                    focusedContainerColor = SurfaceVariantDark.copy(alpha = 0.3f),
                                    unfocusedContainerColor = SurfaceVariantDark.copy(alpha = 0.3f)
                                )
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            IconButton(
                                onClick = { if (text.isNotBlank()) { viewModel.sendMessage(text); text = "" } },
                                modifier = Modifier.size(36.dp).clip(CircleShape).background(NeonCyan)
                            ) {
                                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, tint = SurfaceDark, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BarmujSettingsView(viewModel: BarmujViewModel, uiState: BarmujUIState) {
    var localApiKey by remember { mutableStateOf(uiState.userApiKey) }
    
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("إعدادات نموذج الذكاء الاصطناعي ⚙️", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
        
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("نموذج التشغيل المكتشف", color = TextSecondary, fontSize = 12.sp)
                
                BarmujModelType.values().forEach { model ->
                    SetupOption(
                        title = when(model) {
                            BarmujModelType.GEMINI_3_5_FLASH -> "جيميناي 3.5 (Gemini 3.5 Flash) ☁️"
                            BarmujModelType.LLAMA_3 -> "Llama 3 (أوفلاين محلي) 🦙"
                            BarmujModelType.LLAMA_3_1 -> "Llama 3.1 Pro (أوفلاين محلي) 🛠️"
                            BarmujModelType.LLAMA_3_2 -> "Llama 3.2 Lite (أوفلاين محلي) ⚡"
                        },
                        desc = when(model) {
                            BarmujModelType.GEMINI_3_5_FLASH -> "النموذج السحابي الافتراضي الأقوى والأسرع (يتطلب مفتاح API للاتصال)."
                            BarmujModelType.LLAMA_3 -> "نموذج Llama المحلي القياسي المجاني لتلقي المساعدة بالكامل دون الحاجة لإنترنت."
                            BarmujModelType.LLAMA_3_1 -> "الإصدار المطور الداعم للتحليل التعليمي الفردي والرموز المعقدة أوفلاين."
                            BarmujModelType.LLAMA_3_2 -> "النسخة الذكية السريعة والخفيفة المثالية للأجواء منخفضة الموارد والبطاريات أوفلاين."
                        },
                        selected = uiState.modelType == model,
                        onClick = { viewModel.changeModel(model) }
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                }
            }
        }
        
        if (!uiState.modelType.isOffline) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("مفتاح واجهة برمجة التطبيقات (API Key)", color = TextSecondary, fontSize = 12.sp)
                    OutlinedTextField(
                        value = localApiKey,
                        onValueChange = { 
                            localApiKey = it
                            viewModel.updateApiKey(it)
                        },
                        placeholder = { Text("أدخل Gemini API Key", fontSize = 12.sp, color = TextSecondary) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = NeonCyan,
                            unfocusedBorderColor = SurfaceVariantDark
                        )
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Button(
                        onClick = { viewModel.testConnection() },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = SurfaceVariantDark)
                    ) {
                        Text("اختبار الاتصال السحابي ⚡", fontSize = 12.sp, color = Color.White)
                    }
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    // Connection Status indicator
                    when (uiState.connectionStatus) {
                        ConnectionStatus.IDLE -> {
                            Text("الحالة: غير متصل (اضغط للتجربة ببرموج)", color = TextSecondary, fontSize = 11.sp)
                        }
                        ConnectionStatus.TESTING -> {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                CircularProgressIndicator(color = NeonCyan, modifier = Modifier.size(14.dp), strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("جاري فحص واختبار الاتصال بـ Gemini... ⏳", color = NeonCyan, fontSize = 11.sp)
                            }
                        }
                        ConnectionStatus.CONNECTED -> {
                            Text("🟢 متصل بنجاح! الاتصال يعمل بأبهى حلة سحابية.", color = Color.Green, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        ConnectionStatus.FAILED -> {
                            Column {
                                Text("🔴 فشل الاتصال بالخادم السحابي الذكي!", color = ErrorRed, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Text("تأكد من سلامة كود المفتاح (API Key) ومن شبكة الإنترنت، ثم أعد المحاولة.", color = TextSecondary, fontSize = 10.sp)
                            }
                        }
                    }
                }
            }
        } else {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = NeonPurple.copy(alpha = 0.05f)),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, NeonPurple.copy(alpha = 0.15f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("🦙 تشغيل محلي أوفلاين كلياً", color = NeonCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.weight(1f))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.Green.copy(alpha = 0.15f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text("نشط", color = Color.Green, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("لا داعي للقلق بشأن استهلاك باقة الإنترنت أو إدخال مفتاح API سحابي. برموج يستعمل معالجات الذكاء المحلية لتأمين التلميحات والأجوبة التعليمية بشكل خصوصي وسلس بذكاء عالي.", color = TextSecondary, fontSize = 11.sp, lineHeight = 16.sp)
                    }
                }
            }
        }
        
        item {
            Divider(color = Color.White.copy(alpha = 0.05f))
        }
        
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("الخصوصية والبيانات والمحطة", color = TextSecondary, fontSize = 12.sp)
                
                SettingsButton("مسح سجل المحادثات", Icons.Default.DeleteSweep, ErrorRed) {
                    viewModel.clearChat()
                }
                
                SettingsButton("تصدير المحادثات", Icons.Default.FileDownload, NeonCyan) {
                    // Export logic standard
                }
                
                SettingsButton("إعادة تعيين وبدء التهيئة", Icons.Default.Refresh, Color.Gray) {
                    viewModel.resetSetup()
                }
            }
        }
    }
}

@Composable
fun SettingsButton(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        color = SurfaceVariantDark.copy(alpha = 0.2f),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Text(title, color = Color.White, fontSize = 14.sp)
        }
    }
}

@Composable
fun BarmujSetupScreen(viewModel: BarmujViewModel) {
    var selectedType by remember { mutableStateOf(BarmujModelType.LLAMA_3) }
    var apiKey by remember { mutableStateOf("") }
    
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = SurfaceDark,
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, NeonCyan.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Image(
                painter = painterResource(id = R.drawable.img_barmuj_robot_final_1781762659553),
                contentDescription = null,
                modifier = Modifier
                    .size(110.dp)
                    .clip(CircleShape)
                    .border(2.5.dp, NeonCyan, CircleShape),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text("مرحباً بك في برموج 🤖", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text("اختر طريقة تشغيل برموج لتخصيص تجربتك الفريدة.", color = TextSecondary, fontSize = 13.sp, textAlign = TextAlign.Center)
            
            Spacer(modifier = Modifier.height(16.dp))
            
            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Gemini 3.5 option
                item {
                    SetupOption(
                        title = "استخدام Gemini 3.5 Flash ☁️",
                        desc = "الأداء السحابي الفائق السرعة لبرموج (يتطلب إدخال API Key).",
                        selected = selectedType == BarmujModelType.GEMINI_3_5_FLASH,
                        onClick = { selectedType = BarmujModelType.GEMINI_3_5_FLASH }
                    )
                }
                
                // Llama Options
                item {
                    SetupOption(
                        title = "استخدام Llama 3 (محلي مجاني) 🦙",
                        desc = "الخيار الذكي للعمل أوفلاين بالكامل دون إنترنت مجاناً وبكل أمان.",
                        selected = selectedType == BarmujModelType.LLAMA_3,
                        onClick = { selectedType = BarmujModelType.LLAMA_3 }
                    )
                }

                item {
                    SetupOption(
                        title = "استخدام Llama 3.1 Pro 🛠️",
                        desc = "ذكاء مطور لتحليل أخطاء الأكواد أوفلاين دون قيود باقات البيانات.",
                        selected = selectedType == BarmujModelType.LLAMA_3_1,
                        onClick = { selectedType = BarmujModelType.LLAMA_3_1 }
                    )
                }

                item {
                    SetupOption(
                        title = "استخدام Llama 3.2 Lite ⚡",
                        desc = "الأسرع والأخف للحفاظ على طاقة هاتفك مع مساعدة برمجية مميزة.",
                        selected = selectedType == BarmujModelType.LLAMA_3_2,
                        onClick = { selectedType = BarmujModelType.LLAMA_3_2 }
                    )
                }
            }
            
            if (selectedType == BarmujModelType.GEMINI_3_5_FLASH) {
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = apiKey,
                    onValueChange = { apiKey = it },
                    placeholder = { Text("أدخل Gemini API Key", fontSize = 12.sp, color = TextSecondary) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = NeonCyan,
                        unfocusedBorderColor = SurfaceVariantDark
                    )
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = { viewModel.setSetupComplete(selectedType, apiKey) },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NeonCyan)
            ) {
                Text(
                    text = if (selectedType.isOffline) "تفعيل برموج الذكي المحلي ✨" else "تفعيل جيميناي السحابي ⚡", 
                    color = SurfaceDark, 
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun SetupOption(title: String, desc: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        color = if (selected) NeonCyan.copy(alpha = 0.1f) else Color.Transparent,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, if (selected) NeonCyan else SurfaceVariantDark),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            RadioButton(selected = selected, onClick = onClick, colors = RadioButtonDefaults.colors(selectedColor = NeonCyan))
            Column {
                Text(title, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                Text(desc, color = TextSecondary, fontSize = 11.sp)
            }
        }
    }
}

@Composable
fun ActionChip(modifier: Modifier = Modifier, title: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    Surface(
        modifier = modifier.clickable { onClick() },
        color = DarkBackground,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, SurfaceVariantDark)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(icon, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(title, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
fun BarmujMessageBubble(message: ChatMessage) {
    val alignment = if (message.isUser) Alignment.CenterEnd else Alignment.CenterStart
    val bgColor = if (message.isUser) NeonPurple.copy(alpha = 0.15f) else SurfaceVariantDark
    val textColor = if (message.isUser) Color.White else TextPrimary
    val shape = if (message.isUser) RoundedCornerShape(16.dp, 16.dp, 4.dp, 16.dp)
                else RoundedCornerShape(16.dp, 16.dp, 16.dp, 4.dp)

    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = alignment) {
        Column(
            horizontalAlignment = if (message.isUser) Alignment.End else Alignment.Start,
            modifier = Modifier.fillMaxWidth(0.9f)
        ) {
            Box(
                modifier = Modifier
                    .shadow(2.dp, shape)
                    .clip(shape)
                    .background(bgColor)
                    .border(if (message.isUser) 1.dp else 0.dp, NeonPurple.copy(alpha = 0.3f), shape)
                    .padding(12.dp)
            ) {
                Column {
                    if (message.image != null) {
                        Image(
                            bitmap = message.image.asImageBitmap(),
                            contentDescription = "Attachment",
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 200.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .padding(bottom = 8.dp),
                            contentScale = ContentScale.Crop
                        )
                    }
                    Text(message.text, color = textColor, fontSize = 14.sp, lineHeight = 20.sp)
                }
            }
            
            Text(
                text = java.text.SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(message.timestamp)),
                color = TextSecondary,
                fontSize = 10.sp,
                modifier = Modifier.padding(top = 4.dp, start = 4.dp, end = 4.dp)
            )
        }
    }
}

@Composable
fun BarmujTypingIndicator() {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceVariantDark)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        val infiniteTransition = rememberInfiniteTransition()
        repeat(3) { index ->
            val alpha by infiniteTransition.animateFloat(
                initialValue = 0.3f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(600, delayMillis = index * 150),
                    repeatMode = RepeatMode.Reverse
                )
            )
            Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(NeonCyan.copy(alpha = alpha)))
        }
    }
}
