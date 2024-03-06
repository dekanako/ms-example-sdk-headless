package com.theminesec.example.headless.exampleHelper

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.theminesec.example.headless.util.TAG
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import ulid.ULID

class HelperViewModel : ViewModel() {

    // for demo setups
    private val _messages: MutableStateFlow<List<String>> = MutableStateFlow(emptyList())
    val messages: StateFlow<List<String>> = _messages
    fun writeMessage(message: String) = viewModelScope.launch {
        val temp = _messages.value
            .toMutableList()
            .apply { add("==> $message") }

        _messages.emit(temp)
    }

    fun clearLog() = viewModelScope.launch { _messages.emit(emptyList()) }

    // demo part
    var posReference by mutableStateOf(ULID.randomULID())
    val currency by mutableStateOf("HKD")
    var amountStr by mutableStateOf("2")
        private set

    fun handleInputAmt(incomingStr: String) {
        Log.d(TAG, "incoming: $incomingStr")
        if (incomingStr.length > 12) return
        if (incomingStr.count { c -> c == '.' } > 1) return
        incomingStr.split(".").getOrNull(1)?.let { afterDecimal ->
            if (afterDecimal.length > 2) return
        }
        amountStr = incomingStr.filter { c -> c.isDigit() || c == '.' }
    }

    fun resetRandomPosReference() {
        posReference = ULID.randomULID()
    }
}
