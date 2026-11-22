package com.example.orcamentoapp.data.repositories

import com.example.orcamentoapp.data.ApiClient
import com.example.orcamentoapp.data.api.EnderecoApiService
import com.example.orcamentoapp.data.models.EnderecoRequest
import com.example.orcamentoapp.data.models.EnderecoResponse
import com.example.orcamentoapp.data.models.EnderecoUpdateRequest

internal open class EnderecoRepository(
    private val apiService: EnderecoApiService = ApiClient.enderecoApi
) {

    open suspend fun listar(clienteId: Int): List<EnderecoResponse> {
        val resposta = apiService.listarEnderecos(clienteId)
        if (resposta.code() == 404) {
            return emptyList()
        }
        check(resposta.isSuccessful) {
            resposta.errorBody()?.string()?.takeIf { it.isNotBlank() }
                ?: ("Não foi possível carregar os endereços (HTTP ${resposta.code()}).")
        }
        return resposta.body().orEmpty()
    }

    open suspend fun salvar(
        clienteId: Int,
        id: Int?,
        cep: String,
        endereco: String,
        numero: String,
        complemento: String?,
        bairro: String,
        cidade: String,
        estado: String
    ): EnderecoResponse {
        check(clienteId > 0) { "ID de cliente inválido." }
        val complementoTratado = complemento?.trim()?.takeIf { it.isNotBlank() }

        val resposta = if (id == null) {
            apiService.criarEndereco(
                clienteId,
                EnderecoRequest(
                    cep = cep.trim(),
                    endereco = endereco.trim(),
                    numero = numero.trim(),
                    complemento = complementoTratado,
                    bairro = bairro.trim(),
                    cidade = cidade.trim(),
                    estado = estado.trim()
                )
            )
        } else {
            apiService.atualizarEndereco(
                clienteId,
                id,
                EnderecoUpdateRequest(
                    cep = cep.trim(),
                    endereco = endereco.trim(),
                    numero = numero.trim(),
                    complemento = complementoTratado,
                    bairro = bairro.trim(),
                    cidade = cidade.trim(),
                    estado = estado.trim()
                )
            )
        }
        check(resposta.isSuccessful) {
            resposta.errorBody()?.string()?.takeIf { it.isNotBlank() }
                ?: ("Não foi possível salvar o endereço (HTTP ${resposta.code()}).")
        }
        return checkNotNull(resposta.body()) { "A API não retornou o endereço salvo." }
    }

    open suspend fun excluir(clienteId: Int, id: Int) {
        val resposta = apiService.excluirEndereco(clienteId, id)
        check(resposta.isSuccessful) {
            resposta.errorBody()?.string()?.takeIf { it.isNotBlank() }
                ?: ("Não foi possível excluir o endereço (HTTP ${resposta.code()}).")
        }
    }
}
