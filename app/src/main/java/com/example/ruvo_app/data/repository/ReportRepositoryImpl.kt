package com.example.ruvo_app.data.repository

import com.example.ruvo_app.domain.model.Report
import com.example.ruvo_app.domain.model.ReportStatus
import com.example.ruvo_app.domain.repository.ReportRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReportRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : ReportRepository {

    override suspend fun createReport(report: Report): Result<Unit> {
        return try {
            val docRef = firestore.collection("reports").document()
            val reportWithId = report.copy(id = docRef.id)
            docRef.set(ReportDto.fromDomain(reportWithId)).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getReports(): Flow<List<Report>> = callbackFlow {
        val subscription = firestore.collection("reports")
            .orderBy("createdAt")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val reports = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(ReportDto::class.java)?.toDomain(doc.id)
                } ?: emptyList()
                trySend(reports)
            }
        awaitClose { subscription.remove() }
    }

    override fun getReportsByStatus(status: ReportStatus): Flow<List<Report>> = callbackFlow {
        val subscription = firestore.collection("reports")
            .whereEqualTo("status", status.name)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val reports = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(ReportDto::class.java)?.toDomain(doc.id)
                } ?: emptyList()
                trySend(reports)
            }
        awaitClose { subscription.remove() }
    }

    override suspend fun updateReportStatus(
        reportId: String,
        status: ReportStatus,
        note: String?,
        moderatorId: String
    ): Result<Unit> {
        return try {
            firestore.collection("reports").document(reportId)
                .update(
                    mapOf(
                        "status" to status.name,
                        "resolutionNote" to note,
                        "resolvedBy" to moderatorId,
                        "resolvedAt" to System.currentTimeMillis()
                    )
                ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

data class ReportDto(
    val id: String = "",
    val reportedId: String = "",
    val reporterId: String = "",
    val type: String = "SERVICIO",
    val reason: String = "",
    val description: String = "",
    val status: String = "PENDIENTE",
    val createdAt: Long = 0L,
    val resolvedAt: Long? = null,
    val resolvedBy: String? = null,
    val resolutionNote: String? = null
) {
    fun toDomain(id: String) = Report(
        id = id,
        reportedId = reportedId,
        reporterId = reporterId,
        type = com.example.ruvo_app.domain.model.ReportType.valueOf(type),
        reason = reason,
        description = description,
        status = com.example.ruvo_app.domain.model.ReportStatus.valueOf(status),
        createdAt = createdAt,
        resolvedAt = resolvedAt,
        resolvedBy = resolvedBy,
        resolutionNote = resolutionNote
    )

    companion object {
        fun fromDomain(report: Report) = ReportDto(
            id = report.id,
            reportedId = report.reportedId,
            reporterId = report.reporterId,
            type = report.type.name,
            reason = report.reason,
            description = report.description,
            status = report.status.name,
            createdAt = report.createdAt,
            resolvedAt = report.resolvedAt,
            resolvedBy = report.resolvedBy,
            resolutionNote = report.resolutionNote
        )
    }
}
