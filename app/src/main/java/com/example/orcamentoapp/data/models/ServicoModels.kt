package com.example.orcamentoapp.data.models


import com.google.gson.annotations.SerializedName

data class ServicoRequest(
    val descricao: String,
    val valor: String,
    @SerializedName("categorias_id")
    val categoriasId: Int
)

data class ServicoUpdateRequest(
    val descricao: String? = null,
    val valor: String? = null,
    @SerializedName("categorias_id")
    val categoriasId: Int? = null
)

data class ServicoResponse(
    val idservicos: Int,
    val descricao: String,
    val valor: String,
    @SerializedName("data_cadastro")
    val dataCadastro: String,
    @SerializedName("data_atualizacao")
    val dataAtualizacao: String? = null,
    @SerializedName("categorias_id")
    val categoriasId: Int
)
