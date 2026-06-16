package org.calculator.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.calculator.ui.utils.VSpacer

/**
 * Dialog for creating a new plot.
 *
 * [onConfirm] receives the assembled formula string (never empty when the
 * user presses Confirm; the caller is responsible for ignoring blank strings).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddGraphDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    val options = listOf(
        ComboOption("Function",    0),
        ComboOption("Point",       1),
        ComboOption("Vector",      2),
        ComboOption("Arc",         3),
        ComboOption("Area",        4),
        ComboOption("Parametric",  5),
        ComboOption("Integral",    6),
    )
    val selectedOption = remember { mutableStateOf<ComboOption?>(null) }

    // Per-option local formula holders — avoids derived-state re-derivation on
    // each keystroke, and keeps each sub-form's state independent.
    val functionInput = remember { mutableStateOf("") }
    val pointX        = remember { mutableStateOf("") }
    val pointY        = remember { mutableStateOf("") }
    val vecX          = remember { mutableStateOf("") }
    val vecY          = remember { mutableStateOf("") }
    val arcCX         = remember { mutableStateOf("") }
    val arcCY         = remember { mutableStateOf("") }
    val arcR          = remember { mutableStateOf("") }
    val arcStart      = remember { mutableStateOf("") }
    val arcSweep      = remember { mutableStateOf("") }
    val areaInput     = remember { mutableStateOf("") }
    val paramX        = remember { mutableStateOf("") }
    val paramY        = remember { mutableStateOf("") }
    val integFunc     = remember { mutableStateOf("") }
    val integA        = remember { mutableStateOf("") }
    val integB        = remember { mutableStateOf("") }

    // Derive the current formula string from whichever sub-form is active
    val formula by remember {
        derivedStateOf {
            when (selectedOption.value?.id) {
                0    -> functionInput.value.trim()
                1    -> "(${pointX.value},${pointY.value})"
                2    -> "vec(${vecX.value},${vecY.value})"
                3    -> "arc((${arcCX.value},${arcCY.value}),${arcR.value},${arcStart.value},${arcSweep.value})"
                4    -> "[${areaInput.value}]"
                5    -> "r(t)=(${paramX.value},${paramY.value})"
                6    -> "integral(${integFunc.value},${integA.value},${integB.value})"
                else -> ""
            }
        }
    }

    BasicAlertDialog(onDismissRequest = onDismiss) {
        Surface(
            modifier       = Modifier.size(width = 520.dp, height = 680.dp),
            shape          = RoundedCornerShape(28.dp),
            color          = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Box(modifier = Modifier.padding(24.dp), contentAlignment = Alignment.TopStart) {

                // Close button
                IconButton(
                    onClick  = onDismiss,
                    modifier = Modifier.align(Alignment.TopEnd)
                ) {
                    Icon(Icons.Default.Close, "Close", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                Column(modifier = Modifier.fillMaxSize()) {
                    Text("Create New Plot", style = MaterialTheme.typography.headlineSmall)
                    VSpacer(20)

                    Text("Type", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                    VSpacer(6)
                    MultiComboBox(
                        labelText      = "Select type",
                        options        = options,
                        onOptionsChosen = { selectedOption.value = it.firstOrNull() }
                    )
                    VSpacer(20)

                    // Sub-form area
                    Surface(
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        color    = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        shape    = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            when (selectedOption.value?.id) {

                                // 0 — Explicit / implicit / polar / parametric free-text
                                0 -> {
                                    Text("Formula", style = MaterialTheme.typography.titleMedium)
                                    VSpacer(10)
                                    OutlinedTextField(
                                        value         = functionInput.value,
                                        onValueChange = { functionInput.value = it },
                                        modifier      = Modifier.fillMaxWidth(),
                                        placeholder   = { Text("y = x^2  |  r = sin(u)  |  x^2+y^2=1") },
                                        shape         = RoundedCornerShape(12.dp),
                                        singleLine    = true
                                    )
                                    VSpacer(8)
                                    Text(
                                        "Accepted forms:  y=f(x)  ·  x=c  ·  f(x,y)=g(x,y)  ·  r=f(u)  ·  r(t)=(x,y)",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                // 1 — Point
                                1 -> {
                                    Text("Point  (x, y)", style = MaterialTheme.typography.titleMedium)
                                    VSpacer(10)
                                    Row(modifier = Modifier.fillMaxWidth()) {
                                        OutlinedTextField(
                                            value         = pointX.value,
                                            onValueChange = { pointX.value = it },
                                            label         = { Text("x") },
                                            modifier      = Modifier.weight(1f),
                                            shape         = RoundedCornerShape(12.dp), singleLine = true
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        OutlinedTextField(
                                            value         = pointY.value,
                                            onValueChange = { pointY.value = it },
                                            label         = { Text("y") },
                                            modifier      = Modifier.weight(1f),
                                            shape         = RoundedCornerShape(12.dp), singleLine = true
                                        )
                                    }
                                }

                                // 2 — Vector
                                2 -> {
                                    Text("Vector  vec(x, y)", style = MaterialTheme.typography.titleMedium)
                                    VSpacer(10)
                                    Row(modifier = Modifier.fillMaxWidth()) {
                                        OutlinedTextField(
                                            value         = vecX.value,
                                            onValueChange = { vecX.value = it },
                                            label         = { Text("x") },
                                            modifier      = Modifier.weight(1f),
                                            shape         = RoundedCornerShape(12.dp), singleLine = true
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        OutlinedTextField(
                                            value         = vecY.value,
                                            onValueChange = { vecY.value = it },
                                            label         = { Text("y") },
                                            modifier      = Modifier.weight(1f),
                                            shape         = RoundedCornerShape(12.dp), singleLine = true
                                        )
                                    }
                                }

                                // 3 — Arc
                                3 -> {
                                    Text("Arc", style = MaterialTheme.typography.titleMedium)
                                    VSpacer(10)
                                    Row(Modifier.fillMaxWidth()) {
                                        OutlinedTextField(arcCX.value, { arcCX.value = it },
                                            Modifier.weight(1f), label = { Text("Centre x") },
                                            shape = RoundedCornerShape(12.dp), singleLine = true)
                                        Spacer(Modifier.width(8.dp))
                                        OutlinedTextField(arcCY.value, { arcCY.value = it },
                                            Modifier.weight(1f), label = { Text("Centre y") },
                                            shape = RoundedCornerShape(12.dp), singleLine = true)
                                    }
                                    VSpacer(8)
                                    Row(Modifier.fillMaxWidth()) {
                                        OutlinedTextField(arcR.value, { arcR.value = it },
                                            Modifier.weight(1f), label = { Text("Radius") },
                                            shape = RoundedCornerShape(12.dp), singleLine = true)
                                        Spacer(Modifier.width(8.dp))
                                        OutlinedTextField(arcStart.value, { arcStart.value = it },
                                            Modifier.weight(1f), label = { Text("Start °") },
                                            shape = RoundedCornerShape(12.dp), singleLine = true)
                                        Spacer(Modifier.width(8.dp))
                                        OutlinedTextField(arcSweep.value, { arcSweep.value = it },
                                            Modifier.weight(1f), label = { Text("Sweep °") },
                                            shape = RoundedCornerShape(12.dp), singleLine = true)
                                    }
                                }

                                // 4 — Area polygon
                                4 -> {
                                    Text("Polygon area", style = MaterialTheme.typography.titleMedium)
                                    VSpacer(10)
                                    OutlinedTextField(
                                        value         = areaInput.value,
                                        onValueChange = { areaInput.value = it },
                                        modifier      = Modifier.fillMaxWidth(),
                                        placeholder   = { Text("(0,0),(3,0),(1.5,2)") },
                                        shape         = RoundedCornerShape(12.dp)
                                    )
                                    VSpacer(8)
                                    Text("Enter a comma-separated list of (x,y) points.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }

                                // 5 — Parametric
                                5 -> {
                                    Text("Parametric  r(t) = (x(t), y(t))",
                                        style = MaterialTheme.typography.titleMedium)
                                    VSpacer(10)
                                    OutlinedTextField(
                                        value         = paramX.value,
                                        onValueChange = { paramX.value = it },
                                        modifier      = Modifier.fillMaxWidth(),
                                        label         = { Text("x(t)") },
                                        placeholder   = { Text("cos(t)") },
                                        shape         = RoundedCornerShape(12.dp), singleLine = true
                                    )
                                    VSpacer(8)
                                    OutlinedTextField(
                                        value         = paramY.value,
                                        onValueChange = { paramY.value = it },
                                        modifier      = Modifier.fillMaxWidth(),
                                        label         = { Text("y(t)") },
                                        placeholder   = { Text("sin(t)") },
                                        shape         = RoundedCornerShape(12.dp), singleLine = true
                                    )
                                    VSpacer(8)
                                    Text("t ranges over [−2π, 2π] by default.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }

                                // 6 — Integral
                                6 -> {
                                    Text("Integral shading", style = MaterialTheme.typography.titleMedium)
                                    VSpacer(10)
                                    OutlinedTextField(
                                        value         = integFunc.value,
                                        onValueChange = { integFunc.value = it },
                                        modifier      = Modifier.fillMaxWidth(),
                                        label         = { Text("f(x)") },
                                        placeholder   = { Text("x^2 + 1") },
                                        shape         = RoundedCornerShape(12.dp), singleLine = true
                                    )
                                    VSpacer(8)
                                    Row(Modifier.fillMaxWidth()) {
                                        OutlinedTextField(integA.value, { integA.value = it },
                                            Modifier.weight(1f), label = { Text("a (lower)") },
                                            shape = RoundedCornerShape(12.dp), singleLine = true)
                                        Spacer(Modifier.width(8.dp))
                                        OutlinedTextField(integB.value, { integB.value = it },
                                            Modifier.weight(1f), label = { Text("b (upper)") },
                                            shape = RoundedCornerShape(12.dp), singleLine = true)
                                    }
                                }

                                else -> {
                                    Box(Modifier.fillMaxSize(), Alignment.Center) {
                                        Text("Select a graph type above.",
                                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        }
                    }

                    VSpacer(20)

                    // Preview of the assembled formula
                    if (formula.isNotBlank()) {
                        Text(
                            text  = "→  $formula",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
                        )
                    }

                    Button(
                        onClick  = { onConfirm(formula) },
                        enabled  = formula.isNotBlank(),
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape    = RoundedCornerShape(16.dp)
                    ) { Text("Confirm") }
                }
            }
        }
    }
}
