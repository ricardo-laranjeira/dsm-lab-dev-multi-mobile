package com.example.orcamentoapp

import com.example.orcamentoapp.data.models.CategoriaResponse
import com.example.orcamentoapp.data.models.ServicoResponse
import com.example.orcamentoapp.data.repositories.ServicoRepository
import com.example.orcamentoapp.screens.servicos.ServicosViewModel
import com.example.orcamentoapp.util.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ServicosViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var fakeRepository: FakeServicoRepository
    private lateinit var viewModel: ServicosViewModel

    @Before
    fun setup() {
        fakeRepository = FakeServicoRepository()
        viewModel = ServicosViewModel(fakeRepository)
    }

    @Test
    fun carregar_sucesso_atualizaStateComServicosECategorias() = runTest {
        fakeRepository.servicosInMemory.add(
            ServicoResponse(1, "Instalação Elétrica", "200.00", "2023-01-01", null, 10)
        )
        fakeRepository.categoriasInMemory.add(
            CategoriaResponse(10, "Elétrica", "2023-01-01")
        )

        viewModel.carregar()

        val state = viewModel.uiState.value
        assertFalse(state.carregando)
        assertNull(state.erro)
        assertEquals(1, state.servicos.size)
        assertEquals("Instalação Elétrica", state.servicos[0].descricao)
        assertEquals(1, state.categorias.size)
        assertEquals("Elétrica", state.categorias[0].descricao)
    }

    @Test
    fun carregar_falhaRepositorio_defineErroEmState() = runTest {
        fakeRepository.shouldThrowError = true
        fakeRepository.errorMessage = "Erro de conexão ao buscar serviços"

        viewModel.carregar()

        val state = viewModel.uiState.value
        assertFalse(state.carregando)
        assertEquals("Erro de conexão ao buscar serviços", state.erro)
    }

    @Test
    fun salvar_novoServico_sucesso_atualizaListaEMensagemEChamaCallback() = runTest {
        var callbackExecutado = false

        viewModel.salvar(
            id = null,
            descricao = "Pintura Parede",
            valor = "150.00",
            categoriaId = 20
        ) {
            callbackExecutado = true
        }

        val state = viewModel.uiState.value
        assertFalse(state.salvando)
        assertNull(state.erro)
        assertEquals("Serviço cadastrado com sucesso.", state.mensagem)
        assertEquals(1, state.servicos.size)
        assertEquals("Pintura Parede", state.servicos[0].descricao)
        assertTrue(callbackExecutado)
    }

    @Test
    fun salvar_atualizarServico_sucesso_mensagemAtualizada() = runTest {
        fakeRepository.servicosInMemory.add(
            ServicoResponse(1, "Pintura Parede", "150.00", "2023-01-01", null, 20)
        )

        viewModel.salvar(
            id = 1,
            descricao = "Pintura Parede e Teto",
            valor = "250.00",
            categoriaId = 20
        )

        val state = viewModel.uiState.value
        assertFalse(state.salvando)
        assertNull(state.erro)
        assertEquals("Serviço atualizado com sucesso.", state.mensagem)
        assertEquals("Pintura Parede e Teto", state.servicos[0].descricao)
    }

    @Test
    fun salvar_falha_defineErroEmState() = runTest {
        fakeRepository.shouldThrowError = true
        fakeRepository.errorMessage = "Erro ao salvar serviço"

        viewModel.salvar(null, "Teste", "100", 1)

        val state = viewModel.uiState.value
        assertFalse(state.salvando)
        assertEquals("Erro ao salvar serviço", state.erro)
    }

    @Test
    fun excluir_sucesso_atualizaListaEMensagemEChamaCallback() = runTest {
        fakeRepository.servicosInMemory.add(
            ServicoResponse(5, "Serviço Para Remover", "50.0", "2023-01-01", null, 10)
        )
        var callbackExecutado = false

        viewModel.excluir(id = 5) {
            callbackExecutado = true
        }

        val state = viewModel.uiState.value
        assertFalse(state.salvando)
        assertNull(state.erro)
        assertEquals("Serviço excluído com sucesso.", state.mensagem)
        assertTrue(state.servicos.isEmpty())
        assertTrue(callbackExecutado)
    }

    @Test
    fun limparMensagem_removeMensagemDoState() = runTest {
        viewModel.salvar(null, "Teste", "100", 1)
        assertEquals("Serviço cadastrado com sucesso.", viewModel.uiState.value.mensagem)

        viewModel.limparMensagem()

        assertNull(viewModel.uiState.value.mensagem)
    }

    private class FakeServicoRepository : ServicoRepository() {
        val servicosInMemory = mutableListOf<ServicoResponse>()
        val categoriasInMemory = mutableListOf<CategoriaResponse>()
        var shouldThrowError = false
        var errorMessage = "Erro no repositório de serviços"

        override suspend fun carregarDados(): DadosServicos {
            if (shouldThrowError) throw Exception(errorMessage)
            return DadosServicos(servicosInMemory.toList(), categoriasInMemory.toList())
        }

        override suspend fun salvar(
            id: Int?,
            descricao: String,
            valor: String,
            categoriaId: Int
        ): ServicoResponse {
            if (shouldThrowError) throw Exception(errorMessage)
            val item = ServicoResponse(
                idservicos = id ?: (servicosInMemory.size + 1),
                descricao = descricao,
                valor = valor,
                dataCadastro = "2023-01-01",
                categoriasId = categoriaId
            )
            if (id == null) {
                servicosInMemory.add(item)
            } else {
                val idx = servicosInMemory.indexOfFirst { it.idservicos == id }
                if (idx >= 0) servicosInMemory[idx] = item
            }
            return item
        }

        override suspend fun excluir(id: Int) {
            if (shouldThrowError) throw Exception(errorMessage)
            servicosInMemory.removeAll { it.idservicos == id }
        }
    }
}
