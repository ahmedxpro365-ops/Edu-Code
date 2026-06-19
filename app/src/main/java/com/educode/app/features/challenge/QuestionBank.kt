package com.educode.app.features.challenge

enum class QuestionType {
    MULTIPLE_CHOICE,
    TRUE_FALSE,
    CODE_COMPLETION,
    BUG_HUNTER,
    OUTPUT_PREDICTION,
    CODE_WRITING
}

enum class Difficulty {
    EASY,
    MEDIUM,
    HARD
}

data class ChallengeQuestion(
    val id: String,
    val language: String,
    val type: QuestionType,
    val difficulty: Difficulty,
    val questionText: String,
    val codeSnippet: String = "",
    val options: List<String> = emptyList(),
    val correctAnswer: String,
    val hint: String,
    val explanation: String = "",
    val suggestedLesson: String = "الأساسيات العامة"
)

object QuestionBank {

    fun getQuestionsForLanguage(language: String): List<ChallengeQuestion> {
        val list = mutableListOf<ChallengeQuestion>()
        val langKey = language.uppercase().trim()
        
        for (i in 1..50) {
            val difficulty = when {
                i <= 15 -> Difficulty.EASY
                i <= 35 -> Difficulty.MEDIUM
                else -> Difficulty.HARD
            }
            
            // Periodically cycle through the 6 question types
            val type = when (i % 6) {
                1 -> QuestionType.MULTIPLE_CHOICE
                2 -> QuestionType.TRUE_FALSE
                3 -> QuestionType.CODE_COMPLETION
                4 -> QuestionType.BUG_HUNTER
                5 -> QuestionType.OUTPUT_PREDICTION
                else -> QuestionType.CODE_WRITING
            }

            val question = generateQuestionDetails(langKey, i, type, difficulty)
            list.add(question)
        }
        return list
    }

    private fun generateQuestionDetails(
        lang: String, 
        index: Int, 
        type: QuestionType, 
        diff: Difficulty
    ): ChallengeQuestion {
        val qId = "${lang}_CHALLENGE_${index}"
        
        return when (lang) {
            "HTML" -> generateHtmlQuestion(qId, index, type, diff)
            "CSS" -> generateCssQuestion(qId, index, type, diff)
            "JAVASCRIPT" -> generateJsQuestion(qId, index, type, diff)
            "PYTHON" -> generatePythonQuestion(qId, index, type, diff)
            "C" -> generateCQuestion(qId, index, type, diff)
            "C++", "CPP" -> generateCppQuestion(qId, index, type, diff)
            "C#", "CSHARP" -> generateCsharpQuestion(qId, index, type, diff)
            "JAVA" -> generateJavaQuestion(qId, index, type, diff)
            "PHP" -> generatePhpQuestion(qId, index, type, diff)
            "RUST" -> generateRustQuestion(qId, index, type, diff)
            else -> generateGenericQuestion(qId, lang, index, type, diff)
        }
    }

    private fun generateHtmlQuestion(id: String, i: Int, type: QuestionType, diff: Difficulty): ChallengeQuestion {
        val topic = when {
            i <= 10 -> "عناصر الويب الأساسية"
            i <= 20 -> "القوائم والنصوص"
            i <= 30 -> "الروابط والوسائط"
            i <= 40 -> "الجداول والنماذج"
            else -> "هيكلة الويب المتقدمة والبيانات"
        }

        return when (type) {
            QuestionType.MULTIPLE_CHOICE -> ChallengeQuestion(
                id = id, language = "HTML", type = type, difficulty = diff, suggestedLesson = topic,
                questionText = "ما هو الوسم الصحيح والأكثر أهمية لعرض العنوان الرئيسي الأكبر حجماً في الصفحة؟ (التحدي $i)",
                options = listOf("<h1>", "<head>", "<heading>", "<h6>"),
                correctAnswer = "<h1>",
                hint = "استخدم وسم الرأس ذو الرقم 1.",
                explanation = "في هرمية HTML، يعتبر <h1> هو أقوى عنصر للترويسات الرئيسية بينما <h6> هو الأصغر."
            )
            QuestionType.TRUE_FALSE -> ChallengeQuestion(
                id = id, language = "HTML", type = type, difficulty = diff, suggestedLesson = topic,
                questionText = "هل الوسم <br> يحتاج دوماً إلى وسم إغلاق </br> لإقران الفقرات والسطور بنجاح؟ (التحدي $i)",
                options = listOf("صح", "خطأ"),
                correctAnswer = "خطأ",
                hint = "بعض وسوم HTML تسمى 'وسوم فارغة' أو عنصر أحادي النهاية.",
                explanation = "الوسم <br> هو عنصر سطر مكسور أحادي (Self-closing) ولا يحتاج لوسم إغلاق من المفسر."
            )
            QuestionType.CODE_COMPLETION -> ChallengeQuestion(
                id = id, language = "HTML", type = type, difficulty = diff, suggestedLesson = topic,
                questionText = "أكمل الخاصية المناسبة لربط رابط تشعبي بوجهته الصحيحة في الويب العربي:",
                codeSnippet = "<a ___=\"https://educode.org\">موقع إيدو كود</a>",
                options = listOf("href", "src", "link", "url"),
                correctAnswer = "href",
                hint = "اختصار لـ Hypertext Reference.",
                explanation = "خاصية href تحدد الرابط الذي ستنتقل إليه الصفحة عند الضغط على النص."
            )
            QuestionType.BUG_HUNTER -> ChallengeQuestion(
                id = id, language = "HTML", type = type, difficulty = diff, suggestedLesson = topic,
                questionText = "اكتشف الخلل في ترميز الصورة وصححه لكي تظهر الصورة للمستخدم بشكل صحيح:",
                codeSnippet = "<img href=\"logo.png\" alt=\"Edu Code Logo\">",
                options = listOf("src=\"logo.png\"", "link=\"logo.png\"", "source=\"logo.png\"", "url=\"logo.png\""),
                correctAnswer = "src=\"logo.png\"",
                hint = "الصور تستخدم خاصية المصدر 'Source' بدلاً من الارتباط التشعبي.",
                explanation = "الوسم <img> يستعين بخاصية src لتحديد مسار الصورة وليس href."
            )
            QuestionType.OUTPUT_PREDICTION -> ChallengeQuestion(
                id = id, language = "HTML", type = type, difficulty = diff, suggestedLesson = topic,
                questionText = "ماذا سيعرض كود HTML التالي في المتصفح إذا تم تشغيله بنجاح؟",
                codeSnippet = "<p>تعلم البرمجة <strong>بمتعة</strong> مع إيدوكود</p>",
                options = listOf("تعلم البرمجة بمتعة مع إيدوكود (مع نص عريض لـ بمتعة)", "تعلم البرمجة بمتعة مع إيدوكود (مع نص مائل لـ بمتعة)", "خطأ في ترميز الصفحة", "تعلم البرمجة <strong style=\"display:none\">بمتعة</strong>"),
                correctAnswer = "تعلم البرمجة بمتعة مع إيدوكود (مع نص عريض لـ بمتعة)",
                hint = "الوسم strong يمنح النص ثقلاً ويزيد سمكه.",
                explanation = "يجعل المتصفح النص المحاط بـ <strong> داكناً وعريضاً (Bold) للدلالة على الأهمية."
            )
            QuestionType.CODE_WRITING -> ChallengeQuestion(
                id = id, language = "HTML", type = type, difficulty = diff, suggestedLesson = topic,
                questionText = "اكتب الاسم الدقيق لوسم حقل الإدخال النصي الذي يتيح للمستخدم إدخال بياناته بالويب:",
                codeSnippet = "___",
                options = listOf("input", "text", "form", "textarea"),
                correctAnswer = "input",
                hint = "يبدأ بحرف i ويتكون من 5 أحرف.",
                explanation = "الوسم <input> هو أحد أهم وسوم النماذج التي تستقبل مدخلات المستخدم بمختلف أنواعها."
            )
        }
    }

