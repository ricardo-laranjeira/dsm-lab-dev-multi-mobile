package com.example.orcamentoapp.data.api

import com.example.orcamentoapp.data.models.OrcamentoRequest
import com.example.orcamentoapp.data.models.OrcamentoResponse
import com.example.orcamentoapp.data.models.OrcamentoUpdateRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

interface OrcamentoApiService {

    @GET("orcamentos/")
    suspend fun listarOrcamentos(): Response<List<OrcamentoResponse>>

    @GET("orcamentos/{idorcamentos}")
    suspend fun buscarOrcamento(
        @Path("idorcamentos") id: Int
    ): Response<OrcamentoResponse>

    @POST("orcamentos/")
    suspend fun criarOrcamento(
        @Body dados: OrcamentoRequest
    ): Response<OrcamentoResponse>

    @PATCH("orcamentos/{idorcamentos}")
    suspend fun atualizarOrcamento(
        @Path("idorcamentos") id: Int,
        @Body dados: OrcamentoUpdateRequest
    ): Response<OrcamentoResponse>

    @DELETE("orcamentos/{idorcamentos}")
    suspend fun excluirOrcamento(
        @Path("idorcamentos") id: Int
    ): Response<Unit>
}
