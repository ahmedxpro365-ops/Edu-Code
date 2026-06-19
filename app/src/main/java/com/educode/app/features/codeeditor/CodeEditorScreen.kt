package com.educode.app.features.codeeditor

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.viewinterop.AndroidView
import com.educode.app.ui.theme.*
import com.educode.app.features.barmujai.LocalBarmujViewModel

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun CodeEditorScreen(
    onTabClick: (com.educode.app.components.HubTab) -> Unit,
    onBackClick: () -> Unit,
    viewModel: CodeEditorViewModel = viewModel()
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val barmujViewModel = LocalBarmujViewModel.current

    // Local state for TextFieldValue to track selection/cursor position
    var textFieldValueState by remember {
        mutableStateOf(TextFieldValue(viewModel.codeText))
    }

    // Keep state in sync when ViewModel code text resets or changes programmatically
    LaunchedEffect(viewModel.codeText) {
        if (viewModel.codeText != textFieldValueState.text) {
            textFieldValueState = textFieldValueState.copy(text = viewModel.codeText)
        }
    }

    val activeColors = EditorThemeColors.forType(viewModel.themeType)

    // Layout dialog controls
    var showLanguageSheet by remember { mutableStateOf(false) }
    var showFilesSheet by remember { mutableStateOf(false) }
    var showSaveFileDialog by remember { mutableStateOf(false) }
    var newFileNameInput by remember { mutableStateOf("main.py") }
    var showSaveOutputDialog by remember { mutableStateOf(false) }
    var saveOutputNameInput by remember { mutableStateOf("output_log") }
    
    val unmatchedBrackets = remember(viewModel.codeText) {
        viewModel.getUnmatchedBracketErrors()
    }

    com.educode.app.components.HubShell(
        selectedTab = com.educode.app.components.HubTab.EDITOR,
        onTabClick = onTabClick,
        showShell = !viewModel.isFullScreen
    ) { extraPadding ->
        Scaffold(
            modifier = Modifier.padding(extraPadding),
            containerColor = activeColors.background,
        topBar = {
            if (!viewModel.isFullScreen) {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                "محرر الأكواد بريميوم",
                                color = if (viewModel.themeType == CodeThemeType.VS_CODE_LIGHT) Color.Black else NeonCyan,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "الملف الحالي: ${viewModel.currentFileName}",
                                color = if (viewModel.themeType == CodeThemeType.VS_CODE_LIGHT) Color.DarkGray else TextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(
                                Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = if (viewModel.themeType == CodeThemeType.VS_CODE_LIGHT) Color.Black else Color.White
                            )
                        }
                    },
                    actions = {
                        // Undo/Redo
                        IconButton(onClick = { viewModel.undo() }) {
                            Icon(Icons.Default.Undo, "Undo", tint = if (viewModel.themeType == CodeThemeType.VS_CODE_LIGHT) Color.Black else Color.White)
                        }
                        IconButton(onClick = { viewModel.redo() }) {
                            Icon(Icons.Default.Redo, "Redo", tint = if (viewModel.themeType == CodeThemeType.VS_CODE_LIGHT) Color.Black else Color.White)
                        }
                        
                        // Full Screen Toggle
                        IconButton(onClick = { viewModel.isFullScreen = true }) {
                            Icon(Icons.Filled.Fullscreen, "Fullscreen", tint = if (viewModel.themeType == CodeThemeType.VS_CODE_LIGHT) Color.Black else NeonCyan)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = if (viewModel.themeType == CodeThemeType.VS_CODE_LIGHT) Color(0xFFF3F3F3) else SurfaceDark
                    )
                )
            }
        },
        bottomBar = {
            if (!viewModel.isFullScreen && !viewModel.isTerminalOpen) {
                // Bottom tools for Barmuj AI integration
                Column(
                    modifier = Modifier
                        .background(if (viewModel.themeType == CodeThemeType.VS_CODE_LIGHT) Color(0xFFF3F3F3) else SurfaceDark)
                        .padding(8.dp)
                ) {
                    Text(
                        text = "التكامل مَع برموج الذكي AI 🤖",
                        color = if (viewModel.themeType == CodeThemeType.VS_CODE_LIGHT) Color.Black else NeonCyan,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        textAlign = TextAlign.Right
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { barmujViewModel?.sendMessage("اشرح لي هذا الكود بالتفصيل:\n\n```${viewModel.language.name.lowercase()}\n${viewModel.codeText}\n```") },
                            colors = ButtonDefaults.buttonColors(containerColor = SurfaceVariantDark),
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Text("اشرح الكود", fontSize = 11.sp, color = Color.White)
                        }
                        Button(
                            onClick = { barmujViewModel?.sendMessage("قم بعمل فحص وتحليل لبنية هذا الكود وهل هو فعال ونظيف:\n\n```${viewModel.language.name.lowercase()}\n${viewModel.codeText}\n```") },
                            colors = ButtonDefaults.buttonColors(containerColor = SurfaceVariantDark),
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Text("حلل الكود", fontSize = 11.sp, color = Color.White)
                        }
                        Button(
                            onClick = { barmujViewModel?.sendMessage("هل هناك أخطاء برمجية أو منطقية في هذا الكود؟ قم بايجادها وحلها مع كتابة الكود الصحيح:\n\n```${viewModel.language.name.lowercase()}\n${viewModel.codeText}\n```") },
                            colors = ButtonDefaults.buttonColors(containerColor = ErrorRed),
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Text("اكتشف الأخطاء", fontSize = 11.sp, color = Color.White)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "خيارات الملف والمحرر ⚙️",
                        color = if (viewModel.themeType == CodeThemeType.VS_CODE_LIGHT) Color.Black else NeonCyan,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        textAlign = TextAlign.Right
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Cyberpunk & VS Code Styled Premium Tab Switcher
                TabRow(
                    selectedTabIndex = viewModel.activeTab,
                    containerColor = if (viewModel.themeType == CodeThemeType.VS_CODE_LIGHT) Color(0xFFF3F3F3) else SurfaceDark,
                    contentColor = NeonCyan,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Tab(
                        selected = viewModel.activeTab == 0,
                        onClick = { viewModel.activeTab = 0 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Code,
                                    contentDescription = null,
                                    tint = if (viewModel.activeTab == 0) NeonCyan else Color.Gray,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    "محرر الأكواد 📝",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = if (viewModel.activeTab == 0) (if (viewModel.themeType == CodeThemeType.VS_CODE_LIGHT) Color.Black else NeonCyan) else Color.Gray
                                )
                            }
                        }
                    )
                    Tab(
                        selected = viewModel.activeTab == 1,
                        onClick = { viewModel.activeTab = 1 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    tint = if (viewModel.activeTab == 1) NeonCyan else Color.Gray,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    "منصة التشغيل Premium 🚀",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = if (viewModel.activeTab == 1) (if (viewModel.themeType == CodeThemeType.VS_CODE_LIGHT) Color.Black else NeonCyan) else Color.Gray
                                )
                            }
                        }
                    )
                }

                if (viewModel.activeTab == 0) {

                // Options control panel (Language, theme, font size etc)
                if (!viewModel.isFullScreen) {
                    EditorControlBar(
                        language = viewModel.language,
                        themeType = viewModel.themeType,
                        fontSize = viewModel.fontSize,
                        wordWrap = viewModel.wordWrap,
                        onLanguageClick = { showLanguageSheet = true },
                        onThemeClick = {
                            val nextTheme = when (viewModel.themeType) {
                                CodeThemeType.CYBERPUNK_PREMIUM -> CodeThemeType.NEON_GLOW
                                CodeThemeType.NEON_GLOW -> CodeThemeType.VS_CODE_DARK
                                CodeThemeType.VS_CODE_DARK -> CodeThemeType.VS_CODE_LIGHT
                                CodeThemeType.VS_CODE_LIGHT -> CodeThemeType.CYBERPUNK_PREMIUM
                            }
                            viewModel.setTheme(nextTheme)
                        },
                        onFontSizeToggle = {
                            viewModel.fontSize = if (viewModel.fontSize >= 20) 12 else viewModel.fontSize + 2
                        },
                        onWordWrapToggle = { viewModel.wordWrap = !viewModel.wordWrap },
                        onFilesClick = {
                            viewModel.loadSavedFilesList()
                            showFilesSheet = true
                        },
                        onSaveClick = {
                            newFileNameInput = viewModel.currentFileName
                            showSaveFileDialog = true
                        },
                        gutterBackground = activeColors.gutterBackground,
                        themeLight = viewModel.themeType == CodeThemeType.VS_CODE_LIGHT
                    )
                    
                    // Search & Replace drawer
                    SearchReplaceBar(
                        visible = viewModel.showSearchReplace,
                        searchQuery = viewModel.searchQuery,
                        replaceQuery = viewModel.replaceQuery,
                        resultsCount = viewModel.searchResultsCount,
                        onSearchChange = {
                            viewModel.searchQuery = it
                            viewModel.performSearchAndReplace()
                        },
                        onReplaceChange = { viewModel.replaceQuery = it },
                        onReplaceAll = { viewModel.replaceAllOccurrences() },
                        onToggleHide = { viewModel.showSearchReplace = false },
                        themeType = viewModel.themeType
                    )
                } else {
                    // Floating controls for Full Screen Mode
                    FullScreenControls(
                        onExitFullScreen = { viewModel.isFullScreen = false },
                        onRun = { viewModel.runCode() },
                        onUndo = { viewModel.undo() },
                        onRedo = { viewModel.redo() },
                        onSearchToggle = { viewModel.showSearchReplace = !viewModel.showSearchReplace },
                        isLight = viewModel.themeType == CodeThemeType.VS_CODE_LIGHT
                    )
                }

                // Editor secondary actions row (Format, Reset, Run, etc.)
                SecondaryToolbar(
                    onFormat = { viewModel.formatCode() },
                    onReset = { viewModel.resetToTemplate() },
                    onCopy = {
                        clipboardManager.setText(AnnotatedString(viewModel.codeText))
                        Toast.makeText(context, "تم نسخ الكود بنجاح", Toast.LENGTH_SHORT).show()
                    },
                    onRun = { viewModel.runCode() },
                    onSearchToggle = { viewModel.showSearchReplace = !viewModel.showSearchReplace },
                    onGoToLineToggle = { viewModel.showGoToLineDialog = true },
                    themeType = viewModel.themeType,
                    hasErrors = unmatchedBrackets.isNotEmpty()
                )

                // Error Indicator bar
                if (unmatchedBrackets.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(ErrorRed.copy(alpha = 0.2f))
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Error, "Error", tint = ErrorRed, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "تحذير: تم اكتشاف أخطاء في تكافؤ الأقواس المتعرجة أو المربعة!",
                                color = ErrorRed,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Code Suggestion Autocomplete Horizontal Bar
                val suggestions = remember(viewModel.language) {
                    LangTemplates.getSuggestions(viewModel.language)
                }
                CodeSuggestionBar(
                    suggestions = suggestions,
                    onSuggestionSelect = { insertText ->
                        val selection = textFieldValueState.selection
                        val text = textFieldValueState.text
                        val start = selection.start
                        val end = selection.end
                        val newText = text.substring(0, start) + insertText + text.substring(end)
                        val newSelection = start + insertText.length
                        textFieldValueState = TextFieldValue(
                            text = newText,
                            selection = androidx.compose.ui.text.TextRange(newSelection)
                        )
                        viewModel.updateCodeText(newText)
                    },
                    themeType = viewModel.themeType
                )

                // MAIN EDITOR CONTAINER
                val scrollState = rememberScrollState()
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .background(activeColors.background)
                ) {
                    // Left sidebar column: Line Numbers
                    val lines = remember(textFieldValueState.text) {
                        textFieldValueState.text.split("\n")
                    }
                    val lineCount = maxOf(1, lines.size)
                    
                    Column(
                        modifier = Modifier
                            .width(42.dp)
                            .fillMaxHeight()
                            .background(activeColors.gutterBackground)
                            .verticalScroll(scrollState)
                            .padding(vertical = 12.dp),
                        horizontalAlignment = Alignment.End
                    ) {
                        for (i in 1..lineCount) {
                            val isErrorLine = unmatchedBrackets.any { range ->
                                val offset = viewModel.calculateCharOffsetForLine(i)
                                offset in range
                            }
                            Text(
                                text = i.toString(),
                                style = TextStyle(
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = viewModel.fontSize.sp,
                                    fontWeight = if (isErrorLine) FontWeight.ExtraBold else FontWeight.Normal,
                                    textAlign = TextAlign.End,
                                    color = if (isErrorLine) ErrorRed else activeColors.gutterText
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(end = 6.dp, bottom = 2.dp)
                            )
                        }
                    }

                    // Main typing workspace Scrollable View
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f)
                            .verticalScroll(scrollState)
                            .horizontalScroll(rememberScrollState())
                            .padding(vertical = 12.dp, horizontal = 12.dp)
                    ) {
                        // Background match bracket highlights underlay
                        val bracketMatch = remember(textFieldValueState.selection, textFieldValueState.text) {
                            viewModel.getMatchedBracketsForCursor(textFieldValueState.selection.start)
                        }

                        if (bracketMatch != null) {
                            // Draws custom neon glows underneath matched brackets
                            viewModel.codeText.let { text ->
                                // Custom coloring done inside syntax highlighting
                            }
                        }

                        // BasicTextField with live custom syntax highlighting
                        BasicTextField(
                            value = textFieldValueState,
                            onValueChange = { newValue ->
                                // Handle enter key for auto indentation
                                if (newValue.text.length == textFieldValueState.text.length + 1 &&
                                    newValue.text.getOrNull(newValue.selection.start - 1) == '\n'
                                ) {
                                    val (adjustedText, newCursor) = viewModel.handleSmartIndent(newValue.selection.start - 1)
                                    val finalVal = TextFieldValue(
                                        text = adjustedText,
                                        selection = androidx.compose.ui.text.TextRange(newCursor)
                                    )
                                    textFieldValueState = finalVal
                                    viewModel.updateCodeText(adjustedText)
                                } else {
                                    textFieldValueState = newValue
                                    viewModel.updateCodeText(newValue.text)
                                }
                            },
                            textStyle = TextStyle(
                                fontFamily = FontFamily.Monospace,
                                fontSize = viewModel.fontSize.sp,
                                color = activeColors.text,
                                textDirection = TextDirection.Ltr
                            ),
                            cursorBrush = SolidColor(activeColors.cursor),
                            visualTransformation = CodeSyntaxHighlighter(viewModel.language, activeColors),
                            modifier = Modifier
                                .fillMaxSize()
                                .widthIn(min = 800.dp),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Text,
                                imeAction = ImeAction.Default
                            )
                        )
                    }
                }

                // SIMULATED TERMINAL OUTPUT DRAWER (Bottom Panel)
                AnimatedVisibility(
                    visible = viewModel.isTerminalOpen,
                    enter = slideInVertically { it },
                    exit = slideOutVertically { it }
                ) {
                    TerminalOutputPane(
                        output = viewModel.terminalOutput,
                        onClose = { viewModel.isTerminalOpen = false },
                        isRunning = viewModel.isRunningCode,
                        themeType = viewModel.themeType
                    )
                }
                } else {
                    PremiumCodeRunnerDashboard(
                        viewModel = viewModel,
                        onSaveOutputToggle = {
                            saveOutputNameInput = "output_${viewModel.currentFileName.substringBefore(".")}_log"
                            showSaveOutputDialog = true
                        },
                        activeColors = activeColors
                    )
                }
            }
        }
    }

    // Modal Sheet 1: Language selection
    if (showLanguageSheet) {
        AlertDialog(
            onDismissRequest = { showLanguageSheet = false },
            title = { Text("اختر لغة البرمجة 🌐", fontWeight = FontWeight.Bold) },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    ProgrammingLanguage.values().forEach { lang ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.setProgrammingLanguage(lang)
                                    showLanguageSheet = false
                                }
                                .padding(vertical = 12.dp, horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = lang.displayName,
                                fontSize = 16.sp,
                                fontWeight = if (viewModel.language == lang) FontWeight.Bold else FontWeight.Normal,
                                color = if (viewModel.language == lang) NeonCyan else Color.White
                            )
                            if (viewModel.language == lang) {
                                Spacer(modifier = Modifier.weight(1f))
                                Icon(Icons.Default.Check, "Selected", tint = NeonCyan)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showLanguageSheet = false }) {
                    Text("إغلاق", color = NeonCyan)
                }
            }
        )
    }

    // Modal Sheet 2: File Manager
    if (showFilesSheet) {
        AlertDialog(
            onDismissRequest = { showFilesSheet = false },
            title = { Text("مستكشف الملفات المحلية 📁", fontWeight = FontWeight.Bold) },
            text = {
                Column(modifier = Modifier.heightIn(max = 300.dp)) {
                    if (viewModel.savedFilesList.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("لا توجد ملفات محفوظة بعد. اضغط على حفظ لحفظ الكود الحالي.", textAlign = TextAlign.Center, color = TextSecondary)
                        }
                    } else {
                        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                            viewModel.savedFilesList.forEach { fileName ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable {
                                                viewModel.loadCodeFile(fileName)
                                                showFilesSheet = false
                                                Toast.makeText(context, "تم تحميل الملف $fileName", Toast.LENGTH_SHORT).show()
                                            }
                                    ) {
                                        Text(fileName, fontWeight = FontWeight.Bold, color = Color.White)
                                    }
                                    IconButton(
                                        onClick = {
                                            viewModel.deleteCodeFile(fileName)
                                            Toast.makeText(context, "تم حذف الملف $fileName", Toast.LENGTH_SHORT).show()
                                        }
                                    ) {
                                        Icon(Icons.Default.Delete, "Delete", tint = ErrorRed)
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showFilesSheet = false }) {
                    Text("إغلاق", color = NeonCyan)
                }
            }
        )
    }

    // Dialog 3: Save File Dialog
    if (showSaveFileDialog) {
        AlertDialog(
            onDismissRequest = { showSaveFileDialog = false },
            title = { Text("حفظ الملف باسم 💾", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("أدخل اسم الملف مع الامتداد المناسب (مثل main.py):", fontSize = 12.sp, modifier = Modifier.padding(bottom = 8.dp), color = TextSecondary)
                    OutlinedTextField(
                        value = newFileNameInput,
                        onValueChange = { newFileNameInput = it },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = NeonCyan
                        ),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newFileNameInput.trim().isNotEmpty()) {
                            val success = viewModel.saveCodeFile(newFileNameInput.trim())
                            if (success) {
                                Toast.makeText(context, "تم حفظ الملف بنجاح!", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "خطأ أثناء حفظ الملف", Toast.LENGTH_SHORT).show()
                            }
                            showSaveFileDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonCyan)
                ) {
                    Text("حفظ", color = Color.Black)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSaveFileDialog = false }) {
                    Text("إلغاء", color = TextSecondary)
                }
            }
        )
    }

    // Dialog 4: Go To Line Dialog
    if (viewModel.showGoToLineDialog) {
        var lineNumStr by remember { mutableStateOf("1") }
        AlertDialog(
            onDismissRequest = { viewModel.showGoToLineDialog = false },
            title = { Text("الانتقال إلى سطر 🎯", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("أدخل رقم السطر المراد القفز إليه:", fontSize = 12.sp, color = TextSecondary, modifier = Modifier.padding(bottom = 8.dp))
                    OutlinedTextField(
                        value = lineNumStr,
                        onValueChange = { lineNumStr = it },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, focusedBorderColor = NeonCyan),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val lineNum = lineNumStr.toIntOrNull() ?: 1
                        val totalLines = viewModel.codeText.split("\n").size
                        val targetLine = lineNum.coerceIn(1, totalLines)
                        val offset = viewModel.calculateCharOffsetForLine(targetLine)
                        textFieldValueState = textFieldValueState.copy(
                            selection = androidx.compose.ui.text.TextRange(offset)
                        )
                        viewModel.showGoToLineDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonCyan)
                ) {
                    Text("انتقال", color = Color.Black)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.showGoToLineDialog = false }) {
                    Text("إلغاء", color = TextSecondary)
                }
            }
        )
    }

    // Dialog 5: Save Output Dialog
    if (showSaveOutputDialog) {
        AlertDialog(
            onDismissRequest = { showSaveOutputDialog = false },
            title = { Text("حفظ مخرجات منصة التشغيل 💾", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("أدخل اسم ملف التقرير النصي المراد حفظه فيه:", fontSize = 12.sp, modifier = Modifier.padding(bottom = 8.dp), color = TextSecondary)
                    OutlinedTextField(
                        value = saveOutputNameInput,
                        onValueChange = { saveOutputNameInput = it },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = NeonCyan
                        ),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (saveOutputNameInput.trim().isNotEmpty()) {
                            val success = viewModel.saveConsoleOutput(saveOutputNameInput.trim())
                            if (success) {
                                Toast.makeText(context, "تم حفظ المخرجات بنجاح في ملف $saveOutputNameInput.txt!", Toast.LENGTH_LONG).show()
                            } else {
                                Toast.makeText(context, "فشل حفظ المخرجات المحددة", Toast.LENGTH_SHORT).show()
                            }
                            showSaveOutputDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonCyan)
                ) {
                    Text("حفظ الملف", color = Color.Black)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSaveOutputDialog = false }) {
                    Text("إلغاء", color = TextSecondary)
                }
            }
        )
    }
  }
}

