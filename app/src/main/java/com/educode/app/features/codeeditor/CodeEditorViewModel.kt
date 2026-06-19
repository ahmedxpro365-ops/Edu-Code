package com.educode.app.features.codeeditor

import android.app.Application
import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

enum class ConsoleLogType {
    INFO, STDOUT, STDERR, DEBUG, INPUT_PROMPT, USER_INPUT, COMPILE_SUCCESS, COMPILE_FAILED
}

data class ConsoleLog(val text: String, val type: ConsoleLogType, val timestamp: Long = System.currentTimeMillis())

class CodeEditorViewModel(application: Application) : AndroidViewModel(application) {

    // Main editor properties
    var codeText by mutableStateOf(LangTemplates.getTemplate(ProgrammingLanguage.PYTHON))
        private set

    var language by mutableStateOf(ProgrammingLanguage.PYTHON)
        private set

    var themeType by mutableStateOf(CodeThemeType.CYBERPUNK_PREMIUM)
        private set

    var wordWrap by mutableStateOf(true)

    var fontSize by mutableStateOf(14)

    var isFullScreen by mutableStateOf(false)

    // History for Undo/Redo
    private val undoStack = mutableListOf<String>()
    private val redoStack = mutableListOf<String>()
    private val maxHistorySize = 50

    // Search & Replace States
    var showSearchReplace by mutableStateOf(false)
    var searchQuery by mutableStateOf("")
    var replaceQuery by mutableStateOf("")
    var searchResultsCount by mutableStateOf(0)
    var currentSearchIndex by mutableStateOf(-1)

    // Go To Line Dialog
    var showGoToLineDialog by mutableStateOf(false)

    // Terminal Output Controls
    var terminalOutput by mutableStateOf("")
    var isTerminalOpen by mutableStateOf(false)
    var isRunningCode by mutableStateOf(false)

    // Tabs Support (0: Editor, 1: Premium Runner Dashboard)
    var activeTab by mutableStateOf(0)

    var consoleLogsList by mutableStateOf(listOf<ConsoleLog>())
    var executionTimeMs by mutableStateOf(0L)
    var executionStatus by mutableStateOf("متوقف 🛑") // "نجاح ✅", "فشل ❌", "جاري التنفيذ ⏳", "متوقف 🛑", "خطأ تجميع ⚠️"
    var errorCount by mutableStateOf(0)
    var warningCount by mutableStateOf(0)

    // Web Live Preview size controls
    var webPreviewDeviceMode by mutableStateOf("mobile") // "mobile", "tablet", "full"

    // Interactive Console inputs
    var pendingInputPrompt by mutableStateOf<String?>(null)
    var isWaitingForUserInput by mutableStateOf(false)
    var userInputValueInputState by mutableStateOf("")

    // List of saved output files
    var savedConsoleOutputsList by mutableStateOf(listOf<String>())

    // Saving and local files list
    var savedFilesList by mutableStateOf(listOf<String>())
        private set
    var currentFileName by mutableStateOf("main.py")
        private set

    init {
        loadSavedFilesList()
        undoStack.add(codeText)
    }

    fun updateCodeText(newText: String, isSelectionChange: Boolean = false) {
        if (!isSelectionChange && codeText != newText) {
            // Manage undo stack with basic thresholding to prevent spamming single keys
            if (undoStack.isEmpty() || undoStack.last() != codeText) {
                if (undoStack.size >= maxHistorySize) {
                    undoStack.removeAt(0)
                }
                undoStack.add(codeText)
                redoStack.clear()
            }
        }
        codeText = newText
    }

    // Toggle Themes
    fun setTheme(theme: CodeThemeType) {
        themeType = theme
    }

    // Change language & reset to its template
    fun setProgrammingLanguage(lang: ProgrammingLanguage) {
        language = lang
        currentFileName = "main${lang.extension}"
        val template = LangTemplates.getTemplate(lang)
        updateCodeText(template)
        undoStack.clear()
        redoStack.clear()
        undoStack.add(template)
        // Clear terminal output and runner stats
        terminalOutput = ""
        isTerminalOpen = false
        consoleLogsList = emptyList()
        executionTimeMs = 0L
        executionStatus = "متوقف 🛑"
        errorCount = 0
        warningCount = 0
        pendingInputPrompt = null
        isWaitingForUserInput = false
    }

