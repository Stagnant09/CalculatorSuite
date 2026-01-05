package org.calculator.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Square
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import calculatorsuite.composeapp.generated.resources.Res
import calculatorsuite.composeapp.generated.resources.compose_multiplatform
import calculatorsuite.composeapp.generated.resources.select_24px
import com.example.calculator.ui.components.CircularColorPicker
import org.calculator.ui.components.FunctionField
import org.calculator.ui.components.MultiCanvas
import org.calculator.ui.utils.VSpacer
import org.jetbrains.compose.resources.painterResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun XYScreen(viewModel: XYViewmodel) {
    val showSidePanel = remember { mutableStateOf(false) }
    val state = viewModel.uiState.collectAsStateWithLifecycle().value
    val interactionSources = listOf(
        remember { MutableInteractionSource() },
        remember { MutableInteractionSource() },
        remember { MutableInteractionSource() },
        remember { MutableInteractionSource() },
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("XY Screen") },
                navigationIcon = {
                    IconButton(onClick = { showSidePanel.value = !showSidePanel.value }) {
                        Icon(Icons.Filled.Menu, contentDescription = "Menu")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            Row() {
                if (showSidePanel.value) {
                    Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Box(
                                modifier = Modifier.size(width = 48.dp, height = 30.dp).clickable(
                                    interactionSource = interactionSources[0],
                                    onClick = {

                                    }
                                ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Add,
                                    contentDescription = "Add"
                                )
                            }
                            Box(
                                modifier = Modifier.size(width = 48.dp, height = 30.dp).clickable(
                                    interactionSource = interactionSources[1],
                                    onClick = {

                                    }
                                ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    painter = painterResource(Res.drawable.select_24px),
                                    contentDescription = "Select",
                                    tint = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Box(
                                modifier = Modifier.size(width = 48.dp, height = 30.dp).clickable(
                                    interactionSource = interactionSources[2],
                                    onClick = {

                                    }
                                ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Share,
                                    contentDescription = "Share",
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Box(
                                modifier = Modifier.size(width = 48.dp, height = 30.dp).clickable(
                                    interactionSource = interactionSources[3],
                                    onClick = {

                                    }
                                ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Settings,
                                    contentDescription = "Settings",
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        state.fieldsInput.forEachIndexed { index, string ->
                            FunctionField(
                                value = string,
                                color = state.colors[index],
                                onValueChange = { newValue ->
                                    viewModel.setEvent(
                                        XYContract.Event.UpdateFieldInput(
                                            index,
                                            newValue
                                        )
                                    )
                                },
                                onColorClick = { viewModel.setEvent(XYContract.Event.SelectFunction(index)) }
                            )
                        }
                        VSpacer(6)
                        Button(onClick = { viewModel.setEvent(XYContract.Event.AddFunction) }) {
                            Text("Add Function")
                        }
                    }
                }
                Column(modifier = Modifier.weight(4f)) {
                    MultiCanvas(
                        state.expressions,
                        state.colors
                    )
                }
            }
        }
    }

    if (state.selectedFunctionIndex != -1) {
        CircularColorPicker(
            onDismissRequest = { viewModel.setEvent(XYContract.Event.SelectFunction(-1)) },
            onConfirm = { color -> viewModel.setEvent(XYContract.Event.UpdateColor(color)) }
        )
    }
}