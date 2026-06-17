package com.example.ruvo_app.core.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.ManageAccounts
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ruvo_app.domain.model.AccountStatus
import com.example.ruvo_app.domain.model.User
import com.example.ruvo_app.domain.model.UserRole
import java.util.Locale

@Composable
fun AdminUserCard(
    user: User,
    onView: () -> Unit = {},
    onBlock: () -> Unit = {},
    onChangeRole: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar con inicial y color de rol con mejor contraste
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .background(
                            if (user.role == UserRole.MODERATOR) Color(0xFF7B1FA2).copy(alpha = 0.1f) 
                            else MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), 
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = user.fullName.take(1).uppercase(),
                        color = if (user.role == UserRole.MODERATOR) Color(0xFF7B1FA2) else MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 22.sp
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = user.fullName,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        UserStatusBadge(user.status)
                    }
                    Text(
                        text = user.email, 
                        style = MaterialTheme.typography.bodySmall, 
                        color = Color.Gray,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontSize = 13.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Fila de Estadísticas Optimizada para evitar amontonamiento
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF8F9FA), RoundedCornerShape(12.dp))
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                UserStatItem(
                    label = "Servicios", 
                    value = formatStatValue(user.stats.activePosts),
                    modifier = Modifier.weight(1f)
                )
                Box(modifier = Modifier.width(1.dp).height(24.dp).background(Color.LightGray.copy(alpha = 0.5f)))
                UserStatItem(
                    label = "Reportes", 
                    value = formatStatValue(user.stats.reportsCount),
                    modifier = Modifier.weight(1f)
                )
                Box(modifier = Modifier.width(1.dp).height(24.dp).background(Color.LightGray.copy(alpha = 0.5f)))
                UserStatItem(
                    label = "Puntos", 
                    value = formatStatValue(user.reputation.points),
                    modifier = Modifier.weight(1f),
                    isPrimary = true
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Botones de acción con jerarquía visual profesional
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onView,
                    modifier = Modifier.weight(1f).height(44.dp),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp)
                ) {
                    Icon(Icons.Default.Visibility, null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Detalle", fontSize = 12.sp)
                }

                OutlinedButton(
                    onClick = onChangeRole,
                    modifier = Modifier.weight(1f).height(44.dp),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp)
                ) {
                    Icon(Icons.Default.ManageAccounts, null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Rol", fontSize = 12.sp)
                }

                Button(
                    onClick = onBlock,
                    modifier = Modifier.weight(1.2f).height(44.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if(user.status == AccountStatus.BLOCKED) Color(0xFF10B981) else Color(0xFFEF4444)
                    ),
                    contentPadding = PaddingValues(horizontal = 4.dp)
                ) {
                    Icon(if(user.status == AccountStatus.BLOCKED) Icons.Default.CheckCircle else Icons.Default.Lock, null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if(user.status == AccountStatus.BLOCKED) "Activar" else "Bloquear", fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun UserStatItem(label: String, value: String, modifier: Modifier = Modifier, isPrimary: Boolean = false) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value, 
            fontWeight = FontWeight.ExtraBold, 
            fontSize = 15.sp,
            color = if (isPrimary) MaterialTheme.colorScheme.primary else Color.Black
        )
        Text(
            text = label, 
            style = MaterialTheme.typography.labelSmall, 
            color = Color.Gray,
            fontSize = 11.sp
        )
    }
}

@Composable
fun UserStatusBadge(status: AccountStatus) {
    val (color, text, icon) = when (status) {
        AccountStatus.ACTIVE -> Triple(Color(0xFFDCFCE7), "Activo", Icons.Default.CheckCircle)
        AccountStatus.WARNING -> Triple(Color(0xFFFEF3C7), "Alerta", Icons.Default.Warning)
        AccountStatus.BLOCKED -> Triple(Color(0xFFFEE2E2), "Bloqueado", Icons.Default.Lock)
    }
    
    val contentColor = when (status) {
        AccountStatus.ACTIVE -> Color(0xFF166534)
        AccountStatus.WARNING -> Color(0xFF92400E)
        AccountStatus.BLOCKED -> Color(0xFF991B1B)
    }

    Surface(
        color = color,
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = contentColor, modifier = Modifier.size(12.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = text,
                color = contentColor,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.sp)
            )
        }
    }
}

private fun formatStatValue(value: Int): String {
    return when {
        value >= 1000000 -> String.format(Locale.US, "%.1fM", value / 1000000f)
        value >= 1000 -> String.format(Locale.US, "%.1fK", value / 1000f)
        else -> value.toString()
    }
}