    private fun generateCssQuestion(id: String, i: Int, type: QuestionType, diff: Difficulty): ChallengeQuestion {
        val topic = when {
            i <= 10 -> "محددات الألوان والنصوص"
            i <= 20 -> "تنسيقات الخطوط وصندوق الحواف"
            i <= 30 -> "توزيع العناصر وترتيب المواقع"
            i <= 40 -> "تخطيط الفليكس بوكس والشبكة"
            else -> "الانتقالات والتحريكات المتجاوبة"
        }

        return when (type) {
            QuestionType.MULTIPLE_CHOICE -> ChallengeQuestion(
                id = id, language = "CSS", type = type, difficulty = diff, suggestedLesson = topic,
                questionText = "ما هي الطريقة الصحيحة لاستهداف عنصر يحمل المعرف id باسم 'header' في تنسيق CSS؟ (التحدي $i)",
                options = listOf("#header", ".header", "header", "*header"),
                correctAnswer = "#header",
                hint = "استخدام علامة الهاش المربع.",
                explanation = "تستخدم علامة الهاش # للمعرفات (IDs) بينما علامة النقطة . للفئات (Classes) في لغة CSS."
            )
            QuestionType.TRUE_FALSE -> ChallengeQuestion(
                id = id, language = "CSS", type = type, difficulty = diff, suggestedLesson = topic,
                questionText = "هل تعيين القيمة 'display: none' تخفي العنصر تماماً من الصفحة وتحجز مساحته الفارغة كما هي؟ (التحدي $i)",
                options = listOf("صح", "خطأ"),
                correctAnswer = "خطأ",
                hint = "هناك فرق بين الإخاء الكلي display:none وتعديل الشفافية visibility:hidden.",
                explanation = "القيمة 'display: none' تخفي العنصر وتحذفه من حساب المساحة الكلية للصفحة وكأنه غير موجود."
            )
            QuestionType.CODE_COMPLETION -> ChallengeQuestion(
                id = id, language = "CSS", type = type, difficulty = diff, suggestedLesson = topic,
                questionText = "أكمل حاشية كود تخطيط الفليكس بوكس لتوزيع العناصر بشكل متساوٍ بالمنتصف عرضياً:",
                codeSnippet = ".container {\n  display: flex;\n  ___: center;\n}",
                options = listOf("justify-content", "align-items", "flex-direction", "align-content"),
                correctAnswer = "justify-content",
                hint = "تتحكم في المحاذاة على طول المحور الرئيسي للفليكس.",
                explanation = "تستخدم justify-content: center لمحاذاة العناصر وتوسيطها أفقياً على طول المحور الرئيسي."
            )
            QuestionType.BUG_HUNTER -> ChallengeQuestion(
                id = id, language = "CSS", type = type, difficulty = diff, suggestedLesson = topic,
                questionText = "هناك خطأ في طريقة تطبيق سمك الحدود وصياغته الدائرية الزاوية، اكتشفه وصححه:",
                codeSnippet = ".glow-card {\n  border-corners: 10px;\n}",
                options = listOf("border-radius: 10px", "border-round: 10px", "corner-radius: 10px", "border-angle: 10px"),
                correctAnswer = "border-radius: 10px",
                hint = "الاسم الإنجليزي لنصف القطر للحدود.",
                explanation = "الخاصية المعيارية لتدوير زوايا الصندوق وتحويله لشكل ناعم هي border-radius."
            )
            QuestionType.OUTPUT_PREDICTION -> ChallengeQuestion(
                id = id, language = "CSS", type = type, difficulty = diff, suggestedLesson = topic,
                questionText = "ما هو العرض النهائي الكلي الفعلي لعنصر يمتلك الخصائص التالية تحت نظام الـ Box Model؟",
                codeSnippet = ".card {\n  width: 200px;\n  padding: 15px;\n  border: 5px solid cyan;\n}",
                options = listOf("240px (إذا كان box-sizing هو content-box)", "200px بالكلية", "220px بالتعديل", "300px بالتقريب"),
                correctAnswer = "240px (إذا كان box-sizing هو content-box)",
                hint = "العرض الفعلي = العرض + الحواشي من الجهتين + عرض الحدود من الجهتين.",
                explanation = "تحت النموذج الافتراضي content-box، يُحسب العرض كـ: 200 + 15*2 + 5*2 = 240px."
            )
            QuestionType.CODE_WRITING -> ChallengeQuestion(
                id = id, language = "CSS", type = type, difficulty = diff, suggestedLesson = topic,
                questionText = "اكتب اسم السمة البرمجية المسؤولة عن تباعد العناصر وحجز الفراغات الداخلية للصندوق:",
                codeSnippet = "___",
                options = listOf("padding", "margin", "border", "spacing"),
                correctAnswer = "padding",
                hint = "الحشو الداخلي للصندوق ويبدأ بحرف p.",
                explanation = "خاصية padding تجد الفراغ الداخلي الفاصل بين محتوى العنصر وحدوده الخارجية."
            )
        }
    }

