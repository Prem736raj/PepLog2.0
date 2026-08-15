package com.appvexis.peptidetracker.core.common.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

/**
 * Base ViewModel class executing Unidirectional Data Flow (UDF) logic.
 *
 * State: Persistent screen UI representation.
 * Event: Input actions that trigger changes (e.g. user gestures, network responses).
 * Effect: Direct one-off triggers for navigation, notifications, error views.
 */
abstract class BaseViewModel<State : UiState, Event : UiEvent, Effect : UiSideEffect>(
    initialState: State
) : ViewModel() {

    private val _uiState: MutableStateFlow<State> = MutableStateFlow(initialState)
    val uiState: StateFlow<State> = _uiState.asStateFlow()

    private val _uiEvent: MutableSharedFlow<Event> = MutableSharedFlow()
    val uiEvent: SharedFlow<Event> = _uiEvent.asSharedFlow()

    private val _effect: Channel<Effect> = Channel(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    val currentState: State
        get() = _uiState.value

    init {
        subscribeEvents()
    }

    /**
     * Set up UI event collection on the ViewModel Scope.
     */
    private fun subscribeEvents() {
        viewModelScope.launch {
            _uiEvent.collect { event ->
                handleEvent(event)
            }
        }
    }

    /**
     * Subclasses must override this to handle UI events.
     */
    abstract fun handleEvent(event: Event)

    /**
     * Triggers event handling inside the VM.
     */
    fun setEvent(event: Event) {
        viewModelScope.launch { _uiEvent.emit(event) }
    }

    /**
     * Updates the StateFlow state using thread-safe state reduction.
     */
    protected fun updateState(reducer: State.() -> State) {
        val newState = currentState.reducer()
        _uiState.value = newState
    }

    /**
     * Emits a short-lived visual side-effect.
     */
    protected fun sendEffect(effect: Effect) {
        viewModelScope.launch {
            _effect.send(effect)
        }
    }
}