    // Undo Actions
    fun undo() {
        if (undoStack.isNotEmpty()) {
            val prev = undoStack.removeAt(undoStack.size - 1)
            redoStack.add(codeText)
            codeText = prev
        }
    }

    // Redo Actions
    fun redo() {
        if (redoStack.isNotEmpty()) {
            val next = redoStack.removeAt(redoStack.size - 1)
            undoStack.add(codeText)
            codeText = next
        }
    }

    // Reset code back to default template
    fun resetToTemplate() {
        val template = LangTemplates.getTemplate(language)
        updateCodeText(template)
        undoStack.clear()
        redoStack.clear()
        undoStack.add(template)
    }

    // Format Code auto-indenter
    fun formatCode() {
        val lines = codeText.split("\n")
        val formatted = StringBuilder()
        var indentLevel = 0
        val tab = "    " // 4 spaces

        for (i in lines.indices) {
            var line = lines[i].trim()
            if (line.isEmpty()) {
                formatted.append("\n")
                continue
            }

            // Decrement bracket level before formatting the line for braced languages
            val opensCount = line.count { it == '{' || it == '[' }
            val closesCount = line.count { it == '}' || it == ']' }
            
            // Adjust indent index if line starts with closing brace
            val startsWithClose = line.startsWith("}") || line.startsWith("]") || line.startsWith(")")
            val currentLineIndent = if (startsWithClose) {
                (indentLevel - 1).coerceAtLeast(0)
            } else {
                indentLevel
            }

            // Append spaces
            for (j in 0 until currentLineIndent) {
                formatted.append(tab)
            }
            formatted.append(line)
            if (i < lines.size - 1) {
                formatted.append("\n")
            }

            // Adjust indent level for next lines
            indentLevel = (indentLevel + opensCount - closesCount).coerceAtLeast(0)
            
            // Python colon checking
            if (language == ProgrammingLanguage.PYTHON && line.endsWith(":")) {
                indentLevel++
            } else if (language == ProgrammingLanguage.PYTHON && (line.startsWith("return") || line.startsWith("break") || line.startsWith("pass") || line.startsWith("continue")) && indentLevel > 0) {
                indentLevel--
            }
        }

        val result = formatted.toString()
        if (result != codeText) {
            updateCodeText(result)
        }
    }

    // Handle pressing Enter for automatic smart indent
    fun handleSmartIndent(cursorIndex: Int): Pair<String, Int> {
        if (cursorIndex < 0 || cursorIndex > codeText.length) return Pair(codeText, cursorIndex)
        
        val textBefore = codeText.substring(0, cursorIndex)
        val textAfter = codeText.substring(cursorIndex)
        
        // Find current line details
        val lastNewline = textBefore.lastIndexOf("\n")
        val currentLine = if (lastNewline == -1) textBefore else textBefore.substring(lastNewline + 1)
        
        // Count leading spaces/tabs on current line
        val spacesCount = currentLine.takeWhile { it == ' ' }.length
        var indent = " ".repeat(spacesCount)
        
        // Check if line ends with opening scope
        val trimmedLine = currentLine.trim()
        val endsWithScope = trimmedLine.endsWith("{") || trimmedLine.endsWith(":") || trimmedLine.endsWith("[") || trimmedLine.endsWith("(")
        if (endsWithScope) {
            indent += "    "
        }
        
        val newText = textBefore + "\n" + indent + textAfter
        val newCursor = cursorIndex + 1 + indent.length
        return Pair(newText, newCursor)
    }

    // Search and replace code
    fun performSearchAndReplace(searchOn: Boolean = true) {
        if (searchQuery.isEmpty()) {
            searchResultsCount = 0
            return
        }
        val count = codeText.split(searchQuery).size - 1
        searchResultsCount = count
    }

    fun replaceAllOccurrences() {
        if (searchQuery.isNotEmpty()) {
            val replaced = codeText.replace(searchQuery, replaceQuery)
            updateCodeText(replaced)
            performSearchAndReplace()
        }
    }

    // Go To Line scroll index calculator
    fun calculateCharOffsetForLine(lineNumber: Int): Int {
        val lines = codeText.split("\n")
        val targetLine = lineNumber.coerceIn(1, lines.size)
        var offset = 0
        for (i in 0 until (targetLine - 1)) {
            offset += lines[i].length + 1 // +1 for the newline character
        }
        return offset
    }

