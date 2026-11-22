package com.example.orcamentoapp.data.models



import com.google.gson.annotations.SerializedName

data class CategoriaRequest(
    val descricao: String
)

data class CategoriaUpdateRequest(
    val descricao: String? = null
)

data class CategoriaResponse(
    val idcategorias: Int,
    val descricao: String,
    @SerializedName("data_cadastro")
    val dataCadastro: String,
    @SerializedName("data_atualizacao")
    val dataAtualizacao: String? = null
)

