package com.az104.study.data.repository

import com.az104.study.data.remote.ConfirmRequest
import com.az104.study.data.remote.SyncApi
import com.az104.study.util.ServerUrlManager
import com.az104.study.util.TokenManager
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import javax.inject.Inject

data class QrPayload(
    val url: String,
    val setupToken: String,
    val profileId: String,
    val profileName: String
)

data class PairingResult(
    val success: Boolean,
    val message: String = "",
    val profileName: String? = null
)

class PairingRepository @Inject constructor(
    private val syncApi: SyncApi,
    private val tokenManager: TokenManager,
    private val serverUrlManager: ServerUrlManager
) {
    /**
     * Parse a QR code JSON string into a QrPayload.
     * Expected format: {"url": "...", "setupToken": "...", "profileId": "...", "profileName": "..."}
     */
    fun parseQrContent(jsonStr: String): Result<QrPayload> {
        return try {
            val json = Json { ignoreUnknownKeys = true }
            val payload = json.decodeFromString<QrPayloadDto>(jsonStr)
            Result.success(
                QrPayload(
                    url = payload.url,
                    setupToken = payload.setupToken,
                    profileId = payload.profileId,
                    profileName = payload.profileName
                )
            )
        } catch (e: Exception) {
            Result.failure(IllegalArgumentException("Invalid QR code format"))
        }
    }

    /**
     * Confirm the pairing with the server, using the setup token from the QR code.
     * On success, stores apiToken, server URL, and profile info.
     */
    suspend fun confirmPairing(payload: QrPayload): PairingResult {
        return try {
            // Save server URL first so DynamicBaseUrlInterceptor can rewrite the base URL
            serverUrlManager.saveServerUrl(payload.url)

            val response = syncApi.confirmDevice(
                ConfirmRequest(setupToken = payload.setupToken)
            )
            tokenManager.saveApiToken(response.apiToken)
            serverUrlManager.saveProfileId(response.profile.id)
            serverUrlManager.saveProfileName(response.profile.name)
            PairingResult(
                success = true,
                profileName = response.profile.name
            )
        } catch (e: retrofit2.HttpException) {
            serverUrlManager.clearAll()
            val msg = when (e.code()) {
                404 -> "Invalid setup token. Check the QR code is correct."
                410 -> "Setup token has expired. Generate a new QR code from the web app."
                else -> "Server error: ${e.message()}"
            }
            PairingResult(false, msg)
        } catch (e: java.net.ConnectException) {
            serverUrlManager.clearAll()
            PairingResult(false, "Could not connect to the server. Make sure both devices are on the same network.")
        } catch (e: Exception) {
            serverUrlManager.clearAll()
            PairingResult(false, e.message ?: "Pairing failed")
        }
    }
}

@Serializable
private data class QrPayloadDto(
    val url: String,
    val setupToken: String,
    val profileId: String,
    val profileName: String
)
