package com.az104.study.ui.navigation

import java.net.URLDecoder
import java.net.URLEncoder

sealed class Route(val route: String) {
    data object Dashboard : Route("dashboard")
    data object Domains : Route("domains")
    data object DomainChapters : Route("domains/{domainId}") {
        fun create(domainId: String) = "domains/$domainId"
    }
    data object Article : Route("article/{articleId}") {
        fun create(articleId: String) = "article/${URLEncoder.encode(articleId, "UTF-8")}"
    }
    data object StudyGuide : Route("study-guide/{articleId}") {
        fun create(articleId: String) = "study-guide/${URLEncoder.encode(articleId, "UTF-8")}"
    }
    data object QuizSelect : Route("quiz/select")
    data object QuizSession : Route("quiz/session/{domainId}") {
        fun create(domainId: String?) = "quiz/session/${domainId ?: "all"}"
    }
    data object QuizResults : Route("quiz/results/{sessionClientId}") {
        fun create(sessionClientId: String) = "quiz/results/$sessionClientId"
    }
    data object QuizHistory : Route("quiz/history")
    data object Analytics : Route("analytics")
    data object Achievements : Route("achievements")
    data object Settings : Route("settings")
    data object ScanQr : Route("scan-qr") {
        fun create() = "scan-qr"
    }
    data object Pairing : Route("pairing/{setupToken}/{serverUrl}/{profileId}/{profileName}") {
        fun create(setupToken: String, serverUrl: String, profileId: String, profileName: String): String {
            val encUrl = URLEncoder.encode(serverUrl, "UTF-8")
            val encName = URLEncoder.encode(profileName, "UTF-8")
            return "pairing/$setupToken/$encUrl/$profileId/$encName"
        }

        fun decodeUrl(encoded: String): String = URLDecoder.decode(encoded, "UTF-8")
        fun decodeName(encoded: String): String = URLDecoder.decode(encoded, "UTF-8")
    }
}

enum class BottomNavItem(val route: String, val label: String) {
    DASHBOARD("dashboard", "Home"),
    STUDY("domains", "Study"),
    QUIZ("quiz/select", "Quiz"),
    ANALYTICS("analytics", "Analytics"),
    SETTINGS("settings", "Settings")
}