@Composable
fun EditorControlBar(
    language: ProgrammingLanguage,
    themeType: CodeThemeType,
    fontSize: Int,
    wordWrap: Boolean,
    onLanguageClick: () -> Unit,
    onThemeClick: () -> Unit,
    onFontSizeToggle: () -> Unit,
    onWordWrapToggle: () -> Unit,
    onFilesClick: () -> Unit,
    onSaveClick: () -> Unit,
    gutterBackground: Color,
    themeLight: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp)
            .background(gutterBackground)
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Local files management
        IconButton(onClick = onFilesClick) {
            Icon(Icons.Default.FolderOpen, "Files", tint = if (themeLight) Color.Black else NeonCyan)
        }

        IconButton(onClick = onSaveClick) {
            Icon(Icons.Default.Save, "Save", tint = if (themeLight) Color.Black else Color.White)
        }

        VerticalDivider(modifier = Modifier.height(24.dp), color = Color.Gray.copy(alpha = 0.5f))

        // Language Select Button
        Button(
            onClick = onLanguageClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = if (themeLight) Color(0xFFE0E0E0) else SurfaceVariantDark
            ),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
            modifier = Modifier.height(36.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(language.displayName, fontSize = 11.sp, color = if (themeLight) Color.Black else Color.White)
                Spacer(modifier = Modifier.width(4.dp))
                Icon(Icons.Default.ArrowDropDown, null, tint = if (themeLight) Color.Black else NeonCyan, modifier = Modifier.size(16.dp))
            }
        }

        // Theme Toggle Button
        Button(
            onClick = onThemeClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = if (themeLight) Color(0xFFE0E0E0) else SurfaceVariantDark
            ),
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
            modifier = Modifier.height(36.dp)
        ) {
            Text(themeType.displayName, fontSize = 10.sp, color = if (themeLight) Color.Black else Color.White)
        }

        Spacer(modifier = Modifier.weight(1f))

        // Font Size control
        IconButton(onClick = onFontSizeToggle) {
            Icon(Icons.Default.FormatSize, "Font Size", tint = if (themeLight) Color.Black else Color.White)
        }

        // Word Wrap toggle
        IconButton(onClick = onWordWrapToggle) {
            Icon(
                Icons.Default.WrapText,
                "Word Wrap",
                tint = if (wordWrap) (if (themeLight) Color.Black else NeonCyan) else Color.Gray
            )
        }
    }
}

