package com.educode.app.features.challenge

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.educode.app.di.AppModule
import com.educode.app.domain.models.User
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class ChallengeUIState {
    object LanguageSelection : ChallengeUIState()
    
    data class LanguageDashboard(
        val language: String,
        val highestCompletedIndex: Int // 0 to 50
    ) : ChallengeUIState()
    
    data class Playing(
        val language: String,
        val question: ChallengeQuestion,
        val levelIndex: Int, // 1..50
        val codeBuffer: String,
        val consoleOutput: String,
        val consoleError: String?,
        val attemptsCount: Int,
        val userSelectedAnswer: String?,
        val shownAnswerResult: Boolean?, // true: correct, false: wrong, null: unanswered
        val hintShown: Boolean,
        val heartsLeft: Int,
        val totalTimerSeconds: Int,
        val barmujExplanation: String? = null
    ) : ChallengeUIState()
    
    data class Victory(
        val language: String,
        val levelIndex: Int,
        val totalLevels: Int = 50,
        val accuracy: Int,
        val timeTakenSeconds: Int,
        val xpEarned: Int,
        val coinsEarned: Int,
        val bonusXp: Int,
        val bonusCoins: Int
    ) : ChallengeUIState()
    
    data class Failure(
        val language: String,
        val levelIndex: Int,
        val errorSummary: String,
        val suggestedLessons: List<String>
    ) : ChallengeUIState()
}

class ChallengeViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<ChallengeUIState>(ChallengeUIState.LanguageSelection)
    val uiState: StateFlow<ChallengeUIState> = _uiState.asStateFlow()

    private val _userProfile = MutableStateFlow(User(name = "البطل المبرمج", xp = 120, coins = 100, hearts = 5))
    val userProfile: StateFlow<User> = _userProfile.asStateFlow()

    private var timerJob: Job? = null
    private var elapsedSeconds = 0
    private var isLevelCompletedInThisSession = false
    private var attemptsInLevel = 0

    init {
        loadUserProfile()
    }

    private fun loadUserProfile() {
        viewModelScope.launch {
            val userId = AppModule.authRepository.getCurrentUserId()
            if (userId != null) {
                AppModule.userRepository.getUserProfileFlow(userId).collect { user ->
                    if (user != null) {
                        _userProfile.value = user
                    }
                }
            }
        }
    }

    fun selectLanguage(language: String) {
        viewModelScope.launch {
            val userId = AppModule.authRepository.getCurrentUserId()
            var highestCompleted = 0
            if (userId != null) {
                try {
                    val firestore = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                    firestore.collection("users").document(userId)
                        .collection("challenge_progress").document(language.uppercase().trim())
                        .get()
                        .addOnSuccessListener { doc ->
                            highestCompleted = doc.getLong("highestCompletedIndex")?.toInt() ?: 0
                            _uiState.value = ChallengeUIState.LanguageDashboard(language, highestCompleted)
                        }
                        .addOnFailureListener {
                            _uiState.value = ChallengeUIState.LanguageDashboard(language, 0)
                        }
                } catch (e: Exception) {
                    _uiState.value = ChallengeUIState.LanguageDashboard(language, 0)
                }
            } else {
                _uiState.value = ChallengeUIState.LanguageDashboard(language, 0)
            }
        }
    }

    fun startLevel(language: String, levelIndex: Int) {
        // First check hearts!
        val hearts = _userProfile.value.hearts
        if (hearts <= 0) {
            // Blocked in UI
            return
        }

        elapsedSeconds = 0
        attemptsInLevel = 0
        isLevelCompletedInThisSession = false

        val questions = QuestionBank.getQuestionsForLanguage(language)
        val index = (levelIndex - 1).coerceIn(0, 49)
        val question = questions[index]

        // If it uses code, buffer it
        val initialCode = when (question.type) {
            QuestionType.CODE_COMPLETION,
            QuestionType.BUG_HUNTER,
            QuestionType.CODE_WRITING,
            QuestionType.OUTPUT_PREDICTION -> question.codeSnippet
            else -> ""
        }

        _uiState.value = ChallengeUIState.Playing(
            language = language,
            question = question,
            levelIndex = levelIndex,
            codeBuffer = initialCode,
            consoleOutput = "",
            consoleError = null,
            attemptsCount = 0,
            userSelectedAnswer = null,
            shownAnswerResult = null,
            hintShown = false,
            heartsLeft = hearts,
            totalTimerSeconds = 0,
            barmujExplanation = null
        )

        startTimer()
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                elapsedSeconds++
                val current = _uiState.value
                if (current is ChallengeUIState.Playing) {
                    _uiState.value = current.copy(totalTimerSeconds = elapsedSeconds)
                } else {
                    break
                }
            }
        }
    }

    // Runs a simulated compilation test on the editor buffer code
    fun runSimulatedCode() {
        val current = _uiState.value
        if (current !is ChallengeUIState.Playing) return

        val code = current.codeBuffer
        val question = current.question
        
        // Simulating error highlights & compiler logic
        val isCorrectSolution = checkCodeSolution(code, question)

        if (isCorrectSolution) {
            val successTerminal = """
                === COMPILATION SUCCESSFUL (v2.6) ===
                [OK] Syntax check passed. No formatting errors.
                [RUN] Executing output simulation...
                -------------------------------------
                المخرجات: ${question.correctAnswer}
                -------------------------------------
                🚀 ممتاز! تم التثبت بنجاح والحل متوافق مع شروط التحدي.
                اضغط على زر "التالي" بالأسفل للانتقال والمكافآت.
            """.trimIndent()
            
            _uiState.value = current.copy(
                consoleOutput = successTerminal,
                consoleError = null,
                shownAnswerResult = true
            )
        } else {
            attemptsInLevel++
            val rawError = getSimulatedCompilerError(code, question)
            val failedTerminal = """
                === COMPILATION FAILED (v2.6) ===
                [ERROR] $rawError
                💡 لم يحقق الكود المطلوب. اضغط "اسم برموج" أو "تفسير الأخطاء" للمساندة!
            """.trimIndent()

            val remainingHearts = maxOf(0, current.heartsLeft - 1)
            updateHeartsCount(remainingHearts)

            _uiState.value = current.copy(
                consoleOutput = "",
                consoleError = failedTerminal,
                shownAnswerResult = false,
                attemptsCount = attemptsInLevel,
                heartsLeft = remainingHearts,
                barmujExplanation = generateDynamicBarmujExpl(question, attemptsInLevel, rawError)
            )

            checkGameOver(remainingHearts, current)
        }
    }

    fun selectAnswer(selected: String) {
        val current = _uiState.value
        if (current !is ChallengeUIState.Playing) return
        if (current.shownAnswerResult == true) return // Already correct

        _uiState.value = current.copy(
            userSelectedAnswer = selected,
            shownAnswerResult = null // Reset result when changing selection
        )
    }

    fun submitCurrentAnswer() {
        val current = _uiState.value
        if (current !is ChallengeUIState.Playing) return
        
        val question = current.question
        
        when (question.type) {
            QuestionType.MULTIPLE_CHOICE, QuestionType.TRUE_FALSE -> {
                val selected = current.userSelectedAnswer ?: return
                val isCorrect = selected.trim() == question.correctAnswer.trim()

                if (isCorrect) {
                    _uiState.value = current.copy(
                        shownAnswerResult = true,
                        barmujExplanation = "أحسنت! إجابة صحيحة وموفقة. 🌟"
                    )
                } else {
                    attemptsInLevel++
                    val remainingHearts = maxOf(0, current.heartsLeft - 1)
                    updateHeartsCount(remainingHearts)

                    _uiState.value = current.copy(
                        shownAnswerResult = false,
                        attemptsCount = attemptsInLevel,
                        heartsLeft = remainingHearts,
                        barmujExplanation = generateDynamicBarmujExpl(question, attemptsInLevel, "اختيار غير موفق")
                    )

                    checkGameOver(remainingHearts, current)
                }
            }
            else -> {
                // For code types, it acts like "Run Code"
                runSimulatedCode()
            }
        }
    }

    private fun checkCodeSolution(code: String, question: ChallengeQuestion): Boolean {
        val cleanCode = code.trim().replace("\\s+".toRegex(), " ")
        val answer = question.correctAnswer.trim()

        return when (question.type) {
            QuestionType.CODE_COMPLETION -> {
                // Should replace "___" with correct answers or include it
                cleanCode.contains(answer) && !cleanCode.contains("___")
            }
            QuestionType.BUG_HUNTER -> {
                // Must fix the bug, meaning original snippet modified to hold corrected answer
                cleanCode.contains(answer)
            }
            QuestionType.CODE_WRITING -> {
                cleanCode.lowercase().contains(answer.lowercase())
            }
            QuestionType.OUTPUT_PREDICTION -> {
                cleanCode.contains(answer)
            }
            else -> false
        }
    }

    private fun getSimulatedCompilerError(code: String, question: ChallengeQuestion): String {
        return when {
            code.trim().contains("___") -> "خطأ سنتكس: تم العثور على الفيد الناقص '___'! يجب استبداله بالكود الفعلي."
            !code.contains(question.correctAnswer) -> {
                when (question.language) {
                    "HTML" -> "الوسم المكتوب لا يحقق شروط المصدر أو الهيكلية لـ HTML."
                    "CSS" -> "المحدد أو القيمة غير منسقة لـ CSS. راجع المخرجات والمسافات."
                    "Python" -> "IndentationError / NameError: متغير أو إزاحة مسافة مفقودة."
                    else -> "مخالفة قواعد التجميع البرمجي: الكلمة المفتاحية أو المعامل غير مستقر."
                }
            }
            else -> "مخالفة برمجية عامة: مخرجات الكود لا تتطابق مع القيمة المتوقعة: ${question.correctAnswer}."
        }
    }

    private fun checkGameOver(remainingHearts: Int, current: ChallengeUIState.Playing) {
        if (remainingHearts <= 0) {
            timerJob?.cancel()
            _uiState.value = ChallengeUIState.Failure(
                language = current.language,
                levelIndex = current.levelIndex,
                errorSummary = "لقد نفدت القلوب! لا يزال بإمكانك التعلم في دبلوم 'وضع التعلم' واستعادة قلب جديد.",
                suggestedLessons = listOf(current.question.suggestedLesson, "أساسيات لغة البرمجة المتكاملة")
            )
        }
    }

    fun requestBarmujAIHint() {
        val current = _uiState.value
        if (current !is ChallengeUIState.Playing) return

        val question = current.question
        val expl = generateDynamicBarmujExpl(question, attemptsInLevel + 1, "مساعدة برموج الذكية")
        _uiState.value = current.copy(
            hintShown = true,
            barmujExplanation = expl
        )
    }

    fun explainConcept() {
        val current = _uiState.value
        if (current !is ChallengeUIState.Playing) return

        val question = current.question
        val conceptText = """
            👨‍💻 *شرح مفهوم: ${question.suggestedLesson}*
            
            هذا السؤال يستكشف مفهومًا هامًا في لغة ${question.language}.
            
            *الفكرة العامة:*
            ${question.explanation}
            
            *كيف تفكر كالمحترفين؟*
            1. حلل صياغة الكود خطوة بخطوة.
            2. تأكد من إغلاق المفسرات والكلمات بحروفها الأصلية.
            3. لا تستعجل بكتابة الحل، بل اختبر تسييل المنطق بعقلك أولاً!
        """.trimIndent()

        _uiState.value = current.copy(
            barmujExplanation = conceptText
        )
    }

    fun formatBufferCode() {
        val current = _uiState.value
        if (current !is ChallengeUIState.Playing) return
        val raw = current.codeBuffer
        
        // Beautiful auto formatting for braces and indents
        val formatted = try {
            val lines = raw.split("\n")
            var depth = 0
            val sb = StringBuilder()
            for (line in lines) {
                var trimmed = line.trim()
                if (trimmed.startsWith("}") || trimmed.startsWith("]")) {
                    depth = maxOf(0, depth - 1)
                }
                
                val indent = "    ".repeat(depth)
                sb.append(indent).append(trimmed).append("\n")
                
                if (trimmed.endsWith("{") || trimmed.endsWith("[")) {
                    depth++
                }
            }
            sb.toString().trimEnd()
        } catch (e: Exception) {
            raw
        }

        _uiState.value = current.copy(codeBuffer = formatted)
    }

    fun updateCodeBuffer(newCode: String) {
        val current = _uiState.value
        if (current !is ChallengeUIState.Playing) return
        _uiState.value = current.copy(codeBuffer = newCode)
    }

    fun useStaticHint() {
        val current = _uiState.value
        if (current !is ChallengeUIState.Playing) return
        _uiState.value = current.copy(hintShown = true)
    }

    fun proceedToNextLevel() {
        val current = _uiState.value
        if (current !is ChallengeUIState.Playing) return

        timerJob?.cancel()
        val currentLevel = current.levelIndex
        
        // Rewards logic
        val baseXP = 15
        val baseCoins = 5
        
        // Session rewards
        var bonusXp = 0
        var bonusCoins = 0
        
        // Bonus for milestones (completing every 5 or 10 set levels, or level 50!)
        if (currentLevel % 5 == 0) {
            bonusXp = 50
            bonusCoins = 15
        }
        if (currentLevel == 50) {
            bonusXp = 250
            bonusCoins = 100
        }

        val accuracy = maxOf(10, 100 - (attemptsInLevel * 15))

        saveVictoryResults(
            language = current.language,
            levelIndex = currentLevel,
            accuracy = accuracy,
            timeSeconds = elapsedSeconds,
            xp = baseXP + bonusXp,
            coins = baseCoins + bonusCoins
        )

        _uiState.value = ChallengeUIState.Victory(
            language = current.language,
            levelIndex = currentLevel,
            accuracy = accuracy,
            timeTakenSeconds = elapsedSeconds,
            xpEarned = baseXP,
            coinsEarned = baseCoins,
            bonusXp = bonusXp,
            bonusCoins = bonusCoins
        )
    }

    private fun updateHeartsCount(hearts: Int) {
        viewModelScope.launch {
            val valid = if (hearts < 0) 0 else hearts
            val currentProfile = _userProfile.value
            
            val newRestoreTime = if (valid >= 5) {
                0L
            } else if (currentProfile.hearts >= 5) {
                System.currentTimeMillis()
            } else {
                currentProfile.lastHeartRestoreTime
            }
            
            _userProfile.value = currentProfile.copy(
                hearts = valid,
                lastHeartRestoreTime = newRestoreTime
            )
            
            val userId = AppModule.authRepository.getCurrentUserId()
            if (userId != null) {
                AppModule.userRepository.updateHearts(userId, valid, newRestoreTime)
            }
        }
    }

    private fun saveVictoryResults(
        language: String,
        levelIndex: Int,
        accuracy: Int,
        timeSeconds: Int,
        xp: Int,
        coins: Int
    ) {
        viewModelScope.launch {
            val currentProfile = _userProfile.value
            _userProfile.value = currentProfile.copy(
                xp = currentProfile.xp + xp,
                coins = currentProfile.coins + coins
            )

            val userId = AppModule.authRepository.getCurrentUserId()
            if (userId != null) {
                // Update coins and XP
                AppModule.userRepository.updateXPAndCoins(userId, xp, coins)

                // Save progression index (maxing out at levelIndex or highest)
                try {
                    val firestore = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                    
                    // First read current progress to make sure we don't overwrite with a smaller value if they replay!
                    firestore.collection("users").document(userId)
                        .collection("challenge_progress").document(language.uppercase().trim())
                        .get()
                        .addOnSuccessListener { doc ->
                            val currentMax = doc.getLong("highestCompletedIndex")?.toInt() ?: 0
                            if (levelIndex > currentMax) {
                                firestore.collection("users").document(userId)
                                    .collection("challenge_progress").document(language.uppercase().trim())
                                    .set(hashMapOf("highestCompletedIndex" to levelIndex))
                            }
                        }

                    // Save log trace
                    val log = hashMapOf(
                        "userId" to userId,
                        "language" to language,
                        "levelIndex" to levelIndex,
                        "accuracy" to accuracy,
                        "timeTakenSeconds" to timeSeconds,
                        "xpEarned" to xp,
                        "coinsEarned" to coins,
                        "timestamp" to System.currentTimeMillis()
                    )
                    firestore.collection("challenge_premium_logs").add(log)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    fun resetToSelection() {
        timerJob?.cancel()
        _uiState.value = ChallengeUIState.LanguageSelection
    }

    // AI Barmuj Dynamic Response generator - supports progressive hints without revealing the answer
    private fun generateDynamicBarmujExpl(
        question: ChallengeQuestion, 
        attempts: Int, 
        err: String
    ): String {
        val lessonLink = "\n\n💡 *نصيحة:* قد تجد هذا المفهوم مشروحاً بالتفصيل في درس: `${question.suggestedLesson}`. أنصحك بإلقاء نظرة خاطفة عليه."
        
        return when {
            attempts <= 1 -> """
                🤖 *أهلاً بك مبرمجنا الصغير! الإجابة غير صحيحة هذه المرة.*
                
                *تلميحة برمجية:*
                فكر في: `${question.hint}`. 
                دقق في الخيارات المتاحة وحاول الربط بينها وبين المفهوم المطلوب.
                $lessonLink
            """.trimIndent()
            
            attempts == 2 -> """
                🚀 *محاولة قوية، لكننا لم نصل بعد!*
                
                *شرح بسيط للخطأ:*
                $err. في لغة ${question.language}، غالباً ما نقع في هذا الخطأ عندما ننسى ${question.hint.lowercase()}.
                حاول مراجعة الأساسيات الخاصة بـ `${question.suggestedLesson}`.
                $lessonLink
            """.trimIndent()
            
            attempts == 3 -> """
                💡 *دعني أساعدك أكثر دون حرق الحل!*
                
                *الفكرة البرمجية:*
                ${question.explanation.take(60)}... (لا أريد إخبارك بكل شيء!)
                تذكر أن الهدف هو فهم المنطق البرمجي. انظر جيداً إلى السؤال، الإجابة تتعلق بـ ${question.correctAnswer.take(1)}... وتخدم غرض ${question.suggestedLesson}.
                محاولة أخرى؟
                $lessonLink
            """.trimIndent()
            
            else -> """
                🎓 *مهلاً! لا تستسلم. العظماء يخطئون ليتعلموا.*
                
                *تحليل معمق:*
                ${question.explanation}
                
                *توجيه أخير:*
                ركز على الخصائص التي توفرها لغة ${question.language} لهذا الموقف. الإجابة موجودة أمامك، فكر خارج الصندوق قليلاً!
                $lessonLink
            """.trimIndent()
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}