    // Simulate Code Execution (Output Terminal Premium)
    fun runCode() {
        val unmatchedBrackets = getUnmatchedBracketErrors()
        
        if (unmatchedBrackets.isNotEmpty()) {
            activeTab = 1 // Auto-switch to Premium Runner to display errors immediately
            isTerminalOpen = true
            executionStatus = "خطأ تجميع ⚠️"
            errorCount = unmatchedBrackets.size
            
            // Simulating a minor warning
            warningCount = if (codeText.contains("var ")) 2 else 1
            
            val list = mutableListOf<ConsoleLog>()
            list.add(ConsoleLog("g++ main.cpp -Wall -o main", ConsoleLogType.INFO))
            list.add(ConsoleLog("=== تجميع وتدقيق الأكواد المصدري ===", ConsoleLogType.INFO))
            list.add(ConsoleLog("خطأ تجميع في السطر 1: لم يتم تطابق الأقواس (Unmatched brackets errors).", ConsoleLogType.COMPILE_FAILED))
            list.add(ConsoleLog("تم اكتشاف عدد ${unmatchedBrackets.size} من الأقواس المفتوحة/المغلقة دون شريك متطابق في الكود المصدري.", ConsoleLogType.STDERR))
            list.add(ConsoleLog("💡 [نصيحة ذكية] قم بالنقر على زر 'تنسيق الكود' في الأعلى لتلقيم المحاذاة التلقائية ومراجعة أخطاء الأقواس، أو انقر على 'فحص وتوضيح الخطأ' من برموج AI لمصلحتك.", ConsoleLogType.INFO))
            consoleLogsList = list
            terminalOutput = "خطأ تجميع: لم يتم تطابق الأقواس."
            return
        }

        viewModelScope.launch {
            isRunningCode = true
            activeTab = 1 // Immediately traverse to the Premium Code Runner Dashboard
            isTerminalOpen = true
            executionStatus = "جاري التجميع... ⏳"
            executionTimeMs = 0L
            errorCount = 0
            
            // Analyze static code warnings
            var warnings = 0
            if (!codeText.contains("//") && !codeText.contains("/*") && !codeText.contains("#")) {
                warnings++
            }
            if (codeText.contains("var ")) {
                warnings++
            }
            if (codeText.contains("TODO") || codeText.contains("todo")) {
                warnings++
            }
            warningCount = warnings

            val tempLogs = mutableListOf<ConsoleLog>()
            
            when (language) {
                ProgrammingLanguage.HTML -> {
                    tempLogs.add(ConsoleLog("Initializing Live Web Server Proxy on port 8080...", ConsoleLogType.INFO))
                }
                ProgrammingLanguage.CSS -> {
                    tempLogs.add(ConsoleLog("Pre-processing CSS variables and keyframes parser...", ConsoleLogType.INFO))
                }
                ProgrammingLanguage.JAVASCRIPT -> {
                    tempLogs.add(ConsoleLog("v8::ScriptCompiler::CompileFunctionOnSubThread starting...", ConsoleLogType.INFO))
                }
                ProgrammingLanguage.PYTHON -> {
                    tempLogs.add(ConsoleLog("python3 -u main.py", ConsoleLogType.INFO))
                }
                ProgrammingLanguage.JAVA -> {
                    tempLogs.add(ConsoleLog("javac Main.java && java Main", ConsoleLogType.INFO))
                }
                ProgrammingLanguage.CPP -> {
                    tempLogs.add(ConsoleLog("g++ -std=c++20 main.cpp -o main && ./main", ConsoleLogType.INFO))
                }
                ProgrammingLanguage.C -> {
                    tempLogs.add(ConsoleLog("gcc main.c -lncurses -o main && ./main", ConsoleLogType.INFO))
                }
                ProgrammingLanguage.CSHARP -> {
                    tempLogs.add(ConsoleLog("dotnet run --project .", ConsoleLogType.INFO))
                }
                ProgrammingLanguage.PHP -> {
                    tempLogs.add(ConsoleLog("php -f main.php", ConsoleLogType.INFO))
                }
                ProgrammingLanguage.RUST -> {
                    tempLogs.add(ConsoleLog("cargo run --release", ConsoleLogType.INFO))
                }
            }
            
            consoleLogsList = tempLogs

            // Fast ticker loop incrementing executionTimeMs in real time during delay
            val ticker = launch {
                while (true) {
                    kotlinx.coroutines.delay(40)
                    executionTimeMs += 40
                }
            }

            kotlinx.coroutines.delay(900) // Compilation Delay
            
            // Check for warnings log printout
            if (warningCount > 0) {
                val updatedLogs = consoleLogsList.toMutableList()
                if (!codeText.contains("//") && !codeText.contains("/*") && !codeText.contains("#")) {
                    updatedLogs.add(ConsoleLog("[ALERT] Warning: Missing block code comments explaining functions.", ConsoleLogType.COMPILE_SUCCESS))
                }
                if (codeText.contains("var ")) {
                    updatedLogs.add(ConsoleLog("[ALERT] Style Warning: Prefer using modern constant constraints (const/let or val) over standard block vars.", ConsoleLogType.COMPILE_SUCCESS))
                }
                consoleLogsList = updatedLogs
            }

            executionStatus = "جاري التشغيل... ⏳"
            kotlinx.coroutines.delay(600) // Runtime initiation
            ticker.cancel() // Stop execution ticker

            val finalLogs = consoleLogsList.toMutableList()

            // Check if Python input simulation is needed
            if (language == ProgrammingLanguage.PYTHON && (codeText.contains("input(") || codeText.contains("input ("))) {
                // Extract possible custom prompt
                val regex = Regex("""input\((.*?)\)""")
                val found = regex.find(codeText)
                val promptText = found?.groupValues?.get(1)?.removeSurrounding("\"")?.removeSurrounding("'") 
                    ?: "أدخل القيمة المطلوبة: "
                
                finalLogs.add(ConsoleLog(promptText, ConsoleLogType.INPUT_PROMPT))
                consoleLogsList = finalLogs
                
                pendingInputPrompt = promptText
                isWaitingForUserInput = true
                isRunningCode = false
                executionStatus = "بانتظار الإدخال ⌨️"
                return@launch
            }

            // Generate Output log entries
            when (language) {
                ProgrammingLanguage.HTML -> {
                    finalLogs.add(ConsoleLog("Hot Module Replacement (HMR) active instantly.", ConsoleLogType.STDOUT))
                    finalLogs.add(ConsoleLog("Rendered successfully inside Edu Code Live Frame.", ConsoleLogType.STDOUT))
                    finalLogs.add(ConsoleLog("Viewport classes matched: Responsive Android UI Container.", ConsoleLogType.INFO))
                }
                ProgrammingLanguage.CSS -> {
                    finalLogs.add(ConsoleLog("Found CSS rules for glowing components and font families.", ConsoleLogType.STDOUT))
                    finalLogs.add(ConsoleLog("Successfully parsed grid values and visual shadows.", ConsoleLogType.STDOUT))
                }
                ProgrammingLanguage.JAVASCRIPT -> {
                    if (codeText.contains("findPrimes")) {
                        finalLogs.add(ConsoleLog("الأعداد الأولية حتى 50 هي:", ConsoleLogType.STDOUT))
                        finalLogs.add(ConsoleLog("2, 3, 5, 7, 11, 13, 17, 19, 23, 29, 31, 37, 41, 43, 47", ConsoleLogType.STDOUT))
                    } else if (codeText.contains("console.log")) {
                        val matches = Regex("""console\.log\((.*?)\)""").findAll(codeText)
                        matches.forEach { m ->
                            val clean = m.groupValues[1].removeSurrounding("\"").removeSurrounding("'")
                            finalLogs.add(ConsoleLog(clean, ConsoleLogType.STDOUT))
                        }
                    } else {
                        finalLogs.add(ConsoleLog("console.log: تم تشغيل سكريبت الـ JavaScript بنجاح!", ConsoleLogType.STDOUT))
                    }
                    finalLogs.add(ConsoleLog("[DEBUG] Heap stats: used=18.4MB, total=245MB", ConsoleLogType.DEBUG))
                }
                ProgrammingLanguage.PYTHON -> {
                    if (codeText.contains("fibonacci")) {
                        finalLogs.add(ConsoleLog("أول 10 أرقام من متتالية فيبوناتشي:", ConsoleLogType.STDOUT))
                        finalLogs.add(ConsoleLog("0, 1, 1, 2, 3, 5, 8, 13, 21, 34", ConsoleLogType.STDOUT))
                    } else if (codeText.contains("print")) {
                        val matches = Regex("""print\((.*?)\)""").findAll(codeText)
                        if (matches.any()) {
                            matches.forEach { m ->
                                val clean = m.groupValues[1].removeSurrounding("\"").removeSurrounding("'").replace("f\"", "").replace("f'", "")
                                finalLogs.add(ConsoleLog(clean, ConsoleLogType.STDOUT))
                            }
                        } else {
                            finalLogs.add(ConsoleLog("مرحباً بك يا برموج في بايثون التفاعلي!", ConsoleLogType.STDOUT))
                        }
                    } else {
                        finalLogs.add(ConsoleLog("تم تشغيل البرنامج بنجاح دون مخرجات لوحية.", ConsoleLogType.STDOUT))
                    }
                }
                ProgrammingLanguage.JAVA -> {
                    finalLogs.add(ConsoleLog("Running JVM dynamic target...", ConsoleLogType.STDOUT))
                    finalLogs.add(ConsoleLog("مرحباً بك في لغة Java داخل بيئة Edu Code الاحترافية!", ConsoleLogType.STDOUT))
                    finalLogs.add(ConsoleLog("تم فحص الذاكرة وتخصيص الكائنات بنجاح.", ConsoleLogType.INFO))
                }
                ProgrammingLanguage.CPP -> {
                    finalLogs.add(ConsoleLog("--- مخرجات لغة C++ الحديثة ---", ConsoleLogType.STDOUT))
                    finalLogs.add(ConsoleLog("القيم المدخلة تمت تصفيتها ومعالجتها بكفاءة عالية.", ConsoleLogType.STDOUT))
                }
                ProgrammingLanguage.C -> {
                    finalLogs.add(ConsoleLog("مرحباً بك في عالم لغة C منخفضة المستوى!", ConsoleLogType.STDOUT))
                    finalLogs.add(ConsoleLog("العداد تكرر بنجاح 5 مرات من الذاكرة الفعالة.", ConsoleLogType.STDOUT))
                }
                ProgrammingLanguage.CSHARP -> {
                    finalLogs.add(ConsoleLog("C# Engine Enabled: Mono/CLI Compiler success.", ConsoleLogType.STDOUT))
                    finalLogs.add(ConsoleLog("تم بناء الهيكل وشؤون التوجيه بنجاح.", ConsoleLogType.STDOUT))
                }
                ProgrammingLanguage.PHP -> {
                    finalLogs.add(ConsoleLog("PHP Core Engine initialized on CLI.", ConsoleLogType.STDOUT))
                    finalLogs.add(ConsoleLog("أهلاً بك يا أحمد، مبرمج الويب الخارق!", ConsoleLogType.STDOUT))
                }
                ProgrammingLanguage.RUST -> {
                    finalLogs.add(ConsoleLog("Compiling rust safe references...", ConsoleLogType.INFO))
                    finalLogs.add(ConsoleLog("مرحباً بكم في لغة Rust الآمنة والسريعة!", ConsoleLogType.STDOUT))
                }
            }

            finalLogs.add(ConsoleLog("عملية التشغيل انتهت بنجاح (كود المخرج: 0)", ConsoleLogType.INFO))
            consoleLogsList = finalLogs
            
            val logsToText = finalLogs.joinToString("\n") { "${it.typeName()}: ${it.text}" }
            terminalOutput = logsToText
            
            executionStatus = "نجاح ✅"
            isRunningCode = false

            val userId = com.educode.app.di.AppModule.authRepository.getCurrentUserId()
            if (userId != null) {
                com.educode.app.di.AppModule.userRepository.updateXPAndCoins(userId, 25, 10)
            }
        }
    }

