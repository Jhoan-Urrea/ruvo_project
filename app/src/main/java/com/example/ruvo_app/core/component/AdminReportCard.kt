package com.example.ruvo_app.core.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ruvo_app.domain.model.Report
import com.example.ruvo_app.domain.model.ReportStatus
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun AdminReportCard(
    report: Report,
    reportedItemAnalysis: String? = null,
    reportedItemTrustScore: Double? = null,
    reportedItemTrustDetails: Map<String, String> = emptyMap(),
    onResolve: (ReportStatus, String?) -> Unit
) {
    val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    val dateStr = sdf.format(Date(report.createdAt))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = when (report.status) {
                        ReportStatus.PENDIENTE -> Color(0xFFFEE2E2)
                        ReportStatus.EN_REVISION -> Color(0xFFFEF3C7)
                        ReportStatus.RESUELTO -> Color(0xFFDCFCE7)
                        ReportStatus.DESESTIMADO -> Color(0xFFF3F4F6)
                    },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = report.status.name,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = when (report.status) {
                            ReportStatus.PENDIENTE -> Color(0xFF991B1B)
                            ReportStatus.EN_REVISION -> Color(0xFF92400E)
                            ReportStatus.RESUELTO -> Color(0xFF166534)
                            ReportStatus.DESESTIMADO -> Color(0xFF374151)
                        }
                    )
                }
                Text(text = dateStr, fontSize = 10.sp, color = Color.Gray)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = report.reason,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = report.description,
                fontSize = 13.sp,
                color = Color.DarkGray,
                lineHeight = 18.sp
            )

            // IA CONTEXT SECTION
            if (reportedItemAnalysis != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    color = Color(0xFFF1F5F9),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Shield, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Contexto de Seguridad IA", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                            if (reportedItemTrustScore != null) {
                                Spacer(modifier = Modifier.weight(1f))
                                Text("${reportedItemTrustScore.toInt()}% Trust", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = reportedItemAnalysis,
                            fontSize = 11.sp,
                            color = Color.DarkGray,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                        )

                        // Mostrar alertas detalladas si existen en el mapa
                        reportedItemTrustDetails.forEach { (key, value) ->
                            if (key.contains("alerta", ignoreCase = true) || key.contains("evidencia", ignoreCase = true)) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(verticalAlignment = Alignment.Top) {
                                    Icon(
                                        imageVector = if (key.contains("alerta")) Icons.Default.Warning else Icons.Default.Info,
                                        contentDescription = null,
                                        tint = if (key.contains("alerta")) Color(0xFFEF4444) else Color.Gray,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = value,
                                        fontSize = 10.sp,
                                        color = if (key.contains("alerta")) Color(0xFFEF4444) else Color.DarkGray,
                                        fontWeight = FontWeight.Bold,
                                        lineHeight = 13.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Surface(
                color = Color(0xFFF8FAFC),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Info, null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Reporte ID: ${report.id.takeLast(8)}... | Objeto: ${report.type}",
                        fontSize = 10.sp,
                        color = Color.Gray
                    )
                }
            }

            if (report.status == ReportStatus.PENDIENTE || report.status == ReportStatus.EN_REVISION) {
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { onResolve(ReportStatus.DESESTIMADO, "Desestimado tras revisión") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Icon(Icons.Default.Close, null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Desestimar", fontSize = 11.sp)
                    }
                    Button(
                        onClick = { onResolve(ReportStatus.RESUELTO, "Medidas tomadas por moderación") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Icon(Icons.Default.Check, null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Resolver", fontSize = 11.sp)
                    }
                }
            } else if (report.resolutionNote != null) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))
                Spacer(modifier = Modifier.height(8.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF0FDF4), RoundedCornerShape(8.dp))
                        .padding(8.dp)
                ) {
                    Text(text = "Resolución:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF166534))
                    Text(
                        text = report.resolutionNote,
                        fontSize = 12.sp,
                        color = Color(0xFF166534),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}
