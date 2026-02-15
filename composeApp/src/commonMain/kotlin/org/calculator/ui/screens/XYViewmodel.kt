package org.calculator.ui.screens

import androidx.compose.ui.graphics.Color
import com.example.calculator.foundation.CustomViewModel
import org.calculator.ui.utils.Expression
import org.calculator.ui.utils.Expression.*
import org.calculator.ui.utils.ExpressionType
import org.calculator.utils.formulaToExpressionType
import org.calculator.utils.randomRGBColor
import org.calculator.utils.removeElement

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
                        ExpressionType.CARTESIAN_Y_X -> CartesianYXExpression(it)
                        ExpressionType.CARTESIAN_X -> CartesianXExpression(it)
                        ExpressionType.CARTESIAN_IMPLICIT -> CartesianImplicitExpression(it)
                        ExpressionType.POLAR_R_U -> PolarRUExpression(it)
                        ExpressionType.POLAR_U -> PolarUExpression(it)
                        ExpressionType.PARAMETRIC -> ParametricExpression(it)
                        ExpressionType.POINT -> PointExpression(it)
                        ExpressionType.INTEGRAL -> IntegralExpression(it)
                        ExpressionType.AREA -> AreaExpression(it)
                        ExpressionType.VECTOR -> VectorExpression(it)
                        ExpressionType.ARC -> ArcExpression(it)
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

            is XYContract.Event.AddFunction -> {
                val newFieldsInput = uiState.value.fieldsInput.toMutableList()
                newFieldsInput.add("y = x")
                val newExpressions = uiState.value.expressions.toMutableList()
                newExpressions.add(CartesianYXExpression("y = x"))
                val newColors = uiState.value.colors.toMutableList()
                newColors.add(randomRGBColor())
                setState(
                    uiState.value.copy(
                        fieldsInput = newFieldsInput,
                        expressions = newExpressions,
                        colors = newColors
                    )
                )
            }

            is XYContract.Event.RemoveFunction -> {
                println("RemoveFunction: $event")
                val newFieldsInput = uiState.value.fieldsInput.toMutableList().removeElement(event.index)
                val newExpressions = uiState.value.expressions.toMutableList().removeElement(event.index)
                val newColors = uiState.value.colors.toMutableList().removeElement(event.index)
                if (uiState.value.selectedFunctionIndex == event.index) {
                    setState(uiState.value.copy(selectedFunctionIndex = -1))
                }
                setState(uiState.value.copy(fieldsInput = newFieldsInput, expressions = newExpressions, colors = newColors))
            }
        }
    }
}