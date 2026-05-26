package com.example.ruvo_app.core.component

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ruvo_app.domain.model.PostStatus
import com.example.ruvo_app.domain.model.ServicePost

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AdminPostCard(
    post: ServicePost,
    authorName: String,
    date: String,
    onView: () -> Unit = {},
    onApprove: () -> Unit = {},
    onReject: (String) -> Unit = {},
    onReanalyze: () -> Unit = {},
    onFeedback: (Boolean) -> Unit = {},
    isAnalyzing: Boolean = false
) {
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .animateContentSize(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
            // Semáforo de riesgo dinámico
            Box(
                modifier = Modifier
                    .width(5.dp)
                    .fillMaxHeight()
                    .background(
                        when {
                            post.status == PostStatus.VERIFICADO -> Color(0xFF10B981)
                            post.status == PostStatus.RECHAZADO -> Color(0xFF000000)
                            post.trustScore < 40 || post.status == PostStatus.REVISION_MANUAL_PRIORITARIA -> Color(0xFFEF4444)
                            post.trustScore < 70 -> Color(0xFFFBBF24)
                            else -> Color(0xFF6B7280)
                        }
                    )
            )

            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = post.title,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                lineHeight = 20.sp
                            ),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Autor: $authorName",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.Gray,
                                fontSize = 12.sp
                            )
                            if (post.aiConfidence > 0) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Box(modifier = Modifier.size(3.dp).background(Color.LightGray, CircleShape))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Certeza IA: ${(post.aiConfidence * 100).toInt()}%",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = if (post.aiConfidence > 0.8) Color(0xFF10B981) else Color(0xFFFBBF24)
                                )
                            }
                        }
                    }
                    
                    // Trust Score Badge
                    Surface(
                        color = when {
                            post.trustScore > 80 -> Color(0xFFDCFCE7)
                            post.trustScore >= 40 -> Color(0xFFFEF3C7)
                            else -> Color(0xFFFEE2E2)
                        },
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "${post.trustScore.toInt()}%",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = when {
                                post.trustScore > 80 -> Color(0xFF166534)
                                post.trustScore >= 40 -> Color(0xFF92400E)
                                else -> Color(0xFF991B1B)
                            }
                        )
                    }
                }

                // Bloque de Auditoría IA
                if (post.trustAnalysis != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        color = Color(0xFFF8FAFC),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth().clickable { isExpanded = !isExpanded },
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.AutoAwesome, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Auditoría Inteligente", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                }
                                Icon(
                                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = Color.Gray
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(6.dp))
                            
                            Text(
                                text = post.trustAnalysis ?: "",
                                style = MaterialTheme.typography.bodySmall.copy(lineHeight = 16.sp),
                                color = Color.DarkGray,
                                fontStyle = FontStyle.Italic,
                                maxLines = if (isExpanded) 20 else 2,
                                overflow = TextOverflow.Ellipsis
                            )

                            if (isExpanded) {
                                // 1. Mapa de Calor (Fragmentos de Riesgo)
                                if (post.riskHighlights.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text("Hallazgos Críticos:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                                    FlowRow(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        post.riskHighlights.forEach { snippet ->
                                            Surface(
                                                color = Color(0xFFFEE2E2).copy(alpha = 0.6f),
                                                shape = RoundedCornerShape(4.dp)
                                            ) {
                                                Text(
                                                    text = snippet,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                    fontSize = 10.sp,
                                                    color = Color(0xFF991B1B),
                                                    fontWeight = FontWeight.Medium
                                                )
                                            }
                                        }
                                    }
                                }

                                // 2. RLHF (Bucle de Feedback)
                                Spacer(modifier = Modifier.height(16.dp))
                                HorizontalDivider(color = Color.LightGray.copy(alpha = 0.2f))
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("¿Fue útil este análisis?", fontSize = 10.sp, color = Color.Gray)
                                    Row {
                                        IconButton(onClick = { onFeedback(true) }, modifier = Modifier.size(32.dp)) {
                                            Icon(Icons.Default.ThumbUp, null, tint = if(post.aiFeedbackUseful == true) Color(0xFF10B981) else Color.LightGray, modifier = Modifier.size(16.dp))
                                        }
                                        IconButton(onClick = { onFeedback(false) }, modifier = Modifier.size(32.dp)) {
                                            Icon(Icons.Default.ThumbDown, null, tint = if(post.aiFeedbackUseful == false) Color(0xFFEF4444) else Color.LightGray, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Acciones Finales
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onView,
                        modifier = Modifier.weight(1f).height(40.dp),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text("Revisar", fontSize = 12.sp)
                    }

                    // Botón Re-analizar (Varita Mágica)
                    Surface(
                        modifier = Modifier.size(40.dp).clickable(enabled = !isAnalyzing) { onReanalyze() },
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            if (isAnalyzing) {
                                CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                            } else {
                                Icon(Icons.Default.Refresh, contentDescription = "Re-analizar", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                            }
                        }
                    }
                    
                    val showApprovalActions = post.status != PostStatus.VERIFICADO && post.status != PostStatus.RECHAZADO

                    if (showApprovalActions) {
                        Button(
                            onClick = onApprove,
                            modifier = Modifier.weight(1f).height(40.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("Aprobar", fontSize = 12.sp)
                        }
                        Button(
                            onClick = { onReject("Criterio del moderador tras auditoría") },
                            modifier = Modifier.weight(1f).height(40.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("Rechazar", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}
