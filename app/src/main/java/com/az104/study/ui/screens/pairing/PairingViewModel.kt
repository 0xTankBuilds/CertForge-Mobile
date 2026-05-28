package com.az104.study.ui.screens.pairing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.az104.study.data.repository.PairingRepository
import com.az104.study.data.repository.QrPayload
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

    private var serverUrl: String = ""
    private var profileId: String = ""
    private var profileName: String = ""

    fun startPairing(setupToken: String, url: String, pId: String, pName: String) {
        serverUrl = url
        profileId = pId
        profileName = pName

        viewModelScope.launch {
            _state.value = PairingState(status = PairingStatus.CONFIRMING)

            val payload = QrPayload(
                url = url,
                setupToken = setupToken,
                profileId = profileId,
                profileName = profileName
            )

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
