package com.example.orcamentoapp

import com.example.orcamentoapp.data.api.AuthInterceptor
import com.example.orcamentoapp.data.api.UsuarioApiService
import com.example.orcamentoapp.data.repositories.PerfilRepository
import kotlinx.coroutines.test.runTest
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class PerfilRepositoryTest {

    private lateinit var mockWebServer: MockWebServer
    private lateinit var repository: PerfilRepository

    @Before
    fun setup() {
        mockWebServer = MockWebServer()
        mockWebServer.start()

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor { "token123" })
            .build()

        val apiService = Retrofit.Builder()
            .baseUrl(mockWebServer.url("/"))
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(UsuarioApiService::class.java)

        repository = PerfilRepository(apiService)
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    @Test
    fun obterPerfil_sucesso_retornaUsuario() = runTest {
        val jsonResponse = """
            {
                "idusuarios": 1,
                "nome": "Carlos Silva",
                "email": "carlos@exemplo.com",
                "ativo": true,
                "data_cadastro": "2023-01-01",
                "data_atualizacao": "2023-01-02"
            }
        """.trimIndent()

        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(jsonResponse))

        val perfil = repository.obterPerfil()

        assertEquals(1, perfil.idusuarios)
        assertEquals("Carlos Silva", perfil.nome)
        assertEquals("carlos@exemplo.com", perfil.email)

        val request = mockWebServer.takeRequest()
        assertEquals("/usuarios/me", request.path)
        assertEquals("GET", request.method)
        assertEquals("Bearer token123", request.getHeader("Authorization"))
    }

    @Test
    fun obterPerfil_falhaHttp_lancaExcecao() = runTest {
        mockWebServer.enqueue(MockResponse().setResponseCode(401).setBody("Não autorizado"))

        try {
            repository.obterPerfil()
            fail("Deveria ter lançado uma exceção")
        } catch (e: Exception) {
            assertNotNull(e.message)
        }
    }

    @Test
    fun atualizarPerfil_sucesso_retornaUsuarioAtualizado() = runTest {
        val jsonResponse = """
            {
                "idusuarios": 1,
                "nome": "Carlos E. Silva",
                "email": "carlos.novo@exemplo.com",
                "ativo": true,
                "data_cadastro": "2023-01-01",
                "data_atualizacao": "2023-01-03"
            }
        """.trimIndent()

        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(jsonResponse))

        val perfilAtualizado = repository.atualizarPerfil("Carlos E. Silva", "carlos.novo@exemplo.com")

        assertEquals("Carlos E. Silva", perfilAtualizado.nome)
        assertEquals("carlos.novo@exemplo.com", perfilAtualizado.email)

        val request = mockWebServer.takeRequest()
        assertEquals("PATCH", request.method)
        assertEquals("/usuarios/me", request.path)
        assertEquals("Bearer token123", request.getHeader("Authorization"))
    }

    @Test
    fun atualizarPerfil_falhaHttp_lancaExcecao() = runTest {
        mockWebServer.enqueue(MockResponse().setResponseCode(400).setBody("Email já em uso"))

        try {
            repository.atualizarPerfil("Nome", "email@uso.com")
            fail("Deveria ter lançado uma exceção")
        } catch (e: Exception) {
            assertNotNull(e.message)
        }
    }

    @Test
    fun atualizarSenha_sucesso_fazPatch() = runTest {
        mockWebServer.enqueue(MockResponse().setResponseCode(200))

        repository.atualizarSenha("senha123", "novaSenha456")

        val request = mockWebServer.takeRequest()
        assertEquals("PATCH", request.method)
        assertEquals("/usuarios/me/senha", request.path)
        assertEquals("Bearer token123", request.getHeader("Authorization"))
    }

    @Test
    fun atualizarSenha_falhaHttp_lancaExcecao() = runTest {
        mockWebServer.enqueue(MockResponse().setResponseCode(400).setBody("Senha atual incorreta"))

        try {
            repository.atualizarSenha("errada", "nova")
            fail("Deveria ter lançado uma exceção")
        } catch (e: Exception) {
            assertNotNull(e.message)
        }
    }
}
