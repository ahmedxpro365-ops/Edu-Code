package com.educode.app.features.learnmode

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.educode.app.data.local.entity.ChapterEntity
import com.educode.app.data.local.entity.CourseEntity
import com.educode.app.data.local.entity.LessonEntity
import com.educode.app.ui.theme.*

import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import com.educode.app.di.AppModule
import kotlinx.coroutines.launch
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.School

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseRoadmapScreen(
    language: String,
    viewModel: LearnViewModel,
    onBackClick: () -> Unit,
    onLessonClick: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(language) {
        viewModel.loadContentForLanguage(language)
    }

    val configuration = LocalConfiguration.current
    val isTablet = configuration.screenWidthDp >= 600

    var selectedLessonId by remember { mutableStateOf<String?>(null) }
    var selectedLesson by remember { mutableStateOf<LessonEntity?>(null) }
    var isLessonLoading by remember { mutableStateOf(false) }

    LaunchedEffect(selectedLessonId) {
        if (selectedLessonId != null) {
            isLessonLoading = true
            selectedLesson = AppModule.learnRepository.getLessonDetails(selectedLessonId!!)
            isLessonLoading = false
        } else {
            selectedLesson = null
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("مسار $language", color = NeonCyan, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBackground)
            )
        },
        containerColor = DarkBackground
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = NeonCyan)
            }
        } else if (uiState.error != null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = uiState.error!!, color = ErrorRed)
            }
        } else {
            if (isTablet) {
                // Two-Pane Master Detail Layout
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        horizontalArrangement = Arrangement.spacedBy(1.dp)
                    ) {
                        // Left Pane: Roadmap List (Chapters & Lessons: 40% weight)
                        Column(
                            modifier = Modifier
                                .weight(0.40f)
                                .fillMaxHeight()
                                .background(DarkBackground)
                        ) {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 16.dp),
                                contentPadding = PaddingValues(vertical = 16.dp),
                                verticalArrangement = Arrangement.spacedBy(24.dp)
                            ) {
                                uiState.courses.firstOrNull()?.let { course ->
                                    item {
                                        CourseHeader(course)
                                    }
                                }

                                items(uiState.chapters) { chapter ->
                                    ChapterView(
                                        chapter = chapter,
                                        lessons = uiState.lessonsMap[chapter.id] ?: emptyList(),
                                        onLessonClick = { lessonId ->
                                            selectedLessonId = lessonId
                                        }
                                    )
                                }
                            }
                        }

                        // Divider
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .width(1.dp)
                                .background(SurfaceVariantDark)
                        )

                        // Right Pane: Lesson Details Layout (60% weight)
                        Column(
                            modifier = Modifier
                                .weight(0.60f)
                                .fillMaxHeight()
                                .background(SurfaceDark)
                                .padding(24.dp)
                        ) {
                            if (isLessonLoading) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(color = NeonCyan)
                                }
                            } else if (selectedLesson != null) {
                                val currentLesson = selectedLesson!!
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .verticalScroll(rememberScrollState())
                                ) {
                                    Text(
                                        text = currentLesson.title,
                                        color = TextPrimary,
                                        fontSize = 26.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(bottom = 12.dp)
                                    )

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(bottom = 16.dp)
                                    ) {
                                        Badge(containerColor = NeonPurple.copy(alpha = 0.2f), contentColor = NeonPurple) {
                                            Text(currentLesson.difficulty, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Badge(containerColor = SurfaceVariantDark, contentColor = TextSecondary) {
                                            Text("${currentLesson.estimatedDurationMinutes} دقيقة", modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Badge(containerColor = NeonYellow.copy(alpha=0.2f), contentColor = NeonYellow) {
                                            Text("+${currentLesson.xpReward} XP", modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                                        }
                                    }

                                    // Content
                                    Text(
                                        text = currentLesson.contentMarkdown.replace("\\n", "\n"),
                                        color = TextPrimary,
                                        fontSize = 17.sp,
                                        lineHeight = 26.sp,
                                        modifier = Modifier.padding(bottom = 24.dp)
                                    )

                                    if (!currentLesson.codeExamplesJson.isNullOrEmpty()) {
                                        Text("أمثلة تفاعلية", color = NeonCyan, fontWeight = FontWeight.Bold, fontSize = 20.sp, modifier = Modifier.padding(bottom = 12.dp))
                                        Card(
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(12.dp),
                                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E))
                                        ) {
                                            Column(modifier = Modifier.padding(16.dp)) {
                                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 8.dp)) {
                                                    Icon(Icons.Default.Code, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(16.dp))
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Text("مثال مصور لـ ${currentLesson.title}", color = TextSecondary, fontSize = 12.sp)
                                                }
                                                Text("print(\"رائع، برموج يدعمك!\")", color = NeonCyan, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace)
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.weight(1f))
                                    Spacer(modifier = Modifier.height(32.dp))

                                    // Tablet Bottom Interactive Action Panel
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                                    ) {
                                        Button(
                                            onClick = {
                                                coroutineScope.launch {
                                                    AppModule.learnRepository.markLessonCompleted(currentLesson.id)
                                                    selectedLesson = AppModule.learnRepository.getLessonDetails(currentLesson.id)
                                                    viewModel.loadContentForLanguage(language)
                                                }
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                                            modifier = Modifier.weight(1.5f),
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            Text(if (currentLesson.isCompleted) "الدرس مكتمل بالفعل ✓" else "تعلمت الدرس الكوني", color = DarkBackground, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                        }

                                        Button(
                                            onClick = { onLessonClick(currentLesson.id) },
                                            colors = ButtonDefaults.buttonColors(containerColor = NeonPurple),
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            Text("اسأل برموج", color = Color.White)
                                        }
                                    }
                                }
                            } else {
                                // Tablet Placeholder view
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(Icons.Default.School, contentDescription = null, tint = NeonCyan.copy(alpha = 0.4f), modifier = Modifier.size(80.dp))
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Text(
                                            text = "يرحب بك برموج بأعماق مسار $language!",
                                            color = TextPrimary,
                                            fontSize = 20.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = "اختر درساً من القائمة اليسرى لاكتساب نقاط الخبرة والبدء في بناء إمبراطوريتك",
                                            color = TextSecondary,
                                            fontSize = 14.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // PHONE VIEW (Single roadmap timeline list)
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    uiState.courses.firstOrNull()?.let { course ->
                        item {
                            CourseHeader(course)
                        }
                    }

                    items(uiState.chapters) { chapter ->
                        ChapterView(
                            chapter = chapter,
                            lessons = uiState.lessonsMap[chapter.id] ?: emptyList(),
                            onLessonClick = onLessonClick
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CourseHeader(course: CourseEntity) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(course.title, color = NeonCyan, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text(course.description, color = TextSecondary, fontSize = 14.sp)
        }
    }
}

@Composable
fun ChapterView(chapter: ChapterEntity, lessons: List<LessonEntity>, onLessonClick: (String) -> Unit) {
    Column {
        Text(
            text = chapter.title,
            color = TextPrimary,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Text(
            text = chapter.description,
            color = TextSecondary,
            fontSize = 14.sp,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        // Roadmap timeline for lessons
        lessons.forEachIndexed { index, lesson ->
            val isLast = index == lessons.lastIndex
            LessonNode(lesson = lesson, isLast = isLast, onLessonClick = { onLessonClick(lesson.id) })
        }
    }
}

@Composable
fun LessonNode(lesson: LessonEntity, isLast: Boolean, onLessonClick: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
        // Timeline line + Icon
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(40.dp)) {
            val iconColor = if (lesson.isCompleted) SuccessGreen else NeonPurple
            val icon = if (lesson.isCompleted) Icons.Default.Check else Icons.Default.PlayArrow
            
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(iconColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(20.dp))
            }
            
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(80.dp)
                        .background(iconColor.copy(alpha = 0.5f))
                        .padding(vertical = 4.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        // Lesson Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = if (isLast) 0.dp else 24.dp)
                .clickable { onLessonClick() },
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceVariantDark)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(lesson.title, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text("${lesson.xpReward} XP", color = NeonYellow, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(lesson.difficulty, color = NeonCyan, fontSize = 12.sp)
            }
        }
    }
}
