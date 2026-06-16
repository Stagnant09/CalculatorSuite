package org.calculator.ui.screens.options

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.calculator.ui.utils.VSpacer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OptionsDialog(
    onDismiss: () -> Unit
) {
    var thickness by remember { mutableStateOf(7f) }

    BasicAlertDialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier.size(width = 520.dp, height = 680.dp),
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier.fillMaxSize().padding(32.dp)
            ){
                VSpacer(16)
                Text("Thickness")
                VSpacer(2)
                Slider(
                    value = thickness,
                    onValueChange = { thickness = it },
                    valueRange = 1f..10f
                )
                VSpacer(2)
                Text("${thickness.toInt()}")
            }
        }
    }
}
