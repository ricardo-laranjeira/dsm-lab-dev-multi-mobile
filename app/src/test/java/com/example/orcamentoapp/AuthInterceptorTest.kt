package com.example.orcamentoapp

import com.example.orcamentoapp.data.api.AuthInterceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class AuthInterceptorTest {

    private lateinit var mockWebServer: MockWebServer
    private var tokenProvider: () -> String? = { "meu_token_secret" }

    @Before
    fun setup() {
        mockWebServer = MockWebServer()
        mockWebServer.start()
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    @Test
    fun intercept_comTokenValido_adicionaHeaderAuthorization() {
        tokenProvider = { "meu_token_secret" }
        val client = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor { tokenProvider() })
            .build()

        mockWebServer.enqueue(MockResponse().setResponseCode(200))

        val request = Request.Builder()
            .url(mockWebServer.url("/clientes/"))
            .build()

        client.newCall(request).execute()

        val recordedRequest = mockWebServer.takeRequest()
        assertEquals("Bearer meu_token_secret", recordedRequest.getHeader("Authorization"))
    }

    @Test
    fun intercept_comTokenJaFormatado_naoDuplicaBearer() {
        tokenProvider = { "Bearer token_existente" }
        val client = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor { tokenProvider() })
            .build()

        mockWebServer.enqueue(MockResponse().setResponseCode(200))

        val request = Request.Builder()
            .url(mockWebServer.url("/clientes/"))
            .build()

        client.newCall(request).execute()

        val recordedRequest = mockWebServer.takeRequest()
        assertEquals("Bearer token_existente", recordedRequest.getHeader("Authorization"))
    }

    @Test
    fun intercept_comTokenNuloOuEmBranco_naoAdicionaHeader() {
        tokenProvider = { "" }
        val client = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor { tokenProvider() })
            .build()

        mockWebServer.enqueue(MockResponse().setResponseCode(200))

        val request = Request.Builder()
            .url(mockWebServer.url("/clientes/"))
            .build()

        client.newCall(request).execute()

        val recordedRequest = mockWebServer.takeRequest()
        assertNull(recordedRequest.getHeader("Authorization"))
    }

    @Test
    fun intercept_endpointDeLogin_naoAdicionaHeader() {
        tokenProvider = { "meu_token_secret" }
        val client = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor { tokenProvider() })
            .build()

        mockWebServer.enqueue(MockResponse().setResponseCode(200))

        val request = Request.Builder()
            .url(mockWebServer.url("/auth/login"))
            .post(okhttp3.RequestBody.create(null, ""))
            .build()

        client.newCall(request).execute()

        val recordedRequest = mockWebServer.takeRequest()
        assertNull(recordedRequest.getHeader("Authorization"))
    }

    @Test
    fun intercept_endpointDeCadastroUsuario_naoAdicionaHeader() {
        tokenProvider = { "meu_token_secret" }
        val client = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor { tokenProvider() })
            .build()

        mockWebServer.enqueue(MockResponse().setResponseCode(200))

        val request = Request.Builder()
            .url(mockWebServer.url("/usuarios/"))
            .post(okhttp3.RequestBody.create(null, ""))
            .build()

        client.newCall(request).execute()

        val recordedRequest = mockWebServer.takeRequest()
        assertNull(recordedRequest.getHeader("Authorization"))
    }

    @Test
    fun intercept_headerAuthorizationJaPresente_naoSobrescreve() {
        tokenProvider = { "token_interceptor" }
        val client = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor { tokenProvider() })
            .build()

        mockWebServer.enqueue(MockResponse().setResponseCode(200))

        val request = Request.Builder()
            .url(mockWebServer.url("/clientes/"))
            .header("Authorization", "Bearer token_manual")
            .build()

        client.newCall(request).execute()

        val recordedRequest = mockWebServer.takeRequest()
        assertEquals("Bearer token_manual", recordedRequest.getHeader("Authorization"))
    }
}
