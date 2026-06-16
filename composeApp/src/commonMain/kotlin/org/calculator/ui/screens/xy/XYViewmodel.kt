package org.calculator.ui.screens

import androidx.compose.ui.graphics.Color
import org.calculator.foundation.CustomViewModel
import org.calculator.ui.utils.Expression
import org.calculator.ui.utils.Expression.*
import org.calculator.ui.utils.ExpressionType
import org.calculator.utils.formulaToExpressionType
import org.calculator.utils.randomRGBColor
import org.calculator.utils.removeElement
import org.calculator.utils.moveElement

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
            is XYContract.Event.SetColorToBeEditedForIndex -> {
                setState(uiState.value.copy(colorToBeEditedForIndex = event.index))
            }
            is XYContract.Event.SelectFunction -> {
                val s = uiState.value
                val newSelected = if (event.index in s.isSelectedIndexes) {
                    s.isSelectedIndexes - event.index
                } else {
                    s.isSelectedIndexes + event.index
                }
                setState(s.copy(isSelectedIndexes = newSelected))
            }

            is XYContract.Event.UpdateColor -> {
                val s = uiState.value
                if (s.colorToBeEditedForIndex < 0) return
                val newColors = s.colors.toMutableList()
                    .also { it[s.colorToBeEditedForIndex] = event.color }
                setState(s.copy(colors = newColors, colorToBeEditedForIndex = -1))
            }

            is XYContract.Event.RemoveFunction -> {
                val s = uiState.value
                val idx = event.index
                val newSelected = if (s.colorToBeEditedForIndex == idx) -1 else s.colorToBeEditedForIndex
                setState(
                    s.copy(
                        fieldsInput          = s.fieldsInput.removeElement(idx),
                        expressions          = s.expressions.removeElement(idx),
                        colors               = s.colors.removeElement(idx),
                        errors               = s.errors.removeElement(idx),
                        colorToBeEditedForIndex = newSelected
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

            is XYContract.Event.MoveFunction -> {
                val s = uiState.value
                val from = event.fromIndex
                val to = event.toIndex
                
                if (from !in s.fieldsInput.indices || to !in s.fieldsInput.indices) return

                val newFieldsInput = s.fieldsInput.moveElement(from, to)
                val newExpressions = s.expressions.moveElement(from, to)
                val newColors = s.colors.moveElement(from, to)
                val newErrors = s.errors.moveElement(from, to)
                
                // Handle selection and color editing index shifts
                val newSelected = s.isSelectedIndexes.map {
                    when {
                        it == from -> to
                        from < to && it in (from + 1)..to -> it - 1
                        from > to && it in to until from -> it + 1
                        else -> it
                    }
                }.toSet()
                
                val newColorEdited = when {
                    s.colorToBeEditedForIndex == from -> to
                    s.colorToBeEditedForIndex == -1 -> -1
                    from < to && s.colorToBeEditedForIndex in (from + 1)..to -> s.colorToBeEditedForIndex - 1
                    from > to && s.colorToBeEditedForIndex in to until from -> s.colorToBeEditedForIndex + 1
                    else -> s.colorToBeEditedForIndex
                }

                setState(s.copy(
                    fieldsInput = newFieldsInput,
                    expressions = newExpressions,
                    colors = newColors,
                    errors = newErrors,
                    isSelectedIndexes = newSelected,
                    colorToBeEditedForIndex = newColorEdited
                ))
            }

            is XYContract.Event.RemoveFunctions -> {
                val s = uiState.value
                val indexesToRemove = event.indexes.sortedDescending()
                
                var newFieldsInput = s.fieldsInput
                var newExpressions = s.expressions
                var newColors = s.colors
                var newErrors = s.errors
                
                indexesToRemove.forEach { idx ->
                    newFieldsInput = newFieldsInput.removeElement(idx)
                    newExpressions = newExpressions.removeElement(idx)
                    newColors = newColors.removeElement(idx)
                    newErrors = newErrors.removeElement(idx)
                }
                
                setState(s.copy(
                    fieldsInput = newFieldsInput,
                    expressions = newExpressions,
                    colors = newColors,
                    errors = newErrors,
                    isSelectedIndexes = emptySet(),
                    colorToBeEditedForIndex = -1
                ))
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
