package org.calculator.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.zIndex
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
                        val lazyListState = rememberLazyListState()
                        var draggedItemIndex by remember { mutableStateOf<Int?>(null) }
                        var dragOffset by remember { mutableStateOf(0f) }

                        LazyColumn(
                            modifier = Modifier.weight(1f).pointerInput(Unit) {
                                if (currentAction.value != SidePanelAction.DRAG) return@pointerInput
                                detectDragGesturesAfterLongPress(
                                    onDragStart = { offset ->
                                        lazyListState.layoutInfo.visibleItemsInfo
                                            .firstOrNull { item ->
                                                offset.y.toInt() in item.offset..(item.offset + item.size)
                                            }?.also {
                                                if (it.index < state.fieldsInput.size) {
                                                    draggedItemIndex = it.index
                                                }
                                            }
                                    },
                                    onDrag = { change, dragAmount ->
                                        change.consume()
                                        dragOffset += dragAmount.y

                                        val currentDraggedIndex = draggedItemIndex ?: return@detectDragGesturesAfterLongPress
                                        val layoutInfo = lazyListState.layoutInfo
                                        val draggedItemInfo = layoutInfo.visibleItemsInfo.firstOrNull { it.index == currentDraggedIndex }
                                            ?: return@detectDragGesturesAfterLongPress

                                        val currentOffset = draggedItemInfo.offset + dragOffset
                                        val targetItem = layoutInfo.visibleItemsInfo.firstOrNull { item ->
                                            item.index < state.fieldsInput.size &&
                                                    item.index != currentDraggedIndex &&
                                                    currentOffset.toInt() in item.offset..(item.offset + item.size)
                                        }

                                        if (targetItem != null) {
                                            viewModel.setEvent(XYContract.Event.MoveFunction(currentDraggedIndex, targetItem.index))
                                            draggedItemIndex = targetItem.index
                                            dragOffset = 0f
                                        }
                                    },
                                    onDragEnd = {
                                        draggedItemIndex = null
                                        dragOffset = 0f
                                    },
                                    onDragCancel = {
                                        draggedItemIndex = null
                                        dragOffset = 0f
                                    }
                                )
                            },
                            state = lazyListState
                        ) {
                            itemsIndexed(state.fieldsInput) { index, input ->
                                val isDragging = index == draggedItemIndex
                                FunctionField(
                                    modifier = Modifier
                                        .zIndex(if (isDragging) 1f else 0f)
                                        .graphicsLayer {
                                            translationY = if (isDragging) dragOffset else 0f
                                        },
                                    value              = input,
                                    action             = currentAction.value,
                                    color              = state.colors.getOrElse(index) { Color.Gray },
                                    error              = state.errors.getOrNull(index),
                                    isSelected         = state.isSelectedIndexes.contains(index),
                                    onValueChange      = { newValue ->
                                        viewModel.setEvent(XYContract.Event.UpdateFieldInput(index, newValue))
                                    },
                                    onColorButtonClick = {
                                        viewModel.setEvent(XYContract.Event.SetColorToBeEditedForIndex(index))
                                    },
                                    onSelectButtonClick = {
                                        viewModel.setEvent(XYContract.Event.SelectFunction(index))
                                    },
                                    onClearClick = {
                                        viewModel.setEvent(XYContract.Event.RemoveFunction(index))
                                    }
                                )
                            }

                            item {
                                VSpacer(16)
                                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                    when (currentAction.value) {
                                        SidePanelAction.COLOR -> {
                                            FilledTonalButton(
                                                onClick = {
                                                    viewModel.setEvent(XYContract.Event.AddFunction)
                                                },
                                                shape = RoundedCornerShape(12.dp)
                                            ) {
                                                Icon(Icons.Filled.Add, null, Modifier.size(18.dp))
                                                Spacer(Modifier.width(8.dp))
                                                Text("Add Function")
                                            }
                                        }

                                        SidePanelAction.SELECT -> {
                                            FilledTonalButton(
                                                onClick = {
                                                    viewModel.setEvent(XYContract.Event.RemoveFunctions(state.isSelectedIndexes))
                                                },
                                                enabled = state.isSelectedIndexes.isNotEmpty(),
                                                shape = RoundedCornerShape(12.dp)
                                            ) {
                                                Icon(Icons.Filled.Remove, null, Modifier.size(18.dp))
                                                Spacer(Modifier.width(8.dp))
                                                Text("Remove Function")
                                            }
                                        }
                                        SidePanelAction.DRAG -> {/*nothing*/}
                                    }
                                }
                                VSpacer(16)
                            }
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
    if (state.colorToBeEditedForIndex != -1) {
        CircularColorPicker(
            onDismissRequest = { viewModel.setEvent(XYContract.Event.SetColorToBeEditedForIndex(-1)) },
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
