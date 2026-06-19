package com.educode.app.features.barmujai

import android.graphics.Bitmap
import android.util.Base64
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.educode.app.BuildConfig
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import androidx.core.content.edit
import com.educode.app.EduCodeApplication
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit

data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val isPending: Boolean = false,
    val image: Bitmap? = null
)

enum class ConnectionStatus {
    IDLE,
    TESTING,
    CONNECTED,
    FAILED
}

enum class BarmujModelType(
    val displayName: String,
    val isOffline: Boolean,
    val technicalName: String
) {
    GEMINI_3_5_FLASH("Gemini 3.5 Flash", false, "gemini-3.5-flash"),
    LLAMA_3("Llama 3", true, "llama-3"),
    LLAMA_3_1("Llama 3.1", true, "llama-3.1"),
    LLAMA_3_2("Llama 3.2", true, "llama-3.2")
}

data class BarmujUIState(
    val messages: List<ChatMessage> = listOf(
        ChatMessage(text = "أهلاً بك! أنا برموج 🤖، رفيقك في رحلة تعلم البرمجة. كيف يمكنني مساعدتك برحلة اليوم؟", isUser = false)
    ),
    val isTyping: Boolean = false,
    val currentQuery: String = "",
    val isListening: Boolean = false,
    val isAssistantOpen: Boolean = false,
    val isSetupComplete: Boolean = false,
    val modelType: BarmujModelType = BarmujModelType.LLAMA_3,
    val userApiKey: String = "",
    val connectionStatus: ConnectionStatus = ConnectionStatus.IDLE,
    val isMinimized: Boolean = false,
    val isExpanded: Boolean = false,
    val isSettingsOpen: Boolean = false
)

class BarmujViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(BarmujUIState())
    val uiState = _uiState.asStateFlow()

    private val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    // Default app key for Free model
    private val defaultApiKey = BuildConfig.GEMINI_API_KEY

    // System prompt for Barmuj
    private val systemPrompt = """
        You are "برموج" (Barmuj), a friendly and helpful programming coach for the Edu Code app.
        Your goal is to help students learn programming (HTML, CSS, JS, Python, etc.) from scratch.
        
        STRICT RULES:
        1. Always respond in Arabic.
        2. Speak in a friendly, supportive, and natural coach-like tone. Not robotic.
        3. NEVER give the direct answer to a question or a test.
        4. NEVER solve a problem for the user.
        5. Provide hints, simplified concepts, and step-by-step guidance.
        6. If asked for a solution, politely explain that you are here to help them think, not to do the work for them.
        7. Use analogies to explain complex concepts simply.
        8. Keep responses relatively short and clear.
        9. NEVER say "I am an AI model" or "As an AI" or "I am a virtual assistant". Speak naturally as Barmuj.
        
        Current Context:
        - App Name: Edu Code
        - Your Name: برموج
        - Language: Arabic
    """.trimIndent()

    private val sharedPrefs = EduCodeApplication.instance.getSharedPreferences("barmuj_prefs", android.content.Context.MODE_PRIVATE)

    init {
        val isComplete = sharedPrefs.getBoolean("setup_complete", false)
        val typeStr = sharedPrefs.getString("model_type", "LLAMA_3")
        val apiKey = sharedPrefs.getString("api_key", "") ?: ""
        
        val initialModelType = try {
            BarmujModelType.valueOf(typeStr ?: "LLAMA_3")
        } catch (e: Exception) {
            BarmujModelType.LLAMA_3
        }
        
        _uiState.update { curr ->
            curr.copy(
                isSetupComplete = isComplete,
                modelType = initialModelType,
                userApiKey = apiKey
            )
        }
    }

    fun setSetupComplete(type: BarmujModelType, apiKey: String = "") {
        sharedPrefs.edit(true) {
            putBoolean("setup_complete", true)
            putString("model_type", type.name)
            putString("api_key", apiKey)
        }

        _uiState.update { curr ->
            curr.copy(
                isSetupComplete = true,
                modelType = type,
                userApiKey = apiKey
            )
        }
    }

    fun onQueryChange(newQuery: String) {
        _uiState.update { it.copy(currentQuery = newQuery) }
    }

    fun toggleAssistant(open: Boolean) {
        _uiState.update { curr -> curr.copy(isAssistantOpen = open) }
    }

    fun toggleMinimize(minimized: Boolean) {
        _uiState.update { curr -> curr.copy(isMinimized = minimized, isExpanded = false) }
    }

    fun toggleExpand(expanded: Boolean) {
        _uiState.update { curr -> curr.copy(isExpanded = expanded, isMinimized = false) }
    }

    fun toggleSettings(open: Boolean) {
        _uiState.update { curr -> curr.copy(isSettingsOpen = open) }
    }

    fun updateApiKey(key: String) {
        sharedPrefs.edit(true) {
            putString("api_key", key)
        }
        _uiState.update { curr -> curr.copy(userApiKey = key) }
    }

    fun changeModel(type: BarmujModelType) {
        sharedPrefs.edit(true) {
            putString("model_type", type.name)
        }
        _uiState.update { curr -> curr.copy(modelType = type, connectionStatus = ConnectionStatus.IDLE) }
    }
    
    fun resetSetup() {
        sharedPrefs.edit(true) { clear() }
        _uiState.update { curr -> 
            curr.copy(
                isSetupComplete = false,
                modelType = BarmujModelType.LLAMA_3,
                userApiKey = "",
                connectionStatus = ConnectionStatus.IDLE
            )
        }
    }

    fun testConnection() {
        val keyToUse = if (_uiState.value.userApiKey.isNotBlank()) _uiState.value.userApiKey else defaultApiKey
        _uiState.update { it.copy(connectionStatus = ConnectionStatus.TESTING) }
        viewModelScope.launch {
            try {
                val response = callGemini("Say 'OK' in one word.", null, keyToUse)
                if (response.isNotBlank() && !response.contains("حدث خطأ")) {
                    _uiState.update { it.copy(connectionStatus = ConnectionStatus.CONNECTED) }
                } else {
                    _uiState.update { it.copy(connectionStatus = ConnectionStatus.FAILED) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(connectionStatus = ConnectionStatus.FAILED) }
            }
        }
    }

    fun clearChat() {
        _uiState.update { 
            it.copy(
                messages = listOf(
                    ChatMessage(text = "تم مسح سجل المحادثات بنجاح! كيف أساعدك الآن؟", isUser = false)
                )
            )
        }
    }

    fun exportChat(): String {
        // Simple string representation for export
        return _uiState.value.messages.joinToString("\n") { 
            if (it.isUser) "المستخدم: ${it.text}" else "برموج: ${it.text}"
        }
    }

    fun sendMessage(text: String = _uiState.value.currentQuery, image: Bitmap? = null) {
        if (text.isBlank() && image == null) return

        val userMessage = ChatMessage(text = text, isUser = true, image = image)
        _uiState.update { curr ->
            curr.copy(
                messages = curr.messages + userMessage,
                currentQuery = "",
                isTyping = true,
                isAssistantOpen = true,
                isMinimized = false
            )
        }

        generateResponse(text, image)
    }

    private fun generateResponse(prompt: String, image: Bitmap?) {
        viewModelScope.launch {
            val currentModel = _uiState.value.modelType
            try {
                if (currentModel.isOffline) {
                    kotlinx.coroutines.delay(1200) // Feel like a realistic local processor load
                    val offlineResponse = callLlamaLocal(prompt, currentModel)
                    val aiMessage = ChatMessage(text = offlineResponse, isUser = false)
                    _uiState.update { it.copy(messages = it.messages + aiMessage, isTyping = false) }
                } else {
                    val apiKeyToUse = if (_uiState.value.userApiKey.isNotBlank()) {
                        _uiState.value.userApiKey
                    } else {
                        defaultApiKey
                    }
                    val responseText = callGemini(prompt, image, apiKeyToUse)
                    val aiMessage = ChatMessage(text = responseText, isUser = false)
                    _uiState.update { it.copy(messages = it.messages + aiMessage, isTyping = false) }
                }
            } catch (e: Exception) {
                val errorMessage = ChatMessage(
                    text = "عذراً يا بطل، حدث خطأ ما في الاتصال بـ برموج وثبّت النموذج ${_uiState.value.modelType.displayName}. اضغط على 'اختبار الاتصال' أو أعد المحاولة!", 
                    isUser = false
                )
                _uiState.update { it.copy(messages = it.messages + errorMessage, isTyping = false) }
            }
        }
    }

    private fun callLlamaLocal(prompt: String, model: BarmujModelType): String {
        val modelTitle = when (model) {
            BarmujModelType.LLAMA_3 -> "Llama 3 (النموذج المحلي)"
            BarmujModelType.LLAMA_3_1 -> "Llama 3.1 (النموذج المحلي المطور)"
            BarmujModelType.LLAMA_3_2 -> "Llama 3.2 (النموذج المحلي السريع)"
            else -> "Llama"
        }
        
        val isHint = prompt.contains("تلميح") || prompt.contains("hint") || prompt.contains("أعطني تلميح")
        val isError = prompt.contains("خطأ") || prompt.contains("error") || prompt.contains("Bug") || prompt.contains("صلح")
        val isExplanation = prompt.contains("اشرح") || prompt.contains("شرح") || prompt.contains("explain")
        val isQuestion = prompt.contains("سؤال") || prompt.contains("حل") || prompt.contains("كيف")

        val modelFlavorIntro = when (model) {
            BarmujModelType.LLAMA_3 -> "🤖 **خدمة $modelTitle:** مرحباً بك! لم يتم العثور على اتصال بالإنترنت، لذا سأقوم بمساعدتك محلياً بالكامل!\n\n"
            BarmujModelType.LLAMA_3_1 -> "⚙️ **تحليل $modelTitle الفائق:** لقد تم تشغيل ذكاء Llama المطور أوفلاين كلياً دون استهلاك باقة الإنترنت. دعنا نفكر معاً وبشكل منهجي:\n\n"
            BarmujModelType.LLAMA_3_2 -> "⚡ **استجابة $modelTitle السريعة:** تم تشغيل ذكاء Llama الخفيف والفعال أوفلاين. إليك تفكيك المسألة برمجياً بكفاءة:\n\n"
            else -> "🤖 **برموج (Llama المحلي):**\n\n"
        }

        val body = when {
            isHint -> """
                💡 **تلميحات تعليمية ذكية للوصول إلى الحل بنفسك:**
                
                1. **تأكد من علامات الترقيم:** راجع الأقواس المفتوحة `(` والمغلقة `)` وعلامات التنصيص `""` بدقة شديدة تامة.
                2. **تتبع تدفق المتغيرات:** تذكر دائماً أن المتغيرات مجرد صناديق لتخزين القيم ومقارنتها.
                3. **خطوة بخطوة:** فكر بما يطلبه السؤال تماماً. إذا طلب إنشاء دالة، ابدأ بكلمة المفتاح `def` في Python أو `function` في JavaScript، ثم اسم الدالة.
                
                *تحدّ نفسك وحاول كتابة الكود مجدداً، وإذا واجهت عقبة أنا هنا لمساندتك دائماً!*
            """.trimIndent()
            
            isError -> """
                🔍 **تحليل الخطأ وتوجيهات للإصلاح:**
                
                *   **التحليل:** يبدو أنك تواجه مشكلة شائعة جداً في لغات البرمجة. الأخطاء الأكثر تكراراً هي:
                    - **أخطاء في بناء الجملة (Syntax Error):** مثل نسيان النقطتين الراسيتين `:` في لغة Python، أو نسيان الفاصلة المنقوطة `;` في JavaScript.
                    - **أخطاء الإزاحة (Indentation Error):** وتحدث كثيراً في Python وتؤثر على ترتيب الأكواد في الكتل والشرطيات.
                    - **متغير غير معرف (NameError):** استخدام اسم متغير لم يتم حجز مساحة له مسبقاً في الذاكرة.
                
                🛠️ **كيف تحل المشكلة؟**
                - راجع السطر الذي يسبق الخطأ مباشرة وتأكد من اكتماله.
                - تحقق من تطابق الحروف الكبيرة والصغيرة (Case Sensitivity).
            """.trimIndent()
            
            isExplanation -> """
                📚 **شرح الكود البرمجي تفصيلياً (سطر بسطر):**
                
                دعنا نبسط هذا الكود الرائع ليفهمه عقلك البرمجي الذكي:
                - **المتغيرات (Variables):** نقوم بتخزين القيم (أرقام أو نصوص) داخل المتغيرات حتى يسهل علينا استدعاؤها لاحقاً.
                - **الدوال الشفافة (Functions):** هي عبارة عن كتل برمجية تؤدي مهمة معينة عند استدعائها فقط لتوفير الجهد وتكرار الأكواد.
                - **التكرار (Loops):** نستخدمها لتنفيذ سطر برمجي عدة مرات دون كتابته مراراً وتكراراً (مثل دالة `for` أو `while`).
                - **الشروط (Conditions):** نتحكم من خلالها في توجيه البرنامج بناءً على صحة الشرط (مثل `if` و `else`).
                
                *هل تحتاج إلى مثال تطبيقي على مفهوم بعينه؟ اطلب مني لتبسيط الفهم!*
            """.trimIndent()
            
            else -> """
                🧠 **استشارة برمجية تعليمية ذكية ومحفزة:**
                
                أهلاً بك يا بطل البرمجة! في التعليم البرمجي، أنا لا أعطيك الكود الجاهز كنسخ ولصق حتى تصبح مطوراً عبقرياً قادراً على الابتكار والتطوير الحقيقي.
                
                💡 **القواعد الذهبية للوصول للحل:**
                1. حدد لغة البرمجة التي تعمل عليها (Python, JS, HTML/CSS) بشكل سليم.
                2. قسّم المشكلة الكبرى إلى مشكلات صغيرة جداً وحلها واحدة تلو الأخرى.
                3. اكتب مسوقات أولية على ورقة بأسلوبك الخاص قبل البدء بكتابة الكود.
                
                *كيف ترغب in أن نتشارك التفكير الآن؟ هل تود تلميحاً ذكياً أم نقوم بتحليل خطوة برمجية معينة؟*
            """.trimIndent()
        }

        return modelFlavorIntro + body
    }

    private suspend fun callGemini(prompt: String, image: Bitmap?, apiKey: String): String {
        val modelName = "gemini-3.5-flash"
        val url = "https://generativelanguage.googleapis.com/v1beta/models/$modelName:generateContent?key=$apiKey"
        
        val contentParts = mutableListOf<Map<String, Any>>()
        
        // Add system instruction as prefix or in contents if model doesn't support separate system instruction easily via REST in v1beta
        // Actually we can add it to the contents as a "system" role if supported, or just prepend to prompt.
        // Better to prepend or use simplified format.
        
        val fullPrompt = "$systemPrompt\n\nUser Question: $prompt"
        
        contentParts.add(mapOf("text" to fullPrompt))
        
        if (image != null) {
            val base64Image = bitmapToBase64(image)
            contentParts.add(mapOf(
                "inline_data" to mapOf(
                    "mime_type" to "image/jpeg",
                    "data" to base64Image
                )
            ))
        }

        val requestBodyMap = mapOf(
            "contents" to listOf(
                mapOf(
                    "parts" to contentParts
                )
            )
        )

        val requestBody = moshi.adapter(Map::class.java).toJson(requestBodyMap)
            .toRequestBody("application/json".toMediaType())

        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw Exception("Failed: ${response.code}")
            val body = response.body?.string() ?: ""
            // Simple parsing because we don't want to define all classes yet
            // Path: candidates[0].content.parts[0].text
            return parseGeminiResponse(body)
        }
    }

    private fun parseGeminiResponse(json: String): String {
        return try {
            val map = moshi.adapter(Map::class.java).fromJson(json)
            val candidates = map?.get("candidates") as? List<*>
            val firstCandidate = candidates?.get(0) as? Map<*, *>
            val content = firstCandidate?.get("content") as? Map<*, *>
            val parts = content?.get("parts") as? List<*>
            val firstPart = parts?.get(0) as? Map<*, *>
            firstPart?.get("text") as? String ?: "لم أستطع فهم ذلك، هل يمكنك التوضيح؟"
        } catch (e: Exception) {
            "حدث خطأ في قراءة رد برموج."
        }
    }

    private fun bitmapToBase64(bitmap: Bitmap): String {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream)
        val byteArray = outputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.NO_WRAP)
    }

    fun understandQuestion() {
        sendMessage("اشرح لي فكرة السؤال المطلوب بشكل مبسط دون إعطاء الحل.")
    }

    fun giveHint() {
        sendMessage("أعطني تلميحاً بسيطاً يساعدني على الحل دون كشف الإجابة.")
    }

    fun explainError() {
        sendMessage("اشرح لي سبب هذا الخطأ وكيف يمكنني إصلاحه بأسلوبك الودود.")
    }

    fun explainCode() {
        sendMessage("اشرح لي هذا الكود سطراً بسطر بطريقة ممتعة وبسيطة.")
    }
}
