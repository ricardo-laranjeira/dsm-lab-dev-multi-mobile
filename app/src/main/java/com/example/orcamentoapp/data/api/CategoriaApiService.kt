package com.example.orcamentoapp.data.api

import com.example.orcamentoapp.data.models.CategoriaRequest
import com.example.orcamentoapp.data.models.CategoriaResponse
import com.example.orcamentoapp.data.models.CategoriaUpdateRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

interface CategoriaApiService {

    @GET("categorias/")
    suspend fun listarCategorias(): Response<List<CategoriaResponse>>

    @GET("categorias/{idcategorias}")
    suspend fun buscarCategoria(
        @Path("idcategorias") id: Int
    ): Response<CategoriaResponse>

    @POST("categorias/")
    suspend fun criarCategoria(
        @Body dados: CategoriaRequest
    ): Response<CategoriaResponse>

    @PATCH("categorias/{idcategorias}")
    suspend fun atualizarCategoria(
        @Path("idcategorias") id: Int,
        @Body dados: CategoriaUpdateRequest
    ): Response<CategoriaResponse>

    @DELETE("categorias/{idcategorias}")
    suspend fun excluirCategoria(
        @Path("idcategorias") id: Int
    ): Response<Unit>
}
