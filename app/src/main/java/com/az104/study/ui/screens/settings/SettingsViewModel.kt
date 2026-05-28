package com.az104.study.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.az104.study.data.local.dao.QuestionDao
import com.az104.study.data.local.dao.StudyGuideDao
import com.az104.study.domain.sync.SyncManager
import com.az104.study.domain.sync.SyncStatus
import com.az104.study.util.DarkModePreference
import com.az104.study.util.ServerUrlManager
import com.az104.study.util.TokenManager
import com.az104.study.util.UserPreferencesManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

data class SettingsState(
    val isPaired: Boolean = false,
    val serverUrl: String = "",
    val profileName: String = "",
    val homeWifiSsid: String = "",
    val darkMode: DarkModePreference = DarkModePreference.SYSTEM,
    val syncStatus: SyncStatus = SyncStatus.IDLE,
    val syncMessage: String = "",
    val lastSyncTime: String = "Never",
    val isLoading: Boolean = false,
    val studyGuideCount: Int = 0,
    val questionCount: Int = 0
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val tokenManager: TokenManager,
    private val serverUrlManager: ServerUrlManager,
    private val userPreferencesManager: UserPreferencesManager,
    private val syncManager: SyncManager,
    private val studyGuideDao: StudyGuideDao,
    private val questionDao: QuestionDao
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsState())
    val state: StateFlow<SettingsState> = _state.asStateFlow()

    init { load() }

    private fun load() {
        viewModelScope.launch {
            val serverUrl = serverUrlManager.getServerUrl()?.toString() ?: ""
            val profileName = serverUrlManager.getProfileName() ?: ""
            val wifiSsid = userPreferencesManager.homeWifiSsid.first() ?: ""
            val darkMode = userPreferencesManager.darkMode.first()
            val lastSync = serverUrlManager.getLastSyncTimestamp()

            val studyGuideCount = studyGuideDao.count()
            val questionCount = questionDao.observeCount().first()

            _state.value = SettingsState(
                isPaired = tokenManager.isPaired(),
                serverUrl = serverUrl,
                profileName = profileName,
                homeWifiSsid = wifiSsid,
                darkMode = darkMode,
                syncStatus = syncManager.syncState.value.status,
                syncMessage = syncManager.syncState.value.message,
                lastSyncTime = if (lastSync > 0)
                    SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()).format(Date(lastSync))
                else "Never",
                studyGuideCount = studyGuideCount,
                questionCount = questionCount
            )
        }
    }

    fun setHomeWifiSsid(ssid: String) {
        _state.value = _state.value.copy(homeWifiSsid = ssid)
        viewModelScope.launch {
            userPreferencesManager.setHomeWifiSsid(ssid)
        }
    }

    fun setDarkMode(mode: DarkModePreference) {
        _state.value = _state.value.copy(darkMode = mode)
        viewModelScope.launch {
            userPreferencesManager.setDarkMode(mode)
        }
    }

    fun triggerSync() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            val result = syncManager.sync(isManual = true)
            val lastSync = serverUrlManager.getLastSyncTimestamp()
            _state.value = _state.value.copy(
                isLoading = false,
                syncStatus = result.status,
                syncMessage = result.message,
                lastSyncTime = if (lastSync > 0)
                    SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()).format(Date(lastSync))
                else "Never"
            )
        }
    }

    fun clearPairing() {
        viewModelScope.launch {
            tokenManager.clearApiToken()
            serverUrlManager.clearAll()
            _state.value = _state.value.copy(isPaired = false, serverUrl = "", profileName = "")
        }
    }
}
