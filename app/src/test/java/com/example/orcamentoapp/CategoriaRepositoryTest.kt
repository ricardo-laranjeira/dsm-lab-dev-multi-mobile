package com.example.orcamentoapp

import com.example.orcamentoapp.data.api.AuthInterceptor
import com.example.orcamentoapp.data.api.CategoriaApiService
import com.example.orcamentoapp.data.repositories.CategoriaRepository
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

class CategoriaRepositoryTest {

    private lateinit var mockWebServer: MockWebServer
    private lateinit var repository: CategoriaRepository

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
            .create(CategoriaApiService::class.java)

        repository = CategoriaRepository(apiService)
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    @Test
    fun listar_sucesso_retornaListaDeCategorias() = runTest {
        val jsonResponse = """
            [
                {
                    "idcategorias": 1,
                    "descricao": "Elétrica",
                    "data_cadastro": "2023-01-01"
                },
                {
                    "idcategorias": 2,
                    "descricao": "Hidráulica",
                    "data_cadastro": "2023-01-02"
                }
            ]
        """.trimIndent()

        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(jsonResponse))

        val resultado = repository.listar()

        assertEquals(2, resultado.size)
        assertEquals(1, resultado[0].idcategorias)
        assertEquals("Elétrica", resultado[0].descricao)
        assertEquals(2, resultado[1].idcategorias)

        val request = mockWebServer.takeRequest()
        assertEquals("/categorias/", request.path)
        assertEquals("Bearer token123", request.getHeader("Authorization"))
    }

    @Test
    fun listar_falhaHttp_lancaExcecao() = runTest {
        mockWebServer.enqueue(MockResponse().setResponseCode(500).setBody("Erro interno no servidor"))

        try {
            repository.listar()
            fail("Deveria ter lançado uma exceção")
        } catch (e: Exception) {
            assertNotNull(e.message)
        }
    }

    @Test
    fun salvar_criarNovaCategoria_fazPostESucesso() = runTest {
        val jsonResponse = """
            {
                "idcategorias": 10,
                "descricao": "Pintura",
                "data_cadastro": "2023-01-03"
            }
        """.trimIndent()

        mockWebServer.enqueue(MockResponse().setResponseCode(201).setBody(jsonResponse))

        val categoriaSalva = repository.salvar(id = null, descricao = "Pintura")

        assertEquals(10, categoriaSalva.idcategorias)
        assertEquals("Pintura", categoriaSalva.descricao)

        val request = mockWebServer.takeRequest()
        assertEquals("POST", request.method)
        assertEquals("/categorias/", request.path)
        assertEquals("Bearer token123", request.getHeader("Authorization"))
    }

    @Test
    fun salvar_atualizarCategoria_fazPatchESucesso() = runTest {
        val jsonResponse = """
            {
                "idcategorias": 5,
                "descricao": "Pintura Geral",
                "data_cadastro": "2023-01-01",
                "data_atualizacao": "2023-01-03"
            }
        """.trimIndent()

        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(jsonResponse))

        val categoriaAtualizada = repository.salvar(id = 5, descricao = "Pintura Geral")

        assertEquals(5, categoriaAtualizada.idcategorias)
        assertEquals("Pintura Geral", categoriaAtualizada.descricao)

        val request = mockWebServer.takeRequest()
        assertEquals("PATCH", request.method)
        assertEquals("/categorias/5", request.path)
    }

    @Test
    fun excluir_sucesso_fazDelete() = runTest {
        mockWebServer.enqueue(MockResponse().setResponseCode(204))

        repository.excluir(id = 5)

        val request = mockWebServer.takeRequest()
        assertEquals("DELETE", request.method)
        assertEquals("/categorias/5", request.path)
        assertEquals("Bearer token123", request.getHeader("Authorization"))
    }
}