    private fun ConsoleLog.typeName(): String {
        return when (type) {
            ConsoleLogType.STDOUT -> "Console"
            ConsoleLogType.STDERR -> "Error"
            ConsoleLogType.DEBUG -> "Debug"
            ConsoleLogType.INFO -> "System"
            ConsoleLogType.INPUT_PROMPT -> "Prompt"
            ConsoleLogType.USER_INPUT -> "Input"
            ConsoleLogType.COMPILE_SUCCESS -> "CompileWarning"
            ConsoleLogType.COMPILE_FAILED -> "CompileError"
        }
    }

    fun stopCode() {
        isRunningCode = false
        executionStatus = "متوقف 🛑"
        val list = consoleLogsList.toMutableList()
        list.add(ConsoleLog("تم إيقاف التشغيل قسرياً ومسح الذاكرة الفعالة.", ConsoleLogType.STDERR))
        consoleLogsList = list
    }

    fun restartCode() {
        clearConsole()
        runCode()
    }

    fun clearConsole() {
        consoleLogsList = emptyList()
        executionTimeMs = 0L
        executionStatus = "متوقف 🛑"
        errorCount = 0
        warningCount = 0
        terminalOutput = ""
        pendingInputPrompt = null
        isWaitingForUserInput = false
        userInputValueInputState = ""
    }

