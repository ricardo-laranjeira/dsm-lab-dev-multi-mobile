package com.example.orcamentoapp.data.models

data class UsuarioCadastroRequest(
    val nome: String,
    val email: String,
    val senha: String
)

data class LoginRequest(
    val email: String,
    val senha: String
)

data class TokenResponse(
    val access_token: String,
    val token_type: String = "bearer"
)

data class UsuarioResponse(
    val idusuarios: Int,
    val nome: String,
    val email: String,
    val ativo: Boolean,
    val data_cadastro: String,
    val data_atualizacao: String
)
