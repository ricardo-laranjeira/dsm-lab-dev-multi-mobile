package com.example.orcamentoapp.data.models


import com.google.gson.annotations.SerializedName

data class OrcamentoMaterialRequest(
    @SerializedName("materiais_id")
    val materiaisId: Int,
    val quantidade: Int
)

data class OrcamentoMaterialUpdateRequest(
    val quantidade: Int
)

data class OrcamentoMaterialResponse(
    @SerializedName("orcamentos_id")
    val orcamentosId: Int,

    @SerializedName("materiais_id")
    val materiaisId: Int,

    val quantidade: Int
)

data class OrcamentoServicoRequest(
    @SerializedName("servicos_id")
    val servicosId: Int,
    val quantidade: Int
)

data class OrcamentoServicoUpdateRequest(
    val quantidade: Int
)

data class OrcamentoServicoResponse(
    @SerializedName("orcamentos_id")
    val orcamentosId: Int,

    @SerializedName("servicos_id")
    val servicosId: Int,

    val quantidade: Int
)
