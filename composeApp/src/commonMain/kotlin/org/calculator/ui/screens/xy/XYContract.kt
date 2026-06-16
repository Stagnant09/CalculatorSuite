package org.calculator.ui.screens

import androidx.compose.ui.graphics.Color
import org.calculator.foundation.CustomEffect
import org.calculator.foundation.CustomEvent
import org.calculator.foundation.CustomState
import org.calculator.ui.utils.Expression

sealed interface XYContract {

    data class State(
        val fieldsInput: List<String> = emptyList(),
        val expressions: List<Expression> = emptyList(),
        val colors: List<Color> = emptyList(),
        val thicknesses: List<Float> = emptyList(),
        val colorToBeEditedForIndex: Int = -1,
        val isSelectedIndexes: Set<Int> = emptySet(),
        val errors: List<String?> = emptyList(),
        val scale: Float = 1f,
        val offsetX: Float = 0f,
        val offsetY: Float = 0f
    ) : CustomState

    sealed interface Event : CustomEvent {
        data class UpdateFieldInput(val index: Int, val value: String) : Event
        data class SetColorToBeEditedForIndex(val index: Int) : Event
        data class SelectFunction(val index: Int) : Event
        data class UpdateColor(val color: Color) : Event
        /** Adds a blank "y = x" slot */
        data object AddFunction : Event
        /** Adds a slot pre-populated with a specific formula (from AddGraphDialog) */
        data class AddFunctionWithFormula(val formula: String) : Event
        data class RemoveFunction(val index: Int) : Event
        data class RemoveFunctions(val indexes: Set<Int>) : Event
        data class MoveFunction(val fromIndex: Int, val toIndex: Int) : Event
        data class UpdateViewport(val scale: Float, val offsetX: Float, val offsetY: Float) : Event
        data object ResetView : Event
    }

    sealed interface Effect : CustomEffect
}