    private fun generateJsQuestion(id: String, i: Int, type: QuestionType, diff: Difficulty): ChallengeQuestion {
        val topic = when {
            i <= 10 -> "تعريف المتغيرات والشروط"
            i <= 20 -> "المصفوفات والتكرارات"
            i <= 30 -> "الدوال وعمليات الكائنات"
            i <= 40 -> "معالجة أحداث والـ DOM"
            else -> "الوعد والبرمجة اللامتزامنة بالأكواد"
        }

        return when (type) {
            QuestionType.MULTIPLE_CHOICE -> ChallengeQuestion(
                id = id, language = "JavaScript", type = type, difficulty = diff, suggestedLesson = topic,
                questionText = "أي من الكلمات التالية تمنع إعادة تعيين قيمة المتغير بعد تعريفه الأول في JavaScript؟ (التحدي $i)",
                options = listOf("const", "let", "var", "immutable"),
                correctAnswer = "const",
                hint = "اختصار لكلمة Constant أو ثابت.",
                explanation = "الكلمة المفتاحية const تنشئ إشارة للقيمة لا يمكن تغيير مقصده وإعادة تعيينه."
            )
            QuestionType.TRUE_FALSE -> ChallengeQuestion(
                id = id, language = "JavaScript", type = type, difficulty = diff, suggestedLesson = topic,
                questionText = "هل تعيد مقارنة النوع والقيمة باستخدام عامل التطابق الثلاثي '1 === 1' النتيجة true في لغة JS؟ (التحدي $i)",
                options = listOf("صح", "خطأ"),
                correctAnswer = "صح",
                hint = "يقارن القيمة والنوع معاً دون تسيير تلقائي للأرقام.",
                explanation = "النوع متطابق (رقم ورقم) والقيمتين متساويتين، بالتالي سيعيد ناتجاً صحيحاً تماماً."
            )
            QuestionType.CODE_COMPLETION -> ChallengeQuestion(
                id = id, language = "JavaScript", type = type, difficulty = diff, suggestedLesson = topic,
                questionText = "أكمل الكلمة المفتاحية المناسبة لإرجاع ناتج من الدالة الحسابية التالية:",
                codeSnippet = "function add(a, b) {\n  ___ a + b;\n}",
                options = listOf("return", "send", "give", "output"),
                correctAnswer = "return",
                hint = "كلمة مفتاحية مرادعة للإعادة وتنهي تنفيذ الدالة في الويب.",
                explanation = "تستخدم عبارة return لإرجاع ناتج ما عند المفسر وتعمل على الخروج من الدالة فوراً."
            )
            QuestionType.BUG_HUNTER -> ChallengeQuestion(
                id = id, language = "JavaScript", type = type, difficulty = diff, suggestedLesson = topic,
                questionText = "اكتشف الخلل في تصفية مصفوفة الأعداد الفردية وصححه:",
                codeSnippet = "const odd = [1, 2, 3].filter(n => n % 2 = 1);",
                options = listOf("filter(n => n % 2 === 1)", "filter(n => n % 2 == 1)", "filter(n => n % 2 == 0)", "filter(n => n & 1)"),
                correctAnswer = "filter(n => n % 2 === 1)",
                hint = "أنت تحاول المقارنة وليس تعيين القيمة بعلامة تساوي منفردة.",
                explanation = "استخدم عامل المساواة الثلاثي === للتحقق من الشرط، وليس عامل الإسناد المفرد =."
            )
            QuestionType.OUTPUT_PREDICTION -> ChallengeQuestion(
                id = id, language = "JavaScript", type = type, difficulty = diff, suggestedLesson = topic,
                questionText = "ماذا سيتم طباعته في تيرمينال المتصفح بعد تشغيل هذا الكود البسيط؟",
                codeSnippet = "console.log(typeof NaN);",
                options = listOf("number", "NaN", "undefined", "object"),
                correctAnswer = "number",
                hint = "رغم أنه يعني 'ليس رقماً' إلا أن مفسر الجافاسكريبت يصنفه تحت عائلة معينة.",
                explanation = "يُعد NaN (Not a Number) ذا نوع رقمي 'number' تماشياً مع معايير الحساب العائمة."
            )
            QuestionType.CODE_WRITING -> ChallengeQuestion(
                id = id, language = "JavaScript", type = type, difficulty = diff, suggestedLesson = topic,
                questionText = "اكتب الأسلوب الخاص بطابعة السجلات والنصوص بكونسول المتصفح (المخرج):",
                codeSnippet = "___",
                options = listOf("console.log", "print", "alert", "document.write"),
                correctAnswer = "console.log",
                hint = "يبدأ بكلمة لوحة التحكم console وتنتهي باللوج النقدي.",
                explanation = "الأسلوب القياسي لطباعة كشوفات وتتبع الأخطاء البرمجية في كونسول المتصفح هو console.log."
            )
        }
    }

    private fun generatePythonQuestion(id: String, i: Int, type: QuestionType, diff: Difficulty): ChallengeQuestion {
        val topic = when {
            i <= 10 -> "أدوات الطباعة وإسناد القيم"
            i <= 20 -> "الشروط والفهرسة الأساسية"
            i <= 30 -> "تكرارات الفوران والقوائم"
            i <= 40 -> "أدوات القوالب والقواميس الذكية"
            else -> "الوحدات البرمجية والأخطاء والاوبجكت"
        }

        return when (type) {
            QuestionType.MULTIPLE_CHOICE -> ChallengeQuestion(
                id = id, language = "Python", type = type, difficulty = diff, suggestedLesson = topic,
                questionText = "أي من العبارات الشرطية تستخدم لإعلان الفرع البديل في لغة بايثون؟ (التحدي $i)",
                options = listOf("elif", "else if", "elseif", "elsif"),
                correctAnswer = "elif",
                hint = "دمج الكلمتين 'else if' بذكاء.",
                explanation = "في بايثون تُستخدم الكلمة الاختصارية elif لصياغة شرط بديل وربطه بالاختبار الأول."
            )
            QuestionType.TRUE_FALSE -> ChallengeQuestion(
                id = id, language = "Python", type = type, difficulty = diff, suggestedLesson = topic,
                questionText = "هل تعتبر القوائم (Lists) في بايثون قابلة للتعديل والتحوير بينما القواميس الرقمية مغلقة الحركية؟ (التحدي $i)",
                options = listOf("صح", "خطأ"),
                correctAnswer = "خطأ",
                hint = "كلا الكائنين يقبل الإدراج، المسح، والتعديل بكافة الطرق البرمجية.",
                explanation = "كل من القوائم (Lists) والقواميس (Dictionaries) كائنات مرنة قابلة للتغيير (Mutable) في بايثون."
            )
            QuestionType.CODE_COMPLETION -> ChallengeQuestion(
                id = id, language = "Python", type = type, difficulty = diff, suggestedLesson = topic,
                questionText = "أكمل الإعلان الصحيح لاستيراد مكتبة عشوائية مدمجة في بايثون:",
                codeSnippet = "___ random\nprint(random.randint(1, 10))",
                options = listOf("import", "include", "require", "using"),
                correctAnswer = "import",
                hint = "كلمة برمجية من 6 حروف تعني جلب واستيراد.",
                explanation = "الكلمة المفتاحية import تستخدم في بايثون لاستيراد الوحدات والمكتبات المدمجة والخارجية."
            )
            QuestionType.BUG_HUNTER -> ChallengeQuestion(
                id = id, language = "Python", type = type, difficulty = diff, suggestedLesson = topic,
                questionText = "الكود التالي يقع في فخ خطأ التنسيق والمسافات (IndentationError). اكتشفه وصححه:",
                codeSnippet = "def run():\nprint('Running Code!')",
                options = listOf("  print('Running Code!')", "print('Running Code!')", "run(): print('Running Code!')", "def run(): print('Running Code!')"),
                correctAnswer = "  print('Running Code!')",
                hint = "بايثون تعتمد على المسافات البادئة بدلاً من الأقواس المعقوفة.",
                explanation = "يجب إزاحة السطور البرمجية الواقعة داخل محيط دالة بمسافة بادئة (غالباً 4 مسافات أو بادئة واحدة)."
            )
            QuestionType.OUTPUT_PREDICTION -> ChallengeQuestion(
                id = id, language = "Python", type = type, difficulty = diff, suggestedLesson = topic,
                questionText = "ما هو ناتج معالجة كود بايثون التالي لقائمة الأطعمة المدرجة؟",
                codeSnippet = "fruits = ['Apple', 'Banana']\nprint(fruits[-1])",
                options = listOf("Banana", "Apple", "IndexError: list index out of range", "None"),
                correctAnswer = "Banana",
                hint = "الفهرسة السالبة تبدأ قراءة القوائم من المؤخرة.",
                explanation = "الرقم -1 يشير للعنصر الأخير دائماً في القوائم في لغة بايثون وهو الموز."
            )
            QuestionType.CODE_WRITING -> ChallengeQuestion(
                id = id, language = "Python", type = type, difficulty = diff, suggestedLesson = topic,
                questionText = "اكتب الدالة المدمجة الشهيرة المسؤولة عن الحصول على طول القائمة أو النص في بايثون:",
                codeSnippet = "___",
                options = listOf("len", "length", "size", "count"),
                correctAnswer = "len",
                hint = "اختصار لـ Length وتتكون من 3 حروف.",
                explanation = "الدالة len() تعود بالعدد الكلي للعناصر بداخل القوائم والسلاسل في لغة بايثون."
            )
        }
    }

