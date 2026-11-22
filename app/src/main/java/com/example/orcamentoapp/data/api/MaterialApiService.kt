package com.example.orcamentoapp.data.api

import com.example.orcamentoapp.data.models.MaterialRequest
import com.example.orcamentoapp.data.models.MaterialResponse
import com.example.orcamentoapp.data.models.MaterialUpdateRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

interface MaterialApiService {

    @GET("materiais/")
    suspend fun listarMateriais(): Response<List<MaterialResponse>>

    @GET("materiais/{idmateriais}")
    suspend fun buscarMaterial(
        @Path("idmateriais") id: Int
    ): Response<MaterialResponse>

    @POST("materiais/")
    suspend fun criarMaterial(
        @Body dados: MaterialRequest
    ): Response<MaterialResponse>

    @PATCH("materiais/{idmateriais}")
    suspend fun atualizarMaterial(
        @Path("idmateriais") id: Int,
        @Body dados: MaterialUpdateRequest
    ): Response<MaterialResponse>

    @DELETE("materiais/{idmateriais}")
    suspend fun excluirMaterial(
        @Path("idmateriais") id: Int
    ): Response<Unit>
}
