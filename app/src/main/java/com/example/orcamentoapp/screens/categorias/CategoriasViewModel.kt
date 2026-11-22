package com.example.orcamentoapp.screens.categorias

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.orcamentoapp.data.repositories.CategoriaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal data class CategoriaUi(
    val id: Int,
    val nome: String
)

internal data class CategoriasUiState(
    val categorias: List<CategoriaUi> = emptyList(),
    val carregando: Boolean = false,
    val salvando: Boolean = false,
    val erro: String? = null,
    val mensagem: String? = null
)

internal class CategoriasViewModel(
    private val repository: CategoriaRepository = CategoriaRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(CategoriasUiState())
    val uiState: StateFlow<CategoriasUiState> = _uiState.asStateFlow()

    fun carregar() {
        if (_uiState.value.carregando) return
        viewModelScope.launch {
            _uiState.update { it.copy(carregando = true, erro = null, mensagem = null) }
            try {
                val lista = repository.listar()
                _uiState.update { state ->
                    state.copy(
                        categorias = lista.map { CategoriaUi(it.idcategorias, it.descricao) },
                        erro = null
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(erro = e.localizedMessage ?: "Erro ao carregar categorias.") }
            } finally {
                _uiState.update { it.copy(carregando = false) }
            }
        }
    }

    fun salvar(id: Int?, descricao: String, aoConcluir: () -> Unit = {}) {
        if (_uiState.value.salvando) return
        viewModelScope.launch {
            _uiState.update { it.copy(salvando = true, erro = null, mensagem = null) }
            try {
                repository.salvar(id, descricao)
                val lista = repository.listar()
                _uiState.update { state ->
                    state.copy(
                        categorias = lista.map { CategoriaUi(it.idcategorias, it.descricao) },
                        mensagem = if (id == null) "Categoria salva com sucesso." else "Categoria atualizada.",
                        erro = null
                    )
                }
                aoConcluir()
            } catch (e: Exception) {
                _uiState.update { it.copy(erro = e.localizedMessage ?: "Erro ao salvar categoria.") }
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
                val lista = repository.listar()
                _uiState.update { state ->
                    state.copy(
                        categorias = lista.map { CategoriaUi(it.idcategorias, it.descricao) },
                        mensagem = "Categoria excluída com sucesso.",
                        erro = null
                    )
                }
                aoConcluir()
            } catch (e: Exception) {
                _uiState.update { it.copy(erro = e.localizedMessage ?: "Erro ao excluir categoria.") }
            } finally {
                _uiState.update { it.copy(salvando = false) }
            }
        }
    }

    fun limparMensagem() { _uiState.update { it.copy(mensagem = null) } }
}