    private fun generateCQuestion(id: String, i: Int, type: QuestionType, diff: Difficulty): ChallengeQuestion {
        val topic = when {
            i <= 10 -> "بداية C والمترجمات السلوكية"
            i <= 20 -> "تحليل الشروط والتحويرات الأساسية"
            i <= 30 -> "مصفوفات الحروف والمؤشرات"
            i <= 40 -> "إدارة الذاكرة والتعيين الديناميكي"
            else -> "الملفات وهياكل البيانات وتشييد المكاتب"
        }

        return when (type) {
            QuestionType.MULTIPLE_CHOICE -> ChallengeQuestion(
                id = id, language = "C", type = type, difficulty = diff, suggestedLesson = topic,
                questionText = "ما هي الدالة القياسية لطباعة النصوص والمخرجات على الشاشة في لغة C السيادية؟ (التحدي $i)",
                options = listOf("printf", "print", "cout", "write"),
                correctAnswer = "printf",
                hint = "تبدأ بـ print وتليها f التي تشير للتنسيق (Formatted).",
                explanation = "تستخدم دالة printf المضمنة في ستوديو هيدر <stdio.h> لطباعة الأكواد والنصوص في لغة C."
            )
            QuestionType.TRUE_FALSE -> ChallengeQuestion(
                id = id, language = "C", type = type, difficulty = diff, suggestedLesson = topic,
                questionText = "هل يعتبر المؤشر (Pointer) في لغة C متغيراً يحمل عنواناً لذاكرة عنصر آخر بدلاً من القيمة الفعلية؟ (التحدي $i)",
                options = listOf("صح", "خطأ"),
                correctAnswer = "صح",
                hint = "إدارة العناوين والوصول المباشر للذاكرة.",
                explanation = "المؤشر هو عنوان لخلية في الذاكرة يحفظ مكان تخزين المتغير الأصلي ويسمح بالوصول الفائق إليه."
            )
            QuestionType.CODE_COMPLETION -> ChallengeQuestion(
                id = id, language = "C", type = type, difficulty = diff, suggestedLesson = topic,
                questionText = "اكتب محدد النوع الصحيح للأرقام الصحيحة عند طباعتها في دالة printf المنسقة:",
                codeSnippet = "int score = 100;\nprintf(\"SCORE: ___\\n\", score);",
                options = listOf("%d", "%f", "%c", "%s"),
                correctAnswer = "%d",
                hint = "يشير للأرقام العشرية الصحيحة Decimal.",
                explanation = "الرمز %d هو محدد التنسيق المعياري لطباعة قيم المتغيرات النصية الصحيحة (Integers) في لغة C."
            )
            QuestionType.BUG_HUNTER -> ChallengeQuestion(
                id = id, language = "C", type = type, difficulty = diff, suggestedLesson = topic,
                questionText = "المترجم يثور لإخبارك بوجود تسريب ذاكرة لعدم تحرير العنوان الديناميكي، أصلحه:",
                codeSnippet = "int *ptr = malloc(sizeof(int));\n*ptr = 10;\n// ماذا ينقص هنا؟",
                options = listOf("free(ptr);", "delete ptr;", "release(ptr);", "clear(ptr);"),
                correctAnswer = "free(ptr);",
                hint = "عكس حجز الذاكرة malloc هو تحريرها بكلمة إنجليزية من 4 حروف.",
                explanation = "في لغة C، تتطلب الذاكرة المحجوزة ديناميكياً بواسطة malloc تحريراً يدوياً صريحاً باستخدام free(ptr)."
            )
            QuestionType.OUTPUT_PREDICTION -> ChallengeQuestion(
                id = id, language = "C", type = type, difficulty = diff, suggestedLesson = topic,
                questionText = "ماذا سيطبع البرنامج التالي بعد المرور بالشرط والتحقق من القيمة؟",
                codeSnippet = "int x = 5;\nif(x = 10) {\n  printf(\"TRUE\");\n} else {\n  printf(\"FALSE\");\n}",
                options = listOf("TRUE", "FALSE", "Compilation Error", "null"),
                correctAnswer = "TRUE",
                hint = "لاحظ أن المكتوب بالشرط هو إسناد للقيمة '=' وليس مقارنة '==' وتعتبر 10 قيمة صحيحة كغير صفرية.",
                explanation = "الإسناد x = 10 يجعل قيمة التعبير 10، وتعتبر أي قيمة غير صفرية في لغة C هي معامل منطقي صحيح (True)."
            )
            QuestionType.CODE_WRITING -> ChallengeQuestion(
                id = id, language = "C", type = type, difficulty = diff, suggestedLesson = topic,
                questionText = "اكتب الكلمة المفتاحية لترميز البنى المركبة وقواعد البيانات المخصصة بـ C للمستخدمين:",
                codeSnippet = "___",
                options = listOf("struct", "class", "union", "typedef"),
                correctAnswer = "struct",
                hint = "اختصار لمفهوم هيكلية 'Structure' وتتكون من 6 حروف.",
                explanation = "الكلمة struct تُستخدم لتعريف وحشد حقول بيانية متنوعة تحت اسم هيكلي موحد في لغة C."
            )
        }
    }

