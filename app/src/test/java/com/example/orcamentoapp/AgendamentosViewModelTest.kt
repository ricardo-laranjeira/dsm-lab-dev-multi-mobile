package com.example.orcamentoapp

import com.example.orcamentoapp.data.models.AgendamentoResponse
import com.example.orcamentoapp.data.models.ClienteResponse
import com.example.orcamentoapp.data.models.OrcamentoResponse
import com.example.orcamentoapp.data.repositories.AgendamentoRepository
import com.example.orcamentoapp.screens.agendamentos.AgendamentosViewModel
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
class AgendamentosViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var fakeRepository: FakeAgendamentoRepository
    private lateinit var viewModel: AgendamentosViewModel

    @Before
    fun setup() {
        fakeRepository = FakeAgendamentoRepository()
        viewModel = AgendamentosViewModel(fakeRepository)
    }

    @Test
    fun carregar_sucesso_atualizaStateComAgendamentosOrcamentosEClientes() = runTest {
        fakeRepository.agendamentosInMemory.add(
            AgendamentoResponse(1, "2023-12-01", "14:00", 100, "2023-01-01")
        )
        fakeRepository.orcamentosInMemory.add(
            OrcamentoResponse(
                idorcamentos = 100,
                validade = "2023-12-31",
                status = true,
                dataCadastro = "2023-01-01",
                dataAtualizacao = null,
                usuariosId = 1,
                clientesId = 10
            )
        )
        fakeRepository.clientesInMemory.add(
            ClienteResponse(10, "João Silva", "11999998888", true, 1, "2023-01-01")
        )

        viewModel.carregar()

        val state = viewModel.uiState.value
        assertFalse(state.carregando)
        assertNull(state.erro)
        assertEquals(1, state.agendamentos.size)
        assertEquals("2023-12-01", state.agendamentos[0].data)
        assertEquals(1, state.orcamentos.size)
        assertEquals(100, state.orcamentos[0].idorcamentos)
        assertEquals(1, state.clientes.size)
        assertEquals("João Silva", state.clientes[0].nome)
    }

    @Test
    fun carregar_falhaRepositorio_defineErroEmState() = runTest {
        fakeRepository.shouldThrowError = true
        fakeRepository.errorMessage = "Erro ao carregar agendamentos"

        viewModel.carregar()

        val state = viewModel.uiState.value
        assertFalse(state.carregando)
        assertEquals("Erro ao carregar agendamentos", state.erro)
    }

    @Test
    fun salvar_novoAgendamento_sucesso_atualizaListaEMensagemEChamaCallback() = runTest {
        var callbackExecutado = false

        viewModel.salvar(
            id = null,
            data = "2023-12-05",
            hora = "09:00",
            orcamentoId = 100
        ) {
            callbackExecutado = true
        }

        val state = viewModel.uiState.value
        assertFalse(state.salvando)
        assertNull(state.erro)
        assertEquals("Agendamento cadastrado com sucesso.", state.mensagem)
        assertEquals(1, state.agendamentos.size)
        assertEquals("2023-12-05", state.agendamentos[0].data)
        assertTrue(callbackExecutado)
    }

    @Test
    fun salvar_atualizarAgendamento_sucesso_mensagemAtualizada() = runTest {
        fakeRepository.agendamentosInMemory.add(
            AgendamentoResponse(1, "2023-12-05", "09:00", 100, "2023-01-01")
        )

        viewModel.salvar(
            id = 1,
            data = "2023-12-06",
            hora = "10:00",
            orcamentoId = 100
        )

        val state = viewModel.uiState.value
        assertFalse(state.salvando)
        assertNull(state.erro)
        assertEquals("Agendamento atualizado com sucesso.", state.mensagem)
        assertEquals("2023-12-06", state.agendamentos[0].data)
        assertEquals("10:00", state.agendamentos[0].hora)
    }

    @Test
    fun salvar_falha_defineErroEmState() = runTest {
        fakeRepository.shouldThrowError = true
        fakeRepository.errorMessage = "Falha ao salvar agendamento"

        viewModel.salvar(null, "2023-12-01", "10:00", 100)

        val state = viewModel.uiState.value
        assertFalse(state.salvando)
        assertEquals("Falha ao salvar agendamento", state.erro)
    }

    @Test
    fun excluir_sucesso_atualizaListaEMensagemEChamaCallback() = runTest {
        fakeRepository.agendamentosInMemory.add(
            AgendamentoResponse(7, "2023-12-01", "10:00", 100, "2023-01-01")
        )
        var callbackExecutado = false

        viewModel.excluir(id = 7) {
            callbackExecutado = true
        }

        val state = viewModel.uiState.value
        assertFalse(state.salvando)
        assertNull(state.erro)
        assertEquals("Agendamento excluído com sucesso.", state.mensagem)
        assertTrue(state.agendamentos.isEmpty())
        assertTrue(callbackExecutado)
    }

    @Test
    fun limparMensagem_removeMensagemDoState() = runTest {
        viewModel.salvar(null, "2023-12-01", "10:00", 100)
        assertEquals("Agendamento cadastrado com sucesso.", viewModel.uiState.value.mensagem)

        viewModel.limparMensagem()

        assertNull(viewModel.uiState.value.mensagem)
    }

    private class FakeAgendamentoRepository : AgendamentoRepository() {
        val agendamentosInMemory = mutableListOf<AgendamentoResponse>()
        val orcamentosInMemory = mutableListOf<OrcamentoResponse>()
        val clientesInMemory = mutableListOf<ClienteResponse>()
        var shouldThrowError = false
        var errorMessage = "Erro no repositório de agendamentos"

        override suspend fun carregarDados(): DadosAgendamentos {
            if (shouldThrowError) throw Exception(errorMessage)
            return DadosAgendamentos(
                agendamentosInMemory.toList(),
                orcamentosInMemory.toList(),
                clientesInMemory.toList()
            )
        }

        override suspend fun salvar(
            id: Int?,
            data: String,
            hora: String,
            orcamentoId: Int
        ): AgendamentoResponse {
            if (shouldThrowError) throw Exception(errorMessage)
            val item = AgendamentoResponse(
                idagendamentos = id ?: (agendamentosInMemory.size + 1),
                data = data,
                hora = hora,
                orcamentosId = orcamentoId,
                dataCadastro = "2023-01-01"
            )
            if (id == null) {
                agendamentosInMemory.add(item)
            } else {
                val idx = agendamentosInMemory.indexOfFirst { it.idagendamentos == id }
                if (idx >= 0) agendamentosInMemory[idx] = item
            }
            return item
        }

        override suspend fun excluir(id: Int) {
            if (shouldThrowError) throw Exception(errorMessage)
            agendamentosInMemory.removeAll { it.idagendamentos == id }
        }
    }
}
