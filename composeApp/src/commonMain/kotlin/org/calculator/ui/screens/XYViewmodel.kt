package org.calculator.ui.screens

import androidx.compose.ui.graphics.Color
import org.calculator.foundation.CustomViewModel
import org.calculator.ui.utils.Expression
import org.calculator.ui.utils.Expression.*
import org.calculator.ui.utils.ExpressionType
import org.calculator.utils.formulaToExpressionType
import org.calculator.utils.randomRGBColor
import org.calculator.utils.removeElement

class XYViewmodel : CustomViewModel<XYContract.State, XYContract.Event, XYContract.Effect>(
    initialState = XYContract.State(
        fieldsInput = listOf("y = 2*x", "y = sin(x)"),
        expressions = listOf(
            CartesianYXExpression("y = 2*x"),
            CartesianYXExpression("y = sin(x)")
        ),
        colors = listOf(Color.Red, Color.Blue),
        errors = listOf(null, null)
    )
) {
    override suspend fun handleEvent(event: XYContract.Event) {
        when (event) {
            // ---------------------------------------------------------------
            // User edited the formula in a FunctionField
            // ---------------------------------------------------------------
            is XYContract.Event.UpdateFieldInput -> {
                val s = uiState.value
                val newInputs = s.fieldsInput.toMutableList().also { it[event.index] = event.value }
                val newExprs  = s.expressions.toMutableList()
                val newErrors = s.errors.toMutableList()

                try {
                    newExprs[event.index] = buildExpression(event.value)
                    newErrors[event.index] = null
                } catch (e: Exception) {
                    newErrors[event.index] = e.message ?: "Invalid formula"
                }

                setState(s.copy(fieldsInput = newInputs, expressions = newExprs, errors = newErrors))
            }

            // ---------------------------------------------------------------
            // "Add Function" button in the side panel (blank slot)
            // ---------------------------------------------------------------
            is XYContract.Event.AddFunction -> {
                val s = uiState.value
                setState(
                    s.copy(
                        fieldsInput = s.fieldsInput + "y = x",
                        expressions = s.expressions + CartesianYXExpression("y = x"),
                        colors      = s.colors + randomRGBColor(),
                        errors      = s.errors + null
                    )
                )
            }

            // ---------------------------------------------------------------
            // AddGraphDialog confirmed with a specific formula string
            // ---------------------------------------------------------------
            is XYContract.Event.AddFunctionWithFormula -> {
                val s = uiState.value
                val formula = event.formula
                val expr = try { buildExpression(formula) }
                           catch (_: Exception) { CartesianYXExpression(formula) }
                setState(
                    s.copy(
                        fieldsInput = s.fieldsInput + formula,
                        expressions = s.expressions + expr,
                        colors      = s.colors + randomRGBColor(),
                        errors      = s.errors + null
                    )
                )
            }

            // ---------------------------------------------------------------
            is XYContract.Event.SelectFunction -> {
                setState(uiState.value.copy(selectedFunctionIndex = event.index))
            }

            is XYContract.Event.UpdateColor -> {
                val s = uiState.value
                if (s.selectedFunctionIndex < 0) return
                val newColors = s.colors.toMutableList()
                    .also { it[s.selectedFunctionIndex] = event.color }
                setState(s.copy(colors = newColors, selectedFunctionIndex = -1))
            }

            is XYContract.Event.RemoveFunction -> {
                val s = uiState.value
                val idx = event.index
                val newSelected = if (s.selectedFunctionIndex == idx) -1 else s.selectedFunctionIndex
                setState(
                    s.copy(
                        fieldsInput          = s.fieldsInput.removeElement(idx),
                        expressions          = s.expressions.removeElement(idx),
                        colors               = s.colors.removeElement(idx),
                        errors               = s.errors.removeElement(idx),
                        selectedFunctionIndex = newSelected
                    )
                )
            }

            is XYContract.Event.UpdateViewport -> {
                setState(uiState.value.copy(
                    scale   = event.scale,
                    offsetX = event.offsetX,
                    offsetY = event.offsetY
                ))
            }

            is XYContract.Event.ResetView -> {
                setState(uiState.value.copy(scale = 1f, offsetX = 0f, offsetY = 0f))
            }
        }
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private fun buildExpression(formula: String): Expression = when (formulaToExpressionType(formula)) {
        ExpressionType.CARTESIAN_Y_X       -> CartesianYXExpression(formula)
        ExpressionType.CARTESIAN_X         -> CartesianXExpression(formula)
        ExpressionType.CARTESIAN_IMPLICIT  -> CartesianImplicitExpression(formula)
        ExpressionType.POLAR_R_U           -> PolarRUExpression(formula)
        ExpressionType.POLAR_U             -> PolarUExpression(formula)
        ExpressionType.PARAMETRIC          -> ParametricExpression(formula)
        ExpressionType.POINT               -> PointExpression(formula)
        ExpressionType.INTEGRAL            -> IntegralExpression(formula)
        ExpressionType.AREA                -> AreaExpression(formula)
        ExpressionType.VECTOR              -> VectorExpression(formula)
        ExpressionType.ARC                 -> ArcExpression(formula)
    }
}
