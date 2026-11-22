package com.example.orcamentoapp.screens.materiais

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.orcamentoapp.data.models.CategoriaResponse
import com.example.orcamentoapp.data.models.MaterialResponse
import com.example.orcamentoapp.data.repositories.MaterialRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal data class MateriaisUiState(
    val materiais: List<MaterialResponse> = emptyList(),
    val categorias: List<CategoriaResponse> = emptyList(),
    val carregando: Boolean = false,
    val salvando: Boolean = false,
    val erro: String? = null,
    val mensagem: String? = null
)

internal class MateriaisViewModel(
    private val repository: MaterialRepository = MaterialRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(MateriaisUiState())
    val uiState: StateFlow<MateriaisUiState> = _uiState.asStateFlow()

    fun carregar() {
        if (_uiState.value.carregando) return
        viewModelScope.launch {
            _uiState.update { it.copy(carregando = true, erro = null, mensagem = null) }
            try {
                val dados = repository.carregarDados()
                _uiState.update { state ->
                    state.copy(
                        materiais = dados.materiais,
                        categorias = dados.categorias,
                        erro = null
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(erro = e.localizedMessage ?: "Erro ao carregar materiais.") }
            } finally {
                _uiState.update { it.copy(carregando = false) }
            }
        }
    }

    fun salvar(
        id: Int?,
        descricao: String,
        valor: String,
        unidadeMedida: String,
        categoriaId: Int,
        aoConcluir: () -> Unit = {}
    ) {
        if (_uiState.value.salvando) return
        viewModelScope.launch {
            _uiState.update { it.copy(salvando = true, erro = null, mensagem = null) }
            try {
                repository.salvar(id, descricao, valor, unidadeMedida, categoriaId)
                val dados = repository.carregarDados()
                _uiState.update { state ->
                    state.copy(
                        materiais = dados.materiais,
                        categorias = dados.categorias,
                        mensagem = if (id == null) "Material cadastrado com sucesso." else "Material atualizado com sucesso.",
                        erro = null
                    )
                }
                aoConcluir()
            } catch (e: Exception) {
                _uiState.update { it.copy(erro = e.localizedMessage ?: "Erro ao salvar material.") }
            } finally {
                _uiState.update { it.copy(salvando = false) }
            }
        }
    }

    fun excluir(id: Int, aoConcluir: () -> Unit = {}) {
        if (_uiState.value.salvando) return
        viewModelScope.launch {
            _uiState.update { it.copy(salvando = true, erro = null, mensagem = null) }
            try {
                repository.excluir(id)
                val dados = repository.carregarDados()
                _uiState.update { state ->
                    state.copy(
                        materiais = dados.materiais,
                        categorias = dados.categorias,
                        mensagem = "Material excluído com sucesso.",
                        erro = null
                    )
                }
                aoConcluir()
            } catch (e: Exception) {
                _uiState.update { it.copy(erro = e.localizedMessage ?: "Erro ao excluir material.") }
            } finally {
                _uiState.update { it.copy(salvando = false) }
            }
        }
    }

    fun limparMensagem() { _uiState.update { it.copy(mensagem = null) } }
}
