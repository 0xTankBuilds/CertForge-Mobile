package com.certforge.app.ui.screens.scanning

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.certforge.app.data.repository.PairingRepository
import com.certforge.app.data.repository.PendingPairing
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ScanQrState(
    val scanned: Boolean = false,
    val processing: Boolean = false,
    val error: String? = null,
    val setupToken: String? = null
)

@HiltViewModel
class ScanQrViewModel @Inject constructor(
    application: Application,
    private val pairingRepository: PairingRepository
) : AndroidViewModel(application) {

    private val _state = MutableStateFlow(ScanQrState())
    val state: StateFlow<ScanQrState> = _state.asStateFlow()

    fun onQrScanned(rawValue: String) {
        if (_state.value.processing) return

        _state.value = _state.value.copy(processing = true, error = null)

        val result = try {
            pairingRepository.parseQrContent(rawValue)
        } catch (e: Exception) {
            _state.value = _state.value.copy(
                processing = false,
                error = "Failed to parse QR code: ${e.message}"
            )
            return
        }

        if (result.isFailure) {
            _state.value = _state.value.copy(
                processing = false,
                error = "Invalid QR code: ${result.exceptionOrNull()?.message}"
            )
            return
        }

        val payload = result.getOrNull() ?: return
        PendingPairing.payload = payload
        _state.value = _state.value.copy(
            scanned = true,
            setupToken = payload.setupToken,
            processing = false
        )
    }

    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }

    fun setError(error: String) {
        _state.value = _state.value.copy(error = error, processing = false)
    }

    fun reset() {
        _state.value = ScanQrState()
    }
}
