package com.educode.app.data.repository

import com.educode.app.domain.models.BugReport
import com.educode.app.domain.repository.BugReportRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FirebaseBugReportRepository(private val firestore: FirebaseFirestore) : BugReportRepository {
    override suspend fun submitReport(report: BugReport): Result<Unit> {
        return try {
            val docRef = if (report.id.isEmpty()) {
                firestore.collection("bug_reports").document()
            } else {
                firestore.collection("bug_reports").document(report.id)
            }
            
            docRef.set(report.copy(id = docRef.id)).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
