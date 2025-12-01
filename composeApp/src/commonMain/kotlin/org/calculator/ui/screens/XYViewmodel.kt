package org.calculator.ui.screens

import androidx.compose.ui.graphics.Color
import com.example.calculator.foundation.CustomViewModel
import org.calculator.ui.utils.Expression

class XYViewmodel : CustomViewModel<XYContract.State, XYContract.Event, XYContract.Effect>(
    initialState = XYContract.State(
        fieldsInput = listOf("y = 2x", "y = sin(x)"),
        expressions = listOf(Expression.CartesianYXExpression("y = 2x"), Expression.CartesianYXExpression("y = sin(x)")),
        colors = listOf(Color.Red, Color.Blue)
    )
) {
    override suspend fun handleEvent(event: XYContract.Event) {
        when(event){
            is XYContract.Event.UpdateFieldInput -> {
                println("UpdateFieldInput: $event")
                val newFieldsInput = uiState.value.fieldsInput.toMutableList()
                newFieldsInput[event.index] = event.value
                setState(uiState.value.copy(fieldsInput = newFieldsInput, expressions = newFieldsInput.map { Expression.CartesianYXExpression(it) }))
            }
            is XYContract.Event.SelectFunction -> {
                println("SelectFunction: $event")
                setState(uiState.value.copy(selectedFunctionIndex = event.index))
            }
            is XYContract.Event.UpdateColor -> {
                println("UpdateColor: $event")
                val newColors = uiState.value.colors.toMutableList()
                newColors[uiState.value.selectedFunctionIndex] = event.color
                setState(uiState.value.copy(colors = newColors, selectedFunctionIndex = -1))
            }
        }
    }
}