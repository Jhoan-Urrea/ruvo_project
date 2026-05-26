package com.example.ruvo_app.core.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.NewReleases
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ruvo_app.R
import com.example.ruvo_app.core.utils.PriceFormatter
import com.example.ruvo_app.domain.model.PostStatus
import com.example.ruvo_app.domain.model.ServicePost
import java.util.Locale

@Composable
fun ServicePostCard(
    post: ServicePost,
    modifier: Modifier = Modifier,
    showOptions: Boolean = false,
    onArchive: () -> Unit = {},
    onReactivate: () -> Unit = {},
    onClick: () -> Unit = {}
) {
    val isRecent = System.currentTimeMillis() - post.createdAt < 48 * 60 * 60 * 1000
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(
            containerColor = if (post.status == PostStatus.ARCHIVADO) Color(0xFFF5F5F5) else Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        onClick = onClick
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
            ) {
                val primaryImage = post.images.find { it.isPrimary } ?: post.images.firstOrNull()
                
                if (primaryImage != null) {
                    AsyncImage(
                        model = primaryImage.url,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        alpha = if (post.status == PostStatus.ARCHIVADO) 0.6f else 1f,
                        error = painterResource(id = R.drawable.card_service)
                    )
                } else {
                    Image(
                        painter = painterResource(id = R.drawable.card_service),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        alpha = if (post.status == PostStatus.ARCHIVADO) 0.6f else 1f
                    )
                }

                // Top Left Badges (Category & Featured)
                Row(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        color = Color(0xFFE8F0FE).copy(alpha = 0.9f),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            text = post.category.name.lowercase().replaceFirstChar { it.uppercase() },
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color(0xFF0047FF),
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        )
                    }

                    if (post.isFeatured && post.status != PostStatus.ARCHIVADO) {
                        Surface(color = Color(0xFF0047FF), shape = RoundedCornerShape(16.dp)) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.AutoMirrored.Filled.TrendingUp, null, tint = Color.White, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Destacado", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                if (showOptions) {
                    Box(modifier = Modifier.align(Alignment.TopEnd).padding(4.dp)) {
                        IconButton(
                            onClick = { expanded = true }, 
                            colors = IconButtonDefaults.iconButtonColors(containerColor = Color.White.copy(alpha = 0.7f))
                        ) {
                            Icon(Icons.Default.MoreVert, contentDescription = "Opciones")
                        }
                        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                            if (post.status != PostStatus.ARCHIVADO) {
                                DropdownMenuItem(text = { Text("Archivar") }, onClick = { onArchive(); expanded = false })
                            } else {
                                DropdownMenuItem(text = { Text("Reactivar") }, onClick = { onReactivate(); expanded = false })
                            }
                        }
                    }
                }
            }

            Column(modifier = Modifier.padding(16.dp)) {
                // Título y Badge de Verificado
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = post.title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold, 
                            fontSize = 17.sp,
                            lineHeight = 22.sp
                        ),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 44.dp),
                        color = if (post.status == PostStatus.ARCHIVADO) Color.Gray else Color.Black
                    )

                    if (post.status == PostStatus.VERIFICADO) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            color = Color(0xFFDCFCE7),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = Color(0xFF166534),
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Verificado",
                                    color = Color(0xFF166534),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Star, null, tint = Color(0xFFFFC107), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (post.rating > 0) String.format(Locale.getDefault(), "%.1f", post.rating) else "Nuevo",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                    if (post.reviewsCount > 0) {
                        Text(
                            text = " (${post.reviewsCount} reseñas)", 
                            style = MaterialTheme.typography.labelSmall, 
                            color = Color.Gray,
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = post.description,
                    style = MaterialTheme.typography.bodySmall.copy(lineHeight = 18.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    fontSize = 13.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Box(modifier = Modifier.size(28.dp).clip(CircleShape).background(Color.LightGray)) {
                            if (post.authorProfilePictureUrl != null) {
                                AsyncImage(model = post.authorProfilePictureUrl, contentDescription = null, contentScale = ContentScale.Crop)
                            } else {
                                Icon(Icons.Default.Person, null, tint = Color.White, modifier = Modifier.padding(6.dp))
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = post.authorName,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Text(
                        text = PriceFormatter.formatRange(post.minPrice, post.maxPrice),
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = if (post.status == PostStatus.ARCHIVADO) Color.Gray else MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.sp
                        )
                    )
                }
            }
        }
    }
}
