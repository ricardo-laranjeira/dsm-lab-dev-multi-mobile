package com.example.orcamentoapp.screens.enderecos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.orcamentoapp.data.repositories.EnderecoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal data class EnderecoUi(
    val id: Int,
    val clienteId: String,
    val cep: String,
    val endereco: String,
    val numero: String,
    val complemento: String,
    val bairro: String,
    val cidade: String,
    val estado: String
)

internal data class EnderecosUiState(
    val enderecos: List<EnderecoUi> = emptyList(),
    val carregando: Boolean = false,
    val salvando: Boolean = false,
    val erro: String? = null,
    val mensagem: String? = null
)

internal class EnderecosViewModel(
    private val repository: EnderecoRepository = EnderecoRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(EnderecosUiState())
    val uiState: StateFlow<EnderecosUiState> = _uiState.asStateFlow()

    fun carregar(clienteId: Int) {
        if (_uiState.value.carregando) return
        viewModelScope.launch {
            _uiState.update { it.copy(carregando = true, erro = null, mensagem = null) }
            try {
                val lista = repository.listar(clienteId)
                _uiState.update { state ->
                    state.copy(
                        enderecos = lista.map {
                            EnderecoUi(
                                id = it.idenderecos,
                                clienteId = it.clienteId.toString(),
                                cep = it.cep,
                                endereco = it.endereco,
                                numero = it.numero,
                                complemento = it.complemento.orEmpty(),
                                bairro = it.bairro,
                                cidade = it.cidade,
                                estado = it.estado
                            )
                        },
                        erro = null
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(erro = e.localizedMessage ?: "Erro ao carregar endereços.") }
            } finally {
                _uiState.update { it.copy(carregando = false) }
            }
        }
    }

    fun salvar(
        clienteId: Int,
        id: Int?,
        cep: String,
        endereco: String,
        numero: String,
        complemento: String,
        bairro: String,
        cidade: String,
        estado: String,
        aoConcluir: () -> Unit = {}
    ) {
        if (_uiState.value.salvando) return
        viewModelScope.launch {
            _uiState.update { it.copy(salvando = true, erro = null, mensagem = null) }
            try {
                repository.salvar(
                    clienteId, id, cep, endereco, numero, complemento, bairro, cidade, estado
                )
                val lista = repository.listar(clienteId)
                _uiState.update { state ->
                    state.copy(
                        enderecos = lista.map {
                            EnderecoUi(
                                id = it.idenderecos,
                                clienteId = it.clienteId.toString(),
                                cep = it.cep,
                                endereco = it.endereco,
                                numero = it.numero,
                                complemento = it.complemento.orEmpty(),
                                bairro = it.bairro,
                                cidade = it.cidade,
                                estado = it.estado
                            )
                        },
                        mensagem = if (id == null) "Endereço cadastrado com sucesso." else "Endereço atualizado com sucesso.",
                        erro = null
                    )
                }
                aoConcluir()
            } catch (e: Exception) {
                _uiState.update { it.copy(erro = e.localizedMessage ?: "Erro ao salvar endereço.") }
            } finally {
                _uiState.update { it.copy(salvando = false) }
            }
        }
    }

    fun excluir(clienteId: Int, id: Int, aoConcluir: () -> Unit = {}) {
        if (_uiState.value.salvando) return
        viewModelScope.launch {
            _uiState.update { it.copy(salvando = true, erro = null, mensagem = null) }
            try {
                repository.excluir(clienteId, id)
                val lista = repository.listar(clienteId)
                _uiState.update { state ->
                    state.copy(
                        enderecos = lista.map {
                            EnderecoUi(
                                id = it.idenderecos,
                                clienteId = it.clienteId.toString(),
                                cep = it.cep,
                                endereco = it.endereco,
                                numero = it.numero,
                                complemento = it.complemento.orEmpty(),
                                bairro = it.bairro,
                                cidade = it.cidade,
                                estado = it.estado
                            )
                        },
                        mensagem = "Endereço excluído com sucesso.",
                        erro = null
                    )
                }
                aoConcluir()
            } catch (e: Exception) {
                _uiState.update { it.copy(erro = e.localizedMessage ?: "Erro ao excluir endereço.") }
            } finally {
                _uiState.update { it.copy(salvando = false) }
            }
        }
    }

    fun limparMensagem() { _uiState.update { it.copy(mensagem = null) } }
}