    private fun generateCppQuestion(id: String, i: Int, type: QuestionType, diff: Difficulty): ChallengeQuestion {
        val topic = when {
            i <= 10 -> "المخرجات والتدفقات بـ C++"
            i <= 20 -> "توجيهات المكاتب والمحاذاة الشاملة"
            i <= 30 -> "متجهات STL وإسناد العناصر"
            i <= 40 -> "الفئات والوراثة الافتراضية"
            else -> "محللات الذاكرة والبوليمورفيزم"
        }

        return when (type) {
            QuestionType.MULTIPLE_CHOICE -> ChallengeQuestion(
                id = id, language = "C++", type = type, difficulty = diff, suggestedLesson = topic,
                questionText = "ما هو المعامل الصحيح لكتابة مخرج برمجي إلى كائن السي تيار المكتوب (cout) في C++؟ (التحدي $i)",
                options = listOf("<<", ">>", "<-", "->"),
                correctAnswer = "<<",
                hint = "سهمين يشيران لجهة دالة الطباعة.",
                explanation = "المعامل << هو معامل الإدراج الموجه لتدفق المخرجات cout لإسقاط النصوص المترجمة للمستخدم."
            )
            QuestionType.TRUE_FALSE -> ChallengeQuestion(
                id = id, language = "C++", type = type, difficulty = diff, suggestedLesson = topic,
                questionText = "هل يمكن استخدام الوراثة المتعددة (Multiple Inheritance) لوراثة فئة واحدة من فئات برمجية متعددة في C++؟ (التحدي $i)",
                options = listOf("صح", "خطأ"),
                correctAnswer = "صح",
                hint = "على عكس جافا وسي شارب اللتان تمنعان ذلك لمشاكل تضارب المهام.",
                explanation = "تدعم C++ الوراثة المتعددة بشكل صريح متكامل، وهي ميزة معمارية قوية تستلزم صيانة دقيقة."
            )
            QuestionType.CODE_COMPLETION -> ChallengeQuestion(
                id = id, language = "C++", type = type, difficulty = diff, suggestedLesson = topic,
                questionText = "أكمل الكلمة المفتاحية الصحيحة لتضمين مكتبة المتجهات vector في C++:",
                codeSnippet = "#___ <vector>\nusing namespace std;",
                options = listOf("include", "define", "import", "using"),
                correctAnswer = "include",
                hint = "تبدأ بعلامة الهاشتاق المكتشفة.",
                explanation = "أمر التوجيه #include يطلب من معالج البناء دمج ترويسة المكتبات مثل vector في ملف المشروع."
            )
            QuestionType.BUG_HUNTER -> ChallengeQuestion(
                id = id, language = "C++", type = type, difficulty = diff, suggestedLesson = topic,
                questionText = "هناك خطأ في تحرير مصفوفة الذاكرة الديناميكية المخصصة بمترجمات C++ الرائدة:",
                codeSnippet = "int *arr = new int[10];\n// ...\ndelete arr;",
                options = listOf("delete[] arr;", "free(arr);", "delete arr[];", "release arr;"),
                correctAnswer = "delete[] arr;",
                hint = "لتحرير مصفوفة محجوزة بـ new[] يجب استخدام وسم مصفوفة مناسب مع delete.",
                explanation = "استخدام delete[] arr هو الأسلوب الصحيح لمنع تسريب الذاكرة عند تدمير مصفوفة ديناميكية كاملة."
            )
            QuestionType.OUTPUT_PREDICTION -> ChallengeQuestion(
                id = id, language = "C++", type = type, difficulty = diff, suggestedLesson = topic,
                questionText = "ما هو ناتج تنفيذ البرنامج البرمجي التالي والمغلف بنظام نطاق std المعبر؟",
                codeSnippet = "int a = 10, b = 20;\nauto f = [&]() { a += 5; };\nf();\ncout << a;",
                options = listOf("15", "10", "20", "Compilation Error"),
                correctAnswer = "15",
                hint = "تم تمرير المتغيرات بالمرجع '&' بداخل تعبير لامدا المميز.",
                explanation = "المعامل [&] يتيح للدالة اللامدا الوصول للمتغير 'a' عبر مرجعه وتعديل قيمته الأصلية إلى 15."
            )
            QuestionType.CODE_WRITING -> ChallengeQuestion(
                id = id, language = "C++", type = type, difficulty = diff, suggestedLesson = topic,
                questionText = "اكتب الكلمة المفتاحية لرموز ونطاقات الأسماء الموحدة المستعملة بكثرة لتبسيط الكود كـ (using ___ std;):",
                codeSnippet = "___",
                options = listOf("namespace", "class", "module", "using"),
                correctAnswer = "namespace",
                hint = "مساحة الاسم تبسط تكرار std:: ويتكون المقدار من 9 حروف.",
                explanation = "كلمة namespace تعرف نطاقاً جامعاً للأسماء والدوال لتلافي تصادم المسميات البرمجية بوضوح."
            )
        }
    }

