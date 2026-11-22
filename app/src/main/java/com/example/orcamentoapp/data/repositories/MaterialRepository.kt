package com.example.orcamentoapp.data.repositories

import com.example.orcamentoapp.data.ApiClient
import com.example.orcamentoapp.data.api.CategoriaApiService
import com.example.orcamentoapp.data.api.MaterialApiService
import com.example.orcamentoapp.data.models.CategoriaResponse
import com.example.orcamentoapp.data.models.MaterialRequest
import com.example.orcamentoapp.data.models.MaterialResponse
import com.example.orcamentoapp.data.models.MaterialUpdateRequest

internal open class MaterialRepository(
    private val materialApi: MaterialApiService = ApiClient.materialApi,
    private val categoriaApi: CategoriaApiService = ApiClient.categoriaApi
) {

    data class DadosMateriais(
        val materiais: List<MaterialResponse>,
        val categorias: List<CategoriaResponse>
    )

    open suspend fun carregarDados(): DadosMateriais {
        val rm = materialApi.listarMateriais()
        val rc = categoriaApi.listarCategorias()

        check(rm.isSuccessful && rc.isSuccessful) {
            "Não foi possível carregar materiais e categorias (HTTP M:${rm.code()} C:${rc.code()})."
        }

        return DadosMateriais(rm.body().orEmpty(), rc.body().orEmpty())
    }

    open suspend fun salvar(
        id: Int?,
        descricao: String,
        valor: String,
        unidadeMedida: String,
        categoriaId: Int
    ): MaterialResponse {
        val resposta = if (id == null) {
            materialApi.criarMaterial(
                MaterialRequest(descricao, valor, unidadeMedida, categoriaId)
            )
        } else {
            materialApi.atualizarMaterial(
                id,
                MaterialUpdateRequest(descricao, valor, unidadeMedida, categoriaId)
            )
        }
        check(resposta.isSuccessful) {
            resposta.errorBody()?.string()?.takeIf { it.isNotBlank() }
                ?: ("Não foi possível salvar o material (HTTP ${resposta.code()}).")
        }
        return checkNotNull(resposta.body()) { "A API não retornou o material salvo." }
    }

    open suspend fun excluir(id: Int) {
        val resposta = materialApi.excluirMaterial(id)
        check(resposta.isSuccessful) {
            resposta.errorBody()?.string()?.takeIf { it.isNotBlank() }
                ?: ("Não foi possível excluir o material (HTTP ${resposta.code()}).")
        }
    }
}
