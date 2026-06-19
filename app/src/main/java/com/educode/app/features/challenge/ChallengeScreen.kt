package com.educode.app.features.challenge

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.educode.app.features.barmujai.LocalBarmujViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.educode.app.components.CyberpunkParticlesBackground
import com.educode.app.domain.models.User
import com.educode.app.features.codeeditor.CodeSyntaxHighlighter
import com.educode.app.features.codeeditor.EditorThemeColors
import com.educode.app.features.codeeditor.ProgrammingLanguage
import com.educode.app.features.codeeditor.LangTemplates
import com.educode.app.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

// Helper mapping function for editor syntax highlighting
private fun mapToProgrammingLanguage(lang: String): ProgrammingLanguage {
    return when (lang.uppercase().trim()) {
        "HTML" -> ProgrammingLanguage.HTML
        "CSS" -> ProgrammingLanguage.CSS
        "JAVASCRIPT", "JS" -> ProgrammingLanguage.JAVASCRIPT
        "PYTHON" -> ProgrammingLanguage.PYTHON
        "C" -> ProgrammingLanguage.C
        "C++", "CPP" -> ProgrammingLanguage.CPP
        "C#", "CSHARP" -> ProgrammingLanguage.CSHARP
        "JAVA" -> ProgrammingLanguage.JAVA
        "PHP" -> ProgrammingLanguage.PHP
        "RUST" -> ProgrammingLanguage.RUST
        else -> ProgrammingLanguage.JAVASCRIPT
    }
}

// Color palettes for neon tags
private val LanguageColors = mapOf(
    "HTML" to TierHtml,
    "CSS" to NeonCyan,
    "JAVASCRIPT" to TierJs,
    "PYTHON" to TierPython,
    "C" to TierC,
    "C++" to TierAdvanced,
    "C#" to TierBeginner,
    "JAVA" to ErrorRed,
    "PHP" to NeonPurple,
    "RUST" to SuccessGreen
)

@Composable
fun ChallengeScreen(
    onTabClick: (com.educode.app.components.HubTab) -> Unit,
    onBackClick: () -> Unit,
    viewModel: ChallengeViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()

    var showHeartsReplenishDialog by remember { mutableStateOf(false) }

    com.educode.app.components.HubShell(
        selectedTab = com.educode.app.components.HubTab.CHALLENGES,
        onTabClick = onTabClick,
        showShell = false
    ) { extraPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(extraPadding)
                .background(DarkBackground)
        ) {
        CyberpunkParticlesBackground()

        AnimatedContent(
            targetState = uiState,
            transitionSpec = {
                slideInHorizontally { width -> width } + fadeIn() togetherWith
                        slideOutHorizontally { width -> -width } + fadeOut()
            },
            label = "ChallengeScreensAnimation"
        ) { state ->
            when (state) {
                is ChallengeUIState.LanguageSelection -> {
                    LanguageSelectionView(
                        user = userProfile,
                        onBackClick = onBackClick,
                        onSelectLanguage = { lang ->
                            if (userProfile.hearts <= 0) {
                                showHeartsReplenishDialog = true
                            } else {
                                viewModel.selectLanguage(lang)
                            }
                        }
                    )
                }
                is ChallengeUIState.LanguageDashboard -> {
                    LanguageDashboardView(
                        language = state.language,
                        highestCompletedIndex = state.highestCompletedIndex,
                        userHearts = userProfile.hearts,
                        onBackSelection = { viewModel.resetToSelection() },
                        onStartLevel = { lvl -> viewModel.startLevel(state.language, lvl) }
                    )
                }
                is ChallengeUIState.Playing -> {
                    ActiveGameplayView(
                        state = state,
                        user = userProfile,
                        onBackSelection = { viewModel.selectLanguage(state.language) },
                        onCodeChange = { code -> viewModel.updateCodeBuffer(code) },
                        onRunSimSim = { viewModel.submitCurrentAnswer() },
                        onFormatCode = { viewModel.formatBufferCode() },
                        onExplainError = { viewModel.explainConcept() },
                        onSelectOption = { opt -> viewModel.selectAnswer(opt) },
                        onSubmitAnswer = { viewModel.submitCurrentAnswer() },
                        onAskBarmuj = { viewModel.requestBarmujAIHint() },
                        onUseHint = { viewModel.useStaticHint() },
                        onNextLevel = { viewModel.proceedToNextLevel() }
                    )
                }
                is ChallengeUIState.Victory -> {
                    VictoryView(
                        state = state,
                        onContinue = {
                            val nextLevel = state.levelIndex + 1
                            if (nextLevel <= 50) {
                                viewModel.startLevel(state.language, nextLevel)
                            } else {
                                viewModel.selectLanguage(state.language)
                            }
                        },
                        onBackDashboard = { viewModel.selectLanguage(state.language) }
                    )
                }
                is ChallengeUIState.Failure -> {
                    FailureView(
                        state = state,
                        onRetry = { viewModel.startLevel(state.language, state.levelIndex) },
                        onBackDashboard = { viewModel.selectLanguage(state.language) }
                    )
                }
            }
        }

        if (showHeartsReplenishDialog) {
            AlertDialog(
                onDismissRequest = { showHeartsReplenishDialog = false },
                icon = { Icon(Icons.Default.HeartBroken, contentDescription = null, tint = ErrorRed, modifier = Modifier.size(48.dp)) },
                title = { Text("💔 نفدت القلوب!", color = Color.White, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center) },
                text = {
                    Text(
                        "تحديات وضع Challenge Mode معطلة حالياً لأن طاقتك انتهت. اذهب فوراً إلى وضع التعلم (Learn Mode) وحل الدروس لتستعيد قلوب جديدة وتواصل صعودك البرمجي!",
                        color = Color.LightGray,
                        textAlign = TextAlign.Center,
                        fontSize = 14.sp
                    )
                },
                confirmButton = {
                    Button(
                        onClick = { showHeartsReplenishDialog = false; onBackClick() },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonPurple)
                    ) {
                        Text("الذهاب للتعلم 🎓", color = Color.White)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showHeartsReplenishDialog = false }) {
                        Text("حسناً", color = Color.Gray)
                    }
                },
                containerColor = SurfaceDark,
                shape = RoundedCornerShape(16.dp)
            )
        }
    }
  }
}

