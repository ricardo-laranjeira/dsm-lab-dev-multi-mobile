package com.example.orcamentoapp

import com.example.orcamentoapp.data.repositories.OrcamentoRepository
import com.example.orcamentoapp.screens.orcamentos.ClienteOrcamentoUi
import com.example.orcamentoapp.screens.orcamentos.ItemOrcamentoUi
import com.example.orcamentoapp.screens.orcamentos.OrcamentoUi
import com.example.orcamentoapp.screens.orcamentos.OrcamentosViewModel
import com.example.orcamentoapp.screens.orcamentos.ProdutoOrcamentoUi
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
class OrcamentosViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var fakeRepository: FakeOrcamentoRepository
    private lateinit var viewModel: OrcamentosViewModel

    @Before
    fun setup() {
        fakeRepository = FakeOrcamentoRepository()
        viewModel = OrcamentosViewModel(fakeRepository)
    }

    @Test
    fun carregarDados_sucesso_atualizaStateComDadosCompletos() = runTest {
        fakeRepository.clientesInMemory.add(ClienteOrcamentoUi(1, "Cliente A"))
        fakeRepository.materiaisInMemory.add(ProdutoOrcamentoUi(10, "Cabo 10mm", 15.0))
        fakeRepository.servicosInMemory.add(ProdutoOrcamentoUi(20, "Instalação", 100.0))
        fakeRepository.orcamentosInMemory.add(
            OrcamentoUi(
                id = 100,
                clienteId = 1,
                clienteNome = "Cliente A",
                validade = "2023-12-31",
                status = "Aprovado",
                itens = listOf(
                    ItemOrcamentoUi(1, "Material", 10, "Cabo 10mm", 2, 15.0)
                )
            )
        )

        viewModel.carregarDados()

        val state = viewModel.uiState.value
        assertFalse(state.carregando)
        assertNull(state.erro)
        assertEquals(1, state.clientes.size)
        assertEquals(1, state.materiais.size)
        assertEquals(1, state.servicos.size)
        assertEquals(1, state.orcamentos.size)
        assertEquals("Cliente A", state.orcamentos[0].clienteNome)
        assertEquals(30.0, state.orcamentos[0].total, 0.01)
    }

    @Test
    fun carregarDados_falha_defineErroEmState() = runTest {
        fakeRepository.shouldThrowError = true
        fakeRepository.errorMessage = "Erro de conexão ao carregar orçamentos"

        viewModel.carregarDados()

        val state = viewModel.uiState.value
        assertFalse(state.carregando)
        assertEquals("Erro de conexão ao carregar orçamentos", state.erro)
    }

    @Test
    fun salvarOrcamento_novoOrcamento_sucesso_atualizaStateEMensagemEChamaCallback() = runTest {
        var callbackExecutado = false
        val novoOrcamento = OrcamentoUi(
            id = 0,
            clienteId = 1,
            clienteNome = "Cliente A",
            validade = "2024-01-01",
            status = "Não aprovado",
            itens = emptyList()
        )

        viewModel.salvarOrcamento(atual = null, novo = novoOrcamento) {
            callbackExecutado = true
        }

        val state = viewModel.uiState.value
        assertFalse(state.salvando)
        assertNull(state.erro)
        assertEquals("Orçamento salvo com sucesso.", state.mensagem)
        assertEquals(1, state.orcamentos.size)
        assertTrue(callbackExecutado)
    }

    @Test
    fun salvarOrcamento_atualizarOrcamento_sucesso() = runTest {
        val existente = OrcamentoUi(1, 1, "Cliente A", "2023-12-31", "Não aprovado", emptyList())
        fakeRepository.orcamentosInMemory.add(existente)

        val alterado = existente.copy(status = "Aprovado")

        viewModel.salvarOrcamento(atual = existente, novo = alterado)

        val state = viewModel.uiState.value
        assertFalse(state.salvando)
        assertNull(state.erro)
        assertEquals("Orçamento salvo com sucesso.", state.mensagem)
        assertEquals("Aprovado", state.orcamentos[0].status)
    }

    @Test
    fun salvarOrcamento_falha_defineMensagemDeErroComInstrucoes() = runTest {
        fakeRepository.shouldThrowError = true
        fakeRepository.errorMessage = "Timeout na API"

        val orcamento = OrcamentoUi(0, 1, "Cliente A", "2024-01-01", "Aprovado", emptyList())

        viewModel.salvarOrcamento(atual = null, novo = orcamento)

        val state = viewModel.uiState.value
        assertFalse(state.salvando)
        assertTrue(state.erro?.contains("Timeout na API") == true)
        assertTrue(state.erro?.contains("Se o orçamento já foi criado, atualize a lista") == true)
    }

    @Test
    fun excluirOrcamento_sucesso_removeOrcamentoEMostraMensagem() = runTest {
        val orcamento = OrcamentoUi(10, 1, "Cliente A", "2023-12-31", "Aprovado", emptyList())
        fakeRepository.orcamentosInMemory.add(orcamento)
        var callbackExecutado = false

        viewModel.excluirOrcamento(id = 10) {
            callbackExecutado = true
        }

        val state = viewModel.uiState.value
        assertFalse(state.salvando)
        assertNull(state.erro)
        assertEquals("Orçamento excluído.", state.mensagem)
        assertTrue(state.orcamentos.isEmpty())
        assertTrue(callbackExecutado)
    }

    @Test
    fun limparMensagem_resetaMensagemParaNull() = runTest {
        val novoOrcamento = OrcamentoUi(0, 1, "Cliente A", "2024-01-01", "Aprovado", emptyList())
        viewModel.salvarOrcamento(atual = null, novo = novoOrcamento)
        assertEquals("Orçamento salvo com sucesso.", viewModel.uiState.value.mensagem)

        viewModel.limparMensagem()

        assertNull(viewModel.uiState.value.mensagem)
    }

    private class FakeOrcamentoRepository : OrcamentoRepository() {
        val orcamentosInMemory = mutableListOf<OrcamentoUi>()
        val clientesInMemory = mutableListOf<ClienteOrcamentoUi>()
        val materiaisInMemory = mutableListOf<ProdutoOrcamentoUi>()
        val servicosInMemory = mutableListOf<ProdutoOrcamentoUi>()

        var shouldThrowError = false
        var errorMessage = "Erro no repositório de orçamentos"

        override suspend fun carregar(): DadosOrcamentos {
            if (shouldThrowError) throw Exception(errorMessage)
            return DadosOrcamentos(
                orcamentos = orcamentosInMemory.toList(),
                clientes = clientesInMemory.toList(),
                materiais = materiaisInMemory.toList(),
                servicos = servicosInMemory.toList()
            )
        }

        override suspend fun salvar(atual: OrcamentoUi?, novo: OrcamentoUi) {
            if (shouldThrowError) throw Exception(errorMessage)
            if (atual == null) {
                val orcamentoSalvo = novo.copy(id = orcamentosInMemory.size + 1)
                orcamentosInMemory.add(orcamentoSalvo)
            } else {
                val idx = orcamentosInMemory.indexOfFirst { it.id == atual.id }
                if (idx >= 0) {
                    orcamentosInMemory[idx] = novo
                }
            }
        }

        override suspend fun excluir(idOrcamento: Int) {
            if (shouldThrowError) throw Exception(errorMessage)
            orcamentosInMemory.removeAll { it.id == idOrcamento }
        }
    }
}
