package com.example.orcamentoapp

import com.example.orcamentoapp.data.models.CategoriaResponse
import com.example.orcamentoapp.data.models.MaterialResponse
import com.example.orcamentoapp.data.repositories.MaterialRepository
import com.example.orcamentoapp.screens.materiais.MateriaisViewModel
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
class MateriaisViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var fakeRepository: FakeMaterialRepository
    private lateinit var viewModel: MateriaisViewModel

    @Before
    fun setup() {
        fakeRepository = FakeMaterialRepository()
        viewModel = MateriaisViewModel(fakeRepository)
    }

    @Test
    fun carregar_sucesso_atualizaStateComMateriaisECategorias() = runTest {
        fakeRepository.materiaisInMemory.add(
            MaterialResponse(1, "Cabo 2.5mm", "12.0", "m", "2023-01-01", null, 10)
        )
        fakeRepository.categoriasInMemory.add(
            CategoriaResponse(10, "Elétrica", "2023-01-01")
        )

        viewModel.carregar()

        val state = viewModel.uiState.value
        assertFalse(state.carregando)
        assertNull(state.erro)
        assertEquals(1, state.materiais.size)
        assertEquals("Cabo 2.5mm", state.materiais[0].descricao)
        assertEquals("m", state.materiais[0].unidadeMedida)
        assertEquals(1, state.categorias.size)
        assertEquals("Elétrica", state.categorias[0].descricao)
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
    fun salvar_novoMaterial_sucesso_comUnidadeMedida_atualizaListaEMensagemEChamaCallback() = runTest {
        var callbackExecutado = false

        viewModel.salvar(
            id = null,
            descricao = "Disjuntor 20A",
            valor = "35.00",
            unidadeMedida = "unidade",
            categoriaId = 10
        ) {
            callbackExecutado = true
        }

        val state = viewModel.uiState.value
        assertFalse(state.salvando)
        assertNull(state.erro)
        assertEquals("Material cadastrado com sucesso.", state.mensagem)
        assertEquals(1, state.materiais.size)
        assertEquals("Disjuntor 20A", state.materiais[0].descricao)
        assertEquals("unidade", state.materiais[0].unidadeMedida)
        assertTrue(callbackExecutado)
    }

    @Test
    fun salvar_atualizarMaterial_sucesso_mensagemAtualizada() = runTest {
        fakeRepository.materiaisInMemory.add(
            MaterialResponse(1, "Disjuntor 10A", "30.00", "unidade", "2023-01-01", null, 10)
        )

        viewModel.salvar(
            id = 1,
            descricao = "Disjuntor 20A",
            valor = "35.00",
            unidadeMedida = "unidade",
            categoriaId = 10
        )

        val state = viewModel.uiState.value
        assertFalse(state.salvando)
        assertNull(state.erro)
        assertEquals("Material atualizado com sucesso.", state.mensagem)
        assertEquals("Disjuntor 20A", state.materiais[0].descricao)
    }

    @Test
    fun salvar_falha_defineErroEmState() = runTest {
        fakeRepository.shouldThrowError = true
        fakeRepository.errorMessage = "Erro ao salvar material"

        viewModel.salvar(null, "Teste", "10", "un", 1)

        val state = viewModel.uiState.value
        assertFalse(state.salvando)
        assertEquals("Erro ao salvar material", state.erro)
    }

    @Test
    fun excluir_sucesso_atualizaListaEMensagemEChamaCallback() = runTest {
        fakeRepository.materiaisInMemory.add(
            MaterialResponse(5, "Material Para Remover", "10.0", "m", "2023-01-01", null, 10)
        )
        var callbackExecutado = false

        viewModel.excluir(id = 5) {
            callbackExecutado = true
        }

        val state = viewModel.uiState.value
        assertFalse(state.salvando)
        assertNull(state.erro)
        assertEquals("Material excluído com sucesso.", state.mensagem)
        assertTrue(state.materiais.isEmpty())
        assertTrue(callbackExecutado)
    }

    @Test
    fun limparMensagem_removeMensagemDoState() = runTest {
        viewModel.salvar(null, "Teste", "10", "un", 1)
        assertEquals("Material cadastrado com sucesso.", viewModel.uiState.value.mensagem)

        viewModel.limparMensagem()

        assertNull(viewModel.uiState.value.mensagem)
    }

    private class FakeMaterialRepository : MaterialRepository() {
        val materiaisInMemory = mutableListOf<MaterialResponse>()
        val categoriasInMemory = mutableListOf<CategoriaResponse>()
        var shouldThrowError = false
        var errorMessage = "Erro no repositório de materiais"

        override suspend fun carregarDados(): DadosMateriais {
            if (shouldThrowError) throw Exception(errorMessage)
            return DadosMateriais(materiaisInMemory.toList(), categoriasInMemory.toList())
        }

        override suspend fun salvar(
            id: Int?,
            descricao: String,
            valor: String,
            unidadeMedida: String,
            categoriaId: Int
        ): MaterialResponse {
            if (shouldThrowError) throw Exception(errorMessage)
            val item = MaterialResponse(
                idmateriais = id ?: (materiaisInMemory.size + 1),
                descricao = descricao,
                valor = valor,
                unidadeMedida = unidadeMedida,
                dataCadastro = "2023-01-01",
                categoriasId = categoriaId
            )
            if (id == null) {
                materiaisInMemory.add(item)
            } else {
                val idx = materiaisInMemory.indexOfFirst { it.idmateriais == id }
                if (idx >= 0) materiaisInMemory[idx] = item
            }
            return item
        }

        override suspend fun excluir(id: Int) {
            if (shouldThrowError) throw Exception(errorMessage)
            materiaisInMemory.removeAll { it.idmateriais == id }
        }
    }
}