@Composable
fun SecondaryToolbar(
    onFormat: () -> Unit,
    onReset: () -> Unit,
    onCopy: () -> Unit,
    onRun: () -> Unit,
    onSearchToggle: () -> Unit,
    onGoToLineToggle: () -> Unit,
    themeType: CodeThemeType,
    hasErrors: Boolean
) {
    val isLight = themeType == CodeThemeType.VS_CODE_LIGHT
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (isLight) Color(0xFFE6E6E6) else Color(0xFF131726))
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Format button
        Button(
            onClick = onFormat,
            colors = ButtonDefaults.buttonColors(containerColor = NeonPurple.copy(alpha = 0.2f)),
            border = BorderStroke(1.dp, NeonPurple),
            contentPadding = PaddingValues(horizontal = 8.dp),
            modifier = Modifier.height(32.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.FormatAlignLeft, "", tint = NeonPurple, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("تنسيق الكود", fontSize = 10.sp, color = if (isLight) Color.Black else Color.White)
            }
        }

        // Reset Template code
        IconButton(onClick = onReset, modifier = Modifier.size(32.dp)) {
            Icon(Icons.Default.Refresh, "Reset to Template", tint = Color.Gray, modifier = Modifier.size(18.dp))
        }

        // Copy button
        IconButton(onClick = onCopy, modifier = Modifier.size(32.dp)) {
            Icon(Icons.Default.ContentCopy, "Copy Code", tint = if (isLight) Color.Black else Color.White, modifier = Modifier.size(18.dp))
        }

        // Search toggle
        IconButton(onClick = onSearchToggle, modifier = Modifier.size(32.dp)) {
            Icon(Icons.Default.Search, "Search & Replace", tint = if (isLight) Color.Black else NeonCyan, modifier = Modifier.size(18.dp))
        }

        // Go To Line button
        IconButton(onClick = onGoToLineToggle, modifier = Modifier.size(32.dp)) {
            Icon(Icons.Default.FilterList, "Go To Line", tint = if (isLight) Color.Black else Color.White, modifier = Modifier.size(18.dp))
        }

        Spacer(modifier = Modifier.weight(1f))

        // RUN CODE ACTION BUTTON
        Button(
            onClick = onRun,
            colors = ButtonDefaults.buttonColors(
                containerColor = if (hasErrors) ErrorRed else SuccessGreen
            ),
            contentPadding = PaddingValues(horizontal = 16.dp),
            modifier = Modifier
                .height(34.dp)
                .shadow(
                    4.dp,
                    shape = RoundedCornerShape(8.dp),
                    spotColor = if (hasErrors) ErrorRed else SuccessGreen
                )
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.PlayArrow, "", tint = Color.Black, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("تشغيل الكود", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp)
            }
        }
    }
}

