package com.example.orcamentoapp.data.repositories

import com.example.orcamentoapp.data.ApiClient
import com.example.orcamentoapp.data.api.UsuarioApiService
import com.example.orcamentoapp.data.models.SenhaUpdateRequest
import com.example.orcamentoapp.data.models.UsuarioPerfilUpdateRequest
import com.example.orcamentoapp.data.models.UsuarioResponse

internal open class PerfilRepository(
    private val usuarioApi: UsuarioApiService = ApiClient.usuarioApi
) {

    open suspend fun obterPerfil(): UsuarioResponse {
        val resposta = usuarioApi.obterMeuPerfil()
        check(resposta.isSuccessful) {
            resposta.errorBody()?.string()?.takeIf { it.isNotBlank() }
                ?: ("Não foi possível carregar os dados do perfil (HTTP ${resposta.code()}).")
        }
        return checkNotNull(resposta.body()) { "A API não retornou os dados do usuário." }
    }

    open suspend fun atualizarPerfil(nome: String, email: String): UsuarioResponse {
        val resposta = usuarioApi.atualizarMeuPerfil(
            UsuarioPerfilUpdateRequest(nome, email)
        )
        check(resposta.isSuccessful) {
            resposta.errorBody()?.string()?.takeIf { it.isNotBlank() }
                ?: ("Não foi possível atualizar o perfil (HTTP ${resposta.code()}).")
        }
        return checkNotNull(resposta.body()) { "A API não retornou o perfil atualizado." }
    }

    open suspend fun atualizarSenha(senhaAtual: String, novaSenha: String) {
        val resposta = usuarioApi.atualizarMinhaSenha(
            SenhaUpdateRequest(senhaAtual, novaSenha)
        )
        check(resposta.isSuccessful) {
            resposta.errorBody()?.string()?.takeIf { it.isNotBlank() }
                ?: ("Não foi possível alterar a senha (HTTP ${resposta.code()}).")
        }
    }
}
