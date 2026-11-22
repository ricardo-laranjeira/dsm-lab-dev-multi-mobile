package com.example.orcamentoapp

import com.example.orcamentoapp.data.models.EnderecoResponse
import com.example.orcamentoapp.data.repositories.EnderecoRepository
import com.example.orcamentoapp.screens.enderecos.EnderecosViewModel
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
class EnderecosViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var fakeRepository: FakeEnderecoRepository
    private lateinit var viewModel: EnderecosViewModel

    @Before
    fun setup() {
        fakeRepository = FakeEnderecoRepository()
        viewModel = EnderecosViewModel(fakeRepository)
    }

    @Test
    fun carregar_sucesso_atualizaStateComEnderecos() = runTest {
        fakeRepository.enderecosInMemory.addAll(
            listOf(
                EnderecoResponse(1, 10, "01001-000", "Rua A", "10", "Apto 1", "Bairro A", "Cidade A", "SP", "2023-01-01"),
                EnderecoResponse(2, 10, "02002-000", "Rua B", "20", null, "Bairro B", "Cidade B", "SP", "2023-01-02")
            )
        )

        viewModel.carregar(clienteId = 10)

        val state = viewModel.uiState.value
        assertFalse(state.carregando)
        assertNull(state.erro)
        assertEquals(2, state.enderecos.size)
        assertEquals("Rua A", state.enderecos[0].endereco)
        assertEquals("Apto 1", state.enderecos[0].complemento)
        assertEquals("Rua B", state.enderecos[1].endereco)
        assertEquals("", state.enderecos[1].complemento)
    }

    @Test
    fun carregar_listaVazia_atualizaStateComListaVazia() = runTest {
        viewModel.carregar(clienteId = 10)

        val state = viewModel.uiState.value
        assertFalse(state.carregando)
        assertNull(state.erro)
        assertTrue(state.enderecos.isEmpty())
    }

    @Test
    fun carregar_falhaRepositorio_defineErroEmState() = runTest {
        fakeRepository.shouldThrowError = true
        fakeRepository.errorMessage = "Erro ao buscar endereços"

        viewModel.carregar(clienteId = 10)

        val state = viewModel.uiState.value
        assertFalse(state.carregando)
        assertEquals("Erro ao buscar endereços", state.erro)
    }

    @Test
    fun salvar_novoEndereco_sucesso_atualizaListaEMensagemEChamaCallback() = runTest {
        var callbackExecutado = false

        viewModel.salvar(
            clienteId = 10,
            id = null,
            cep = "01001-000",
            endereco = "Rua Nova",
            numero = "100",
            complemento = "Casa",
            bairro = "Centro",
            cidade = "São Paulo",
            estado = "SP"
        ) {
            callbackExecutado = true
        }

        val state = viewModel.uiState.value
        assertFalse(state.salvando)
        assertNull(state.erro)
        assertEquals("Endereço cadastrado com sucesso.", state.mensagem)
        assertEquals(1, state.enderecos.size)
        assertEquals("Rua Nova", state.enderecos[0].endereco)
        assertTrue(callbackExecutado)
    }

    @Test
    fun salvar_atualizarEndereco_sucesso_mensagemAtualizada() = runTest {
        fakeRepository.enderecosInMemory.add(
            EnderecoResponse(1, 10, "01001-000", "Rua Antiga", "10", null, "Bairro", "Cidade", "SP", "2023-01-01")
        )

        viewModel.salvar(
            clienteId = 10,
            id = 1,
            cep = "01001-000",
            endereco = "Rua Atualizada",
            numero = "10",
            complemento = "",
            bairro = "Bairro",
            cidade = "Cidade",
            estado = "SP"
        )

        val state = viewModel.uiState.value
        assertFalse(state.salvando)
        assertNull(state.erro)
        assertEquals("Endereço atualizado com sucesso.", state.mensagem)
        assertEquals("Rua Atualizada", state.enderecos[0].endereco)
    }

    @Test
    fun salvar_falha_defineErroEmState() = runTest {
        fakeRepository.shouldThrowError = true
        fakeRepository.errorMessage = "Falha ao salvar endereço"

        viewModel.salvar(10, null, "00000-000", "Rua", "1", "", "B", "C", "SP")

        val state = viewModel.uiState.value
        assertFalse(state.salvando)
        assertEquals("Falha ao salvar endereço", state.erro)
    }

    @Test
    fun excluir_sucesso_atualizaListaEMensagemEChamaCallback() = runTest {
        fakeRepository.enderecosInMemory.add(
            EnderecoResponse(5, 10, "01001-000", "Rua Para Remover", "10", null, "Bairro", "Cidade", "SP", "2023-01-01")
        )
        var callbackExecutado = false

        viewModel.excluir(clienteId = 10, id = 5) {
            callbackExecutado = true
        }

        val state = viewModel.uiState.value
        assertFalse(state.salvando)
        assertNull(state.erro)
        assertEquals("Endereço excluído com sucesso.", state.mensagem)
        assertTrue(state.enderecos.isEmpty())
        assertTrue(callbackExecutado)
    }

    @Test
    fun limparMensagem_removeMensagemDoState() = runTest {
        viewModel.salvar(10, null, "00000-000", "Rua", "1", "", "B", "C", "SP")
        assertEquals("Endereço cadastrado com sucesso.", viewModel.uiState.value.mensagem)

        viewModel.limparMensagem()

        assertNull(viewModel.uiState.value.mensagem)
    }

    private class FakeEnderecoRepository : EnderecoRepository() {
        val enderecosInMemory = mutableListOf<EnderecoResponse>()
        var shouldThrowError = false
        var errorMessage = "Erro no repositório de endereços"

        override suspend fun listar(clienteId: Int): List<EnderecoResponse> {
            if (shouldThrowError) throw Exception(errorMessage)
            return enderecosInMemory.filter { it.clienteId == clienteId }
        }

        override suspend fun salvar(
            clienteId: Int,
            id: Int?,
            cep: String,
            endereco: String,
            numero: String,
            complemento: String?,
            bairro: String,
            cidade: String,
            estado: String
        ): EnderecoResponse {
            if (shouldThrowError) throw Exception(errorMessage)
            val item = EnderecoResponse(
                idenderecos = id ?: (enderecosInMemory.size + 1),
                clienteId = clienteId,
                cep = cep,
                endereco = endereco,
                numero = numero,
                complemento = complemento,
                bairro = bairro,
                cidade = cidade,
                estado = estado,
                dataCadastro = "2023-01-01"
            )
            if (id == null) {
                enderecosInMemory.add(item)
            } else {
                val idx = enderecosInMemory.indexOfFirst { it.idenderecos == id }
                if (idx >= 0) enderecosInMemory[idx] = item
            }
            return item
        }

        override suspend fun excluir(clienteId: Int, id: Int) {
            if (shouldThrowError) throw Exception(errorMessage)
            enderecosInMemory.removeAll { it.idenderecos == id && it.clienteId == clienteId }
        }
    }
}
