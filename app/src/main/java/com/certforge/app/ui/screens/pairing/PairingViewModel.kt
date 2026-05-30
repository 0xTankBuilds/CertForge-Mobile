package com.certforge.app.ui.screens.pairing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.certforge.app.data.repository.PairingRepository
import com.certforge.app.data.repository.PendingPairing
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PairingState(
    val status: PairingStatus = PairingStatus.CONFIRMING,
    val error: String? = null,
    val profileName: String? = null
)

enum class PairingStatus {
    CONFIRMING, SUCCESS, ERROR
}

@HiltViewModel
class PairingViewModel @Inject constructor(
    private val pairingRepository: PairingRepository
) : ViewModel() {

    private val _state = MutableStateFlow(PairingState())
    val state: StateFlow<PairingState> = _state.asStateFlow()

    fun startPairing(setupToken: String) {
        val payload = PendingPairing.payload.also { PendingPairing.payload = null }
        if (payload == null) {
            _state.value = PairingState(
                status = PairingStatus.ERROR,
                error = "QR code data not found. Please scan again."
            )
            return
        }

        viewModelScope.launch {
            _state.value = PairingState(status = PairingStatus.CONFIRMING)

            val result = pairingRepository.confirmPairing(payload)
            if (result.success) {
                _state.value = PairingState(
                    status = PairingStatus.SUCCESS,
                    profileName = result.profileName
                )
            } else {
                _state.value = PairingState(
                    status = PairingStatus.ERROR,
                    error = result.message
                )
            }
        }
    }
}
