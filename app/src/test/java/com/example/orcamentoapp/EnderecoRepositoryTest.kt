package com.example.orcamentoapp

import com.example.orcamentoapp.data.api.AuthInterceptor
import com.example.orcamentoapp.data.api.EnderecoApiService
import com.example.orcamentoapp.data.repositories.EnderecoRepository
import kotlinx.coroutines.test.runTest
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class EnderecoRepositoryTest {

    private lateinit var mockWebServer: MockWebServer
    private lateinit var repository: EnderecoRepository

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
            .create(EnderecoApiService::class.java)

        repository = EnderecoRepository(apiService)
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    @Test
    fun listar_sucesso_retornaListaDeEnderecos() = runTest {
        val jsonResponse = """
            [
                {
                    "idenderecos": 1,
                    "cliente_id": 10,
                    "cep": "01001-000",
                    "endereco": "Praça da Sé",
                    "numero": "100",
                    "complemento": "Apto 11",
                    "bairro": "Sé",
                    "cidade": "São Paulo",
                    "estado": "SP",
                    "data_cadastro": "2023-01-01"
                },
                {
                    "idenderecos": 2,
                    "cliente_id": 10,
                    "cep": "02002-000",
                    "endereco": "Rua das Flores",
                    "numero": "200",
                    "complemento": null,
                    "bairro": "Centro",
                    "cidade": "São Paulo",
                    "estado": "SP",
                    "data_cadastro": "2023-01-02"
                }
            ]
        """.trimIndent()

        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(jsonResponse))

        val resultado = repository.listar(clienteId = 10)

        assertEquals(2, resultado.size)
        assertEquals(1, resultado[0].idenderecos)
        assertEquals("Praça da Sé", resultado[0].endereco)
        assertEquals("Apto 11", resultado[0].complemento)
        assertEquals(2, resultado[1].idenderecos)

        val request = mockWebServer.takeRequest()
        assertEquals("/clientes/10/enderecos/", request.path)
        assertEquals("Bearer token123", request.getHeader("Authorization"))
    }

    @Test
    fun listar_listaVazia_retornaListaVazia() = runTest {
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody("[]"))

        val resultado = repository.listar(clienteId = 10)

        assertTrue(resultado.isEmpty())

        val request = mockWebServer.takeRequest()
        assertEquals("/clientes/10/enderecos/", request.path)
    }

    @Test
    fun listar_falhaHttp_lancaExcecao() = runTest {
        mockWebServer.enqueue(MockResponse().setResponseCode(500).setBody("Erro interno no servidor"))

        try {
            repository.listar(clienteId = 10)
            fail("Deveria ter lançado uma exceção")
        } catch (e: Exception) {
            assertNotNull(e.message)
        }
    }

    @Test
    fun salvar_criarNovoEndereco_fazPostESucesso() = runTest {
        val jsonResponse = """
            {
                "idenderecos": 5,
                "cliente_id": 10,
                "cep": "01001-000",
                "endereco": "Praça da Sé",
                "numero": "100",
                "complemento": "Bloco A",
                "bairro": "Sé",
                "cidade": "São Paulo",
                "estado": "SP",
                "data_cadastro": "2023-01-03"
            }
        """.trimIndent()

        mockWebServer.enqueue(MockResponse().setResponseCode(201).setBody(jsonResponse))

        val enderecoSalvo = repository.salvar(
            clienteId = 10,
            id = null,
            cep = "01001-000",
            endereco = "Praça da Sé",
            numero = "100",
            complemento = "Bloco A",
            bairro = "Sé",
            cidade = "São Paulo",
            estado = "SP"
        )

        assertEquals(5, enderecoSalvo.idenderecos)
        assertEquals("Praça da Sé", enderecoSalvo.endereco)

        val request = mockWebServer.takeRequest()
        assertEquals("POST", request.method)
        assertEquals("/clientes/10/enderecos/", request.path)
        assertEquals("Bearer token123", request.getHeader("Authorization"))
    }

    @Test
    fun salvar_atualizarEndereco_fazPatchESucesso() = runTest {
        val jsonResponse = """
            {
                "idenderecos": 5,
                "cliente_id": 10,
                "cep": "01001-000",
                "endereco": "Praça da Sé Alterada",
                "numero": "101",
                "complemento": null,
                "bairro": "Sé",
                "cidade": "São Paulo",
                "estado": "SP",
                "data_cadastro": "2023-01-03",
                "data_atualizacao": "2023-01-04"
            }
        """.trimIndent()

        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(jsonResponse))

        val enderecoAtualizado = repository.salvar(
            clienteId = 10,
            id = 5,
            cep = "01001-000",
            endereco = "Praça da Sé Alterada",
            numero = "101",
            complemento = null,
            bairro = "Sé",
            cidade = "São Paulo",
            estado = "SP"
        )

        assertEquals(5, enderecoAtualizado.idenderecos)
        assertEquals("Praça da Sé Alterada", enderecoAtualizado.endereco)

        val request = mockWebServer.takeRequest()
        assertEquals("PATCH", request.method)
        assertEquals("/clientes/10/enderecos/5", request.path)
    }

    @Test
    fun excluir_sucesso_fazDelete() = runTest {
        mockWebServer.enqueue(MockResponse().setResponseCode(204))

        repository.excluir(clienteId = 10, id = 5)

        val request = mockWebServer.takeRequest()
        assertEquals("DELETE", request.method)
        assertEquals("/clientes/10/enderecos/5", request.path)
        assertEquals("Bearer token123", request.getHeader("Authorization"))
    }
}
