package com.example.orcamentoapp.data.models


import com.google.gson.annotations.SerializedName

data class MaterialRequest(
    val descricao: String,
    val valor: String,
    @SerializedName("unidade_medida")
    val unidadeMedida: String,
    @SerializedName("categorias_id")
    val categoriasId: Int
)

data class MaterialUpdateRequest(
    val descricao: String? = null,
    val valor: String? = null,
    @SerializedName("unidade_medida")
    val unidadeMedida: String? = null,
    @SerializedName("categorias_id")
    val categoriasId: Int? = null
)

data class MaterialResponse(
    val idmateriais: Int,
    val descricao: String,
    val valor: String,
    @SerializedName("unidade_medida")
    val unidadeMedida: String,
    @SerializedName("data_cadastro")
    val dataCadastro: String,
    @SerializedName("data_atualizacao")
    val dataAtualizacao: String? = null,
    @SerializedName("categorias_id")
    val categoriasId: Int
)
