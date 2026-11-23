package com.example.orcamentoapp

import com.example.orcamentoapp.data.models.ClienteResponse
import com.example.orcamentoapp.data.repositories.ClienteRepository
import com.example.orcamentoapp.screens.clientes.ClientesViewModel
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
class ClientesViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var fakeRepository: FakeClienteRepository
    private lateinit var viewModel: ClientesViewModel

    @Before
    fun setup() {
        fakeRepository = FakeClienteRepository()
        viewModel = ClientesViewModel(fakeRepository)
    }

    @Test
    fun carregar_sucesso_atualizaStateComClientes() = runTest {
        fakeRepository.clientesInMemory.addAll(
            listOf(
                ClienteResponse(1, "João Silva", "11999998888", true, 10, "2023-01-01"),
                ClienteResponse(2, "Maria Souza", "11888887777", false, 10, "2023-01-02")
            )
        )

        viewModel.carregar()

        val state = viewModel.uiState.value
        assertFalse(state.carregando)
        assertNull(state.erro)
        assertEquals(2, state.clientes.size)
        assertEquals("João Silva", state.clientes[0].nome)
        assertEquals("11999998888", state.clientes[0].telefone)
        assertTrue(state.clientes[0].ativo)
        assertEquals("Maria Souza", state.clientes[1].nome)
        assertFalse(state.clientes[1].ativo)
    }

    @Test
    fun carregar_falhaRepositorio_defineErroEmState() = runTest {
        fakeRepository.shouldThrowError = true
        fakeRepository.errorMessage = "Falha de rede ao listar clientes"

        viewModel.carregar()

        val state = viewModel.uiState.value
        assertFalse(state.carregando)
        assertEquals("Falha de rede ao listar clientes", state.erro)
    }

    @Test
    fun salvar_novoCliente_sucesso_atualizaListaEMensagemEChamaCallback() = runTest {
        var callbackExecutado = false

        viewModel.salvar(
            id = null,
            nome = "Carlos Eduardo",
            telefone = "11777776666",
            ativo = true
        ) { clienteSalvo ->
            callbackExecutado = true
            assertEquals("Carlos Eduardo", clienteSalvo.nome)
        }

        val state = viewModel.uiState.value
        assertFalse(state.salvando)
        assertNull(state.erro)
        assertEquals("Cliente cadastrado com sucesso.", state.mensagem)
        assertEquals(1, state.clientes.size)
        assertEquals("Carlos Eduardo", state.clientes[0].nome)
        assertTrue(callbackExecutado)
    }

    @Test
    fun salvar_atualizarCliente_sucesso_mensagemAtualizada() = runTest {
        fakeRepository.clientesInMemory.add(
            ClienteResponse(1, "Carlos", "11777776666", true, 10, "2023-01-01")
        )

        viewModel.salvar(
            id = 1,
            nome = "Carlos Eduardo Silva",
            telefone = "11777776666",
            ativo = false
        )

        val state = viewModel.uiState.value
        assertFalse(state.salvando)
        assertNull(state.erro)
        assertEquals("Cliente atualizado com sucesso.", state.mensagem)
        assertEquals("Carlos Eduardo Silva", state.clientes[0].nome)
        assertFalse(state.clientes[0].ativo)
    }

    @Test
    fun salvar_falha_defineErroEmState() = runTest {
        fakeRepository.shouldThrowError = true
        fakeRepository.errorMessage = "Erro ao salvar dados do cliente"

        viewModel.salvar(id = null, nome = "Teste", telefone = "123", ativo = true)

        val state = viewModel.uiState.value
        assertFalse(state.salvando)
        assertEquals("Erro ao salvar dados do cliente", state.erro)
    }

    @Test
    fun excluir_sucesso_atualizaListaEMensagemEChamaCallback() = runTest {
        fakeRepository.clientesInMemory.add(
            ClienteResponse(5, "Cliente Para Excluir", "0000", true, 10, "2023-01-01")
        )
        var callbackExecutado = false

        viewModel.excluir(id = 5) {
            callbackExecutado = true
        }

        val state = viewModel.uiState.value
        assertFalse(state.salvando)
        assertNull(state.erro)
        assertEquals("Cliente excluído com sucesso.", state.mensagem)
        assertTrue(state.clientes.isEmpty())
        assertTrue(callbackExecutado)
    }

    @Test
    fun limparMensagem_removeMensagemDoState() = runTest {
        viewModel.salvar(id = null, nome = "Teste", telefone = "123", ativo = true)
        assertEquals("Cliente cadastrado com sucesso.", viewModel.uiState.value.mensagem)

        viewModel.limparMensagem()

        assertNull(viewModel.uiState.value.mensagem)
    }

    private class FakeClienteRepository : ClienteRepository() {
        val clientesInMemory = mutableListOf<ClienteResponse>()
        var shouldThrowError = false
        var errorMessage = "Erro no repositório de clientes"

        override suspend fun listar(): List<ClienteResponse> {
            if (shouldThrowError) throw Exception(errorMessage)
            return clientesInMemory.toList()
        }

        override suspend fun salvar(
            id: Int?,
            nome: String,
            telefone: String,
            ativo: Boolean
        ): ClienteResponse {
            if (shouldThrowError) throw Exception(errorMessage)
            val item = ClienteResponse(
                idclientes = id ?: (clientesInMemory.size + 1),
                nome = nome,
                telefone = telefone,
                ativo = ativo,
                usuarios_id = 10,
                data_cadastro = "2023-01-01"
            )
            if (id == null) {
                clientesInMemory.add(item)
            } else {
                val idx = clientesInMemory.indexOfFirst { it.idclientes == id }
                if (idx >= 0) clientesInMemory[idx] = item
            }
            return item
        }

        override suspend fun excluir(id: Int) {
            if (shouldThrowError) throw Exception(errorMessage)
            clientesInMemory.removeAll { it.idclientes == id }
        }
    }
}
