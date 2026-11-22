package com.example.orcamentoapp.screens.servicos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.orcamentoapp.data.models.CategoriaResponse
import com.example.orcamentoapp.data.models.ServicoResponse
import com.example.orcamentoapp.data.repositories.ServicoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal data class ServicosUiState(
    val servicos: List<ServicoResponse> = emptyList(),
    val categorias: List<CategoriaResponse> = emptyList(),
    val carregando: Boolean = false,
    val salvando: Boolean = false,
    val erro: String? = null,
    val mensagem: String? = null
)

internal class ServicosViewModel(
    private val repository: ServicoRepository = ServicoRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(ServicosUiState())
    val uiState: StateFlow<ServicosUiState> = _uiState.asStateFlow()

    fun carregar() {
        if (_uiState.value.carregando) return
        viewModelScope.launch {
            _uiState.update { it.copy(carregando = true, erro = null, mensagem = null) }
            try {
                val dados = repository.carregarDados()
                _uiState.update { state ->
                    state.copy(
                        servicos = dados.servicos,
                        categorias = dados.categorias,
                        erro = null
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(erro = e.localizedMessage ?: "Erro ao carregar serviços.") }
            } finally {
                _uiState.update { it.copy(carregando = false) }
            }
        }
    }

    fun salvar(
        id: Int?,
        descricao: String,
        valor: String,
        categoriaId: Int,
        aoConcluir: () -> Unit = {}
    ) {
        if (_uiState.value.salvando) return
        viewModelScope.launch {
            _uiState.update { it.copy(salvando = true, erro = null, mensagem = null) }
            try {
                repository.salvar(id, descricao, valor, categoriaId)
                val dados = repository.carregarDados()
                _uiState.update { state ->
                    state.copy(
                        servicos = dados.servicos,
                        categorias = dados.categorias,
                        mensagem = if (id == null) "Serviço cadastrado com sucesso." else "Serviço atualizado com sucesso.",
                        erro = null
                    )
                }
                aoConcluir()
            } catch (e: Exception) {
                _uiState.update { it.copy(erro = e.localizedMessage ?: "Erro ao salvar serviço.") }
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
                        servicos = dados.servicos,
                        categorias = dados.categorias,
                        mensagem = "Serviço excluído com sucesso.",
                        erro = null
                    )
                }
                aoConcluir()
            } catch (e: Exception) {
                _uiState.update { it.copy(erro = e.localizedMessage ?: "Erro ao excluir serviço.") }
            } finally {
                _uiState.update { it.copy(salvando = false) }
            }
        }
    }

    fun limparMensagem() { _uiState.update { it.copy(mensagem = null) } }
}
