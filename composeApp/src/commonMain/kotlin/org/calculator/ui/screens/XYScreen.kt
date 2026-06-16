package org.calculator.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuOpen
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import calculatorsuite.composeapp.generated.resources.Res
import calculatorsuite.composeapp.generated.resources.select_24px
import calculatorsuite.composeapp.generated.resources.swipe_vertical_24px
import org.calculator.models.SidePanelAction
import org.calculator.ui.components.AddGraphDialog
import org.calculator.ui.components.CircularColorPicker
import org.calculator.ui.components.FunctionField
import org.calculator.ui.components.MultiCanvas
import org.calculator.ui.utils.VSpacer
import org.jetbrains.compose.resources.painterResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun XYScreen(viewModel: XYViewmodel) {
    val showSidePanel      = remember { mutableStateOf(true) }
    val showAddGraphDialog = remember { mutableStateOf(false) }
    val state              = viewModel.uiState.collectAsStateWithLifecycle().value
    val currentAction      = remember { mutableStateOf(SidePanelAction.COLOR) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter            = painterResource(Res.drawable.select_24px),
                            contentDescription = null,
                            modifier           = Modifier.size(24.dp),
                            tint               = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("CalculatorSuite", style = MaterialTheme.typography.titleLarge)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { showSidePanel.value = !showSidePanel.value }) {
                        Icon(
                            if (showSidePanel.value) Icons.AutoMirrored.Filled.MenuOpen
                            else Icons.Filled.Menu,
                            contentDescription = "Toggle Menu"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.setEvent(XYContract.Event.ResetView) }) {
                        Icon(Icons.Filled.RestartAlt, contentDescription = "Reset View")
                    }
                    IconButton(onClick = { /* Share */ }) {
                        Icon(Icons.Filled.Share, contentDescription = "Share")
                    }
                    IconButton(onClick = { /* Settings */ }) {
                        Icon(Icons.Filled.Settings, contentDescription = "Settings")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)
                )
            )
        }
    ) { paddingValues ->
        Row(modifier = Modifier.fillMaxSize().padding(paddingValues)) {

            // ---------------------------------------------------------------
            // Side panel
            // ---------------------------------------------------------------
            if (showSidePanel.value) {
                Surface(
                    modifier        = Modifier.weight(1.3f).fillMaxHeight(),
                    tonalElevation  = 1.dp,
                    shadowElevation = 2.dp
                ) {
                    Column(
                        modifier              = Modifier.fillMaxSize().padding(vertical = 8.dp),
                        horizontalAlignment   = Alignment.CenterHorizontally
                    ) {
                        // Toolbar
                        Surface(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp),
                            shape    = RoundedCornerShape(24.dp),
                            color    = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ) {
                            Row(
                                modifier            = Modifier.padding(horizontal = 4.dp, vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment   = Alignment.CenterVertically
                            ) {
                                SidePanelIconButton(
                                    icon     = Icons.Filled.Add,
                                    selected = false,
                                    onClick  = { showAddGraphDialog.value = true }
                                )
                                SidePanelIconButton(
                                    icon     = painterResource(Res.drawable.select_24px),
                                    selected = currentAction.value == SidePanelAction.SELECT,
                                    onClick  = {
                                        currentAction.value =
                                            if (currentAction.value == SidePanelAction.SELECT)
                                                SidePanelAction.COLOR
                                            else SidePanelAction.SELECT
                                    }
                                )
                                SidePanelIconButton(
                                    icon     = painterResource(Res.drawable.swipe_vertical_24px),
                                    selected = currentAction.value == SidePanelAction.DRAG,
                                    onClick  = {
                                        currentAction.value =
                                            if (currentAction.value == SidePanelAction.DRAG)
                                                SidePanelAction.COLOR
                                            else SidePanelAction.DRAG
                                    }
                                )
                            }
                        }

                        VSpacer(8)

                        // Function list
                        Column(
                            modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())
                        ) {
                            state.fieldsInput.forEachIndexed { index, input ->
                                FunctionField(
                                    value              = input,
                                    action             = currentAction.value,
                                    color              = state.colors.getOrElse(index) { Color.Gray },
                                    error              = state.errors.getOrNull(index),
                                    onValueChange      = { newValue ->
                                        viewModel.setEvent(XYContract.Event.UpdateFieldInput(index, newValue))
                                    },
                                    onActionButtonClick = {
                                        viewModel.setEvent(XYContract.Event.SelectFunction(index))
                                    },
                                    onClearClick = {
                                        viewModel.setEvent(XYContract.Event.RemoveFunction(index))
                                    }
                                )
                            }

                            VSpacer(16)
                            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                FilledTonalButton(
                                    onClick = { viewModel.setEvent(XYContract.Event.AddFunction) },
                                    shape   = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Filled.Add, null, Modifier.size(18.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text("Add Function")
                                }
                            }
                            VSpacer(16)
                        }
                    }
                }
            }

            // ---------------------------------------------------------------
            // Canvas
            // ---------------------------------------------------------------
            Box(modifier = Modifier.weight(4f).fillMaxHeight()) {
                MultiCanvas(
                    expressions     = state.expressions,
                    colors          = state.colors,
                    scale           = state.scale,
                    offsetX         = state.offsetX,
                    offsetY         = state.offsetY,
                    onViewportChange = { scale, offsetX, offsetY ->
                        viewModel.setEvent(XYContract.Event.UpdateViewport(scale, offsetX, offsetY))
                    }
                )
            }
        }
    }

    // -----------------------------------------------------------------------
    // Color picker overlay
    // -----------------------------------------------------------------------
    if (state.selectedFunctionIndex != -1) {
        CircularColorPicker(
            onDismissRequest = { viewModel.setEvent(XYContract.Event.SelectFunction(-1)) },
            onConfirm        = { color -> viewModel.setEvent(XYContract.Event.UpdateColor(color)) }
        )
    }

    // -----------------------------------------------------------------------
    // Add-graph dialog
    // Bug fix: was firing AddFunction then UpdateFieldInput(size) in sequence,
    // which caused an index-out-of-bounds because size was already incremented
    // by AddFunction before UpdateFieldInput ran.
    //
    // Now: a single AddFunctionWithFormula event atomically inserts the formula.
    // -----------------------------------------------------------------------
    if (showAddGraphDialog.value) {
        AddGraphDialog(
            onDismiss = { showAddGraphDialog.value = false },
            onConfirm = { formula ->
                if (formula.isNotBlank()) {
                    viewModel.setEvent(XYContract.Event.AddFunctionWithFormula(formula))
                }
                showAddGraphDialog.value = false
            }
        )
    }
}

// ---------------------------------------------------------------------------
// Private helpers
// ---------------------------------------------------------------------------

@Composable
private fun SidePanelIconButton(
    icon:     Any,
    selected: Boolean,
    onClick:  () -> Unit
) {
    val bg    = if (selected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
    val tint  = if (selected) MaterialTheme.colorScheme.onPrimaryContainer
                else MaterialTheme.colorScheme.onSurfaceVariant

    IconButton(
        onClick  = onClick,
        modifier = Modifier.size(40.dp).background(bg, CircleShape)
    ) {
        when (icon) {
            is androidx.compose.ui.graphics.vector.ImageVector ->
                Icon(icon, null, Modifier.size(20.dp), tint)
            is androidx.compose.ui.graphics.painter.Painter ->
                Icon(icon, null, Modifier.size(20.dp), tint)
            else -> Unit   // type-safe exhaustion: impossible at call sites
        }
    }
}
