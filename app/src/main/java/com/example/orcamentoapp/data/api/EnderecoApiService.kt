package com.example.orcamentoapp.data.api

import com.example.orcamentoapp.data.models.EnderecoRequest
import com.example.orcamentoapp.data.models.EnderecoResponse
import com.example.orcamentoapp.data.models.EnderecoUpdateRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

interface EnderecoApiService {

    @GET("clientes/{idclientes}/enderecos/")
    suspend fun listarEnderecos(
        @Path("idclientes") idCliente: Int
    ): Response<List<EnderecoResponse>>

    @GET("clientes/{idclientes}/enderecos/{idenderecos}")
    suspend fun buscarEndereco(
        @Path("idclientes") idCliente: Int,
        @Path("idenderecos") idEndereco: Int
    ): Response<EnderecoResponse>

    @POST("clientes/{idclientes}/enderecos/")
    suspend fun criarEndereco(
        @Path("idclientes") idCliente: Int,
        @Body endereco: EnderecoRequest
    ): Response<EnderecoResponse>

    @PATCH("clientes/{idclientes}/enderecos/{idenderecos}")
    suspend fun atualizarEndereco(
        @Path("idclientes") idCliente: Int,
        @Path("idenderecos") idEndereco: Int,
        @Body endereco: EnderecoUpdateRequest
    ): Response<EnderecoResponse>

    @DELETE("clientes/{idclientes}/enderecos/{idenderecos}")
    suspend fun excluirEndereco(
        @Path("idclientes") idCliente: Int,
        @Path("idenderecos") idEndereco: Int
    ): Response<Unit>
}
