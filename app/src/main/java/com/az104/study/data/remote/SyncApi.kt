package com.az104.study.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

// ─── DTOs ────────────────────────────────────────────────────────────────────

@Serializable
data class PairRequest(
    @SerialName("profileId") val profileId: String,
    @SerialName("deviceName") val deviceName: String? = null,
    @SerialName("pin") val pin: String? = null
)

@Serializable
data class PairResponse(
    @SerialName("setupToken") val setupToken: String,
    @SerialName("expiresAt") val expiresAt: Long,
    @SerialName("profile") val profile: ProfileInfo
)

@Serializable
data class ConfirmRequest(
    @SerialName("setupToken") val setupToken: String
)

@Serializable
data class ConfirmResponse(
    @SerialName("apiToken") val apiToken: String,
    @SerialName("deviceId") val deviceId: String,
    @SerialName("profile") val profile: ProfileInfo
)

@Serializable
data class ProfileInfo(
    @SerialName("id") val id: String,
    @SerialName("name") val name: String,
    @SerialName("avatarColor") val avatarColor: String? = null,
    @SerialName("hasPin") val hasPin: Boolean? = null
)

@Serializable
data class ManifestResponse(
    @SerialName("manifest") val manifest: Map<String, String>,
    @SerialName("serverTime") val serverTime: Long
)

@Serializable
data class DomainsResponse(
    @SerialName("domains") val domains: List<DomainDto>,
    @SerialName("timestamp") val timestamp: Long
)

@Serializable
data class DomainDto(
    @SerialName("id") val id: String,
    @SerialName("name") val name: String,
    @SerialName("description") val description: String,
    @SerialName("weight") val weight: Int,
    @SerialName("moduleId") val moduleId: String,
    @SerialName("chapters") val chapters: List<ChapterDto>
)

@Serializable
data class ChapterDto(
    @SerialName("id") val id: String,
    @SerialName("title") val title: String,
    @SerialName("articleId") val articleId: String,
    @SerialName("contentUrl") val contentUrl: String
)

@Serializable
data class QuestionsResponse(
    @SerialName("questions") val questions: List<QuestionDto>,
    @SerialName("count") val count: Int,
    @SerialName("timestamp") val timestamp: Long
)

@Serializable
data class QuestionDto(
    @SerialName("id") val id: String,
    @SerialName("domainId") val domainId: String?,
    @SerialName("type") val type: String,
    @SerialName("questionText") val questionText: String,
    @SerialName("options") val options: List<String>,
    @SerialName("correctAnswerIndex") val correctAnswerIndex: Int,
    @SerialName("explanation") val explanation: String,
    @SerialName("difficulty") val difficulty: String,
    @SerialName("sourceReference") val sourceReference: String?,
    @SerialName("createdAt") val createdAt: Long,
    @SerialName("updatedAt") val updatedAt: Long?
)

@Serializable
data class StudyGuidesResponse(
    @SerialName("studyGuides") val studyGuides: List<StudyGuideDto>,
    @SerialName("count") val count: Int,
    @SerialName("timestamp") val timestamp: Long
)

@Serializable
data class StudyGuideDto(
    @SerialName("id") val id: String,
    @SerialName("articleId") val articleId: String,
    @SerialName("title") val title: String,
    @SerialName("content") val content: String,
    @SerialName("createdAt") val createdAt: Long,
    @SerialName("updatedAt") val updatedAt: Long?
)

@Serializable
data class ArticlesResponse(
    @SerialName("articles") val articles: List<ArticleDto>,
    @SerialName("count") val count: Int,
    @SerialName("timestamp") val timestamp: Long
)

@Serializable
data class ArticleDto(
    @SerialName("id") val id: String,
    @SerialName("title") val title: String,
    @SerialName("url") val url: String,
    @SerialName("domainId") val domainId: String,
    @SerialName("domainName") val domainName: String?
)

@Serializable
data class ArticleContentResponse(
    @SerialName("success") val success: Boolean,
    @SerialName("data") val data: ArticleContentDto
)

@Serializable
data class ArticleContentDto(
    @SerialName("articleId") val articleId: String,
    @SerialName("title") val title: String,
    @SerialName("html") val html: String,
    @SerialName("breadcrumb") val breadcrumb: List<String>? = null,
    @SerialName("relatedModules") val relatedModules: List<String>? = null
)

@Serializable
data class StudyGuideContentResponse(
    @SerialName("success") val success: Boolean,
    @SerialName("guide") val guide: StudyGuideContentDto
)

@Serializable
data class StudyGuideContentDto(
    @SerialName("id") val id: String,
    @SerialName("articleId") val articleId: String,
    @SerialName("title") val title: String,
    @SerialName("content") val content: String,
    @SerialName("createdAt") val createdAt: Long
)