    private fun generateCsharpQuestion(id: String, i: Int, type: QuestionType, diff: Difficulty): ChallengeQuestion {
        val topic = when {
            i <= 10 -> "المتغيرات والنطاقات بـ C#"
            i <= 20 -> "أدوات التحويل وإسناد الأكواد"
            i <= 30 -> "المصفوفات وحلقات الفوريتش"
            i <= 40 -> "الخصائص والواجهات البرمجية"
            else -> "المهام اللامتزامنة وتعبيرات اللينك"
        }

        return when (type) {
            QuestionType.MULTIPLE_CHOICE -> ChallengeQuestion(
                id = id, language = "C#", type = type, difficulty = diff, suggestedLesson = topic,
                questionText = "أي من الكلمات المفتاحية تتيح الكشف التلقائي والمبسط لتعريف نوع المتغيرات وقت الترجمة بـ C#؟ (التحدي $i)",
                options = listOf("var", "let", "dynamic", "auto"),
                correctAnswer = "var",
                hint = "حروفها ثلاثة وهي مطابقة لأسلوب الجافا سكريبت.",
                explanation = "الاسم المستعار var يطلب من المجمع استنباط نوع المتغير الصحيح تلقائياً عند وقت البناء."
            )
            QuestionType.TRUE_FALSE -> ChallengeQuestion(
                id = id, language = "C#", type = type, difficulty = diff, suggestedLesson = topic,
                questionText = "هل يعتبر كائن النصوص (String) في لغة C# كائناً غير قابل للتغيير (Immutable) في الذاكرة؟ (التحدي $i)",
                options = listOf("صح", "خطأ"),
                correctAnswer = "صح",
                hint = "كلما عدلت نصاً، يتم تشييد كائن نصي جديد كلياً بالذاكرة الخلفية.",
                explanation = "النصوص في إطار C# و .NET ثابتة الذاكرة لتأمين وحماية البيانات من التصدعات الجانبية."
            )
            QuestionType.CODE_COMPLETION -> ChallengeQuestion(
                id = id, language = "C#", type = type, difficulty = diff, suggestedLesson = topic,
                questionText = "أكمل الكلمة المفتاحية لتمثيل الحلقة المتطورة لقراءة المتجهات والمصفوفات الكأداء:",
                codeSnippet = "string[] tech = {\"C#\", \"ASP\"};\n___ (var t in tech) {\n  Console.WriteLine(t);\n}",
                options = listOf("foreach", "for", "while", "each"),
                correctAnswer = "foreach",
                hint = "تعني 'لكل عنصر' مدمجة سوياً.",
                explanation = "تستخدم حلقة foreach للمرور وقراءة عناصر مصفوفة أو مجمع بيانات دون تدوين مؤشر يدوي."
            )
            QuestionType.BUG_HUNTER -> ChallengeQuestion(
                id = id, language = "C#", type = type, difficulty = diff, suggestedLesson = topic,
                questionText = "اكتشف الخلل وعالجه لإرسال المتغير كمرجع للتعديل الدقيق في دالة C# التالية:",
                codeSnippet = "void Setup(int value) {}\nint x = 10;\nSetup(ref x); // ما المشكلة؟",
                options = listOf("void Setup(ref int value) {}", "void Setup(out int value) {}", "void Setup(int &value) {}", "void Setup(pointer int value) {}"),
                correctAnswer = "void Setup(ref int value) {}",
                hint = "تحتاج لتطابق الكلمة ref في كل من نداء الدالة البرمجية وصياغتها المصدرية.",
                explanation = "عند تمرير متغير بالمرجع بواسطة ref، يجب وصد الكلمة ref في رأس تعريف الدالة أيضاً."
            )
            QuestionType.OUTPUT_PREDICTION -> ChallengeQuestion(
                id = id, language = "C#", type = type, difficulty = diff, suggestedLesson = topic,
                questionText = "ما هي القيمة المرتجعة للبرنامج بعد المرور بإسناد الخصائص التالية لخاصية العمر المدمجة؟",
                codeSnippet = "class User {\n  private int age = 10;\n  public int Age {\n    get { return age; }\n    set { age = value < 0 ? 0 : value; }\n  }\n}\n// User.Age = -5;",
                options = listOf("0", "-5", "10", "Exception error"),
                correctAnswer = "0",
                hint = "تمت تغذية المحرر بقيمة سالبة، راجع قانون صياغة الـ set الحاضرة.",
                explanation = "يقوم المشيد value بالتحقق من القيمة المدخلة، ولما كانت أقل من الصفر عيّن القيمة 0 تلقائياً."
            )
            QuestionType.CODE_WRITING -> ChallengeQuestion(
                id = id, language = "C#", type = type, difficulty = diff, suggestedLesson = topic,
                questionText = "اكتب الكلمة المفتاحية لترميز المكاتب والنطاقات الخارجية في قمة ملف C# لفتح المزايا:",
                codeSnippet = "___",
                options = listOf("using", "import", "include", "namespace"),
                correctAnswer = "using",
                hint = "حروفها خمسة وتعبر عن الاستخدام.",
                explanation = "الكلمة using تجعل النطاقات الخارجية متبادلة للوصول للمطورين مثل System لتفعيل لوحة التحكم."
            )
        }
    }

    private fun generateJavaQuestion(id: String, i: Int, type: QuestionType, diff: Difficulty): ChallengeQuestion {
        val topic = when {
            i <= 10 -> "الصياغات والفئات بـ Java"
            i <= 20 -> "تحليل الشروط وفحوص الذاكرة"
            i <= 30 -> "حلقات التكرار والمصنوفات"
            i <= 40 -> "البنى كائنية التوجه وتصميم الكلاس"
            else -> "الواجهات والاستثناءات ومراكز الجمع"
        }

        return when (type) {
            QuestionType.MULTIPLE_CHOICE -> ChallengeQuestion(
                id = id, language = "Java", type = type, difficulty = diff, suggestedLesson = topic,
                questionText = "ما هي الكلمة المفتاحية المستخدمة لصناعة كائن (Object) جديد بالذاكرة الـ Java الهرمية؟ (التحدي $i)",
                options = listOf("new", "class", "create", "instantiate"),
                correctAnswer = "new",
                hint = "ثلاثة حروف برمجية ترمز للجديد.",
                explanation = "تنشئ الكلمة new كائناً جديداً بالذاكرة وتخصص المساحة لمشيد الفئة المطلوب تمثيله جلياً."
            )
            QuestionType.TRUE_FALSE -> ChallengeQuestion(
                id = id, language = "Java", type = type, difficulty = diff, suggestedLesson = topic,
                questionText = "هل يعتبر جامع القمامة (Garbage Collector) ميزة مدمجة بآلة JVM لحل وتدمير الرموز غير المستغلة تلقائياً؟ (التحدي $i)",
                options = listOf("صح", "خطأ"),
                correctAnswer = "صح",
                hint = "تحرير الذاكرة دون تدخل المطور اليدوي المباشر.",
                explanation = "يتعقب جامع المهملات في جافا الكائنات غير المستخدمة ليتولى مسحها وتحرير الذاكرة تلقائياً بالخلفية."
            )
            QuestionType.CODE_COMPLETION -> ChallengeQuestion(
                id = id, language = "Java", type = type, difficulty = diff, suggestedLesson = topic,
                questionText = "أكمل مسار دالة وراثة الفئات الممثلة بالنمط المصمم بقواعد Java الهيكلية:",
                codeSnippet = "public class Dog ___ Animal {\n  // الكلب يرث كائن الحيوانات\n}",
                options = listOf("extends", "implements", "inherits", "extends_class"),
                correctAnswer = "extends",
                hint = "تعني 'يوسع' أو يعبر من 7 حروف تبدأ بـ ext.",
                explanation = "تستخدم كلمة extends لإجراء وراثة صريحة للفئات الفائقة، بينما implements لتنفيذ الواجهات."
            )
            QuestionType.BUG_HUNTER -> ChallengeQuestion(
                id = id, language = "Java", type = type, difficulty = diff, suggestedLesson = topic,
                questionText = "اكتشف الخلل وقم بالمقارنة الدقيقة للنصوص معاً بأسلوب جافا الصحيح والآمن:",
                codeSnippet = "String a = \"edu\";\nString b = \"edu\";\nif(a == b) { /* خطأ مقارنة هيكلي */ }",
                options = listOf("if(a.equals(b))", "if(a.compare(b))", "if(a === b)", "if(a.match(b))"),
                correctAnswer = "if(a.equals(b))",
                hint = "المقارنة الرياضية '==' تقارن المعرف وموقع الكائن وليس القيمة الفعلية للنصوص في جافا.",
                explanation = "دالة equals() تقارن المحتوى الفعلي للنصوص بينما المعامل == يقارن عناوين الذاكرة."
            )
            QuestionType.OUTPUT_PREDICTION -> ChallengeQuestion(
                id = id, language = "Java", type = type, difficulty = diff, suggestedLesson = topic,
                questionText = "ماذا سيطبع البرنامج المكتوب أدناه في موجه مخرجات Java الهيكلية؟",
                codeSnippet = "int[] numbers = {1, 2, 3};\ntry {\n  System.out.println(numbers[3]);\n} catch(ArrayIndexOutOfBoundsException e) {\n  System.out.println(\"OUT\");\n}",
                options = listOf("OUT", "3", "ArrayIndexOutOfBoundsException", "null"),
                correctAnswer = "OUT",
                hint = "الفهرسة للمصفوفة ذات 3 عناصر تنتهي عند الرقم 2، بينما حاولنا الوصول لموقع مستحيل.",
                explanation = "يحصل استثناء لتجاوز حيز المصفوفة، ويلتقطه المفسر فوراً لينفذ عبارة الطباعة المكتوبة بـ OUT."
            )
            QuestionType.CODE_WRITING -> ChallengeQuestion(
                id = id, language = "Java", type = type, difficulty = diff, suggestedLesson = topic,
                questionText = "اكتب الكلمة المفتاحية المعرفة للمتغيرات والثوابت ذات القيمة الواحدة وغير القابلة للتغيير بجافا:",
                codeSnippet = "___",
                options = listOf("final", "const", "static", "sealed"),
                correctAnswer = "final",
                hint = "مماثل لكلمة 'نهائي' البرمجية الحتمية.",
                explanation = "في جافا، تعرّف الكلمة final الثوابت وتمنع إعادة الكتابة على تعريف المتغير أو وراثة الكلاس."
            )
        }
    }

