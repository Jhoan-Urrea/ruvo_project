package com.example.ruvo_app.domain.repository

import com.example.ruvo_app.domain.model.Report
import com.example.ruvo_app.domain.model.ReportStatus
import kotlinx.coroutines.flow.Flow

interface ReportRepository {
    suspend fun createReport(report: Report): Result<Unit>
    fun getReports(): Flow<List<Report>>
    fun getReportsByStatus(status: ReportStatus): Flow<List<Report>>
    suspend fun updateReportStatus(reportId: String, status: ReportStatus, note: String?, moderatorId: String): Result<Unit>
}
