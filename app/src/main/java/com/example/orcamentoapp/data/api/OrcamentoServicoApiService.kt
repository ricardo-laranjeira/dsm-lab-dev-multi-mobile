package com.example.orcamentoapp.data.api

import com.example.orcamentoapp.data.models.OrcamentoServicoRequest
import com.example.orcamentoapp.data.models.OrcamentoServicoResponse
import com.example.orcamentoapp.data.models.OrcamentoServicoUpdateRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

interface OrcamentoServicoApiService {

    @GET("orcamentos/{idorcamentos}/servicos/")
    suspend fun listarServicosOrcamento(
        @Path("idorcamentos") orcamentoId: Int
    ): Response<List<OrcamentoServicoResponse>>

    @POST("orcamentos/{idorcamentos}/servicos/")
    suspend fun adicionarServicoOrcamento(
        @Path("idorcamentos") orcamentoId: Int,
        @Body dados: OrcamentoServicoRequest
    ): Response<OrcamentoServicoResponse>

    @PATCH("orcamentos/{idorcamentos}/servicos/{servicos_id}")
    suspend fun atualizarServicoOrcamento(
        @Path("idorcamentos") orcamentoId: Int,
        @Path("servicos_id") servicoId: Int,
        @Body dados: OrcamentoServicoUpdateRequest
    ): Response<OrcamentoServicoResponse>

    @DELETE("orcamentos/{idorcamentos}/servicos/{servicos_id}")
    suspend fun excluirServicoOrcamento(
        @Path("idorcamentos") orcamentoId: Int,
        @Path("servicos_id") servicoId: Int
    ): Response<Unit>
}
