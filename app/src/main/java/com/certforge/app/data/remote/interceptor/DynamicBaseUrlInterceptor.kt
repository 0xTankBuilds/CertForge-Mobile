package com.certforge.app.data.remote.interceptor

import com.certforge.app.util.ServerUrlManager
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

        // Rewrite scheme/host/port to the paired server URL
        val builder = original.url.newBuilder()
            .scheme(serverUrl.scheme)
            .host(serverUrl.host)
        if (serverUrl.port > 0) {
            builder.port(serverUrl.port)
        }
        val newUrl = builder.build()

        return chain.proceed(
            original.newBuilder()
                .url(newUrl)
                .build()
        )
    }
}
