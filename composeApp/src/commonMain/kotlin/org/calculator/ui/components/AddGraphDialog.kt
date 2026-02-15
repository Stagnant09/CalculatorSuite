package org.calculator.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.calculator.ui.utils.VSpacer
import org.jetbrains.compose.ui.tooling.preview.Preview

/** This dialog allows the user choose among various options such as function, arc, area, etc.
 * in order to create a new plot
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddGraphDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    val selectedOption: MutableState<ComboOption?> = remember { mutableStateOf(null) }
    val typeOfGraphOptions = listOf(
        ComboOption("Function", 0),
        ComboOption("Point", id = 1),
        ComboOption("Vector", id = 2),
        ComboOption("Arc", id = 3),
        ComboOption("Area", id = 4)
    )
    val textParam = remember { mutableStateOf("") }
    BasicAlertDialog(
        onDismissRequest = { onDismiss() }
    ) {
        Box(
            modifier = Modifier.size(width = 500.dp, height = 600.dp).clip(
                RoundedCornerShape(16.dp)
            ).background(Color(50, 50, 50)).padding(24.dp),
            contentAlignment = Alignment.TopStart
        ) {
            // Close button (X) in top right
            androidx.compose.material3.IconButton(
                onClick = { onDismiss() },
                modifier = Modifier.align(Alignment.TopEnd)
            ) {
                Text("✕", color = Color.White, fontSize = 20.sp)
            }

            Column(
                modifier = Modifier.padding(18.dp)
            ) {
                Text("Select type of graph", color = Color.White)
                VSpacer(8)
                MultiComboBox(
                    labelText = "",
                    options = typeOfGraphOptions,
                    onOptionsChosen = { selectedOption.value = if (it.isNotEmpty()) it[0] else null },
                )
                VSpacer(16)
                when (selectedOption.value?.id) {
                    0 -> { // Function
                        Text("Enter the function", color = Color.White)
                        VSpacer(8)
                        val functionInput = remember { mutableStateOf("") }
                        TextField(
                            value = functionInput.value,
                            onValueChange = {
                                functionInput.value = it
                                textParam.value = it
                            },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("e.g., y = x^2, r = sin(u), x(t), y(t)") }
                        )
                    }

                    1 -> { // Point
                        Text("Enter the coordinates of the point", color = Color.White)
                        VSpacer(8)
                        val xInput = remember { mutableStateOf("") }
                        val yInput = remember { mutableStateOf("") }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("X = ", color = Color.White)
                            TextField(
                                value = xInput.value,
                                onValueChange = {
                                    xInput.value = it
                                    textParam.value = "(" + it + "," + yInput.value + ")"
                                }
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Y = ", color = Color.White)
                            TextField(
                                value = yInput.value,
                                onValueChange = {
                                    yInput.value = it
                                    textParam.value = "(" + xInput.value + "," + it + ")"
                                }
                            )
                        }
                    }

                    2 -> { // Vector
                        Text("Enter the vector components", color = Color.White)
                        VSpacer(8)
                        val xInput = remember { mutableStateOf("") }
                        val yInput = remember { mutableStateOf("") }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("X = ", color = Color.White)
                            TextField(
                                value = xInput.value,
                                onValueChange = {
                                    xInput.value = it
                                    textParam.value = "vec(" + it + "," + yInput.value + ")"
                                },
                                placeholder = { Text("x-component") }
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Y = ", color = Color.White)
                            TextField(
                                value = yInput.value,
                                onValueChange = {
                                    yInput.value = it
                                    textParam.value = "vec(" + xInput.value + "," + it + ")"
                                },
                                placeholder = { Text("y-component") }
                            )
                        }
                    }

                    3 -> { // Arc
                        Text("Enter the arc parameters", color = Color.White)
                        VSpacer(8)
                        val centerXInput = remember { mutableStateOf("") }
                        val centerYInput = remember { mutableStateOf("") }
                        val radiusInput = remember { mutableStateOf("") }
                        val startAngleInput = remember { mutableStateOf("") }
                        val sweepAngleInput = remember { mutableStateOf("") }
                        textParam.value = "arc(" + centerXInput.value + "," + centerYInput.value + "," + radiusInput.value + "," + startAngleInput.value + "," + sweepAngleInput.value + ")"

                        Text("Center:", color = Color.White)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("X = ", color = Color.White)
                            TextField(
                                value = centerXInput.value,
                                onValueChange = { centerXInput.value = it }
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Y = ", color = Color.White)
                            TextField(
                                value = centerYInput.value,
                                onValueChange = { centerYInput.value = it }
                            )
                        }
                        VSpacer(8)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Radius = ", color = Color.White)
                            TextField(
                                value = radiusInput.value,
                                onValueChange = { radiusInput.value = it }
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Start Angle = ", color = Color.White)
                            TextField(
                                value = startAngleInput.value,
                                onValueChange = { startAngleInput.value = it },
                                placeholder = { Text("degrees") }
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Sweep Angle = ", color = Color.White)
                            TextField(
                                value = sweepAngleInput.value,
                                onValueChange = { sweepAngleInput.value = it },
                                placeholder = { Text("degrees") }
                            )
                        }
                    }

                    4 -> { // Area
                        Text("Enter the area points (minimum 3)", color = Color.White)
                        VSpacer(8)
                        val pointsList = remember { mutableStateOf(mutableListOf("", "", "")) }
                        textParam.value = "[" + pointsList.value.joinToString(separator = ",") + "]"

                        pointsList.value.forEachIndexed { index, point ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Point ${index + 1}: ", color = Color.White)
                                TextField(
                                    value = point,
                                    onValueChange = {
                                        val newList = pointsList.value.toMutableList()
                                        newList[index] = it
                                        pointsList.value = newList
                                    },
                                    placeholder = { Text("(x,y)") },
                                    modifier = Modifier.weight(1f)
                                )
                                if (pointsList.value.size > 3) {
                                    androidx.compose.material3.IconButton(
                                        onClick = {
                                            val newList = pointsList.value.toMutableList()
                                            newList.removeAt(index)
                                            pointsList.value = newList
                                        }
                                    ) {
                                        Text("-", color = Color.Red)
                                    }
                                }
                            }
                            VSpacer(4)
                        }

                        androidx.compose.material3.Button(
                            onClick = {
                                val newList = pointsList.value.toMutableList()
                                newList.add("")
                                pointsList.value = newList
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("+ Add Point")
                        }
                    }

                    else -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Pick a graph type", color = Color.White)
                        }
                    }
                }

                VSpacer(16)

                // CONFIRM and DISCARD buttons
                if (selectedOption.value != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Button(
                            onClick = {
                                // TODO: Define discard functionality
                                onDismiss()
                            },
                            colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                                containerColor = Color.Transparent,
                                contentColor = Color.White
                            )
                        ) {
                            Text("DISCARD")
                        }

                        Button(
                            onClick = {
                                if (selectedOption.value != null) {
                                    onConfirm(textParam.value)
                                }
                            }
                        ) {
                            Text("CONFIRM")
                        }
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun AddGraphDialogPreview() {
    AddGraphDialog(onDismiss = {}, onConfirm = {  })
}