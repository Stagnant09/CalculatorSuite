package org.calculator.ui.screens

import androidx.compose.ui.graphics.Color
import com.example.calculator.foundation.CustomEffect
import com.example.calculator.foundation.CustomEvent
import com.example.calculator.foundation.CustomState
import org.calculator.ui.utils.Expression

sealed interface XYContract {

    data class State(
        val fieldsInput: List<String> = emptyList(),
        val expressions: List<Expression> = emptyList(),
        val colors: List<Color> = emptyList(),
        val selectedFunctionIndex: Int = -1
    ) : CustomState

    sealed interface Event : CustomEvent {
        data class UpdateFieldInput(val index: Int, val value: String) : Event
        data class SelectFunction(val index: Int) : Event
        data class UpdateColor(val color: Color) : Event
    }

    sealed interface Effect : CustomEffect {

    }

}