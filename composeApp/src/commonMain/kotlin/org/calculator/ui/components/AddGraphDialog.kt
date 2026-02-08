package org.calculator.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/** This dialog allows the user choose among various options such as function, arc, area, etc.
 * in order to create a new plot
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddGraphDialog(
    onDismiss: () -> Unit
){
    val typeOfGraphOptions = listOf(
        ComboOption("Function", 0),
        ComboOption("Point", id = 1),
        ComboOption("Vector", id = 2),
        ComboOption("Arc", id = 3),
        ComboOption("Area", id = 4)
    )
    BasicAlertDialog(
        onDismissRequest = { onDismiss() }
    ){
        Column(
            modifier = Modifier.padding(24.dp)
        ) {
            MultiComboBox(
                labelText = "",
                options = typeOfGraphOptions,
                onOptionsChosen = {},
            )
        }
    }
}