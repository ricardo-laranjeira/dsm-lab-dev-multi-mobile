package com.example.orcamentoapp.data.api

import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(
    private val obterToken: () -> String?
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val path = originalRequest.url.encodedPath

        val isLoginOrSignup = path.endsWith("auth/login") ||
                (originalRequest.method.equals("POST", ignoreCase = true) &&
                        (path.endsWith("/usuarios") || path.endsWith("/usuarios/")))

        val token = obterToken()

        return if (!token.isNullOrBlank() && originalRequest.header("Authorization") == null && !isLoginOrSignup) {
            val formattedToken = if (token.startsWith("Bearer ", ignoreCase = true)) token else "Bearer $token"
            val newRequest = originalRequest.newBuilder()
                .header("Authorization", formattedToken)
                .build()
            chain.proceed(newRequest)
        } else {
            chain.proceed(originalRequest)
        }
    }
}
