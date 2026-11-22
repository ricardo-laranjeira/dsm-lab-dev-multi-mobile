package com.example.orcamentoapp.data.api

import com.example.orcamentoapp.data.models.ClienteRequest
import com.example.orcamentoapp.data.models.ClienteResponse
import com.example.orcamentoapp.data.models.ClienteUpdateRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

interface ClienteApiService {

    @GET("clientes/")
    suspend fun listarClientes(): Response<List<ClienteResponse>>

    @POST("clientes/")
    suspend fun criarCliente(
        @Body cliente: ClienteRequest
    ): Response<ClienteResponse>

    @GET("clientes/{idclientes}")
    suspend fun buscarCliente(
        @Path("idclientes") idCliente: Int
    ): Response<ClienteResponse>

    @PATCH("clientes/{idclientes}")
    suspend fun atualizarCliente(
        @Path("idclientes") idCliente: Int,
        @Body cliente: ClienteUpdateRequest
    ): Response<ClienteResponse>

    @DELETE("clientes/{idclientes}")
    suspend fun excluirCliente(
        @Path("idclientes") idCliente: Int
    ): Response<Unit>
}
