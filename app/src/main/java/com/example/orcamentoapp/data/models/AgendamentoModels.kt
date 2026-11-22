package com.example.orcamentoapp.data.models

import com.google.gson.annotations.SerializedName

data class AgendamentoRequest(
    @SerializedName("data")
    val data: String,

    @SerializedName("hora")
    val hora: String,

    @SerializedName("orcamentos_id")
    val orcamentosId: Int
)

data class AgendamentoUpdateRequest(
    @SerializedName("data")
    val data: String? = null,

    @SerializedName("hora")
    val hora: String? = null
)

data class AgendamentoResponse(
    @SerializedName("idagendamentos")
    val idagendamentos: Int,

    @SerializedName("data")
    val data: String,

    @SerializedName("hora")
    val hora: String,

    @SerializedName("orcamentos_id")
    val orcamentosId: Int,

    @SerializedName("data_cadastro")
    val dataCadastro: String,

    @SerializedName("data_atualizacao")
    val dataAtualizacao: String? = null
)
