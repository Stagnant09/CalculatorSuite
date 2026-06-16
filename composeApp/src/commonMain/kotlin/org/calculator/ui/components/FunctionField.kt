package org.calculator.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.CheckBoxOutlineBlank
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DragIndicator
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.calculator.models.SidePanelAction

@Composable
fun FunctionField(
    modifier: Modifier = Modifier,
    label: String = "y = f(x)",
    action: SidePanelAction = SidePanelAction.COLOR,
    color: Color = Color.Red,
    value: String,
    error: String? = null,
    isSelected: Boolean = false,
    onValueChange: (String) -> Unit,
    onColorButtonClick: () -> Unit = {},
    onSelectButtonClick: () -> Unit = {},
    onDragButtonClick: () -> Unit = {},
    onOptionsButtonClick: () -> Unit = {},
    onClearClick: () -> Unit = {}
) {
    val isFocused = remember { mutableStateOf(false) }
    
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = if (isFocused.value) 8.dp else 1.dp,
        shadowElevation = if (isFocused.value) 2.dp else 0.dp
    ) {
        Column(
            modifier = Modifier
                .padding(8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Action Icon (Color or Other)
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .padding(4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    when (action) {
                        SidePanelAction.COLOR -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape)
                                    .background(color)
                                    .clickable { onColorButtonClick() }
                                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
                            )
                        }
                        SidePanelAction.SELECT -> {
                            IconButton(onClick = {
                                onSelectButtonClick()
                            }) {
                                Icon(
                                    imageVector = if (isSelected) Icons.Filled.CheckBox else Icons.Filled.CheckBoxOutlineBlank,
                                    contentDescription = "Select",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        SidePanelAction.DRAG -> {
                            IconButton(onClick = {}) {
                                Icon(
                                    imageVector = Icons.Filled.DragIndicator,
                                    contentDescription = "Drag",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Text field for function input
                TextField(
                    value = value,
                    onValueChange = onValueChange,
                    modifier = Modifier
                        .weight(1f)
                        .onFocusChanged { focusState ->
                            isFocused.value = focusState.isFocused
                        },
                    placeholder = { Text(label, style = MaterialTheme.typography.bodyMedium) },
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                    ),
                    textStyle = MaterialTheme.typography.bodyLarge,
                    trailingIcon = {
                        if (value.isNotEmpty() && action == SidePanelAction.COLOR) {
                            IconButton(
                                onClick = {
                                    onOptionsButtonClick()
                                })
                            {
                                Icon(
                                    imageVector = Icons.Default.MoreVert,
                                    contentDescription = "Options",
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                )
            }
            if (error != null) {
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(start = 48.dp, bottom = 4.dp)
                )
            }
        }
    }
}
