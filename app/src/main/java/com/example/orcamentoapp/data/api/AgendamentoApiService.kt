package com.example.orcamentoapp.data.api

import com.example.orcamentoapp.data.models.AgendamentoRequest
import com.example.orcamentoapp.data.models.AgendamentoResponse
import com.example.orcamentoapp.data.models.AgendamentoUpdateRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

interface AgendamentoApiService {

    @GET("agendamentos/")
    suspend fun listarAgendamentos(): Response<List<AgendamentoResponse>>

    @GET("agendamentos/{idagendamentos}")
    suspend fun buscarAgendamento(
        @Path("idagendamentos") id: Int
    ): Response<AgendamentoResponse>

    @POST("agendamentos/")
    suspend fun criarAgendamento(
        @Body dados: AgendamentoRequest
    ): Response<AgendamentoResponse>

    @PATCH("agendamentos/{idagendamentos}")
    suspend fun atualizarAgendamento(
        @Path("idagendamentos") id: Int,
        @Body dados: AgendamentoUpdateRequest
    ): Response<AgendamentoResponse>

    @DELETE("agendamentos/{idagendamentos}")
    suspend fun excluirAgendamento(
        @Path("idagendamentos") id: Int
    ): Response<Unit>
}
