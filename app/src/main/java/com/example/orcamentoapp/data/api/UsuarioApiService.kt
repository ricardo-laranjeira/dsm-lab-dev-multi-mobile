package com.example.orcamentoapp.data.api

import com.example.orcamentoapp.data.models.LoginRequest
import com.example.orcamentoapp.data.models.SenhaUpdateRequest
import com.example.orcamentoapp.data.models.TokenResponse
import com.example.orcamentoapp.data.models.UsuarioCadastroRequest
import com.example.orcamentoapp.data.models.UsuarioPerfilUpdateRequest
import com.example.orcamentoapp.data.models.UsuarioResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST

interface UsuarioApiService {

    @POST("auth/login")
    suspend fun login(
        @Body credenciais: LoginRequest
    ): Response<TokenResponse>

    @POST("usuarios/")
    suspend fun cadastrarUsuario(
        @Body usuario: UsuarioCadastroRequest
    ): Response<UsuarioResponse>

    @GET("usuarios/me")
    suspend fun obterMeuPerfil(): Response<UsuarioResponse>

    @PATCH("usuarios/me")
    suspend fun atualizarMeuPerfil(
        @Body dados: UsuarioPerfilUpdateRequest
    ): Response<UsuarioResponse>

    @PATCH("usuarios/me/senha")
    suspend fun atualizarMinhaSenha(
        @Body dados: SenhaUpdateRequest
    ): Response<Unit>
}
