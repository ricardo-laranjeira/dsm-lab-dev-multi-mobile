package com.example.orcamentoapp.data.models

import com.google.gson.annotations.SerializedName

data class OrcamentoRequest(
    val validade: String,
    val status: Boolean = true,
    @SerializedName("clientes_id")
    val clientesId: Int
)

data class OrcamentoUpdateRequest(
    val validade: String? = null,
    val status: Boolean? = null,
    @SerializedName("clientes_id")
    val clientesId: Int? = null
)

data class OrcamentoResponse(
    @SerializedName("idorcamentos")
    val idorcamentos: Int,

    val validade: String,
    val status: Boolean,

    @SerializedName("data_cadastro")
    val dataCadastro: String,

    @SerializedName("data_atualizacao")
    val dataAtualizacao: String? = null,

    @SerializedName("usuarios_id")
    val usuariosId: Int,

    @SerializedName("clientes_id")
    val clientesId: Int
)