    private fun generatePhpQuestion(id: String, i: Int, type: QuestionType, diff: Difficulty): ChallengeQuestion {
        val topic = when {
            i <= 10 -> "قواعد طباعة PHP الأساسية"
            i <= 20 -> "تحليل النصوص والرموز"
            i <= 30 -> "تكرار الويب والتشابك الخفي"
            i <= 40 -> "مدخلات النماذج والواجهات الذكية"
            else -> "محللات الجلسة والأوبجكت المتقدم"
        }

        return when (type) {
            QuestionType.MULTIPLE_CHOICE -> ChallengeQuestion(
                id = id, language = "PHP", type = type, difficulty = diff, suggestedLesson = topic,
                questionText = "ما هي الحاوية أو الرمز البرمجي الصحيح والمفروض لبدء أي وسم PHP لتفسير الكود بالمخدم؟ (التحدي $i)",
                options = listOf("<?php", "<php>", "<script>", "<?"),
                correctAnswer = "<?php",
                hint = "يبدأ بمثبت علامة استفهام ولفظ لغة البرمجة.",
                explanation = "الوسم المعماري القياسي والآمن المعتمد للمتصفحات والمخدمات لتفسير الأكواد هو <?php."
            )
            QuestionType.TRUE_FALSE -> ChallengeQuestion(
                id = id, language = "PHP", type = type, difficulty = diff, suggestedLesson = topic,
                questionText = "هل نستخدم علامة الضم النقطي '.' بدلاً من علامة الجمع المعتاد لدعم دمج النصوص المبرمجة بـ PHP؟ (التحدي $i)",
                options = listOf("صح", "خطأ"),
                correctAnswer = "صح",
                hint = "الربط النصي للمتغيرات يتم بوضع نقطة بين السجلين.",
                explanation = "تستخدم النقطة . في لغة PHP كمعامل للربط (String Concatenation) والدمج المباشر بذكاء."
            )
            QuestionType.CODE_COMPLETION -> ChallengeQuestion(
                id = id, language = "PHP", type = type, difficulty = diff, suggestedLesson = topic,
                questionText = "أكمل الرمز المفروض لصناعة مصفوفة تفاعلية مترجمة تترأس الويب العربي بـ PHP:",
                codeSnippet = "${'$'}langs = ___(\"PHP\", \"SQL\");",
                options = listOf("array", "list", "dict", "collection"),
                correctAnswer = "array",
                hint = "كلمة من 5 أحرف تعني هيكل طابور مصفوفة.",
                explanation = "تستخدم دالة array() لبناء وتوليد مصفوفة برمجية ذكية تتسع للعديد من الأغراض الحركية."
            )
            QuestionType.BUG_HUNTER -> ChallengeQuestion(
                id = id, language = "PHP", type = type, difficulty = diff, suggestedLesson = topic,
                questionText = "اكتشف الخلل في ترميز استدعاء متغير لتدفق نصوص PHP وحله بالنوعية المطلوبة:",
                codeSnippet = "${'$'}title = 'إيدوكود';\necho 'مرحباً بك في ${'$'}title'; // تمنع المعالجة الحية للمتغير",
                options = listOf("echo \"مرحباً بك في {${'$'}title}\";", "echo 'مرحباً بك في . ${'$'}title';", "echo \"مرحباً بك في \" + ${'$'}title;", "echo ${'$'}title;"),
                correctAnswer = "echo \"مرحباً بك في {${'$'}title}\";",
                hint = "علامات التنصيص الزوجية (\") تقبل تمثيل وعلاج المتغيرات داخلها، بينما الفردية تعاملهم كنصوص خرساء.",
                explanation = "النص ذو علامات الاقتباس الثنائية يعالج قيم المتغيرات ويستبدلها بسلاسة وقت التفسير."
            )
            QuestionType.OUTPUT_PREDICTION -> ChallengeQuestion(
                id = id, language = "PHP", type = type, difficulty = diff, suggestedLesson = topic,
                questionText = "ما هو ناتج تتبع كود الويب البرمجي الذكي المخصص لتدقيق الجلسات هنا؟",
                codeSnippet = "${'$'}val = isset(${'$'}undefined_var) ? 'CHALLENGE' : 'LEARN';\necho ${'$'}val;",
                options = listOf("LEARN", "CHALLENGE", "Notice: Undefined Variable", "null"),
                correctAnswer = "LEARN",
                hint = "تفحص دالة isset صحة استيقاظ وتوافر المتغير المعرّف قبل البدء.",
                explanation = "المتغير غير معرّف، بالتالي تعيد دالة isset المقدار false، ليعالج الفرع البديل المقدار 'LEARN'."
            )
            QuestionType.CODE_WRITING -> ChallengeQuestion(
                id = id, language = "PHP", type = type, difficulty = diff, suggestedLesson = topic,
                questionText = "اكتب الرمز الشهير والمحتم الذي تبدأ به جميع أسماء المتغيرات بلا استثناء في لغة PHP:",
                codeSnippet = "___",
                options = listOf("$", "@", "#", "&"),
                correctAnswer = "$",
                hint = "رمز العملة الشهير 'الدولار'.",
                explanation = "كافة المتغيرات تبدأ برمز علامة الدولار $ لإعلام مفسر اللغة عن تعريف مرجع بالذاكرة."
            )
        }
    }

