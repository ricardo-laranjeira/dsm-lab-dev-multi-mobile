package com.example.orcamentoapp.screens.agendamentos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.orcamentoapp.data.models.AgendamentoResponse
import com.example.orcamentoapp.data.models.ClienteResponse
import com.example.orcamentoapp.data.models.OrcamentoResponse
import com.example.orcamentoapp.data.repositories.AgendamentoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal data class AgendamentosUiState(
    val agendamentos: List<AgendamentoResponse> = emptyList(),
    val orcamentos: List<OrcamentoResponse> = emptyList(),
    val clientes: List<ClienteResponse> = emptyList(),
    val carregando: Boolean = false,
    val salvando: Boolean = false,
    val erro: String? = null,
    val mensagem: String? = null
)

internal class AgendamentosViewModel(
    private val repository: AgendamentoRepository = AgendamentoRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(AgendamentosUiState())
    val uiState: StateFlow<AgendamentosUiState> = _uiState.asStateFlow()

    fun carregar() {
        if (_uiState.value.carregando) return
        viewModelScope.launch {
            _uiState.update { it.copy(carregando = true, erro = null, mensagem = null) }
            try {
                val dados = repository.carregarDados()
                _uiState.update { state ->
                    state.copy(
                        agendamentos = dados.agendamentos,
                        orcamentos = dados.orcamentos,
                        clientes = dados.clientes,
                        erro = null
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(erro = e.localizedMessage ?: "Erro ao carregar agendamentos.") }
            } finally {
                _uiState.update { it.copy(carregando = false) }
            }
        }
    }

    fun salvar(
        id: Int?,
        data: String,
        hora: String,
        orcamentoId: Int,
        aoConcluir: () -> Unit = {}
    ) {
        if (_uiState.value.salvando) return
        viewModelScope.launch {
            _uiState.update { it.copy(salvando = true, erro = null, mensagem = null) }
            try {
                repository.salvar(id, data, hora, orcamentoId)
                val dados = repository.carregarDados()
                _uiState.update { state ->
                    state.copy(
                        agendamentos = dados.agendamentos,
                        orcamentos = dados.orcamentos,
                        clientes = dados.clientes,
                        mensagem = if (id == null) "Agendamento cadastrado com sucesso." else "Agendamento atualizado com sucesso.",
                        erro = null
                    )
                }
                aoConcluir()
            } catch (e: Exception) {
                _uiState.update { it.copy(erro = e.localizedMessage ?: "Erro ao salvar agendamento.") }
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
                        agendamentos = dados.agendamentos,
                        orcamentos = dados.orcamentos,
                        clientes = dados.clientes,
                        mensagem = "Agendamento excluído com sucesso.",
                        erro = null
                    )
                }
                aoConcluir()
            } catch (e: Exception) {
                _uiState.update { it.copy(erro = e.localizedMessage ?: "Erro ao excluir agendamento.") }
            } finally {
                _uiState.update { it.copy(salvando = false) }
            }
        }
    }

    fun limparMensagem() { _uiState.update { it.copy(mensagem = null) } }
}
