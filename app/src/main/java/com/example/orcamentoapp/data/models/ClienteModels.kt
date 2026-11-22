package com.example.orcamentoapp.data.models


data class ClienteRequest(
    val nome: String,
    val telefone: String,
    val ativo: Boolean = true
)

data class ClienteResponse(
    val idclientes: Int,
    val nome: String,
    val telefone: String,
    val ativo: Boolean,
    val usuarios_id: Int,
    val data_cadastro: String,
    val data_atualizacao: String? = null
)

data class ClienteUpdateRequest(
    val nome: String? = null,
    val telefone: String? = null,
    val ativo: Boolean? = null
)
