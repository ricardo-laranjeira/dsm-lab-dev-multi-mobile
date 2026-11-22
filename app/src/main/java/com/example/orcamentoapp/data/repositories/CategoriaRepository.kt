package com.example.orcamentoapp.data.repositories

import com.example.orcamentoapp.data.ApiClient
import com.example.orcamentoapp.data.api.CategoriaApiService
import com.example.orcamentoapp.data.models.CategoriaRequest
import com.example.orcamentoapp.data.models.CategoriaResponse
import com.example.orcamentoapp.data.models.CategoriaUpdateRequest

internal open class CategoriaRepository(
    private val apiService: CategoriaApiService = ApiClient.categoriaApi
) {

    open suspend fun listar(): List<CategoriaResponse> {
        val resposta = apiService.listarCategorias()
        check(resposta.isSuccessful) {
            resposta.errorBody()?.string()?.takeIf { it.isNotBlank() }
                ?: ("Não foi possível carregar as categorias (HTTP ${resposta.code()}).")
        }
        return resposta.body().orEmpty()
    }

    open suspend fun salvar(id: Int?, descricao: String): CategoriaResponse {
        val resposta = if (id == null) {
            apiService.criarCategoria(CategoriaRequest(descricao))
        } else {
            apiService.atualizarCategoria(id, CategoriaUpdateRequest(descricao))
        }
        check(resposta.isSuccessful) {
            resposta.errorBody()?.string()?.takeIf { it.isNotBlank() }
                ?: ("Não foi possível salvar a categoria (HTTP ${resposta.code()}).")
        }
        return checkNotNull(resposta.body()) { "A API não retornou a categoria salva." }
    }

    open suspend fun excluir(id: Int) {
        val resposta = apiService.excluirCategoria(id)
        check(resposta.isSuccessful) {
            resposta.errorBody()?.string()?.takeIf { it.isNotBlank() }
                ?: ("Não foi possível excluir a categoria (HTTP ${resposta.code()}).")
        }
    }
}
