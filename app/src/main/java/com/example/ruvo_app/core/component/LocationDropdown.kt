package com.example.ruvo_app.core.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationDropdown(
    selectedOption: String,
    options: List<String>,
    onOptionSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "",
    placeholder: String = "Seleccionar"
) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        if (label.isNotEmpty()) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                ),
                modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
            )
        }

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
            modifier = Modifier.fillMaxWidth()
        ) {
            val displayValue = if (selectedOption == label || selectedOption.isEmpty()) "" else selectedOption
            
            OutlinedTextField(
                value = displayValue,
                onValueChange = {},
                readOnly = true,
                placeholder = { 
                    Text(
                        text = placeholder, 
                        fontSize = 13.sp, 
                        color = Color.Gray,
                        maxLines = 1
                    ) 
                },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Color.LightGray.copy(alpha = 0.5f),
                    focusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                    unfocusedContainerColor = Color(0xFFF8F9FA),
                    focusedContainerColor = Color(0xFFF8F9FA),
                    disabledBorderColor = Color.LightGray.copy(alpha = 0.5f),
                    disabledContainerColor = Color(0xFFF8F9FA)
                ),
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
                    .heightIn(max = 48.dp),
                shape = RoundedCornerShape(12.dp),
                textStyle = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                ),
                singleLine = true
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.background(Color.White)
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { 
                            Text(
                                text = option, 
                                fontSize = 14.sp,
                                style = MaterialTheme.typography.bodyMedium
                            ) 
                        },
                        onClick = {
                            onOptionSelected(option)
                            expanded = false
                        },
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp)
                    )
                }
            }
        }
    }
}
