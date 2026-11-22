package com.example.orcamentoapp

import com.example.orcamentoapp.data.models.CategoriaResponse
import com.example.orcamentoapp.data.repositories.CategoriaRepository
import com.example.orcamentoapp.screens.categorias.CategoriasViewModel
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
class CategoriasViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var fakeRepository: FakeCategoriaRepository
    private lateinit var viewModel: CategoriasViewModel

    @Before
    fun setup() {
        fakeRepository = FakeCategoriaRepository()
        viewModel = CategoriasViewModel(fakeRepository)
    }

    @Test
    fun carregar_sucesso_atualizaStateComCategorias() = runTest {
        fakeRepository.categoriasInMemory.addAll(
            listOf(
                CategoriaResponse(1, "Elétrica", "2023-01-01"),
                CategoriaResponse(2, "Hidráulica", "2023-01-02")
            )
        )

        viewModel.carregar()

        val state = viewModel.uiState.value
        assertFalse(state.carregando)
        assertNull(state.erro)
        assertEquals(2, state.categorias.size)
        assertEquals("Elétrica", state.categorias[0].nome)
        assertEquals("Hidráulica", state.categorias[1].nome)
    }

    @Test
    fun carregar_falhaRepositorio_defineErroEmState() = runTest {
        fakeRepository.shouldThrowError = true
        fakeRepository.errorMessage = "Erro de conexão com o servidor"

        viewModel.carregar()

        val state = viewModel.uiState.value
        assertFalse(state.carregando)
        assertEquals("Erro de conexão com o servidor", state.erro)
    }

    @Test
    fun salvar_novaCategoria_sucesso_atualizaListaEMensagemEChamaCallback() = runTest {
        var callbackExecutado = false

        viewModel.salvar(id = null, descricao = "Pintura") {
            callbackExecutado = true
        }

        val state = viewModel.uiState.value
        assertFalse(state.salvando)
        assertNull(state.erro)
        assertEquals("Categoria salva com sucesso.", state.mensagem)
        assertEquals(1, state.categorias.size)
        assertEquals("Pintura", state.categorias[0].nome)
        assertTrue(callbackExecutado)
    }

    @Test
    fun salvar_atualizarCategoria_sucesso_mensagemAtualizada() = runTest {
        fakeRepository.categoriasInMemory.add(CategoriaResponse(1, "Antiga", "2023-01-01"))

        viewModel.salvar(id = 1, descricao = "Nova Descricao")

        val state = viewModel.uiState.value
        assertFalse(state.salvando)
        assertNull(state.erro)
        assertEquals("Categoria atualizada.", state.mensagem)
        assertEquals("Nova Descricao", state.categorias[0].nome)
    }

    @Test
    fun salvar_falha_defineErroEmState() = runTest {
        fakeRepository.shouldThrowError = true
        fakeRepository.errorMessage = "Falha ao salvar"

        viewModel.salvar(id = null, descricao = "Teste")

        val state = viewModel.uiState.value
        assertFalse(state.salvando)
        assertEquals("Falha ao salvar", state.erro)
    }

    @Test
    fun excluir_sucesso_atualizaListaEMensagemEChamaCallback() = runTest {
        fakeRepository.categoriasInMemory.add(CategoriaResponse(10, "Remover", "2023-01-01"))
        var callbackExecutado = false

        viewModel.excluir(id = 10) {
            callbackExecutado = true
        }

        val state = viewModel.uiState.value
        assertFalse(state.salvando)
        assertNull(state.erro)
        assertEquals("Categoria excluída com sucesso.", state.mensagem)
        assertTrue(state.categorias.isEmpty())
        assertTrue(callbackExecutado)
    }

    @Test
    fun limparMensagem_removeMensagemDoState() = runTest {
        viewModel.salvar(id = null, descricao = "Teste")
        assertEquals("Categoria salva com sucesso.", viewModel.uiState.value.mensagem)

        viewModel.limparMensagem()

        assertNull(viewModel.uiState.value.mensagem)
    }

    private class FakeCategoriaRepository : CategoriaRepository() {
        val categoriasInMemory = mutableListOf<CategoriaResponse>()
        var shouldThrowError = false
        var errorMessage = "Erro no repositório"

        override suspend fun listar(): List<CategoriaResponse> {
            if (shouldThrowError) throw Exception(errorMessage)
            return categoriasInMemory.toList()
        }

        override suspend fun salvar(id: Int?, descricao: String): CategoriaResponse {
            if (shouldThrowError) throw Exception(errorMessage)
            val item = CategoriaResponse(
                idcategorias = id ?: (categoriasInMemory.size + 1),
                descricao = descricao,
                dataCadastro = "2023-01-01"
            )
            if (id == null) {
                categoriasInMemory.add(item)
            } else {
                val idx = categoriasInMemory.indexOfFirst { it.idcategorias == id }
                if (idx >= 0) categoriasInMemory[idx] = item
            }
            return item
        }

        override suspend fun excluir(id: Int) {
            if (shouldThrowError) throw Exception(errorMessage)
            categoriasInMemory.removeAll { it.idcategorias == id }
        }
    }
}
