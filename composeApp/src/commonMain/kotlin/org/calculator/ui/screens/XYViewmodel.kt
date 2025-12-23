package org.calculator.ui.screens

import androidx.compose.ui.graphics.Color
import com.example.calculator.foundation.CustomViewModel
import org.calculator.ui.utils.Expression
import org.calculator.ui.utils.ExpressionType
import org.calculator.utils.formulaToExpressionType

class XYViewmodel : CustomViewModel<XYContract.State, XYContract.Event, XYContract.Effect>(
    initialState = XYContract.State(
        fieldsInput = listOf("y = 2x", "y = sin(x)"),
        expressions = listOf(
            Expression.CartesianYXExpression("y = 2x"),
            Expression.CartesianYXExpression("y = sin(x)")
        ),
        colors = listOf(Color.Red, Color.Blue)
    )
) {
    override suspend fun handleEvent(event: XYContract.Event) {
        when (event) {
            is XYContract.Event.UpdateFieldInput -> {
                println("UpdateFieldInput: $event")
                val newFieldsInput = uiState.value.fieldsInput.toMutableList()
                newFieldsInput[event.index] = event.value
                setState(uiState.value.copy(fieldsInput = newFieldsInput, expressions = newFieldsInput.map {
                    when (formulaToExpressionType(it)) {
                        ExpressionType.CARTESIAN_Y_X -> Expression.CartesianYXExpression(it)
                        ExpressionType.CARTESIAN_X -> Expression.CartesianXExpression(it)
                        ExpressionType.CARTESIAN_IMPLICIT -> Expression.CartesianImplicitExpression(it)
                        ExpressionType.POLAR_R_U -> Expression.PolarRUExpression(it)
                        ExpressionType.POLAR_U -> Expression.PolarUExpression(it)
                        ExpressionType.PARAMETRIC -> Expression.ParametricExpression(it)
                        ExpressionType.POINT -> Expression.PointExpression(it)
                        ExpressionType.INTEGRAL -> Expression.IntegralExpression(it)
                        ExpressionType.AREA -> Expression.AreaExpression(it)
                        ExpressionType.VECTOR -> Expression.VectorExpression(it)
                        ExpressionType.ARC -> Expression.ArcExpression(it)
                    }
                }))
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