// 1. SELECT LANGUAGE view
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguageSelectionView(
    user: User,
    onBackClick: () -> Unit,
    onSelectLanguage: (String) -> Unit
) {
    val languages = listOf("HTML", "CSS", "JavaScript", "Python", "C", "C++", "C#", "Java", "PHP", "Rust")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("تحدي المبرمج الخارق 🏆 (Challenge Mode)", color = NeonCyan, fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = Color.Transparent
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Heart tracker and user stats bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(SurfaceDark)
                    .border(1.dp, Color(0xFF1F2937), RoundedCornerShape(12.dp))
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = "Hearts",
                        tint = if (user.hearts > 0) ErrorRed else Color.Gray,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (user.hearts <= 0) "القلوب فارغة 💔 (اذهب لوضع التعلم لملئها)" else "القلوب المتبقية: ${user.hearts}",
                        color = if (user.hearts > 0) Color.White else ErrorRed,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.MonetizationOn, contentDescription = "Coins", tint = NeonYellow, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("${user.coins} BIT", color = NeonYellow, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Icon(Icons.Default.Star, contentDescription = "XP", tint = NeonCyan, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("${user.xp} XP", color = NeonCyan, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "اختر الساحة البرمجية لتتحدى مهاراتك الفنية:",
                color = Color.LightGray,
                fontSize = 15.sp,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Start
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Premium Language Grid
            LazyVerticalGrid(
                columns = GridCells.Adaptive(140.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 600.dp),
                contentPadding = PaddingValues(bottom = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(languages) { lang ->
                    LanguageGridCard(
                        lang = lang,
                        isEnabled = user.hearts > 0,
                        onClick = { onSelectLanguage(lang) }
                    )
                }
            }
        }
    }
}

@Composable
fun LanguageGridCard(
    lang: String,
    isEnabled: Boolean,
    onClick: () -> Unit
) {
    val baseColor = LanguageColors[lang.uppercase()] ?: NeonCyan
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(if (isEnabled) SurfaceVariantDark else SurfaceDark.copy(0.5f))
            .border(
                width = 1.5.dp,
                brush = if (isEnabled) Brush.linearGradient(listOf(baseColor, baseColor.copy(0.4f)))
                else SolidColor(Color.DarkGray),
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onClick)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(baseColor.copy(0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = lang.take(2).uppercase(),
                    color = baseColor,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = lang,
                color = if (isEnabled) Color.White else Color.Gray,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "50 تحدٍ متدرج",
                color = Color.Gray,
                fontSize = 11.sp
            )
        }
    }
}

// 2. LANGUAGE DASHBOARD VIEW showing the 50 progressive nodes
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguageDashboardView(
    language: String,
    highestCompletedIndex: Int,
    userHearts: Int,
    onBackSelection: () -> Unit,
    onStartLevel: (Int) -> Unit
) {
    val baseColor = LanguageColors[language.uppercase()] ?: NeonCyan
    val currentPlayLevel = highestCompletedIndex + 1

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("$language - لوحة الإنجازات والتقدم", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = onBackSelection) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = Color.Transparent
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp)
        ) {
            // High level progress card
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(SurfaceDark)
                    .border(1.dp, baseColor.copy(0.5f), RoundedCornerShape(16.dp))
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.size(70.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        progress = { highestCompletedIndex / 50f },
                        modifier = Modifier.fillMaxSize(),
                        color = baseColor,
                        strokeWidth = 6.dp,
                        trackColor = Color(0xFF1F2937),
                    )
                    Text(
                        text = "${(highestCompletedIndex * 100) / 50}%",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        text = "التحدي المتدرج لـ $language",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        text = "أنجزت $highestCompletedIndex من 50 تحدياً بنجاح.",
                        color = Color.LightGray,
                        fontSize = 13.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    if (highestCompletedIndex < 50) {
                        Button(
                            onClick = { onStartLevel(currentPlayLevel) },
                            colors = ButtonDefaults.buttonColors(containerColor = baseColor),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("العب التحدي الحالي (المستوى $currentPlayLevel) 🚀", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    } else {
                        Text("🎉 عظيم! لقد ختمت ساحة $language بالكامل!", color = SuccessGreen, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "جميع مستويات المسار (من الأسهل إلى الأكثر صعوبة):",
                color = Color.LightGray,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 50 node grid
            LazyVerticalGrid(
                columns = GridCells.Adaptive(64.dp),
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(bottom = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(50) { index ->
                    val level = index + 1
                    val state = when {
                        level <= highestCompletedIndex -> NodeState.COMPLETED
                        level == currentPlayLevel -> NodeState.PLAYABLE
                        else -> NodeState.LOCKED
                    }

                    LevelNode(
                        level = level,
                        state = state,
                        baseColor = baseColor,
                        onClick = {
                            if (state != NodeState.LOCKED) {
                                onStartLevel(level)
                            }
                        }
                    )
                }
            }
        }
    }
}

enum class NodeState {
    COMPLETED, PLAYABLE, LOCKED
}

@Composable
fun LevelNode(
    level: Int,
    state: NodeState,
    baseColor: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(64.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(
                when (state) {
                    NodeState.COMPLETED -> SuccessGreen.copy(0.15f)
                    NodeState.PLAYABLE -> baseColor.copy(0.25f)
                    NodeState.LOCKED -> SurfaceDark.copy(0.25f)
                }
            )
            .border(
                width = 2.dp,
                color = when (state) {
                    NodeState.COMPLETED -> SuccessGreen
                    NodeState.PLAYABLE -> baseColor
                    NodeState.LOCKED -> Color.DarkGray
                },
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "$level",
                color = when (state) {
                    NodeState.COMPLETED -> SuccessGreen
                    NodeState.PLAYABLE -> Color.White
                    NodeState.LOCKED -> Color.Gray
                },
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            
            Spacer(modifier = Modifier.height(2.dp))
            
            Icon(
                imageVector = when (state) {
                    NodeState.COMPLETED -> Icons.Default.Check
                    NodeState.PLAYABLE -> Icons.Default.PlayArrow
                    NodeState.LOCKED -> Icons.Default.Lock
                },
                contentDescription = null,
                tint = when (state) {
                    NodeState.COMPLETED -> SuccessGreen
                    NodeState.PLAYABLE -> baseColor
                    NodeState.LOCKED -> Color.Gray
                },
                modifier = Modifier.size(12.dp)
            )
        }
    }
}


// 3. THE GRAND ACTIVE GAMEPLAY VIEW (Tablet Dual-Pane vs Phone column)
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ActiveGameplayView(
    state: ChallengeUIState.Playing,
    user: User,
    onBackSelection: () -> Unit,
    onCodeChange: (String) -> Unit,
    onRunSimSim: () -> Unit,
    onFormatCode: () -> Unit,
    onExplainError: () -> Unit,
    onSelectOption: (String) -> Unit,
    onSubmitAnswer: () -> Unit,
    onAskBarmuj: () -> Unit,
    onUseHint: () -> Unit,
    onNextLevel: () -> Unit
) {
    val configuration = LocalConfiguration.current
    val isTablet = configuration.screenWidthDp >= 600

    val systemAccentColor = LanguageColors[state.language.uppercase()] ?: NeonCyan
    val codeLanguage = mapToProgrammingLanguage(state.language)

    val listSuggestions = LangTemplates.getSuggestions(codeLanguage)

    val isCodeType = state.question.type !in listOf(QuestionType.MULTIPLE_CHOICE, QuestionType.TRUE_FALSE)

    // Layout representation responsive
    if (isTablet) {
        Row(modifier = Modifier.fillMaxSize()) {
            // Left Panel: Code Editor (takes 55% weight)
            if (isCodeType) {
                Box(modifier = Modifier.weight(0.55f).fillMaxHeight()) {
                    CodeEditorComponent(
                        code = state.codeBuffer,
                        onCodeChange = onCodeChange,
                        programmingLanguage = codeLanguage,
                        onRun = onRunSimSim,
                        onFormat = onFormatCode,
                        onAnalyzeError = onExplainError,
                        suggestions = listSuggestions,
                        consoleOutput = state.consoleOutput,
                        consoleError = state.consoleError,
                        shownResult = state.shownAnswerResult,
                        accentColor = systemAccentColor
                    )
                }
                VerticalDivider(color = Color(0xFF1F2937), thickness = 1.dp)
            }

            // Right Panel: Question details
            Column(
                modifier = Modifier
                    .weight(if (isCodeType) 0.45f else 1f)
                    .fillMaxHeight()
                    .background(SurfaceDark)
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                QuestionDetailsPanel(
                    state = state,
                    accentColor = systemAccentColor,
                    onBackSelection = onBackSelection,
                    onSelectOption = onSelectOption,
                    onSubmitAnswer = onSubmitAnswer,
                    onAskBarmuj = onAskBarmuj,
                    onUseHint = onUseHint,
                    onNextLevel = onNextLevel,
                    onExplainConcept = onExplainError
                )
            }
        }
    } else {
        // MOBILE VIEW (Vertical layout)
        Column(modifier = Modifier.fillMaxSize()) {
            // Top Scrollable Question Panel
            Column(
                modifier = Modifier
                    .weight(if (isCodeType) 0.40f else 1f)
                    .fillMaxWidth()
                    .background(SurfaceDark)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                QuestionDetailsPanel(
                    state = state,
                    accentColor = systemAccentColor,
                    onBackSelection = onBackSelection,
                    onSelectOption = onSelectOption,
                    onSubmitAnswer = onSubmitAnswer,
                    onAskBarmuj = onAskBarmuj,
                    onUseHint = onUseHint,
                    onNextLevel = onNextLevel,
                    onExplainConcept = onExplainError
                )
            }

            if (isCodeType) {
                // Separator line
                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFF1F2937)))

                // Bottom Code Editor Panel
                Box(modifier = Modifier.weight(0.60f).fillMaxWidth()) {
                    CodeEditorComponent(
                        code = state.codeBuffer,
                        onCodeChange = onCodeChange,
                        programmingLanguage = codeLanguage,
                        onRun = onRunSimSim,
                        onFormat = onFormatCode,
                        onAnalyzeError = onExplainError,
                        suggestions = listSuggestions,
                        consoleOutput = state.consoleOutput,
                        consoleError = state.consoleError,
                        shownResult = state.shownAnswerResult,
                        accentColor = systemAccentColor
                    )
                }
            }
        }
    }
}

// Separate code editor layout component
@Composable
fun CodeEditorComponent(
    code: String,
    onCodeChange: (String) -> Unit,
    programmingLanguage: ProgrammingLanguage,
    onRun: () -> Unit,
    onFormat: () -> Unit,
    onAnalyzeError: () -> Unit,
    suggestions: List<String>,
    consoleOutput: String,
    consoleError: String?,
    shownResult: Boolean?,
    accentColor: Color
) {
    var expandedTerminal by remember { mutableStateOf(false) }

    // Auto toggle terminal drawer when output or error updates
    LaunchedEffect(consoleOutput, consoleError) {
        if (consoleOutput.isNotEmpty() || consoleError != null) {
            expandedTerminal = true
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F121E))
    ) {
        // Gutter line numbers + BasicTextField editing rows
        Box(modifier = Modifier.weight(1f)) {
            Row(modifier = Modifier.fillMaxSize()) {
                // Determine line counts
                val linesCount = maxOf(1, code.split("\n").size)
                val lineNumbersText = (1..linesCount).joinToString("\n")

                // Left Gutter Column
                Text(
                    text = lineNumbersText,
                    color = accentColor.copy(alpha = 0.5f),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    lineHeight = 22.sp,
                    textAlign = TextAlign.End,
                    modifier = Modifier
                        .width(40.dp)
                        .background(Color(0xFF07090F))
                        .padding(top = 16.dp, end = 8.dp)
                )

                // Split border
                Box(modifier = Modifier.width(1.dp).fillMaxHeight().background(Color(0xFF1F2937)))

                // Actual Editor typing landscape
                BasicTextField(
                    value = code,
                    onValueChange = onCodeChange,
                    textStyle = TextStyle(
                        color = Color.White,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 13.sp,
                        lineHeight = 22.sp
                    ),
                    visualTransformation = CodeSyntaxHighlighter(
                        language = programmingLanguage,
                        themeColors = EditorThemeColors.CYBERPUNK_PREMIUM
                    ),
                    cursorBrush = SolidColor(accentColor),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .testTag("interactive_code_editor"),
                    decorationBox = { innerTextField ->
                        Box {
                            if (code.isEmpty()) {
                                Text("// تدرج واكتب خط التحدي هنا...", color = Color.Gray, fontSize = 13.sp, fontFamily = FontFamily.Monospace)
                            }
                            innerTextField()
                        }
                    }
                )
            }
        }

        // Suggestions block row for Auto Completion
        if (suggestions.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF07090F))
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                suggestions.forEach { tag ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(SurfaceVariantDark)
                            .border(1.dp, Color.DarkGray, RoundedCornerShape(6.dp))
                            .clickable {
                                onCodeChange(code + " " + tag)
                            }
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(tag, color = accentColor, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                    }
                }
            }
        }

        // Action controls toolbar
        HorizontalDivider(color = Color(0xFF1F2937), thickness = 1.dp)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(SurfaceDark)
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onFormat,
                    colors = ButtonDefaults.buttonColors(containerColor = SurfaceVariantDark),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Icon(Icons.Default.FormatAlignLeft, contentDescription = "Format", tint = Color.LightGray, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("تنسيق 📐", color = Color.LightGray, fontSize = 11.sp)
                }

                Button(
                    onClick = onAnalyzeError,
                    colors = ButtonDefaults.buttonColors(containerColor = SurfaceVariantDark),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Icon(Icons.Default.Troubleshoot, contentDescription = "Explain Error", tint = accentColor, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("تحليل الخطأ 🛠️", color = Color.White, fontSize = 11.sp)
                }
            }

            Button(
                onClick = onRun,
                colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                shape = RoundedCornerShape(6.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Icon(Icons.Default.Bolt, contentDescription = "Run", tint = Color.Black, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("تشغيل الكود ⚡", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }

        // Expandable Simulation Terminal Drawer
        AnimatedVisibility(
            visible = expandedTerminal,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(Color(0xFF04060A))
            ) {
                HorizontalDivider(color = if (consoleError != null) ErrorRed else SuccessGreen, thickness = 1.dp)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF000000))
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(if (consoleError != null) ErrorRed else SuccessGreen))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (consoleError != null) "مدير المترجم: خطأ!" else "المخرج الحركي والمنفذ",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    IconButton(onClick = { expandedTerminal = false }, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Hide", tint = Color.Gray, modifier = Modifier.size(14.dp))
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(12.dp)
                ) {
                    Text(
                        text = consoleError ?: consoleOutput,
                        color = if (consoleError != null) ErrorRed else Color.Green,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        lineHeight = 18.sp
                    )
                }
            }
        }
    }
}