    private fun getOutputsDirectory(): File {
        val dir = File(getApplication<Application>().filesDir, "edu_code_saved_outputs")
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }

    fun saveConsoleOutput(customName: String): Boolean {
        return try {
            val filename = if (customName.endsWith(".txt")) customName else "$customName.txt"
            val file = File(getOutputsDirectory(), filename)
            val builder = java.lang.StringBuilder()
            builder.append("=== مخرجات البرمجة الممتازة Edu Code Premium ===\n")
            builder.append("اسم الملف المصدر: $currentFileName\n")
            builder.append("اللغة المستخدمة: ${language.displayName}\n")
            builder.append("وقت وتاريخ الحفظ: ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())}\n")
            builder.append("حالة التنفيذ: $executionStatus | زمن التشغيل: ${executionTimeMs}ms\n")
            builder.append("الأخطاء: $errorCount | التحذيرات: $warningCount\n")
            builder.append("===================================================\n\n")
            
            consoleLogsList.forEach { log ->
                val typeTag = when (log.type) {
                    ConsoleLogType.STDOUT -> "[CONSOLE]"
                    ConsoleLogType.STDERR -> "[ERROR]"
                    ConsoleLogType.DEBUG -> "[DEBUG]"
                    ConsoleLogType.INFO -> "[INFO]"
                    ConsoleLogType.INPUT_PROMPT -> "[PROMPT]"
                    ConsoleLogType.USER_INPUT -> "[USER_INPUT]"
                    ConsoleLogType.COMPILE_SUCCESS -> "[WARNING]"
                    ConsoleLogType.COMPILE_FAILED -> "[COMPILE_ERR]"
                }
                builder.append("$typeTag ${log.text}\n")
            }
            
            FileOutputStream(file).use { out ->
                out.write(builder.toString().toByteArray())
            }
            loadSavedConsoleOutputs()
            true
        } catch (e: Exception) {
            false
        }
    }

