package com.educode.app.features.learnmode

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.educode.app.data.local.entity.ChapterEntity
import com.educode.app.data.local.entity.CourseEntity
import com.educode.app.data.local.entity.LessonEntity
import com.educode.app.data.repository.LearnRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class LearnViewModel(private val repository: LearnRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(LearnUiState())
    val uiState: StateFlow<LearnUiState> = _uiState.asStateFlow()

    private var coursesJob: Job? = null
    private var chaptersJob: Job? = null
    private val lessonJobs = mutableMapOf<String, Job>()

    fun loadContentForLanguage(language: String) {
        _uiState.value = _uiState.value.copy(isLoading = true, currentLanguage = language, error = null)
        viewModelScope.launch {
            try {
                // First simulate fetching
                repository.fetchAndCacheContentForLanguage(language)
                
                // Then observe DB
                coursesJob?.cancel()
                coursesJob = repository.getCoursesByLanguage(language)
                    .onEach { courses ->
                        _uiState.value = _uiState.value.copy(courses = courses, isLoading = false)
                        if (courses.isNotEmpty()) {
                            loadChaptersForCourse(courses.first().id)
                        }
                    }
                    .catch { _uiState.value = _uiState.value.copy(isLoading = false, error = it.message) }
                    .launchIn(viewModelScope)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message ?: "حدث خطأ غير معروف")
            }
        }
    }

    private fun loadChaptersForCourse(courseId: String) {
        chaptersJob?.cancel()
        chaptersJob = repository.getChaptersForCourse(courseId)
            .onEach { chapters ->
                _uiState.value = _uiState.value.copy(chapters = chapters)
                // Load lessons for all chapters to build the roadmap
                chapters.forEach { chapter ->
                    loadLessonsForChapter(chapter.id)
                }
            }
            .launchIn(viewModelScope)
    }

    private fun loadLessonsForChapter(chapterId: String) {
        lessonJobs[chapterId]?.cancel()
        lessonJobs[chapterId] = repository.getLessonsForChapter(chapterId)
            .onEach { lessons ->
                val updatedLessons = _uiState.value.lessonsMap.toMutableMap()
                updatedLessons[chapterId] = lessons
                _uiState.value = _uiState.value.copy(lessonsMap = updatedLessons)
            }
            .launchIn(viewModelScope)
    }
}

data class LearnUiState(
    val isLoading: Boolean = false,
    val currentLanguage: String = "",
    val courses: List<CourseEntity> = emptyList(),
    val chapters: List<ChapterEntity> = emptyList(),
    val lessonsMap: Map<String, List<LessonEntity>> = emptyMap(),
    val error: String? = null
)

class LearnViewModelFactory(private val repository: LearnRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LearnViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return LearnViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
