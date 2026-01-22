package org.calculator.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DragIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import calculatorsuite.composeapp.generated.resources.Res
import calculatorsuite.composeapp.generated.resources.select_24px
import org.calculator.models.SidePanelAction
import org.calculator.ui.utils.HSpacer
import org.jetbrains.compose.resources.painterResource

@Composable
fun FunctionField(
    label: String = "y = f(x)",
    action: SidePanelAction = SidePanelAction.COLOR,
    color: Color = Color.Red,
    value: String,
    onValueChange: (String) -> Unit,
    onActionButtonClick: () -> Unit = {},
    onClearClick: () -> Unit = {}
) {
    val isFocused = remember { mutableStateOf(false) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(74.dp)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        when (action) {
            SidePanelAction.COLOR -> {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(color)
                        .clickable { onActionButtonClick() }
                        .border(1.dp, Color.Gray, CircleShape)
                )
            }
            SidePanelAction.SELECT -> {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                ){
                    IconButton(
                        onClick = {}
                    ){
                        Icon(
                            painter = painterResource(Res.drawable.select_24px),
                            contentDescription = "Select"
                        )
                    }
                }
            }
            SidePanelAction.DRAG -> {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                ){
                    IconButton(
                        onClick = {}
                    ){
                        Icon(
                            imageVector = Icons.Filled.DragIndicator,
                            contentDescription = "Select"
                        )
                    }
                }
            }
        }

        HSpacer(12)

        // 🧮 Text field for function input
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .weight(1f)
                .onFocusChanged { focusState ->
                    isFocused.value = focusState.isFocused
                },
            label = { Text(label) },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            trailingIcon = {
                if (isFocused.value) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Remove function",
                        tint = color,
                        modifier = Modifier
                            .combinedClickable(
                                onClick = { onClearClick() }
                            )
                            .pointerInput(Unit) {
                                detectTapGestures {
                                    onClearClick()
                                }
                            }
                            .padding(8.dp)
                    )
                }
            }
        )
    }
}