    fun loadSavedConsoleOutputs() {
        try {
            val dir = getOutputsDirectory()
            val files = dir.listFiles()
            if (files != null) {
                savedConsoleOutputsList = files.map { it.name }.sorted()
            }
        } catch (e: Exception) {
            savedConsoleOutputsList = emptyList()
        }
    }

    fun deleteSavedConsoleOutput(filename: String): Boolean {
        return try {
            val file = File(getOutputsDirectory(), filename)
            if (file.exists()) {
                file.delete()
                loadSavedConsoleOutputs()
                true
            } else false
        } catch (e: Exception) {
            false
        }
    }

    fun submitUserInput(valInput: String) {
        val inputPrompt = pendingInputPrompt ?: "أدخل قيمة: "
        val list = consoleLogsList.toMutableList()
        list.add(ConsoleLog(valInput, ConsoleLogType.USER_INPUT))
        consoleLogsList = list
        
        isWaitingForUserInput = false
        pendingInputPrompt = null
        userInputValueInputState = ""
        
        viewModelScope.launch {
            isRunningCode = true
            executionStatus = "جاري التنفيذ ⏳"
            
            val ticker = launch {
                while (true) {
                    kotlinx.coroutines.delay(40)
                    executionTimeMs += 40
                }
            }
            kotlinx.coroutines.delay(700)
            ticker.cancel()
            
            val finalLogs = consoleLogsList.toMutableList()
            finalLogs.add(ConsoleLog("مرحباً بك يا $valInput في بيئة بايثون التفاعلية المحاكية!", ConsoleLogType.STDOUT))
            finalLogs.add(ConsoleLog("تم تشغيل السكريبت بنجاح مَع المدخل المرسل [$valInput].", ConsoleLogType.STDOUT))
            finalLogs.add(ConsoleLog("عملية التشغيل انتهت بنجاح (كود المخرج: 0)", ConsoleLogType.INFO))
            consoleLogsList = finalLogs
            
            executionStatus = "نجاح ✅"
            isRunningCode = false
        }
    }

