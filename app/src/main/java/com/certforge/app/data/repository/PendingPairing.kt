package com.certforge.app.data.repository

/**
 * Global holder for the QR payload between the scan screen and the pairing screen.
 * Avoids Navigation route argument encoding issues and DI scoping problems.
 */
object PendingPairing {
    @Volatile
    var payload: QrPayload? = null
}
