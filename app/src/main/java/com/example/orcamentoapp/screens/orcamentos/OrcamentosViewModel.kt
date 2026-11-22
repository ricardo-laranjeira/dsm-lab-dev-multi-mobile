package com.example.orcamentoapp.screens.orcamentos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.orcamentoapp.data.repositories.OrcamentoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal data class OrcamentosUiState(
    val orcamentos: List<OrcamentoUi> = emptyList(),
    val clientes: List<ClienteOrcamentoUi> = emptyList(),
    val materiais: List<ProdutoOrcamentoUi> = emptyList(),
    val servicos: List<ProdutoOrcamentoUi> = emptyList(),
    val carregando: Boolean = false,
    val salvando: Boolean = false,
    val erro: String? = null,
    val mensagem: String? = null
)

/** Coordena o estado da tela e delega operações HTTP ao repositório. */
internal class OrcamentosViewModel(
    private val repository: OrcamentoRepository = OrcamentoRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(OrcamentosUiState())
    val uiState: StateFlow<OrcamentosUiState> = _uiState.asStateFlow()

    fun carregarDados() {
        if (_uiState.value.carregando) return
        viewModelScope.launch {
            _uiState.update { it.copy(carregando = true, erro = null, mensagem = null) }
            try {
                val dados = repository.carregar()
                _uiState.update {
                    it.copy(
                        orcamentos = dados.orcamentos,
                        clientes = dados.clientes,
                        materiais = dados.materiais,
                        servicos = dados.servicos,
                        erro = null
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(erro = e.localizedMessage ?: "Não foi possível carregar os orçamentos.")
                }
            } finally {
                _uiState.update { it.copy(carregando = false) }
            }
        }
    }

    fun salvarOrcamento(atual: OrcamentoUi?, novo: OrcamentoUi, aoConcluir: () -> Unit = {}) {
        if (_uiState.value.salvando) return
        viewModelScope.launch {
            _uiState.update { it.copy(salvando = true, erro = null, mensagem = null) }
            try {
                repository.salvar(atual, novo)
                val dados = repository.carregar()
                _uiState.update {
                    it.copy(
                        orcamentos = dados.orcamentos,
                        clientes = dados.clientes,
                        materiais = dados.materiais,
                        servicos = dados.servicos,
                        mensagem = "Orçamento salvo com sucesso.",
                        erro = null
                    )
                }
                aoConcluir()
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(erro = (e.localizedMessage ?: "Erro ao salvar o orçamento.") +
                        " Se o orçamento já foi criado, atualize a lista antes de tentar novamente.")
                }
            } finally {
                _uiState.update { it.copy(salvando = false) }
            }
        }
    }

    fun excluirOrcamento(id: Int, aoConcluir: () -> Unit = {}) {
        if (_uiState.value.salvando) return
        viewModelScope.launch {
            _uiState.update { it.copy(salvando = true, erro = null, mensagem = null) }
            try {
                repository.excluir(id)
                val dados = repository.carregar()
                _uiState.update {
                    it.copy(
                        orcamentos = dados.orcamentos,
                        clientes = dados.clientes,
                        materiais = dados.materiais,
                        servicos = dados.servicos,
                        mensagem = "Orçamento excluído.",
                        erro = null
                    )
                }
                aoConcluir()
            } catch (e: Exception) {
                _uiState.update { it.copy(erro = e.localizedMessage ?: "Erro ao excluir orçamento.") }
            } finally {
                _uiState.update { it.copy(salvando = false) }
            }
        }
    }

    fun limparMensagem() { _uiState.update { it.copy(mensagem = null) } }
}
