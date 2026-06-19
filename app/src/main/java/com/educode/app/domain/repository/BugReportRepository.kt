package com.educode.app.domain.repository

import com.educode.app.domain.models.BugReport

interface BugReportRepository {
    suspend fun submitReport(report: BugReport): Result<Unit>
}
