package org.calculator.ui.screens

import com.example.calculator.foundation.CustomEffect
import com.example.calculator.foundation.CustomEvent
import com.example.calculator.foundation.CustomState
import org.calculator.ui.utils.Expression

sealed interface XYContract {

    data class State(
        val fieldsInput: List<String> = emptyList(),
        val expressions: List<Expression> = emptyList()
    ) : CustomState

    sealed interface Event : CustomEvent {
        data class UpdateFieldInput(val index: Int, val value: String) : Event
    }

    sealed interface Effect : CustomEffect {

    }

}