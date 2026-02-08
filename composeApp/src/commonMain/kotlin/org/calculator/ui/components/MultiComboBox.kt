package org.calculator.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
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
    // when no options available, I want ComboBox to be disabled
    val isEnabled by rememberUpdatedState { options.isNotEmpty() }
    var selectedOptionsList  = remember { mutableStateListOf<Int>()}

    //Initial setup of selected ids
    selectedIds.forEach{
        selectedOptionsList.add(it)
    }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = {
            if (isEnabled()) {
                expanded = !expanded
                if (!expanded) {
                    onOptionsChosen(options.filter { it.id in selectedOptionsList }.toList())
                }
            }
        },
        modifier = modifier,
    ) {
        val selectedSummary = when (selectedOptionsList.size) {
            0 -> ""
            1 -> options.first { it.id == selectedOptionsList.first() }.text
            else -> "Wybrano ${selectedOptionsList.size}"
        }
        TextField(
            enabled = isEnabled(),
            modifier = Modifier.menuAnchor(),
            readOnly = true,
            value = selectedSummary,
            onValueChange = {},
            label = { Text(text = labelText) },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            colors = ExposedDropdownMenuDefaults.textFieldColors(),
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = {
                expanded = false
                onOptionsChosen(options.filter { it.id in selectedOptionsList }.toList())
            },
        ) {
            for (option in options) {

                //use derivedStateOf to evaluate if it is checked
                var checked = remember {
                    derivedStateOf{option.id in selectedOptionsList}
                }.value

                DropdownMenuItem(
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = checked,
                                onCheckedChange = { newCheckedState ->
                                    if (newCheckedState) {
                                        selectedOptionsList.add(option.id)
                                    } else {
                                        selectedOptionsList.remove(option.id)
                                    }
                                },
                            )
                            Text(text = option.text)
                        }
                    },
                    onClick = {
                        if (!checked) {
                            selectedOptionsList.add(option.id)
                        } else {
                            selectedOptionsList.remove(option.id)
                        }
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                )
            }
        }
    }
}
