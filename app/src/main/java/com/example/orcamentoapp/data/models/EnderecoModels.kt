package com.example.orcamentoapp.data.models


import com.google.gson.annotations.SerializedName

data class EnderecoRequest(
    val cep: String,
    val endereco: String,
    val numero: String,
    val complemento: String? = null,
    val bairro: String,
    val cidade: String,
    val estado: String
)

data class EnderecoUpdateRequest(
    val cep: String? = null,
    val endereco: String? = null,
    val numero: String? = null,
    val complemento: String? = null,
    val bairro: String? = null,
    val cidade: String? = null,
    val estado: String? = null
)

data class EnderecoResponse(
    val idenderecos: Int,
    @SerializedName("cliente_id")
    val clienteId: Int,
    val cep: String,
    val endereco: String,
    val numero: String,
    val complemento: String? = null,
    val bairro: String,
    val cidade: String,
    val estado: String,
    @SerializedName("data_cadastro")
    val dataCadastro: String,
    @SerializedName("data_atualizacao")
    val dataAtualizacao: String? = null
)
