package com.example.orcamentoapp

import com.example.orcamentoapp.data.api.AgendamentoApiService
import com.example.orcamentoapp.data.api.AuthInterceptor
import com.example.orcamentoapp.data.api.ClienteApiService
import com.example.orcamentoapp.data.api.OrcamentoApiService
import com.example.orcamentoapp.data.repositories.AgendamentoRepository
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

class AgendamentoRepositoryTest {

    private lateinit var mockWebServer: MockWebServer
    private lateinit var repository: AgendamentoRepository

    @Before
    fun setup() {
        mockWebServer = MockWebServer()
        mockWebServer.start()

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor { "token123" })
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(mockWebServer.url("/"))
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val agendamentoApi = retrofit.create(AgendamentoApiService::class.java)
        val orcamentoApi = retrofit.create(OrcamentoApiService::class.java)
        val clienteApi = retrofit.create(ClienteApiService::class.java)

        repository = AgendamentoRepository(agendamentoApi, orcamentoApi, clienteApi)
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    @Test
    fun carregarDados_sucesso_retornaAgendamentosOrcamentosEClientes() = runTest {
        val jsonAgendamentos = """
            [
                {
                    "idagendamentos": 1,
                    "data": "2023-12-01",
                    "hora": "14:00",
                    "orcamentos_id": 100,
                    "data_cadastro": "2023-01-01"
                }
            ]
        """.trimIndent()

        val jsonOrcamentos = """
            [
                {
                    "idorcamentos": 100,
                    "validade": "2023-12-31",
                    "status": true,
                    "clientes_id": 10,
                    "usuarios_id": 1,
                    "data_cadastro": "2023-01-01"
                }
            ]
        """.trimIndent()

        val jsonClientes = """
            [
                {
                    "idclientes": 10,
                    "nome": "João Silva",
                    "telefone": "11999998888",
                    "ativo": true,
                    "usuarios_id": 1,
                    "data_cadastro": "2023-01-01"
                }
            ]
        """.trimIndent()

        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(jsonAgendamentos))
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(jsonOrcamentos))
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(jsonClientes))

        val dados = repository.carregarDados()

        assertEquals(1, dados.agendamentos.size)
        assertEquals("2023-12-01", dados.agendamentos[0].data)
        assertEquals("14:00", dados.agendamentos[0].hora)
        assertEquals(1, dados.orcamentos.size)
        assertEquals(100, dados.orcamentos[0].idorcamentos)
        assertEquals(1, dados.clientes.size)
        assertEquals("João Silva", dados.clientes[0].nome)

        val req1 = mockWebServer.takeRequest()
        assertEquals("/agendamentos/", req1.path)
        assertEquals("Bearer token123", req1.getHeader("Authorization"))

        val req2 = mockWebServer.takeRequest()
        assertEquals("/orcamentos/", req2.path)

        val req3 = mockWebServer.takeRequest()
        assertEquals("/clientes/", req3.path)
    }

    @Test
    fun carregarDados_falhaHttp_lancaExcecao() = runTest {
        mockWebServer.enqueue(MockResponse().setResponseCode(500).setBody("Erro"))
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody("[]"))
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody("[]"))

        try {
            repository.carregarDados()
            fail("Deveria ter lançado uma exceção")
        } catch (e: Exception) {
            assertNotNull(e.message)
        }
    }

    @Test
    fun salvar_criarNovoAgendamento_fazPostESucesso() = runTest {
        val jsonResponse = """
            {
                "idagendamentos": 7,
                "data": "2023-12-10",
                "hora": "10:30",
                "orcamentos_id": 100,
                "data_cadastro": "2023-01-03"
            }
        """.trimIndent()

        mockWebServer.enqueue(MockResponse().setResponseCode(201).setBody(jsonResponse))

        val agendamentoSalvo = repository.salvar(
            id = null,
            data = "2023-12-10",
            hora = "10:30",
            orcamentoId = 100
        )

        assertEquals(7, agendamentoSalvo.idagendamentos)
        assertEquals("2023-12-10", agendamentoSalvo.data)
        assertEquals("10:30", agendamentoSalvo.hora)

        val request = mockWebServer.takeRequest()
        assertEquals("POST", request.method)
        assertEquals("/agendamentos/", request.path)
        assertEquals("Bearer token123", request.getHeader("Authorization"))
    }

    @Test
    fun salvar_atualizarAgendamento_fazPatchESucesso() = runTest {
        val jsonResponse = """
            {
                "idagendamentos": 7,
                "data": "2023-12-11",
                "hora": "11:00",
                "orcamentos_id": 100,
                "data_cadastro": "2023-01-03",
                "data_atualizacao": "2023-01-04"
            }
        """.trimIndent()

        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(jsonResponse))

        val agendamentoAtualizado = repository.salvar(
            id = 7,
            data = "2023-12-11",
            hora = "11:00",
            orcamentoId = 100
        )

        assertEquals(7, agendamentoAtualizado.idagendamentos)
        assertEquals("2023-12-11", agendamentoAtualizado.data)
        assertEquals("11:00", agendamentoAtualizado.hora)

        val request = mockWebServer.takeRequest()
        assertEquals("PATCH", request.method)
        assertEquals("/agendamentos/7", request.path)
    }

    @Test
    fun excluir_sucesso_fazDelete() = runTest {
        mockWebServer.enqueue(MockResponse().setResponseCode(204))

        repository.excluir(id = 7)

        val request = mockWebServer.takeRequest()
        assertEquals("DELETE", request.method)
        assertEquals("/agendamentos/7", request.path)
        assertEquals("Bearer token123", request.getHeader("Authorization"))
    }
}