    private fun generateRustQuestion(id: String, i: Int, type: QuestionType, diff: Difficulty): ChallengeQuestion {
        val topic = when {
            i <= 10 -> "أدوات ومصانع الأكواد بـ Rust"
            i <= 20 -> "تحليل الأنواع الأولية ومصائد الذاكرة"
            i <= 30 -> "تكرارات التماسك والدوران المنظم"
            i <= 40 -> "الملكية والمطابقة النمطية"
            else -> "معالجات الاختيارات الآمنة والمكروه"
        }

        return when (type) {
            QuestionType.MULTIPLE_CHOICE -> ChallengeQuestion(
                id = id, language = "Rust", type = type, difficulty = diff, suggestedLesson = topic,
                questionText = "أي من الكلمات المفتاحية توفر إمكانية تعديل وتحوير المتغيرات وقت تشغيل Rust الآمن؟ (التحدي $i)",
                options = listOf("mut", "var", "let", "ref"),
                correctAnswer = "mut",
                hint = "التفعيل للتأجير والتغيير باللفظ Mutation.",
                explanation = "المتغيرات بـ Rust تعتبر محصنة وثابتة (Immutable) افتراضياً ولتغيير قيمتها تصرح باسم mut."
            )
            QuestionType.TRUE_FALSE -> ChallengeQuestion(
                id = id, language = "Rust", type = type, difficulty = diff, suggestedLesson = topic,
                questionText = "هل يوفر محرك الاستعارة وتدقيق النطاقات (Borrow Checker) حماية متكاملة ضد سباقات البيانات دون جامع قمامة؟ (التحدي $i)",
                options = listOf("صح", "خطأ"),
                correctAnswer = "صح",
                hint = "الاستقصاء الآمن وقت التصنيف وتدقيق الملكية للبنى العتادية.",
                explanation = "مترجم Rust يدقق في استعارة وملكية البيانات لمنع المشاكل المعمارية دون إحداث بطء وقت التشغيل."
            )
            QuestionType.CODE_COMPLETION -> ChallengeQuestion(
                id = id, language = "Rust", type = type, difficulty = diff, suggestedLesson = topic,
                questionText = "أكمل دالة تدمير المتغيرات الآمن واستحقاق المخرجات القياسية بـ Rust:",
                codeSnippet = "___!(\"HELLO CODER\");",
                options = listOf("println", "print", "log", "write"),
                correctAnswer = "println",
                hint = "استدعاء ماكرو مميز مرفق بمثبت النداء علامة التعجب (!).",
                explanation = "ماكرو println! يعمل على معالجة السطر وطباعته في موجه مخرجات الطرفيات بمثالية."
            )
            QuestionType.BUG_HUNTER -> ChallengeQuestion(
                id = id, language = "Rust", type = type, difficulty = diff, suggestedLesson = topic,
                questionText = "المترجم يثور لرفض محاولة تصدير وتعديل ثابت مخفي، فك التشفير وعالجه بالصحيح:",
                codeSnippet = "let x = 5;\nx = 10; // خطأ وقت الترجمة",
                options = listOf("let mut x = 5;", "mut x = 5;", "let x = mut 5;", "const x = 5;"),
                correctAnswer = "let mut x = 5;",
                hint = "أرفق كلمة المطواعية المكونة من ثلاثة حروف بجانب المورفيم الأصلي.",
                explanation = "لإعطاء الصلاحية للمتغير لإعادة التعيين والتطوير التلقائي، صرح به كـ let mut x = 5."
            )
            QuestionType.OUTPUT_PREDICTION -> ChallengeQuestion(
                id = id, language = "Rust", type = type, difficulty = diff, suggestedLesson = topic,
                questionText = "ما هي القيمة المخرجة للتدفق البرمجي الحاضر في الكود الشرطي لراصت؟",
                codeSnippet = "let x = if true { \"EDU\" } else { \"CODE\" };\nprintln!(\"{}\", x);",
                options = listOf("EDU", "CODE", "SyntaxError", "null"),
                correctAnswer = "EDU",
                hint = "العالم الشرطي في راصت يعمل كوحدة قيمية ناتجة وتعيد المقادير.",
                explanation = "بما أن الشرط صحيح، سيتحقق المخرج الأول وتُمنح قيمته للمستقبل x لتتم طباعته بكفاءة."
            )
            QuestionType.CODE_WRITING -> ChallengeQuestion(
                id = id, language = "Rust", type = type, difficulty = diff, suggestedLesson = topic,
                questionText = "اكتب الكلمة المفتاحية المعيارية المخصصة لبدء تعريف الدوال الحيوية في لغة Rust:",
                codeSnippet = "___",
                options = listOf("fn", "func", "def", "function"),
                correctAnswer = "fn",
                hint = "تتكون من حرفين برمجين مميزين.",
                explanation = "الكلمة fn هي المفتاح لتعريف كافة الدوال وسياقات التنفيذ الذاكراتية بلغة Rust المتفوقة."
            )
        }
    }

    private fun generateGenericQuestion(
        id: String, 
        lang: String, 
        i: Int, 
        type: QuestionType, 
        diff: Difficulty
    ): ChallengeQuestion {
        return ChallengeQuestion(
            id = id, language = lang, type = type, difficulty = diff, suggestedLesson = "أساسيات لغة $lang",
            questionText = "السؤال الحركي $i لـ $lang: هل القواعد الأساسية للمتغيرات مهمة ومطلوبة؟",
            options = listOf("صح", "خطأ"),
            correctAnswer = "صح",
            hint = "البداية القوية لأي مسار علمي.",
            explanation = "المبنى والمفاهيم الأساسية تمنح المطور صلابة وتساعده لحل المعضلات الاحترافية بطلاقة."
        )
    }
}
