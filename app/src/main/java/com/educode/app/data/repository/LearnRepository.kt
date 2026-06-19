package com.educode.app.data.repository

import com.educode.app.data.local.LearnDao
import com.educode.app.data.local.entity.ChapterEntity
import com.educode.app.data.local.entity.CourseEntity
import com.educode.app.data.local.entity.LessonEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LearnRepository(private val learnDao: LearnDao) {

    fun getCoursesByLanguage(language: String): Flow<List<CourseEntity>> =
        learnDao.getCoursesByLanguage(language)

    fun getChaptersForCourse(courseId: String): Flow<List<ChapterEntity>> =
        learnDao.getChaptersForCourse(courseId)

    fun getLessonsForChapter(chapterId: String): Flow<List<LessonEntity>> =
        learnDao.getLessonsForChapter(chapterId)
        
    suspend fun getLessonDetails(lessonId: String): LessonEntity? =
        learnDao.getLessonById(lessonId)

    suspend fun markLessonCompleted(lessonId: String): Boolean = withContext(Dispatchers.IO) {
        val lesson = learnDao.getLessonById(lessonId) ?: return@withContext false
        if (lesson.isCompleted) return@withContext false
        
        learnDao.markLessonCompleted(lessonId)
        
        // Check if all lessons in the course are completed
        val chapter = learnDao.getChapterById(lesson.chapterId) ?: return@withContext false
        val courseId = chapter.courseId
        val lessons = learnDao.getLessonsForCourseSync(courseId)
        val allCompleted = lessons.all { it.isCompleted }
        
        allCompleted
    }

    suspend fun fetchAndCacheContentForLanguage(language: String) = withContext(Dispatchers.IO) {
        // Here we simulate fetching from an API (MDN, W3Schools, etc.)
        // Since we don't have a real API, we seed the database with high-quality simulated data
        val existingCourses = learnDao.getCoursesByLanguage(language).firstOrNull()
        if (existingCourses.isNullOrEmpty()) {
            val courseId = "${language.lowercase()}_fundamentals"
            val course = CourseEntity(
                id = courseId,
                title = "احتراف $language",
                description = "مسار شامل لتعلم $language من الصفر حتى مستوى متقدم.",
                language = language,
                coverImage = null,
                totalChapters = 4,
                totalLessons = 20,
                orderIndex = 0
            )

            val chaptersInfo = listOf(
                Pair("مبتدئ: الأساسيات", "مفاهيم أساسية لبدء البرمجة"),
                Pair("متوسط: التحكم والبيانات", "الهياكل الأساسية للتحكم بالتدفق"),
                Pair("متقدم: البرمجة الكائنية والوظائف", "استخدام النماذج المتقدمة"),
                Pair("احترافي: الأداء العالي والمكتبات", "التعامل مع المشاريع والسرعة")
            )

            val chapters = chaptersInfo.mapIndexed { index, info ->
                ChapterEntity("ch${index}_$courseId", courseId, info.first, info.second, index)
            }

            val sourceNameMap = mapOf(
                "html" to Pair("MDN Web Docs", "https://developer.mozilla.org/"),
                "css" to Pair("W3Schools", "https://www.w3schools.com/"),
                "javascript" to Pair("MDN Web Docs", "https://developer.mozilla.org/"),
                "python" to Pair("Python Documentation", "https://docs.python.org/"),
                "c++" to Pair("GeeksForGeeks", "https://www.geeksforgeeks.org/"),
                "c#" to Pair("Microsoft Learn", "https://learn.microsoft.com/"),
                "java" to Pair("Oracle Documentation", "https://docs.oracle.com/en/java/"),
                "php" to Pair("PHP Documentation", "https://www.php.net/manual/en/"),
                "rust" to Pair("Rust Documentation", "https://doc.rust-lang.org/"),
                "c" to Pair("GeeksForGeeks", "https://www.geeksforgeeks.org/")
            )

            val source = sourceNameMap[language.lowercase()] ?: Pair("FreeCodeCamp", "https://www.freecodecamp.org/")
            
            fun generateDetailedContent(lang: String, title: String): String {
                return """
### مقدمة شاملة عن $title في $lang

أهلاً بك في هذا الدرس التفصيلي والمهم جداً في رحلتك لاحتراف لغة $lang. يعتبر فهم هذا المفهوم ($title) حجر الأساس لبناء تطبيقات قوية، مستقرة، وقابلة للتوسع.

#### 1. لماذا هذا الدرس مهم؟
عندما نتحدث عن $title، فإننا نتحدث عن مهارة أساسية يجب على كل مبرمج إتقانها. في لغة $lang، يتميز هذا المفهوم بمرونته العالية وقوته في تبسيط الأكواد المعقدة. الاستخدام الصحيح له يساعدك على:
*   **توفير الوقت:** كتابة كود نظيف يقلل من وقت صيانة التطبيق واكتشاف الأخطاء.
*   **إعادة الاستخدام (Reusability):** يساعدك هذا المفهوم على استخدام نفس الهيكل البرمجي في أكثر من مكان دون الحاجة لتكرار الكود.
*   **الأداء العالي:** التنفيذ السليم للمفاهيم الأساسية والمتقدمة يزيد من سرعة وأداء البرنامج بشكل ملحوظ.

#### 2. كيف يعمل $title تحت غطاء المحرك؟
لغة $lang تتعامل مع $title بطريقة منهجية. حيث تقوم المترجمات (Compilers) أو المفسرات (Interpreters) بتحليل وتفكيك هذه الأكواد في الذاكرة لتخصيص الموارد بأفضل شكل ممكن والتأكد من توافقها مع نظام التشغيل.

عليك دائماً أن تتذكر:
1.  **المصادر محدودة:** استخدام $title بشكل صحيح وتحرير الموارد غير المستخدمة يوفر من استهلاك الذاكرة.
2.  **وضوح الكود (Readability):** التنظيم المنطقي وتنسيق الأكواد يسهل على المبرمجين الآخرين فهم ما كتبته ضمن فريق العمل.
3.  **التوثيق المستمر:** قراءة المصادر الرسمية (Documentation) سيجعلك دائماً مطلعاً على أحدث التحديثات والممارسات الجيدة (Best Practices).

#### 3. حالات الاستخدام العملية (Real-world Use Cases)
دعنا نتخيل أنك تقوم ببناء نظام تجارة إلكترونية متطور، أو نظام بنكي معقد. هنا ستبرز الحاجة الماسة لاستخدام $title والتعامل باحترافية لضمان:
*   معالجة كميات ضخمة من البيانات بسرعة فائقة ودون أخطاء.
*   تحديد الاستثناءات (Exceptions) قبل وقوعها بفضل المعالجة المسبقة.
*   العمل مع الفرق البرمجية (Agile Teams) بانسجام لأن الجميع يفهم البنية الموحدة.

#### 4. أفضل الممارسات لتكون مبرمجاً محترفاً (Senior Practices)
لتصل إلى مستوى الاحتراف في $lang، يجب ألا تكتفي بتعلم قواعد اللغة (Syntax) فقط، بل عليك اتباع هذه المعايير عند العمل على $title:
*   **تسمية واضحة (Clean Code):** لا تستخدم أسماء متغيرة مبهمة، بل اجعل الأسماء تعبر عن الاستخدام الفعلي والهدف.
*   **الابتعاد عن التعقيد (KISS):** اكتب كوداً بسيطاً قدر الإمكان (Keep It Simple, Stupid). الحلول المعقدة ليست بالضرورة أفضل من الحلول البسيطة.
*   **الاختبار (Testing):** تأكد دائماً من اختبار كل جزء من الكود البرمجي لضمان عدم تعارضه مع الأجزاء الأخرى مستقبلاً.

#### 5. خطوتك العملية التالية 
بعد هذه المقدمة النظرية، يأتي دور التطبيق. البرمجة ممتعة حين تكتبها بنفسك. لا تكتفِ بالقراءة، بل قم بفتح المحرر البرمجي وجرب الأكواد المرفقة في قسم الأمثلة بالأسفل. قم بتعديلها، اختبرها، وتعمد إحداث أخطاء برمجية لترى كيف يتصرف النظام وتتعلم من رسائل الأخطاء (Error Logs).
                """.trimIndent()
            }
            
            val lessons = mutableListOf<LessonEntity>()
            var globalLessonIndex = 0

            val lessonTitles = listOf(
                "مقدمة وتثبيت البيئة", "المتغيرات وأنواع البيانات", "المدخلات والمخرجات", "العمليات الحسابية", "التعليقات وأفضل الممارسات", // Beginner
                "الجمل الشرطية (If/Else)", "حلقات التكرار (For loop)", "حلقات التكرار (While loop)", "المصفوفات (Arrays/Lists)", "السلاسل النصية (Strings)", // Intermediate
                "الدوال (Functions)", "استدعاء الدوال وتمرير القيم", "الهياكل (Structs/Classes) جزء 1", "الهياكل جزء 2", "معالجة الأخطاء (Exceptions)", // Advanced
                "إدارة الذاكرة والمؤشرات", "البرمجة غير المتزامنة (Async)", "المكتبات الخارجية", "تنظيم الكود والملفات", "مشروع التخرج" // Expert
            )
            val difficulties = listOf("مبتدئ", "متوسط", "متقدم", "احترافي")

            chapters.forEachIndexed { chapterIndex, chapter ->
                val chapterDifficulty = difficulties[chapterIndex]
                for (i in 0 until 5) {
                    val lessonTitleStr = lessonTitles[globalLessonIndex]
                    val lessonId = "ls_${chapter.id}_$i"
                    lessons.add(
                        LessonEntity(
                            id = lessonId,
                            chapterId = chapter.id,
                            title = "${globalLessonIndex + 1}. $lessonTitleStr",
                            description = "دليل شامل حول $lessonTitleStr وكيفية تطبيقه عملياً في $language.",
                            contentMarkdown = generateDetailedContent(language, lessonTitleStr),
                            difficulty = chapterDifficulty,
                            estimatedDurationMinutes = (15..30).random(),
                            xpReward = (50 * (chapterIndex + 1)),
                            orderIndex = globalLessonIndex,
                            sourceName = source.first,
                            sourceUrl = source.second,
                            isCompleted = false,
                            codeExamplesJson = """[{"title": "مثال تطبيقي عملي", "code": "// قم بتجربة هذا الكود في بيئة العمل الخاصة بك\n// الدرس: $lessonTitleStr\n// تذكر دائماً أن التطبيق العملي هو سر البرمجة\nprintln(\"مرحباً بك في عالم $language!\");"}]""",
                            lastUpdatedDate = System.currentTimeMillis() - (1000..100000000).random()
                        )
                    )
                    globalLessonIndex++
                }
            }

            learnDao.insertCourse(course)
            learnDao.insertChapters(chapters)
            learnDao.insertLessons(lessons)
        }
    }
}
