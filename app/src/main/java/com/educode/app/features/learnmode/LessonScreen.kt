package com.educode.app.features.learnmode

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Code
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.educode.app.data.local.entity.LessonEntity
import com.educode.app.ui.theme.*
import com.educode.app.data.repository.LearnRepository
import com.educode.app.features.barmujai.LocalBarmujViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LessonScreen(
    lessonId: String,
    repository: LearnRepository,
    onBackClick: () -> Unit,
    onLessonCompleted: () -> Unit
) {
    var lesson by remember { mutableStateOf<LessonEntity?>(null) }
    val coroutineScope = rememberCoroutineScope()
    val barmujViewModel = LocalBarmujViewModel.current
    
    LaunchedEffect(lessonId) {
        lesson = repository.getLessonDetails(lessonId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(lesson?.title ?: "جاري التحميل...", color = NeonCyan) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceDark)
            )
        },
        containerColor = DarkBackground,
        bottomBar = {
            if (lesson != null) {
                Surface(color = SurfaceDark, shadowElevation = 16.dp) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .navigationBarsPadding(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Button(
                            onClick = {
                                barmujViewModel?.sendMessage("أنا أدرس حصة \"${lesson?.title}\" في قسم \"${lesson?.chapterId}\". هل يمكنك شرح الموضوع لي بطريقة مبسطة؟")
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = NeonPurple),
                            modifier = Modifier.weight(0.7f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("اسأل برموج 🤖", fontSize = 12.sp, color = Color.White)
                        }

                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    val isCourseCompleted = repository.markLessonCompleted(lessonId)
                                    val userId = com.educode.app.di.AppModule.authRepository.getCurrentUserId()
                                    if (userId != null) {
                                        // Update XP and Coins (Bonus for completing lesson)
                                        com.educode.app.di.AppModule.userRepository.updateXPAndCoins(userId, 15, 5)
                                        
                                        if (isCourseCompleted) {
                                            // Issue Certificate
                                            // ... assume logic ...
                                            val certificateNum = "EDU-${System.currentTimeMillis()}-${(1000..9999).random()}"
                                            val cert = com.educode.app.domain.models.Certificate(
                                                id = "cert_${System.currentTimeMillis()}",
                                                userId = userId,
                                                title = "شهادة احتراف البرمجة في مسار ${lesson?.title?.split(" ")?.lastOrNull() ?: ""}",
                                                issueDate = System.currentTimeMillis(),
                                                certificateNumber = certificateNum
                                            )
                                            com.educode.app.di.AppModule.userRepository.addCertificate(userId, cert)
                                            com.educode.app.di.AppModule.userRepository.markCourseCompleted(userId, "courseID") // Generic for now
                                            
                                            // Notify Certificate
                                            val settings = com.educode.app.di.AppModule.userRepository.getUserProfile(userId).getOrNull()?.notificationSettings
                                            if (settings?.enabled == true && settings.newCertificates) {
                                                com.educode.app.di.AppModule.notificationManager.notifyCertificateIssued(lesson?.title ?: "D")
                                            }
                                        } else {
                                            // Notify Lesson Completion
                                            val settings = com.educode.app.di.AppModule.userRepository.getUserProfile(userId).getOrNull()?.notificationSettings
                                            if (settings?.enabled == true && settings.lessonReminders) {
                                                com.educode.app.di.AppModule.notificationManager.notifyLessonCompleted(lesson?.title ?: "D")
                                            }
                                        }
                                    }
                                    onLessonCompleted()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("إكمال الدرس", color = DarkBackground, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        if (lesson == null) {
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
                Text(
                    text = lesson!!.title,
                    color = TextPrimary,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 16.dp)) {
                    Badge(containerColor = NeonPurple.copy(alpha = 0.2f), contentColor = NeonPurple) {
                        Text(lesson!!.difficulty, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Badge(containerColor = SurfaceVariantDark, contentColor = TextSecondary) {
                        Text("${lesson!!.estimatedDurationMinutes} دقيقة", modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Badge(containerColor = NeonYellow.copy(alpha=0.2f), contentColor = NeonYellow) {
                        Text("+${lesson!!.xpReward} XP", modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                    }
                }

                // Source and Dates
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceVariantDark.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("المصدر الأصلي: ", color = TextSecondary, fontSize = 12.sp)
                            Text(lesson!!.sourceName ?: "EduCode", color = NeonCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("آخر تحديث: ", color = TextSecondary, fontSize = 12.sp)
                            val date = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date(lesson!!.lastUpdatedDate))
                            Text(date, color = TextPrimary, fontSize = 12.sp)
                        }
                    }
                }
                
                // Content
                Text(
                    text = lesson!!.contentMarkdown.replace("\\n", "\n"), // simple markdown replacement
                    color = TextPrimary,
                    fontSize = 16.sp,
                    lineHeight = 24.sp
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                
                // Code Examples Mock
                if (!lesson!!.codeExamplesJson.isNullOrEmpty()) {
                    Text("أمثلة برمجية", color = NeonCyan, fontWeight = FontWeight.Bold, fontSize = 20.sp, modifier = Modifier.padding(bottom = 16.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 12.dp)) {
                                Icon(Icons.Default.Code, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Example 1", color = TextSecondary, fontSize = 12.sp)
                            }
                            Text("print('Hello EduCode!')", color = NeonCyan, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace)
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(100.dp)) // padding for bottom bar
            }
        }
    }
}
