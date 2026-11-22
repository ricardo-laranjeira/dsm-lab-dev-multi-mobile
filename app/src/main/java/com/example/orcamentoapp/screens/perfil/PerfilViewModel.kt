package com.example.orcamentoapp.screens.perfil

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.orcamentoapp.data.repositories.PerfilRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal data class PerfilUiState(
    val nome: String = "",
    val email: String = "",
    val carregando: Boolean = false,
    val salvando: Boolean = false,
    val erro: String? = null,
    val mensagem: String? = null
)

internal class PerfilViewModel(
    private val repository: PerfilRepository = PerfilRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(PerfilUiState())
    val uiState: StateFlow<PerfilUiState> = _uiState.asStateFlow()

    fun carregar() {
        if (_uiState.value.carregando) return
        viewModelScope.launch {
            _uiState.update { it.copy(carregando = true, erro = null, mensagem = null) }
            try {
                val usuario = repository.obterPerfil()
                _uiState.update { state ->
                    state.copy(
                        nome = usuario.nome,
                        email = usuario.email,
                        erro = null
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(erro = e.localizedMessage ?: "Erro ao carregar perfil.") }
            } finally {
                _uiState.update { it.copy(carregando = false) }
            }
        }
    }

    fun salvar(
        nome: String,
        email: String,
        senhaAtual: String,
        novaSenha: String,
        aoConcluir: () -> Unit = {}
    ) {
        if (_uiState.value.salvando) return
        viewModelScope.launch {
            _uiState.update { it.copy(salvando = true, erro = null, mensagem = null) }
            try {
                val usuarioAtualizado = repository.atualizarPerfil(nome, email)

                var msg = "Perfil atualizado com sucesso."
                if (novaSenha.isNotBlank()) {
                    repository.atualizarSenha(senhaAtual, novaSenha)
                    msg = "Perfil e senha atualizados com sucesso."
                }

                _uiState.update { state ->
                    state.copy(
                        nome = usuarioAtualizado.nome,
                        email = usuarioAtualizado.email,
                        mensagem = msg,
                        erro = null
                    )
                }
                aoConcluir()
            } catch (e: Exception) {
                _uiState.update { it.copy(erro = e.localizedMessage ?: "Erro ao salvar perfil.") }
            } finally {
                _uiState.update { it.copy(salvando = false) }
            }
        }
    }

    fun limparMensagem() { _uiState.update { it.copy(mensagem = null) } }
}