@Composable
fun FullScreenControls(
    onExitFullScreen: () -> Unit,
    onRun: () -> Unit,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    onSearchToggle: () -> Unit,
    isLight: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (isLight) Color(0xFFF3F3F3) else SurfaceDark)
            .padding(horizontal = 8.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onExitFullScreen) {
            Icon(Icons.Default.FullscreenExit, "Exit Fullscreen", tint = NeonCyan)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            IconButton(onClick = onUndo) {
                Icon(Icons.Default.Undo, "Undo", tint = if (isLight) Color.Black else Color.White)
            }
            IconButton(onClick = onRedo) {
                Icon(Icons.Default.Redo, "Redo", tint = if (isLight) Color.Black else Color.White)
            }
            IconButton(onClick = onSearchToggle) {
                Icon(Icons.Default.Search, "Search", tint = if (isLight) Color.Black else NeonCyan)
            }
            IconButton(onClick = onRun) {
                Icon(Icons.Default.PlayArrow, "Run", tint = SuccessGreen)
            }
        }
    }
}

@Composable
fun SearchReplaceBar(
    visible: Boolean,
    searchQuery: String,
    replaceQuery: String,
    resultsCount: Int,
    onSearchChange: (String) -> Unit,
    onReplaceChange: (String) -> Unit,
    onReplaceAll: () -> Unit,
    onToggleHide: () -> Unit,
    themeType: CodeThemeType
) {
    AnimatedVisibility(
        visible = visible,
        enter = expandVertically(),
        exit = shrinkVertically()
    ) {
        val isLight = themeType == CodeThemeType.VS_CODE_LIGHT
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isLight) Color(0xFFEBEBEB) else SurfaceVariantDark
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = onSearchChange,
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("بحث عن كلمة...", fontSize = 12.sp) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = if (isLight) Color.Black else Color.White,
                            unfocusedTextColor = if (isLight) Color.Black else Color.White
                        )
                    )
                    Text(
                        "$resultsCount مطابقة",
                        fontSize = 11.sp,
                        color = if (resultsCount > 0) SuccessGreen else Color.Gray,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = replaceQuery,
                        onValueChange = onReplaceChange,
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("استبدال بـ...", fontSize = 12.sp) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = if (isLight) Color.Black else Color.White,
                            unfocusedTextColor = if (isLight) Color.Black else Color.White
                        )
                    )
                    Button(
                        onClick = onReplaceAll,
                        colors = ButtonDefaults.buttonColors(containerColor = NeonPurple),
                        enabled = searchQuery.isNotEmpty(),
                        modifier = Modifier.height(44.dp)
                    ) {
                        Text("استبدال الكل", fontSize = 11.sp, color = Color.White)
                    }
                    IconButton(onClick = onToggleHide) {
                        Icon(Icons.Default.Close, "Hide Search", tint = if (isLight) Color.Black else Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun CodeSuggestionBar(
    suggestions: List<String>,
    onSuggestionSelect: (String) -> Unit,
    themeType: CodeThemeType
) {
    if (suggestions.isEmpty()) return
    val isLight = themeType == CodeThemeType.VS_CODE_LIGHT

    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (isLight) Color(0xFFEBEBEB) else Color(0xFF0A0D18))
            .padding(vertical = 4.dp, horizontal = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        items(suggestions) { item ->
            Box(
                modifier = Modifier
                    .background(
                        if (isLight) Color(0xFFDCDCDC) else SurfaceDark,
                        shape = RoundedCornerShape(6.dp)
                    )
                    .border(
                        1.dp,
                        if (isLight) Color.LightGray else Color.Gray.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(6.dp)
                    )
                    .clickable { onSuggestionSelect(item) }
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(
                    text = item,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    color = if (isLight) Color.Black else NeonCyan,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun TerminalOutputPane(
    output: String,
    onClose: () -> Unit,
    isRunning: Boolean,
    themeType: CodeThemeType
) {
    val isLight = themeType == CodeThemeType.VS_CODE_LIGHT
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isLight) Color(0xFFEBEBEB) else Color(0xFF030712)
        ),
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        border = BorderStroke(1.dp, if (isLight) Color.DarkGray else NeonCyan.copy(alpha = 0.4f))
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(if (isLight) Color(0xFFD5D5D5) else Color(0xFF0D111D))
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(8.dp).background(Color.Red, CircleShape))
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(modifier = Modifier.size(8.dp).background(Color.Yellow, CircleShape))
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(modifier = Modifier.size(8.dp).background(Color.Green, CircleShape))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        "وحدة التحقق والتشغيل المحوسبة (Terminal) 📟",
                        color = if (isLight) Color.Black else NeonCyan,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                IconButton(onClick = onClose, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Close, "Close", tint = if (isLight) Color.Black else Color.White, modifier = Modifier.size(16.dp))
                }
            }

            // Output container
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(if (isLight) Color.White else Color(0xFF030712))
                    .padding(12.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                if (isRunning) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(
                            color = NeonCyan,
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            "جاري تشغيل الكود في البيئة المحاكاة العميقة...",
                            color = if (isLight) Color.Black else Color.White,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp
                        )
                    }
                } else {
                    Text(
                        text = output,
                        color = if (isLight) Color.Black else SuccessGreen,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        modifier = Modifier.fillMaxWidth(),
                        style = TextStyle(textDirection = TextDirection.Ltr)
                    )
                }
            }
        }
    }
}

