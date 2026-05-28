package com.az104.study.domain.sync

import com.az104.study.data.repository.SyncResult
import com.az104.study.data.repository.SyncRepository
import com.az104.study.util.ServerUrlManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

enum class SyncStatus {
    IDLE, SYNCING, SUCCESS, ERROR
}

data class SyncState(
    val status: SyncStatus = SyncStatus.IDLE,
    val message: String = "",
    val lastSyncTime: Long = 0,
    val sessionsUploaded: Int = 0,
    val chaptersUploaded: Int = 0
)

@Singleton
class SyncManager @Inject constructor(
    private val syncRepository: SyncRepository,
    private val serverUrlManager: ServerUrlManager
) {
    private val _syncState = MutableStateFlow(SyncState())
    val syncState: StateFlow<SyncState> = _syncState.asStateFlow()

    private var lastSyncAttemptTime: Long = 0

    /**
     * Perform sync with rate limiting (max once per 5 minutes).
     */
    suspend fun sync(isManual: Boolean = false): SyncState {
        val now = System.currentTimeMillis()
        if (!isManual && (now - lastSyncAttemptTime) < 300_000L) {
            return _syncState.value // rate limited
        }

        if (!syncRepository.isPaired()) {
            _syncState.value = SyncState(
                status = SyncStatus.ERROR,
                message = "Not paired. Scan a QR code first."
            )
            return _syncState.value
        }

        _syncState.value = _syncState.value.copy(status = SyncStatus.SYNCING)
        lastSyncAttemptTime = now

        val result: SyncResult = syncRepository.performSync(isManual)

        _syncState.value = if (result.success) {
            serverUrlManager.setLastSyncTimestamp(now)
            SyncState(
                status = SyncStatus.SUCCESS,
                lastSyncTime = now,
                sessionsUploaded = result.sessionsUploaded,
                chaptersUploaded = result.chaptersUploaded
            )
        } else {
            SyncState(
                status = SyncStatus.ERROR,
                message = result.message
            )
        }
        return _syncState.value
    }

    fun isPaired(): Boolean {
        return serverUrlManager.getServerUrl() != null && serverUrlManager.getProfileId() != null
    }
}