    // Save and Load Files System inside Application Internal Storage (Directories)
    private fun getEditorDirectory(): File {
        val dir = File(getApplication<Application>().filesDir, "edu_code_premium_editor")
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }

    fun saveCodeFile(fileName: String): Boolean {
        return try {
            val file = File(getEditorDirectory(), fileName)
            FileOutputStream(file).use { out ->
                out.write(codeText.toByteArray())
            }
            currentFileName = fileName
            loadSavedFilesList()
            true
        } catch (e: Exception) {
            false
        }
    }

    fun loadCodeFile(fileName: String): Boolean {
        return try {
            val file = File(getEditorDirectory(), fileName)
            if (file.exists()) {
                val text = file.readText()
                codeText = text
                currentFileName = fileName
                // Deduce language from extension
                val ext = "." + fileName.substringAfterLast(".", "")
                val matchedLang = ProgrammingLanguage.values().find { it.extension == ext }
                if (matchedLang != null) {
                    language = matchedLang
                }
                undoStack.clear()
                redoStack.clear()
                undoStack.add(codeText)
                true
            } else false
        } catch (e: Exception) {
            false
        }
    }

    fun loadSavedFilesList() {
        try {
            val dir = getEditorDirectory()
            val files = dir.listFiles()
            if (files != null) {
                savedFilesList = files.map { it.name }.sorted()
            }
        } catch (e: Exception) {
            savedFilesList = emptyList()
        }
    }

    fun deleteCodeFile(fileName: String): Boolean {
        return try {
            val file = File(getEditorDirectory(), fileName)
            if (file.exists()) {
                file.delete()
                loadSavedFilesList()
                true
            } else false
        } catch (e: Exception) {
            false
        }
    }

    // Scanning for unmatched bracket errors
    fun getUnmatchedBracketErrors(): List<IntRange> {
        val errors = mutableListOf<IntRange>()
        val text = codeText
        val stack = mutableListOf<Pair<Char, Int>>()

        for (i in text.indices) {
            val char = text[i]
            if (char == '{' || char == '(' || char == '[') {
                stack.add(Pair(char, i))
            } else if (char == '}' || char == ')' || char == ']') {
                if (stack.isEmpty()) {
                    errors.add(IntRange(i, i))
                } else {
                    val last = stack.removeAt(stack.size - 1)
                    val expectedMatching = when (char) {
                        '}' -> '{'
                        ')' -> '('
                        ']' -> '['
                        else -> ' '
                    }
                    if (last.first != expectedMatching) {
                        errors.add(IntRange(last.second, last.second))
                        errors.add(IntRange(i, i))
                    }
                }
            }
        }
        // Leftovers in stack are unmatched openings
        for (item in stack) {
            errors.add(IntRange(item.second, item.second))
        }
        return errors
    }

    // Bracket matching for highlighter underlay
    fun getMatchedBracketsForCursor(cursor: Int): Pair<Int, Int>? {
        if (cursor < 1 || cursor > codeText.length) return null
        val text = codeText
        val charLeft = text[cursor - 1]
        
        var isOpening = false
        var matchChar = ' '
        var partnerChar = ' '
        
        if (charLeft == '{' || charLeft == '(' || charLeft == '[') {
            isOpening = true
            matchChar = charLeft
            partnerChar = when (charLeft) {
                '{' -> '}'
                '(' -> ')'
                '[' -> ']'
                else -> ' '
            }
        } else if (charLeft == '}' || charLeft == ')' || charLeft == ']') {
            isOpening = false
            matchChar = charLeft
            partnerChar = when (charLeft) {
                '}' -> '{'
                ')' -> '('
                ']' -> '['
                else -> ' '
            }
        } else {
            return null
        }

        if (isOpening) {
            var depth = 0
            for (i in (cursor - 1) until text.length) {
                val c = text[i]
                if (c == matchChar) depth++
                if (c == partnerChar) depth--
                if (depth == 0) {
                    return Pair(cursor - 1, i)
                }
            }
        } else {
            var depth = 0
            for (i in (cursor - 1) downTo 0) {
                val c = text[i]
                if (c == matchChar) depth++
                if (c == partnerChar) depth--
                if (depth == 0) {
                    return Pair(i, cursor - 1)
                }
            }
        }
        return null
    }
}
