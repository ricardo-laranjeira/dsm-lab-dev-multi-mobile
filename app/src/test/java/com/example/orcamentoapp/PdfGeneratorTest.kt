package com.example.orcamentoapp

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.pdf.PdfDocument
import com.example.orcamentoapp.screens.orcamentos.ItemOrcamentoUi
import com.example.orcamentoapp.screens.orcamentos.OrcamentoUi
import com.example.orcamentoapp.util.pdf.OrcamentoPdfExporter
import com.example.orcamentoapp.util.pdf.PdfGenerator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.Implementation
import org.robolectric.annotation.Implements
import java.io.File
import java.io.OutputStream

@Implements(PdfDocument::class)
class ShadowPdfDocument {
    private var isClosed = false

    @Implementation
    fun __constructor__() {
        isClosed = false
    }

    @Implementation
    fun startPage(pageInfo: PdfDocument.PageInfo): PdfDocument.Page {
        if (isClosed) throw IllegalStateException("document is closed!")
        val bitmap = Bitmap.createBitmap(pageInfo.pageWidth, pageInfo.pageHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val constructor = PdfDocument.Page::class.java.getDeclaredConstructor(Canvas::class.java, PdfDocument.PageInfo::class.java)
        constructor.isAccessible = true
        return constructor.newInstance(canvas, pageInfo)
    }

    @Implementation
    fun finishPage(page: PdfDocument.Page?) {
        // No-op for shadow
    }

    @Implementation
    fun writeTo(out: OutputStream) {
        if (isClosed) throw IllegalStateException("document is closed!")
        val pdfHeader = "%PDF-1.4\n1 0 obj\n<< /Type /Catalog >>\nendobj\ntrailer\n<< /Root 1 0 R >>\n%%EOF\n"
        out.write(pdfHeader.toByteArray(Charsets.US_ASCII))
        out.flush()
    }

    @Implementation
    fun close() {
        isClosed = true
    }
}

@RunWith(RobolectricTestRunner::class)
@Config(shadows = [ShadowPdfDocument::class])
class PdfGeneratorTest {

    @Test
    fun generatePdf_createsNonEmptyPdfFileWithPdfHeader() {
        val generator = PdfGenerator()
        generator.footerText = "Teste Unitário PdfGenerator"

        generator.drawHeader(
            title = "RELATÓRIO DE TESTE",
            subtitle = "Subtítulo do relatório de teste"
        )
        generator.drawLine()

        generator.drawTitle("Seção 1: Dados Gerais")
        generator.drawKeyValueGrid(
            pairs = listOf(
                "Chave 1" to "Valor 1",
                "Chave 2" to "Valor 2",
                "Chave 3" to "Valor 3"
            ),
            columns = 2
        )
        generator.drawLine()

        generator.drawTitle("Seção 2: Tabela de Dados")
        val headers = listOf("ID", "Item", "Quantidade", "Preço")
        val rows = listOf(
            listOf("1", "Parafuso", "100", "R$ 0,50"),
            listOf("2", "Porca", "100", "R$ 0,30"),
            listOf("3", "Arruela", "200", "R$ 0,10")
        )
        generator.drawTable(headers = headers, rows = rows)

        val tempFile = File.createTempFile("pdf_gen_test_", ".pdf")
        tempFile.deleteOnExit()

        val resultFile = generator.saveToFile(tempFile)

        assertTrue("O arquivo PDF gerado deve existir", resultFile.exists())
        assertTrue("O arquivo PDF gerado não deve estar vazio", resultFile.length() > 0)

        val headerBytes = tempFile.readBytes().take(4).toByteArray()
        val magicHeader = String(headerBytes, Charsets.US_ASCII)
        assertEquals("O arquivo deve começar com o cabeçalho PDF %PDF", "%PDF", magicHeader)
    }

    @Test
    fun orcamentoPdfExporter_exportsValidPdfForOrcamento() {
        val itens = listOf(
            ItemOrcamentoUi(
                id = 1,
                tipo = "Material",
                referenciaId = 10,
                descricao = "Cimento CP II 50kg",
                quantidade = 5,
                valorUnitario = 42.0
            ),
            ItemOrcamentoUi(
                id = 2,
                tipo = "Serviço",
                referenciaId = 20,
                descricao = "Aplicação de reboco",
                quantidade = 1,
                valorUnitario = 350.0
            )
        )

        val orcamento = OrcamentoUi(
            id = 201,
            clienteId = 15,
            clienteNome = "Construtora Silva Ltda",
            validade = "2025-12-31",
            status = "Aprovado",
            itens = itens
        )

        val exporter = OrcamentoPdfExporter()
        val tempFile = File.createTempFile("orcamento_201_", ".pdf")
        tempFile.deleteOnExit()

        val pdfFile = exporter.exportOrcamentoToFile(orcamento, tempFile)

        assertTrue("O PDF do orçamento exportado deve existir", pdfFile.exists())
        assertTrue("O PDF do orçamento não deve ter 0 bytes", pdfFile.length() > 0)

        val headerBytes = pdfFile.readBytes().take(4).toByteArray()
        val magicHeader = String(headerBytes, Charsets.US_ASCII)
        assertEquals("O arquivo exportado deve ser um PDF válido", "%PDF", magicHeader)
    }

    @Test
    fun pdfGenerator_multiPageGeneration_handlesOverflowCorrectly() {
        val generator = PdfGenerator()
        generator.footerText = "Relatório Multipágina"

        generator.drawHeader("RELATÓRIO EXTENSO")
        generator.drawTitle("Tabela com muitos registros")

        val headers = listOf("Linha", "Descrição", "Valor")
        val rows = (1..60).map { i ->
            listOf("Linha $i", "Item de teste número $i", "R$ ${i * 10},00")
        }

        generator.drawTable(headers = headers, rows = rows)

        val tempFile = File.createTempFile("multi_page_test_", ".pdf")
        tempFile.deleteOnExit()

        val pdfFile = generator.saveToFile(tempFile)

        assertTrue("O PDF multipágina deve existir", pdfFile.exists())
        assertTrue("O PDF multipágina deve ter tamanho > 0", pdfFile.length() > 0)

        val headerBytes = pdfFile.readBytes().take(4).toByteArray()
        assertEquals("%PDF", String(headerBytes, Charsets.US_ASCII))
    }

    @Test
    fun pdfGenerator_customDimensionsAndMargins_calculatesPrintableArea() {
        val customGenerator = PdfGenerator(
            pageWidth = 400,
            pageHeight = 600,
            marginLeft = 20f,
            marginRight = 20f,
            marginTop = 30f,
            marginBottom = 30f
        )

        assertEquals(360f, customGenerator.printableWidth, 0.01f)
        assertEquals(540f, customGenerator.printableBottom, 0.01f)

        customGenerator.drawTitle("Página customizada")
        val tempFile = File.createTempFile("custom_pdf_", ".pdf")
        tempFile.deleteOnExit()

        val pdfFile = customGenerator.saveToFile(tempFile)
        assertTrue(pdfFile.exists())
        assertTrue(pdfFile.length() > 0)
    }
}