@Serializable
data class ProgressUploadRequest(
    @SerialName("profileId") val profileId: String,
    @SerialName("sessions") val sessions: List<UploadSessionDto> = emptyList(),
    @SerialName("chapterUpdates") val chapterUpdates: List<UploadChapterDto> = emptyList()
)

@Serializable
data class UploadSessionDto(
    @SerialName("clientId") val clientId: String,
    @SerialName("type") val type: String,
    @SerialName("domainId") val domainId: String? = null,
    @SerialName("totalQuestions") val totalQuestions: Int,
    @SerialName("answeredQuestions") val answeredQuestions: Int,
    @SerialName("score") val score: Int? = null,
    @SerialName("startedAt") val startedAt: Long,
    @SerialName("completedAt") val completedAt: Long? = null,
    @SerialName("attempts") val attempts: List<UploadAttemptDto> = emptyList()
)

@Serializable
data class UploadAttemptDto(
    @SerialName("questionId") val questionId: String,
    @SerialName("selectedAnswerIndex") val selectedAnswerIndex: Int,
    @SerialName("isCorrect") val isCorrect: Boolean,
    @SerialName("timeSpentSeconds") val timeSpentSeconds: Int = 0
)

@Serializable
data class UploadChapterDto(
    @SerialName("chapterId") val chapterId: String,
    @SerialName("completedAt") val completedAt: Long? = null,
    @SerialName("timeSpentSeconds") val timeSpentSeconds: Int = 0
)

@Serializable
data class ProgressUploadResponse(
    @SerialName("processed") val processed: ProcessedCounts,
    @SerialName("serverTimestamp") val serverTimestamp: Long
)

@Serializable
data class ProcessedCounts(
    @SerialName("sessions") val sessions: Int,
    @SerialName("chapters") val chapters: Int
)

@Serializable
data class ProgressDownloadResponse(
    @SerialName("sessions") val sessions: List<DownloadSessionDto>,
    @SerialName("chapterProgress") val chapterProgress: List<DownloadChapterDto>,
    @SerialName("serverTimestamp") val serverTimestamp: Long
)

@Serializable
data class DownloadSessionDto(
    @SerialName("id") val id: String,
    @SerialName("type") val type: String,
    @SerialName("domainId") val domainId: String?,
    @SerialName("totalQuestions") val totalQuestions: Int,
    @SerialName("answeredQuestions") val answeredQuestions: Int,
    @SerialName("score") val score: Int?,
    @SerialName("startedAt") val startedAt: Long,
    @SerialName("completedAt") val completedAt: Long?,
    @SerialName("clientId") val clientId: String?,
    @SerialName("attempts") val attempts: List<DownloadAttemptDto>
)

@Serializable
data class DownloadAttemptDto(
    @SerialName("id") val id: String,
    @SerialName("questionId") val questionId: String,
    @SerialName("selectedAnswerIndex") val selectedAnswerIndex: Int,
    @SerialName("isCorrect") val isCorrect: Boolean,
    @SerialName("answeredAt") val answeredAt: Long,
    @SerialName("timeSpentSeconds") val timeSpentSeconds: Int
)

@Serializable
data class DownloadChapterDto(
    @SerialName("chapterId") val chapterId: String,
    @SerialName("completedAt") val completedAt: Long?,
    @SerialName("timeSpentSeconds") val timeSpentSeconds: Int
)

// ─── API Interface ───────────────────────────────────────────────────────────

interface SyncApi {

    @POST("api/devices/confirm")
    suspend fun confirmDevice(@Body request: ConfirmRequest): ConfirmResponse

    @GET("api/sync/manifest")
    suspend fun getManifest(): ManifestResponse

    @GET("api/sync/domains")
    suspend fun getDomains(): DomainsResponse

    @GET("api/sync/questions")
    suspend fun getQuestions(@Query("since") since: Long? = null): QuestionsResponse

    @GET("api/sync/study-guides")
    suspend fun getStudyGuides(@Query("since") since: Long? = null): StudyGuidesResponse

    @GET("api/sync/articles")
    suspend fun getArticles(): ArticlesResponse

    @GET("api/articles/{articleId}")
    suspend fun getArticleContent(@Path("articleId") articleId: String): ArticleContentResponse

    @GET("api/study-guides")
    suspend fun getStudyGuide(@Query("articleId") articleId: String): StudyGuideContentResponse

    @POST("api/sync/progress")
    suspend fun uploadProgress(@Body request: ProgressUploadRequest): ProgressUploadResponse

    @GET("api/sync/progress")
    suspend fun downloadProgress(
        @Query("profileId") profileId: String,
        @Query("since") since: Long? = null
    ): ProgressDownloadResponse
}
