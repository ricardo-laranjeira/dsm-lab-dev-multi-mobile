package com.example.orcamentoapp.screens.orcamentos

import java.text.NumberFormat
import java.util.Locale

internal data class ClienteOrcamentoUi(
    val id: Int,
    val nome: String
)

internal data class ProdutoOrcamentoUi(
    val id: Int,
    val nome: String,
    val valor: Double
)

internal data class ItemOrcamentoUi(
    val id: Int,
    val tipo: String,
    val referenciaId: Int,
    val descricao: String,
    val quantidade: Int,
    val valorUnitario: Double
) {
    val subtotal: Double
        get() = quantidade * valorUnitario
}

internal data class OrcamentoUi(
    val id: Int,
    val clienteId: Int,
    val clienteNome: String,
    val validade: String,
    val status: String,
    val itens: List<ItemOrcamentoUi>
) {
    val total: Double
        get() = itens.sumOf { it.subtotal }
}

internal val statusOrcamento = listOf("Aprovado", "Não aprovado")

internal fun moeda(valor: Double): String =
    NumberFormat.getCurrencyInstance(Locale.forLanguageTag("pt-BR")).format(valor)

internal fun tokenAutorizacao(token: String?): String {
    val valor = token?.trim().orEmpty()
    return when {
        valor.isBlank() -> ""
        valor.startsWith("Bearer ", ignoreCase = true) -> valor
        else -> "Bearer $valor"
    }
}


