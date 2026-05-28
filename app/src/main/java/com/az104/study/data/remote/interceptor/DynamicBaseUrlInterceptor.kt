package com.az104.study.data.remote.interceptor

import com.az104.study.util.ServerUrlManager
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class DynamicBaseUrlInterceptor @Inject constructor(
    private val serverUrlManager: ServerUrlManager
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val serverUrl = serverUrlManager.getServerUrl()

        if (serverUrl == null) {
            // No server paired — fail fast
            return chain.proceed(original)
        }

        // Rewrite the host to the paired server URL
        val newUrl = original.url.newBuilder()
            .scheme("http")
            .host(serverUrl.host)
            .port(serverUrl.port)
            .build()

        return chain.proceed(
            original.newBuilder()
                .url(newUrl)
                .build()
        )
    }
}
