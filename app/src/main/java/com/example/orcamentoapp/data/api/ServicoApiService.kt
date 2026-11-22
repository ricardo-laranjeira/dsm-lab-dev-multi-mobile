package com.example.orcamentoapp.data.api

import com.example.orcamentoapp.data.models.ServicoRequest
import com.example.orcamentoapp.data.models.ServicoResponse
import com.example.orcamentoapp.data.models.ServicoUpdateRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

interface ServicoApiService {

    @GET("servicos/")
    suspend fun listarServicos(): Response<List<ServicoResponse>>

    @GET("servicos/{idservicos}")
    suspend fun buscarServico(
        @Path("idservicos") id: Int
    ): Response<ServicoResponse>

    @POST("servicos/")
    suspend fun criarServico(
        @Body dados: ServicoRequest
    ): Response<ServicoResponse>

    @PATCH("servicos/{idservicos}")
    suspend fun atualizarServico(
        @Path("idservicos") id: Int,
        @Body dados: ServicoUpdateRequest
    ): Response<ServicoResponse>

    @DELETE("servicos/{idservicos}")
    suspend fun excluirServico(
        @Path("idservicos") id: Int
    ): Response<Unit>
}
