package com.example.orcamentoapp.data.repositories

import com.example.orcamentoapp.data.ApiClient
import com.example.orcamentoapp.data.api.CategoriaApiService
import com.example.orcamentoapp.data.api.ServicoApiService
import com.example.orcamentoapp.data.models.CategoriaResponse
import com.example.orcamentoapp.data.models.ServicoRequest
import com.example.orcamentoapp.data.models.ServicoResponse
import com.example.orcamentoapp.data.models.ServicoUpdateRequest

internal open class ServicoRepository(
    private val servicoApi: ServicoApiService = ApiClient.servicoApi,
    private val categoriaApi: CategoriaApiService = ApiClient.categoriaApi
) {

    data class DadosServicos(
        val servicos: List<ServicoResponse>,
        val categorias: List<CategoriaResponse>
    )

    open suspend fun carregarDados(): DadosServicos {
        val rs = servicoApi.listarServicos()
        val rc = categoriaApi.listarCategorias()

        check(rs.isSuccessful && rc.isSuccessful) {
            "Não foi possível carregar serviços e categorias (HTTP S:${rs.code()} C:${rc.code()})."
        }

        return DadosServicos(rs.body().orEmpty(), rc.body().orEmpty())
    }

    open suspend fun salvar(
        id: Int?,
        descricao: String,
        valor: String,
        categoriaId: Int
    ): ServicoResponse {
        val resposta = if (id == null) {
            servicoApi.criarServico(
                ServicoRequest(descricao, valor, categoriaId)
            )
        } else {
            servicoApi.atualizarServico(
                id,
                ServicoUpdateRequest(descricao, valor, categoriaId)
            )
        }
        check(resposta.isSuccessful) {
            resposta.errorBody()?.string()?.takeIf { it.isNotBlank() }
                ?: ("Não foi possível salvar o serviço (HTTP ${resposta.code()}).")
        }
        return checkNotNull(resposta.body()) { "A API não retornou o serviço salvo." }
    }

    open suspend fun excluir(id: Int) {
        val resposta = servicoApi.excluirServico(id)
        check(resposta.isSuccessful) {
            resposta.errorBody()?.string()?.takeIf { it.isNotBlank() }
                ?: ("Não foi possível excluir o serviço (HTTP ${resposta.code()}).")
        }
    }
}
