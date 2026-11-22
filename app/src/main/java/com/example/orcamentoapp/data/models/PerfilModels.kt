package com.example.orcamentoapp.data.models


import com.google.gson.annotations.SerializedName

data class UsuarioPerfilUpdateRequest(
    val nome: String,
    val email: String
)

data class SenhaUpdateRequest(
    @SerializedName("senha_atual")
    val senhaAtual: String,

    @SerializedName("nova_senha")
    val novaSenha: String
)
