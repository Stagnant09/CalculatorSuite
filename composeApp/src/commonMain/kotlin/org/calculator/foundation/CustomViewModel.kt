package com.example.calculator.foundation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

abstract class CustomViewModel<T : CustomState, R : CustomEvent, S : CustomEffect>(
    initialState: T
) : ViewModel() {

    private val _uiState: MutableStateFlow<T> = MutableStateFlow(initialState)
    val uiState: StateFlow<T> = _uiState.asStateFlow()

    private val _effect: Channel<S> = Channel()
    val uiEffect: Flow<S> = _effect.receiveAsFlow()

    /**
     * Sets a new UI effect to be emitted.
     * @param builder A lambda that produces the effect.
     */
    fun setEffect(builder: () -> S) {
        val effectValue = builder()
        viewModelScope.launch {
            _effect.send(effectValue)
        }
    }

    /**
     * Updates the UI state.
     * The new state completely replaces the old state.
     * @param newState The new state to set.
     */
    protected fun setState(newState: T) {
        _uiState.value = newState
    }

    /**
     * Updates the UI state based on the current state.
     * @param reducer A function that takes the current state and returns the new state.
     */
    protected fun setState(reducer: (oldState: T) -> T) {
        _uiState.update { reducer(it) }
    }


    /**
     * Processes a UI event.
     * @param event The event to process.
     */
    fun setEvent(event: R) {
        viewModelScope.launch {
            handleEvent(event)
        }
    }

    /**
     * Abstract method to be implemented by subclasses to handle specific events.
     * This is where you'd typically call setState or setEffect based on the event.
     * @param event The event to handle.
     */
    protected abstract suspend fun handleEvent(event: R)
}