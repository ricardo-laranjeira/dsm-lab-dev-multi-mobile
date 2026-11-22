package com.example.orcamentoapp

import com.example.orcamentoapp.data.api.AuthInterceptor
import com.example.orcamentoapp.data.api.CategoriaApiService
import com.example.orcamentoapp.data.api.MaterialApiService
import com.example.orcamentoapp.data.repositories.MaterialRepository
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

class MaterialRepositoryTest {

    private lateinit var mockWebServer: MockWebServer
    private lateinit var repository: MaterialRepository

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

        val materialApi = retrofit.create(MaterialApiService::class.java)
        val categoriaApi = retrofit.create(CategoriaApiService::class.java)

        repository = MaterialRepository(materialApi, categoriaApi)
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    @Test
    fun carregarDados_sucesso_retornaMateriaisECategorias() = runTest {
        val jsonMateriais = """
            [
                {
                    "idmateriais": 1,
                    "descricao": "Cabo Flexível 2.5mm",
                    "valor": "15.50",
                    "unidade_medida": "metro",
                    "data_cadastro": "2023-01-01",
                    "categorias_id": 10
                }
            ]
        """.trimIndent()

        val jsonCategorias = """
            [
                {
                    "idcategorias": 10,
                    "descricao": "Elétrica",
                    "data_cadastro": "2023-01-01"
                }
            ]
        """.trimIndent()

        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(jsonMateriais))
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(jsonCategorias))

        val dados = repository.carregarDados()

        assertEquals(1, dados.materiais.size)
        assertEquals("Cabo Flexível 2.5mm", dados.materiais[0].descricao)
        assertEquals("metro", dados.materiais[0].unidadeMedida)
        assertEquals(1, dados.categorias.size)
        assertEquals("Elétrica", dados.categorias[0].descricao)

        val req1 = mockWebServer.takeRequest()
        assertEquals("/materiais/", req1.path)
        assertEquals("Bearer token123", req1.getHeader("Authorization"))

        val req2 = mockWebServer.takeRequest()
        assertEquals("/categorias/", req2.path)
        assertEquals("Bearer token123", req2.getHeader("Authorization"))
    }

    @Test
    fun carregarDados_falhaHttp_lancaExcecao() = runTest {
        mockWebServer.enqueue(MockResponse().setResponseCode(500).setBody("Erro no servidor"))
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody("[]"))

        try {
            repository.carregarDados()
            fail("Deveria ter lançado uma exceção")
        } catch (e: Exception) {
            assertNotNull(e.message)
        }
    }

    @Test
    fun salvar_criarNovoMaterial_comUnidadeMedida_fazPostESucesso() = runTest {
        val jsonResponse = """
            {
                "idmateriais": 5,
                "descricao": "Tubo PVC 3/4",
                "valor": "25.00",
                "unidade_medida": "unidade",
                "data_cadastro": "2023-01-03",
                "categorias_id": 20
            }
        """.trimIndent()

        mockWebServer.enqueue(MockResponse().setResponseCode(201).setBody(jsonResponse))

        val materialSalvo = repository.salvar(
            id = null,
            descricao = "Tubo PVC 3/4",
            valor = "25.00",
            unidadeMedida = "unidade",
            categoriaId = 20
        )

        assertEquals(5, materialSalvo.idmateriais)
        assertEquals("Tubo PVC 3/4", materialSalvo.descricao)
        assertEquals("unidade", materialSalvo.unidadeMedida)

        val request = mockWebServer.takeRequest()
        assertEquals("POST", request.method)
        assertEquals("/materiais/", request.path)
        assertEquals("Bearer token123", request.getHeader("Authorization"))
    }

    @Test
    fun salvar_atualizarMaterial_fazPatchESucesso() = runTest {
        val jsonResponse = """
            {
                "idmateriais": 5,
                "descricao": "Tubo PVC 1 pol",
                "valor": "30.00",
                "unidade_medida": "unidade",
                "data_cadastro": "2023-01-03",
                "data_atualizacao": "2023-01-04",
                "categorias_id": 20
            }
        """.trimIndent()

        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(jsonResponse))

        val materialAtualizado = repository.salvar(
            id = 5,
            descricao = "Tubo PVC 1 pol",
            valor = "30.00",
            unidadeMedida = "unidade",
            categoriaId = 20
        )

        assertEquals(5, materialAtualizado.idmateriais)
        assertEquals("Tubo PVC 1 pol", materialAtualizado.descricao)

        val request = mockWebServer.takeRequest()
        assertEquals("PATCH", request.method)
        assertEquals("/materiais/5", request.path)
    }

    @Test
    fun excluir_sucesso_fazDelete() = runTest {
        mockWebServer.enqueue(MockResponse().setResponseCode(204))

        repository.excluir(id = 5)

        val request = mockWebServer.takeRequest()
        assertEquals("DELETE", request.method)
        assertEquals("/materiais/5", request.path)
        assertEquals("Bearer token123", request.getHeader("Authorization"))
    }
}
