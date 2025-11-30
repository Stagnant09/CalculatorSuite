package org.calculator.ui.screens

import com.example.calculator.foundation.CustomViewModel
import org.calculator.ui.utils.Expression

class XYViewmodel : CustomViewModel<XYContract.State, XYContract.Event, XYContract.Effect>(
    initialState = XYContract.State(
        fieldsInput = listOf("y = 2x", "y = sin(x)"),
        expressions = listOf(Expression.CartesianYXExpression("y = 2x"), Expression.CartesianYXExpression("y = sin(x)"))
    )
) {
    override suspend fun handleEvent(event: XYContract.Event) {
        when(event){
            is XYContract.Event.UpdateFieldInput -> {

            }
        }
    }
}