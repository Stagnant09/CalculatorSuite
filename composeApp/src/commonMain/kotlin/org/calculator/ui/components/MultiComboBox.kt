package org.calculator.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

// Source - https://stackoverflow.com/a/75403335
// Posted by Gabriele Mariotti, modified by community. See post 'Timeline' for change history
// Retrieved 2026-02-08, License - CC BY-SA 4.0

interface SelectableOption {
    val text: String
}
data class ComboOption(
    override val text: String,
    val id: Int,
) : SelectableOption

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MultiComboBox(
    labelText: String,
    options: List<ComboOption>,
    onOptionsChosen: (List<ComboOption>) -> Unit,
    modifier: Modifier = Modifier,
    selectedIds: List<Int> = emptyList(),
) {
    var expanded by remember { mutableStateOf(false) }
    val isEnabled = options.isNotEmpty()

    val selectedOptionsList = remember { mutableStateListOf<Int>() }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = {
            if (isEnabled) {
                expanded = !expanded
                if (!expanded) {
                    onOptionsChosen(options.filter { it.id in selectedOptionsList })
                }
            }
        },
        modifier = modifier,
    ) {
        val selectedSummary = when (selectedOptionsList.size) {
            0 -> ""
            1 -> options.firstOrNull { it.id == selectedOptionsList.first() }?.text ?: ""
            else -> ""
        }

        TextField(
            enabled = isEnabled,
            modifier = Modifier.menuAnchor(),
            readOnly = true,
            value = selectedSummary,
            onValueChange = {},
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            colors = ExposedDropdownMenuDefaults.textFieldColors(),
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = {
                expanded = false
                onOptionsChosen(options.filter { it.id in selectedOptionsList })
            },
        ) {
            for (option in options) {

                val checked = option.id in selectedOptionsList

                DropdownMenuItem(
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = checked,
                                onCheckedChange = { newChecked ->
                                    selectedOptionsList.clear()
                                    if (newChecked) {
                                        selectedOptionsList.add(option.id)
                                    }
                                },
                            )
                            Text(option.text)
                        }
                    },
                    onClick = {
                        selectedOptionsList.clear()
                        selectedOptionsList.add(option.id)
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                )
            }
        }
    }
}

