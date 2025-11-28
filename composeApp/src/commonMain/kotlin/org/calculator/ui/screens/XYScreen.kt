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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.zIndex
import org.calculator.ui.components.FunctionField
import org.calculator.ui.components.MultiCanvas

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun XYScreen(viewModel: XYViewmodel) {
    val showSidePanel = remember { mutableStateOf(false) }

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
        Column(modifier = Modifier.padding(paddingValues)){
            Row(){
                if(showSidePanel.value){
                    Column(modifier = Modifier.weight(1f)) {

                    }
                }
                Column(modifier = Modifier.weight(4f)){
                    MultiCanvas()
                }
            }
        }
    }
}