@Composable
fun WebLivePreview(
    htmlCode: String,
    cssCode: String,
    deviceMode: String,
    themeLight: Boolean,
    modifier: Modifier = Modifier
) {
    val combinedHtml = remember(htmlCode, cssCode) {
        if (cssCode.trim().isNotEmpty() || htmlCode.trim().isEmpty()) {
            val styleBlock = "<style>\n$cssCode\n</style>"
            if (htmlCode.contains("</head>")) {
                htmlCode.replace("</head>", "$styleBlock\n</head>")
            } else {
                "<html><head>$styleBlock</head><body style='margin:16px; font-family:sans-serif;'>\n$htmlCode\n</body></html>"
            }
        } else {
            htmlCode
        }
    }

    val targetWidthModifier = when (deviceMode) {
        "mobile" -> Modifier.width(360.dp).fillMaxHeight()
        "tablet" -> Modifier.width(620.dp).fillMaxHeight()
        else -> Modifier.fillMaxSize()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(if (themeLight) Color(0xFFF3F3F3) else Color(0xFF070913)),
        contentAlignment = Alignment.TopCenter
    ) {
        Box(
            modifier = targetWidthModifier
                .padding(8.dp)
                .clip(RoundedCornerShape(12.dp))
                .border(2.dp, if (themeLight) Color.DarkGray else NeonCyan.copy(alpha = 0.8f), RoundedCornerShape(12.dp))
                .shadow(6.dp, RoundedCornerShape(12.dp))
                .background(Color.White)
        ) {
            AndroidView(
                factory = { context ->
                    android.webkit.WebView(context).apply {
                        settings.javaScriptEnabled = true
                        settings.domStorageEnabled = true
                        webViewClient = android.webkit.WebViewClient()
                        setBackgroundColor(if (themeLight) android.graphics.Color.WHITE else android.graphics.Color.parseColor("#0F121E"))
                    }
                },
                update = { webView ->
                    webView.loadDataWithBaseURL("https://local-runner-sandbox", combinedHtml, "text/html", "UTF-8", null)
                },
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PremiumCodeRunnerDashboard(
    viewModel: CodeEditorViewModel,
    onSaveOutputToggle: () -> Unit,
    activeColors: EditorThemeColors
) {
    val isLight = viewModel.themeType == CodeThemeType.VS_CODE_LIGHT
    val isWebLanguage = viewModel.language == ProgrammingLanguage.HTML || viewModel.language == ProgrammingLanguage.CSS

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(if (isLight) Color(0xFFF4F4F4) else Color(0xFF0F121E))
            .verticalScroll(rememberScrollState())
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // SECTION 1: EXECUTION METRICS DASHBOARD CARD
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = if (isLight) Color.White else SurfaceDark
            ),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, if (isLight) Color.LightGray else Color.Gray.copy(alpha = 0.2f))
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "مؤشرات أداء منصة التشغيل 📊",
                    color = if (isLight) Color.Black else NeonCyan,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Status Metric
                    val statusColor = when {
                        viewModel.executionStatus.contains("نجاح") -> SuccessGreen
                        viewModel.executionStatus.contains("خطأ") -> ErrorRed
                        viewModel.executionStatus.contains("جاري") -> NeonCyan
                        else -> Color.Gray
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(statusColor.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                            .border(1.dp, statusColor.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                            .padding(8.dp)
                    ) {
                        Column {
                            Text("حالة التنفيذ", fontSize = 10.sp, color = if (isLight) Color.DarkGray else TextSecondary)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(viewModel.executionStatus, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = statusColor)
                        }
                    }

                    // Execution Time Metric
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(NeonCyan.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                            .border(1.dp, NeonCyan.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                            .padding(8.dp)
                    ) {
                        Column {
                            Text("وقت التنفيذ", fontSize = 10.sp, color = if (isLight) Color.DarkGray else TextSecondary)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text("${viewModel.executionTimeMs / 1000.0} ثانية", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (isLight) Color.Black else Color.White)
                        }
                    }

                    // Error Count Metric
                    val errCardBorder = if (viewModel.errorCount > 0) ErrorRed else Color.Gray.copy(alpha = 0.2f)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(if (viewModel.errorCount > 0) ErrorRed.copy(alpha = 0.15f) else Color.Transparent, RoundedCornerShape(8.dp))
                            .border(1.dp, errCardBorder, RoundedCornerShape(8.dp))
                            .padding(8.dp)
                    ) {
                        Column {
                            Text("الأخطاء", fontSize = 10.sp, color = if (isLight) Color.DarkGray else TextSecondary)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text("${viewModel.errorCount} خطأ", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (viewModel.errorCount > 0) ErrorRed else Color.Gray)
                        }
                    }

                    // Warning Count Metric
                    val warningColor = Color(0xFFFBBF24)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(if (viewModel.warningCount > 0) warningColor.copy(alpha = 0.15f) else Color.Transparent, RoundedCornerShape(8.dp))
                            .border(1.dp, if (viewModel.warningCount > 0) warningColor else Color.Gray.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                            .padding(8.dp)
                    ) {
                        Column {
                            Text("التحذيرات", fontSize = 10.sp, color = if (isLight) Color.DarkGray else TextSecondary)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text("${viewModel.warningCount} تنبيه", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (viewModel.warningCount > 0) warningColor else Color.Gray)
                        }
                    }
                }
            }
        }

        // SECTION 2: OPERATIONS CONTROL BUTTONS
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Button(
                onClick = { viewModel.runCode() },
                enabled = !viewModel.isRunningCode,
                colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                modifier = Modifier.weight(1.3f),
                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.Black)
                Spacer(modifier = Modifier.width(4.dp))
                Text("تشغيل", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            }

            Button(
                onClick = { viewModel.stopCode() },
                enabled = viewModel.isRunningCode,
                colors = ButtonDefaults.buttonColors(containerColor = ErrorRed),
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.Stop, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                Spacer(modifier = Modifier.width(4.dp))
                Text("إيقاف", fontSize = 11.sp, color = Color.White)
            }

            Button(
                onClick = { viewModel.restartCode() },
                colors = ButtonDefaults.buttonColors(containerColor = NeonPurple),
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                Spacer(modifier = Modifier.width(4.dp))
                Text("إعادة", fontSize = 11.sp, color = Color.White)
            }

            Button(
                onClick = { viewModel.clearConsole() },
                colors = ButtonDefaults.buttonColors(containerColor = SurfaceVariantDark),
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.DeleteSweep, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                Spacer(modifier = Modifier.width(4.dp))
                Text("مسح", fontSize = 11.sp, color = Color.White)
            }

            Button(
                onClick = onSaveOutputToggle,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                modifier = Modifier.weight(1.2f),
                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                Spacer(modifier = Modifier.width(4.dp))
                Text("حفظ مخرج", fontSize = 11.sp, color = Color.White)
            }
        }

        // SECTION 3: RESPONSIVE WEB PREVIEW / RETRO TERMINAL OUTPUT PANEL
        if (isWebLanguage) {
            // Devices frame size toggles for Responsive web design
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = if (isLight) Color(0xFFECECEC) else Color(0xFF131729))
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "حجم شاشة محاكاة الويب 🌐",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isLight) Color.Black else Color.White
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            listOf("mobile" to "هاتف 📱", "tablet" to "تابلت 📟", "full" to "تلقائي 💻").forEach { (mode, title) ->
                                val active = viewModel.webPreviewDeviceMode == mode
                                Text(
                                    text = title,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (active) NeonCyan else Color.Gray,
                                    modifier = Modifier
                                        .background(
                                            if (active) NeonCyan.copy(alpha = 0.15f) else Color.Transparent,
                                            RoundedCornerShape(6.dp)
                                        )
                                        .border(
                                            1.dp,
                                            if (active) NeonCyan else Color.Gray.copy(alpha = 0.3f),
                                            RoundedCornerShape(6.dp)
                                        )
                                        .clickable { viewModel.webPreviewDeviceMode = mode }
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Embedded Responsive View of WebView
                    Box(modifier = Modifier.height(280.dp).fillMaxWidth()) {
                        val htmlCode = if (viewModel.language == ProgrammingLanguage.HTML) viewModel.codeText else ""
                        val cssCode = if (viewModel.language == ProgrammingLanguage.CSS) viewModel.codeText else ""
                        WebLivePreview(
                            htmlCode = htmlCode,
                            cssCode = cssCode,
                            deviceMode = viewModel.webPreviewDeviceMode,
                            themeLight = isLight
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Secondary Output Console capturing logs inside frame
                    Text("سجل أحداث المتصفح الداخلي console.log 🎙️", fontSize = 10.sp, color = Color.Gray, modifier = Modifier.padding(bottom = 4.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isLight) Color.White else Color(0xFF04060E))
                            .padding(6.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Text(
                            text = if (viewModel.language == ProgrammingLanguage.HTML) "v8::JS Console: Serving Document HMR..." else "CSS Render: Dynamic stylesheet combined in layout.",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            color = if (isLight) Color.Black else SuccessGreen
                        )
                    }
                }
            }
        } else {
            // TERMINAL CLIENT WORKSPACE FOR GENERAL CODING LANGUAGES
            Text("لوحة التشغيل والمدخلات والمخرجات (Interactive CLI Terminal) 📟", fontSize = 11.sp, color = if (isLight) Color.Black else NeonCyan, fontWeight = FontWeight.Bold)
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .border(2.dp, if (isLight) Color.LightGray else NeonCyan.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                    .background(if (isLight) Color.White else Color(0xFF04060E))
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(12.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        if (viewModel.consoleLogsList.isEmpty()) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text(
                                    "انقر على تشغيل للبدء في تجميع وتنفيذ الأكواد المكتوبة...",
                                    color = Color.Gray,
                                    fontSize = 11.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                viewModel.consoleLogsList.forEach { log ->
                                    val itemColor = when (log.type) {
                                        ConsoleLogType.STDOUT -> if (isLight) Color.DarkGray else Color(0xFFE2E8F0)
                                        ConsoleLogType.STDERR -> ErrorRed
                                        ConsoleLogType.INFO -> NeonCyan
                                        ConsoleLogType.DEBUG -> Color(0xFFD8B4FE)
                                        ConsoleLogType.INPUT_PROMPT -> SuccessGreen
                                        ConsoleLogType.USER_INPUT -> Color(0xFFFDE047)
                                        ConsoleLogType.COMPILE_SUCCESS -> Color(0xFFFBBF24)
                                        ConsoleLogType.COMPILE_FAILED -> ErrorRed
                                    }
                                    val tag = when (log.type) {
                                        ConsoleLogType.STDOUT -> "[STDOUT]"
                                        ConsoleLogType.STDERR -> "[STDERR]"
                                        ConsoleLogType.INFO -> "[SYSTEM]"
                                        ConsoleLogType.DEBUG -> "[DEBUG]"
                                        ConsoleLogType.INPUT_PROMPT -> "[PROMPT]"
                                        ConsoleLogType.USER_INPUT -> "[INPUT]"
                                        ConsoleLogType.COMPILE_SUCCESS -> "[SUCCESS]"
                                        ConsoleLogType.COMPILE_FAILED -> "[C_ERROR]"
                                    }

                                    Row(modifier = Modifier.fillMaxWidth()) {
                                        Text(
                                            text = "$tag  ",
                                            color = itemColor.copy(alpha = 0.7f),
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = log.text,
                                            color = itemColor,
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 11.sp,
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // PYTHON INTERACTIVE USER INPUT BAR
                    if (viewModel.isWaitingForUserInput) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(if (isLight) Color(0xFFECECEC) else Color(0xFF0B0E1E))
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = viewModel.pendingInputPrompt ?: "أدخل قيمة: ",
                                color = SuccessGreen,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = viewModel.userInputValueInputState,
                                onValueChange = { viewModel.userInputValueInputState = it },
                                placeholder = { Text("اكتب القيمة هنا...", fontSize = 10.sp, color = Color.Gray) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = if (isLight) Color.Black else Color.White,
                                    unfocusedTextColor = if (isLight) Color.Black else Color.White,
                                    focusedBorderColor = SuccessGreen
                                ),
                                singleLine = true,
                                modifier = Modifier.width(150.dp),
                                textStyle = TextStyle(fontSize = 11.sp)
                            )
                            Button(
                                onClick = {
                                    if (viewModel.userInputValueInputState.trim().isNotEmpty()) {
                                        viewModel.submitUserInput(viewModel.userInputValueInputState.trim())
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text("إرسال", color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}
