package com.example.orcamentoapp.screens.clientes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.orcamentoapp.data.repositories.ClienteRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal data class ClienteUi(
    val id: Int,
    val nome: String,
    val telefone: String,
    val ativo: Boolean
)

internal data class ClientesUiState(
    val clientes: List<ClienteUi> = emptyList(),
    val carregando: Boolean = false,
    val salvando: Boolean = false,
    val erro: String? = null,
    val mensagem: String? = null
)

internal class ClientesViewModel(
    private val repository: ClienteRepository = ClienteRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(ClientesUiState())
    val uiState: StateFlow<ClientesUiState> = _uiState.asStateFlow()

    fun carregar() {
        if (_uiState.value.carregando) return
        viewModelScope.launch {
            _uiState.update { it.copy(carregando = true, erro = null, mensagem = null) }
            try {
                val lista = repository.listar()
                _uiState.update { state ->
                    state.copy(
                        clientes = lista.map { ClienteUi(it.idclientes, it.nome, it.telefone, it.ativo) },
                        erro = null
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(erro = e.localizedMessage ?: "Erro ao carregar clientes.") }
            } finally {
                _uiState.update { it.copy(carregando = false) }
            }
        }
    }

    fun salvar(id: Int?, nome: String, telefone: String, ativo: Boolean, aoConcluir: () -> Unit = {}) {
        if (_uiState.value.salvando) return
        viewModelScope.launch {
            _uiState.update { it.copy(salvando = true, erro = null, mensagem = null) }
            try {
                repository.salvar(id, nome, telefone, ativo)
                val lista = repository.listar()
                _uiState.update { state ->
                    state.copy(
                        clientes = lista.map { ClienteUi(it.idclientes, it.nome, it.telefone, it.ativo) },
                        mensagem = if (id == null) "Cliente cadastrado com sucesso." else "Cliente atualizado com sucesso.",
                        erro = null
                    )
                }
                aoConcluir()
            } catch (e: Exception) {
                _uiState.update { it.copy(erro = e.localizedMessage ?: "Erro ao salvar cliente.") }
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
                        clientes = lista.map { ClienteUi(it.idclientes, it.nome, it.telefone, it.ativo) },
                        mensagem = "Cliente excluído com sucesso.",
                        erro = null
                    )
                }
                aoConcluir()
            } catch (e: Exception) {
                _uiState.update { it.copy(erro = e.localizedMessage ?: "Erro ao excluir cliente.") }
            } finally {
                _uiState.update { it.copy(salvando = false) }
            }
        }
    }

    fun limparMensagem() { _uiState.update { it.copy(mensagem = null) } }
}
