package org.calculator.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.calculator.ui.components.CircularColorPicker
import org.calculator.ui.components.FunctionField
import org.calculator.ui.components.MultiCanvas
import org.calculator.ui.utils.VSpacer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun XYScreen(viewModel: XYViewmodel) {
    val showSidePanel = remember { mutableStateOf(false) }
    val state = viewModel.uiState.collectAsStateWithLifecycle().value

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