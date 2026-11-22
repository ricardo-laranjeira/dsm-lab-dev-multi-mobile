package com.example.orcamentoapp.data.api

import com.example.orcamentoapp.data.models.OrcamentoMaterialRequest
import com.example.orcamentoapp.data.models.OrcamentoMaterialResponse
import com.example.orcamentoapp.data.models.OrcamentoMaterialUpdateRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

interface OrcamentoMaterialApiService {

    @GET("orcamentos/{idorcamentos}/materiais/")
    suspend fun listarMateriaisOrcamento(
        @Path("idorcamentos") orcamentoId: Int
    ): Response<List<OrcamentoMaterialResponse>>

    @POST("orcamentos/{idorcamentos}/materiais/")
    suspend fun adicionarMaterialOrcamento(
        @Path("idorcamentos") orcamentoId: Int,
        @Body dados: OrcamentoMaterialRequest
    ): Response<OrcamentoMaterialResponse>

    @PATCH("orcamentos/{idorcamentos}/materiais/{materiais_id}")
    suspend fun atualizarMaterialOrcamento(
        @Path("idorcamentos") orcamentoId: Int,
        @Path("materiais_id") materialId: Int,
        @Body dados: OrcamentoMaterialUpdateRequest
    ): Response<OrcamentoMaterialResponse>

    @DELETE("orcamentos/{idorcamentos}/materiais/{materiais_id}")
    suspend fun excluirMaterialOrcamento(
        @Path("idorcamentos") orcamentoId: Int,
        @Path("materiais_id") materialId: Int
    ): Response<Unit>
}
