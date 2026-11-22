package com.example.orcamentoapp.util.pdf

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import com.example.orcamentoapp.screens.orcamentos.OrcamentoUi
import com.example.orcamentoapp.screens.orcamentos.moeda
import java.io.File

/**
 * Exporter responsible for converting [OrcamentoUi] domain models into formatted PDF reports using [PdfGenerator].
 */
internal class OrcamentoPdfExporter {

    /**
     * Generates a PDF report for the provided [orcamento] and stores it in `cache/pdf/orcamento_{id}.pdf`.
     */
    fun exportOrcamento(context: Context, orcamento: OrcamentoUi): File {
        val pdfDir = File(context.cacheDir, "pdf").apply { mkdirs() }
        val file = File(pdfDir, "orcamento_${orcamento.id}.pdf")
        return exportOrcamentoToFile(orcamento, file)
    }

    /**
     * Generates a PDF report for the provided [orcamento] and saves it to [outputFile].
     */
    fun exportOrcamentoToFile(orcamento: OrcamentoUi, outputFile: File): File {
        val generator = PdfGenerator()
        generator.footerText = "OrcamentoApp • Relatório de Orçamento"

        // 1. Header
        generator.drawHeader(
            title = "ORÇAMENTO #${orcamento.id}",
            subtitle = "OrcamentoApp - Gestão de Orçamentos"
        )
        generator.drawLine(thickness = 1.5f, color = Color.rgb(30, 60, 110))

        // 2. Client Info and Status
        generator.drawTitle("Informações do Cliente e Orçamento", fontSize = 13f)
        generator.drawKeyValueGrid(
            pairs = listOf(
                "Cliente" to orcamento.clienteNome,
                "ID Orçamento" to "#${orcamento.id}",
                "Validade" to orcamento.validade,
                "Status" to orcamento.status
            ),
            columns = 2
        )
        generator.drawLine()

        // 3. Items Table (Materials & Services)
        generator.drawTitle("Itens do Orçamento", fontSize = 13f)

        if (orcamento.itens.isEmpty()) {
            generator.drawKeyValue("Itens", "Nenhum item cadastrado.")
        } else {
            val headers = listOf("Tipo", "Descrição", "Qtd", "Valor Unit.", "Subtotal")

            val printWidth = generator.printableWidth
            val widths = listOf(
                printWidth * 0.18f, // Tipo
                printWidth * 0.38f, // Descrição
                printWidth * 0.10f, // Qtd
                printWidth * 0.17f, // Valor Unit.
                printWidth * 0.17f  // Subtotal
            )

            val alignments = listOf(
                Paint.Align.LEFT,
                Paint.Align.LEFT,
                Paint.Align.CENTER,
                Paint.Align.RIGHT,
                Paint.Align.RIGHT
            )

            val rows = orcamento.itens.map { item ->
                listOf(
                    item.tipo,
                    item.descricao,
                    item.quantidade.toString(),
                    moeda(item.valorUnitario),
                    moeda(item.subtotal)
                )
            }

            generator.drawTable(
                headers = headers,
                rows = rows,
                columnWidths = widths,
                alignments = alignments
            )
        }

        generator.drawLine()

        // 4. Totals Summary
        generator.drawSpacer(4f)
        generator.drawTitle(
            title = "VALOR TOTAL: ${moeda(orcamento.total)}",
            fontSize = 15f,
            isBold = true,
            color = Color.rgb(30, 60, 110)
        )

        // 5. Save to File
        return generator.saveToFile(outputFile)
    }
}
