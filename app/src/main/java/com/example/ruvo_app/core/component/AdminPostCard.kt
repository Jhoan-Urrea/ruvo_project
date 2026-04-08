package com.example.ruvo_app.core.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ruvo_app.domain.model.PostStatus
import com.example.ruvo_app.domain.model.ServicePost

@Composable
fun AdminPostCard(
    post: ServicePost,
    authorName: String,
    date: String,
    onView: () -> Unit = {},
    onApprove: () -> Unit = {},
    onReject: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            // Status Bar Color
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .align(Alignment.CenterVertically)
                    .background(
                        when (post.status) {
                            PostStatus.PENDIENTE -> Color(0xFFFBBF24)
                            PostStatus.VERIFICADO -> Color(0xFF10B981)
                            PostStatus.RECHAZADO -> Color(0xFFEF4444)
                            else -> Color.Gray
                        }
                    )
            )

            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = when (post.status) {
                            PostStatus.VERIFICADO -> Icons.Default.CheckCircle
                            else -> Icons.Default.AccessTime
                        },
                        contentDescription = null,
                        tint = when (post.status) {
                            PostStatus.VERIFICADO -> Color(0xFF10B981)
                            else -> Color(0xFFFBBF24)
                        },
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = post.title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = when (post.status) {
                            PostStatus.PENDIENTE -> "Pendiente"
                            PostStatus.VERIFICADO -> "Aprobado"
                            PostStatus.RECHAZADO -> "Rechazado"
                            else -> ""
                        },
                        color = when (post.status) {
                            PostStatus.PENDIENTE -> Color(0xFFFBBF24)
                            PostStatus.VERIFICADO -> Color(0xFF10B981)
                            PostStatus.RECHAZADO -> Color(0xFFEF4444)
                            else -> Color.Gray
                        },
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        color = Color(0xFFE0E7FF),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = "Servicio",
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            fontSize = 10.sp,
                            color = Color(0xFF4338CA),
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "0", fontSize = 12.sp, fontWeight = FontWeight.Bold) // Counter
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = post.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    maxLines = 2
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Person, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = authorName, fontSize = 10.sp, color = Color.LightGray)
                    Spacer(modifier = Modifier.width(12.dp))
                    Box(modifier = Modifier.size(3.dp).background(Color.LightGray, CircleShape))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(text = date, fontSize = 10.sp, color = Color.LightGray)
                    Spacer(modifier = Modifier.width(12.dp))
                    Box(modifier = Modifier.size(3.dp).background(Color.LightGray, CircleShape))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(text = post.category.name.lowercase().capitalize(), fontSize = 10.sp, color = Color.LightGray)
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (post.status == PostStatus.PENDIENTE) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = onView,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Ver", fontSize = 14.sp)
                        }
                        Button(
                            onClick = onApprove,
                            modifier = Modifier.weight(1.5f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Aprobar", fontSize = 14.sp)
                        }
                        Button(
                            onClick = onReject,
                            modifier = Modifier.weight(1.5f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Rechazar", fontSize = 14.sp)
                        }
                    }
                } else if (post.status == PostStatus.VERIFICADO) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onView) {
                            Icon(Icons.Default.Visibility, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Ver", fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)
                        }
                        Text(text = "✓ Ya aprobado", color = Color.LightGray, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

fun String.capitalize() = replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
