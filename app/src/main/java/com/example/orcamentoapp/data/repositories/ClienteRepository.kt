package com.example.orcamentoapp.data.repositories

import com.example.orcamentoapp.data.ApiClient
import com.example.orcamentoapp.data.api.ClienteApiService
import com.example.orcamentoapp.data.models.ClienteRequest
import com.example.orcamentoapp.data.models.ClienteResponse
import com.example.orcamentoapp.data.models.ClienteUpdateRequest

internal open class ClienteRepository(
    private val apiService: ClienteApiService = ApiClient.clienteApi
) {

    open suspend fun listar(): List<ClienteResponse> {
        val resposta = apiService.listarClientes()
        check(resposta.isSuccessful) {
            resposta.errorBody()?.string()?.takeIf { it.isNotBlank() }
                ?: ("Não foi possível carregar os clientes (HTTP ${resposta.code()}).")
        }
        return resposta.body().orEmpty()
    }

    open suspend fun salvar(id: Int?, nome: String, telefone: String, ativo: Boolean = true): ClienteResponse {
        val resposta = if (id == null) {
            apiService.criarCliente(ClienteRequest(nome, telefone, ativo))
        } else {
            apiService.atualizarCliente(id, ClienteUpdateRequest(nome, telefone, ativo))
        }
        check(resposta.isSuccessful) {
            resposta.errorBody()?.string()?.takeIf { it.isNotBlank() }
                ?: ("Não foi possível salvar o cliente (HTTP ${resposta.code()}).")
        }
        return checkNotNull(resposta.body()) { "A API não retornou o cliente salvo." }
    }

    open suspend fun excluir(id: Int) {
        val resposta = apiService.excluirCliente(id)
        check(resposta.isSuccessful) {
            resposta.errorBody()?.string()?.takeIf { it.isNotBlank() }
                ?: ("Não foi possível excluir o cliente (HTTP ${resposta.code()}).")
        }
    }
}