// Right details panel showing question + options, coaches and AI interactions
@Composable
fun ColumnScope.QuestionDetailsPanel(
    state: ChallengeUIState.Playing,
    accentColor: Color,
    onBackSelection: () -> Unit,
    onSelectOption: (String) -> Unit,
    onSubmitAnswer: () -> Unit,
    onAskBarmuj: () -> Unit,
    onUseHint: () -> Unit,
    onNextLevel: () -> Unit,
    onExplainConcept: () -> Unit
) {
    val isMcqType = state.question.type == QuestionType.MULTIPLE_CHOICE || state.question.type == QuestionType.TRUE_FALSE

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBackSelection) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = Color.Gray)
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.LiveHelp, contentDescription = null, tint = accentColor, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "المستوى ${state.levelIndex} / 50",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Favorite, contentDescription = null, tint = ErrorRed, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("${state.heartsLeft}", color = Color.White, fontWeight = FontWeight.Bold)
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Question body
    Text(
        text = state.question.questionText,
        color = Color.White,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        textAlign = TextAlign.Right,
        modifier = Modifier.fillMaxWidth()
    )

    Spacer(modifier = Modifier.height(8.dp))

    // Concept category chip
    Box(
        modifier = Modifier
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(accentColor.copy(0.15f))
            .border(width = 1.dp, color = accentColor.copy(0.4f), shape = RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .align(Alignment.End)
    ) {
        Text(
            text = "الدرس: ${state.question.suggestedLesson}",
            color = accentColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }

    Spacer(modifier = Modifier.height(16.dp))

    // If MULTIPLE_CHOICE or TRUE_FALSE, show option selection buttons
    if (isMcqType) {
        val selectedOption = state.userSelectedAnswer

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            state.question.options.forEach { opt ->
                val isSelected = selectedOption == opt
                val resultShown = state.shownAnswerResult

                val cardBorderColor = when {
                    isSelected && resultShown == true -> SuccessGreen
                    isSelected && resultShown == false -> ErrorRed
                    isSelected -> accentColor
                    else -> Color.DarkGray
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSelected) accentColor.copy(0.11f) else SurfaceVariantDark)
                        .border(1.5.dp, cardBorderColor, RoundedCornerShape(12.dp))
                        .clickable(enabled = resultShown != true) {
                            onSelectOption(opt)
                        }
                        .padding(16.dp),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    Text(
                        text = opt,
                        color = if (isSelected) Color.White else Color.LightGray,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                        textAlign = TextAlign.End
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            if (state.shownAnswerResult != true) {
                Button(
                    onClick = onSubmitAnswer,
                    enabled = selectedOption != null,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    Text("تأكيد الإجابة ✅", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        }
    } else {
        // CODE TYPE instruction box
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(SurfaceVariantDark)
                .border(1.dp, Color.DarkGray, RoundedCornerShape(12.dp))
                .padding(12.dp)
        ) {
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "💻 مسار الكود التفاعلي جاهز!",
                    color = accentColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Right
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "تعديل الكود مفتوح داخل نافذة المحرر. املأ الفراغ، أصلح الأخطاء، أو اكتب كوداً صحيحاً، ثم اضغط على زر 'تشغيل الكود ⚡' لاختبار ناتجك المبرمج.",
                    color = Color.LightGray,
                    fontSize = 12.sp,
                    lineHeight = 18.sp,
                    textAlign = TextAlign.Right
                )
            }
        }
    }

    if (state.shownAnswerResult == true) {
        Spacer(modifier = Modifier.height(20.dp))
        Button(
            onClick = onNextLevel,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
            shape = RoundedCornerShape(12.dp),
            contentPadding = PaddingValues(16.dp)
        ) {
            Text("التالي 🚀", color = Color.White, fontWeight = FontWeight.Bold)
        }
    }

    Spacer(modifier = Modifier.height(20.dp))

    // Coaching Barmuj AI and hints rows
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Button(
            onClick = onAskBarmuj,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6B21A8)),
            shape = RoundedCornerShape(8.dp),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Icon(Icons.Default.Face, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("اسأل برموج 🤖", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TextButton(onClick = onExplainConcept) {
                Text("شرح المفهوم 🏫", color = accentColor, fontSize = 12.sp)
            }
            TextButton(onClick = onUseHint) {
                Text("تلميحة 💡", color = Color.LightGray, fontSize = 12.sp)
            }
        }
    }

    // Dynamic coach AI response box
    state.barmujExplanation?.let { response ->
        Spacer(modifier = Modifier.height(16.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF0F121E))
                .border(1.dp, Color(0xFF3B5B75), RoundedCornerShape(12.dp))
                .padding(14.dp)
        ) {
            Column {
                Text(
                    text = "🤖 المعلم برموج يقترح:",
                    color = Color(0xFF00E5FF),
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Right,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = response,
                    color = Color.LightGray,
                    fontSize = 12.sp,
                    lineHeight = 18.sp,
                    textAlign = TextAlign.Right,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }

    // Displays feedback on answer result
    state.shownAnswerResult?.let { isCorrect ->
        Spacer(modifier = Modifier.height(16.dp))
        if (isCorrect) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(SuccessGreen.copy(alpha = 0.12f))
                    .border(1.5.dp, SuccessGreen, RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                Column(horizontalAlignment = Alignment.End) {
                    Text("✅ أحسنت! الإجابة صحيحة!", color = SuccessGreen, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(state.question.explanation, color = Color.White, fontSize = 12.sp, textAlign = TextAlign.Right)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onNextLevel,
                colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("المطالبة بالمكافآت والتقدم 🚀", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(ErrorRed.copy(alpha = 0.12f))
                    .border(1.5.dp, ErrorRed, RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                Column(horizontalAlignment = Alignment.End) {
                    Text("❌ محاولة غير صحيحة!", color = ErrorRed, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("لا تستسلم! راجع الكود واطلب تفسيراً من المعلم برموج ثم جرب مرة أخرى.", color = Color.LightGray, fontSize = 12.sp, textAlign = TextAlign.Right)
                }
            }
        }
    }

    // Static hint text if clicked
    if (state.hintShown) {
        Spacer(modifier = Modifier.height(12.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(SurfaceVariantDark)
                .padding(12.dp)
        ) {
            Text(
                text = "💡 تلميحة الحكيم: ${state.question.hint}",
                color = Color.LightGray,
                fontSize = 12.sp,
                textAlign = TextAlign.Right,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}


// 4. THE SPLENDID VICTORY VIEW (with dynamic confetti animation!)
@Composable
fun VictoryView(
    state: ChallengeUIState.Victory,
    onContinue: () -> Unit,
    onBackDashboard: () -> Unit
) {
    val baseColor = LanguageColors[state.language.uppercase()] ?: NeonCyan

    var progressScale by remember { mutableStateOf(0f) }
    LaunchedEffect(Unit) {
        progressScale = 1f
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        // High-fidelity Falling Confetti Simulation
        ConfettiSimulation()

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .widthIn(max = 500.dp)
                .background(SurfaceDark, RoundedCornerShape(24.dp))
                .border(2.dp, SuccessGreen, RoundedCornerShape(24.dp))
                .padding(24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Icon(
                imageVector = Icons.Default.EmojiEvents,
                contentDescription = null,
                tint = NeonYellow,
                modifier = Modifier
                    .size(80.dp)
                    .scale(progressScale)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "تحدي عظيم قد تكلل بالنجاح! 🎉",
                color = SuccessGreen,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                textAlign = TextAlign.Center
            )

            Text(
                text = "أكملت بنجاح التحدي رقم ${state.levelIndex} بساحة ${state.language}!",
                color = Color.LightGray,
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Score cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(SurfaceVariantDark)
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("الدقة البرمجية", color = Color.Gray, fontSize = 11.sp)
                        Text("${state.accuracy}%", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(SurfaceVariantDark)
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("الوقت المستهلك", color = Color.Gray, fontSize = 11.sp)
                        Text("${state.timeTakenSeconds} ثانية", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Reward displays (Points and BIT)
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F121E)),
                border = BorderStroke(1.dp, Color(0xFF1F2937))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("المكافآت المحصلة 🪙", color = Color.LightGray, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Star, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text("+${state.xpEarned} XP", color = NeonCyan, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                if (state.bonusXp > 0) {
                                    Text("+${state.bonusXp} بونص!", color = SuccessGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.MonetizationOn, contentDescription = null, tint = NeonYellow, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text("+${state.coinsEarned} BIT", color = NeonYellow, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                if (state.bonusCoins > 0) {
                                    Text("+${state.bonusCoins} بونص!", color = SuccessGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onContinue,
                colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("الانتقال للتحدي التالي 🚀", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }

            Spacer(modifier = Modifier.height(8.dp))

            TextButton(onClick = onBackDashboard, modifier = Modifier.fillMaxWidth()) {
                Text("الخروج لساحة المجموعات", color = Color.Gray, fontSize = 13.sp)
            }
        }
    }
}


// Confetti Canvas Particle Simulation
@Composable
fun ConfettiSimulation() {
    val particles = remember {
        List(40) {
            ConfettiParticle(
                x = Random.nextFloat(),
                y = Random.nextFloat() * -0.5f,
                color = listOf(NeonCyan, NeonPurple, NeonYellow, SuccessGreen, ErrorRed).random(),
                size = Random.nextFloat() * 12f + 8f,
                speed = Random.nextFloat() * 4f + 3f
            )
        }
    }

    var frameTime by remember { mutableStateOf(0L) }
    LaunchedEffect(Unit) {
        var lastTime = System.nanoTime()
        while (true) {
            withFrameNanos { time ->
                val dt = ((time - lastTime) / 1_000_000_000f).coerceIn(0f, 0.1f)
                lastTime = time

                particles.forEach { p ->
                    p.y += p.speed * 0.3f * dt
                    if (p.y > 1f) {
                        p.y = -0.1f
                        p.x = Random.nextFloat()
                    }
                }
                frameTime = time
            }
        }
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        val t = frameTime // Bind state read strictly to draw phase for no recomposition
        particles.forEach { p ->
            drawRect(
                color = p.color,
                topLeft = androidx.compose.ui.geometry.Offset(p.x * size.width, p.y * size.height),
                size = androidx.compose.ui.geometry.Size(p.size, p.size)
            )
        }
    }
}

private class ConfettiParticle(
    var x: Float,
    var y: Float,
    val color: Color,
    val size: Float,
    val speed: Float
)


// 5. THE FAILURE SCREEN (Hearts empty view)
@Composable
fun FailureView(
    state: ChallengeUIState.Failure,
    onRetry: () -> Unit,
    onBackDashboard: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .widthIn(max = 500.dp)
                .background(SurfaceDark, RoundedCornerShape(24.dp))
                .border(2.dp, ErrorRed, RoundedCornerShape(24.dp))
                .padding(24.dp)
        ) {
            Icon(
                imageVector = Icons.Default.FavoriteBorder,
                contentDescription = null,
                tint = ErrorRed,
                modifier = Modifier.size(72.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "لقد فرغت طاقتك! 💔",
                color = ErrorRed,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = state.errorSummary,
                color = Color.LightGray,
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Suggested lessons
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F121E))
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = "📚 دروس موصى بها من المعلم:",
                        color = NeonCyan,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Right,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    state.suggestedLessons.forEach { lesson ->
                        Text(
                            text = "• $lesson",
                            color = Color.White,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Right,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            val barmujViewModel = LocalBarmujViewModel.current
            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    barmujViewModel?.sendMessage("لقد فشلت في التحدي رقم ${state.levelIndex} بِلغة ${state.language}. هل يمكنك مساعدتي بنصائح للتحسن وفهم أين أخطأت؟")
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6B21A8)),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("اطلب المساعدة من بَرموج 🤖", color = Color.White, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(10.dp))

            TextButton(onClick = onBackDashboard, modifier = Modifier.fillMaxWidth()) {
                Text("الخروج لساحة المجموعات", color = Color.Gray, fontSize = 13.sp)
            }
        }
    }
}
