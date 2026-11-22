package com.example.orcamentoapp.util.pdf

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

/**
 * Core utility for generating PDF documents using [PdfDocument].
 *
 * Supports multi-page layouts, headers, section titles, key-value pairs,
 * data tables (with automatic header repetition across pages), horizontal divider lines,
 * custom footers, and page numbering.
 */
class PdfGenerator(
    val pageWidth: Int = 595,  // Standard A4 width in points (72 dpi)
    val pageHeight: Int = 842, // Standard A4 height in points (72 dpi)
    val marginLeft: Float = 36f,
    val marginRight: Float = 36f,
    val marginTop: Float = 40f,
    val marginBottom: Float = 40f
) {
    private val pdfDocument = PdfDocument()
    private var currentPage: PdfDocument.Page? = null
    private var canvas: Canvas? = null
    private var currentPageNumber = 0
    private var currentY = marginTop

    /** Custom footer text displayed at the bottom of pages. */
    var footerText: String? = null

    /** Printable width excluding left and right margins. */
    val printableWidth: Float
        get() = pageWidth - marginLeft - marginRight

    /** Bottom threshold for content area before triggering a page break. */
    val printableBottom: Float
        get() = pageHeight - marginBottom - 30f

    /**
     * Starts a new page in the PDF document.
     */
    fun startPage(): Canvas {
        currentPage?.let { finishCurrentPage(it) }
        currentPageNumber++
        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, currentPageNumber).create()
        val page = pdfDocument.startPage(pageInfo)
        currentPage = page
        val c = page.canvas
        canvas = c
        currentY = marginTop
        return c
    }

    private fun getCurrentCanvas(): Canvas {
        return canvas ?: startPage()
    }

    /**
     * Ensures vertical space is available on the current page; otherwise starts a new page.
     */
    fun ensureSpace(heightNeeded: Float) {
        if (canvas == null || currentY + heightNeeded > printableBottom) {
            startPage()
        }
    }

    /**
     * Draws a main document header with a title and optional subtitle.
     */
    fun drawHeader(
        title: String,
        subtitle: String? = null,
        titleColor: Int = Color.rgb(30, 60, 110),
        subtitleColor: Int = Color.DKGRAY
    ) {
        val titlePaint = Paint().apply {
            color = titleColor
            textSize = 20f
            isFakeBoldText = true
            isAntiAlias = true
        }
        val subtitlePaint = Paint().apply {
            color = subtitleColor
            textSize = 12f
            isAntiAlias = true
        }

        val heightNeeded = 30f + if (subtitle != null) 18f else 0f
        ensureSpace(heightNeeded)
        val c = getCurrentCanvas()

        c.drawText(title, marginLeft, currentY + 20f, titlePaint)
        currentY += 26f

        if (subtitle != null) {
            c.drawText(subtitle, marginLeft, currentY + 12f, subtitlePaint)
            currentY += 18f
        }
        currentY += 8f
    }

    /**
     * Draws a section title or heading.
     */
    fun drawTitle(
        title: String,
        fontSize: Float = 14f,
        isBold: Boolean = true,
        color: Int = Color.BLACK,
        spaceAfter: Float = 6f
    ) {
        val paint = Paint().apply {
            this.color = color
            textSize = fontSize
            isFakeBoldText = isBold
            isAntiAlias = true
        }
        ensureSpace(fontSize + spaceAfter + 8f)
        val c = getCurrentCanvas()
        c.drawText(title, marginLeft, currentY + fontSize, paint)
        currentY += fontSize + spaceAfter
    }

    /**
     * Draws a key-value pair on a single line.
     */
    fun drawKeyValue(
        key: String,
        value: String,
        keyWidth: Float? = null,
        fontSize: Float = 11f,
        keyColor: Int = Color.DKGRAY,
        valueColor: Int = Color.BLACK
    ) {
        val keyPaint = Paint().apply {
            color = keyColor
            textSize = fontSize
            isFakeBoldText = true
            isAntiAlias = true
        }
        val valuePaint = Paint().apply {
            color = valueColor
            textSize = fontSize
            isAntiAlias = true
        }

        ensureSpace(fontSize + 6f)
        val c = getCurrentCanvas()
        val formattedKey = if (key.endsWith(":")) key else "$key:"
        val calculatedKeyWidth = keyWidth ?: (keyPaint.measureText(formattedKey) + 8f)

        c.drawText(formattedKey, marginLeft, currentY + fontSize, keyPaint)
        c.drawText(value, marginLeft + calculatedKeyWidth, currentY + fontSize, valuePaint)
        currentY += fontSize + 6f
    }

    /**
     * Draws a multi-column grid of key-value pairs.
     */
    fun drawKeyValueGrid(
        pairs: List<Pair<String, String>>,
        columns: Int = 2,
        fontSize: Float = 10f
    ) {
        if (pairs.isEmpty()) return
        val colWidth = printableWidth / columns

        var col = 0

        for ((key, value) in pairs) {
            if (col == 0) {
                ensureSpace(fontSize + 8f)
            }
            val c = getCurrentCanvas()
            val startX = marginLeft + (col * colWidth)

            val keyPaint = Paint().apply {
                color = Color.DKGRAY
                textSize = fontSize
                isFakeBoldText = true
                isAntiAlias = true
            }
            val valPaint = Paint().apply {
                color = Color.BLACK
                textSize = fontSize
                isAntiAlias = true
            }

            val formattedKey = if (key.endsWith(":")) key else "$key:"
            val kWidth = keyPaint.measureText(formattedKey) + 6f

            c.drawText(formattedKey, startX, currentY + fontSize, keyPaint)
            c.drawText(value, startX + kWidth, currentY + fontSize, valPaint)

            col++
            if (col >= columns) {
                col = 0
                currentY += fontSize + 8f
            }
        }

        if (col != 0) {
            currentY += fontSize + 8f
        }
    }

    /**
     * Draws a horizontal line divider across the printable width.
     */
    fun drawLine(
        thickness: Float = 1f,
        color: Int = Color.LTGRAY,
        marginVertical: Float = 8f
    ) {
        ensureSpace(thickness + marginVertical * 2)
        val c = getCurrentCanvas()
        currentY += marginVertical
        val paint = Paint().apply {
            this.color = color
            strokeWidth = thickness
            isAntiAlias = true
        }
        c.drawLine(marginLeft, currentY, pageWidth - marginRight, currentY, paint)
        currentY += thickness + marginVertical
    }

    /**
     * Adds vertical space.
     */
    fun drawSpacer(height: Float = 10f) {
        currentY += height
    }

    /**
     * Draws a table with headers and data rows.
     * Automatically handles column width calculations, cell alignments, and page breaks.
     */
    fun drawTable(
        headers: List<String>,
        rows: List<List<String>>,
        columnWidths: List<Float>? = null,
        alignments: List<Paint.Align>? = null,
        headerBgColor: Int = Color.rgb(230, 235, 245),
        headerTextColor: Int = Color.rgb(30, 60, 110),
        fontSize: Float = 10f
    ) {
        if (headers.isEmpty()) return

        val numCols = headers.size
        val widths = columnWidths ?: List(numCols) { printableWidth / numCols }
        val aligns = alignments ?: List(numCols) { Paint.Align.LEFT }

        val headerHeight = fontSize + 14f
        val rowHeight = fontSize + 12f

        // Draw header
        drawTableHeader(headers, widths, aligns, headerHeight, headerBgColor, headerTextColor, fontSize)

        // Draw rows
        for ((rowIndex, row) in rows.withIndex()) {
            if (currentY + rowHeight > printableBottom) {
                startPage()
                // Redraw table header on new page
                drawTableHeader(headers, widths, aligns, headerHeight, headerBgColor, headerTextColor, fontSize)
            }

            val c = getCurrentCanvas()
            val bgPaint = Paint().apply {
                color = if (rowIndex % 2 == 0) Color.WHITE else Color.rgb(248, 249, 250)
            }
            c.drawRect(
                marginLeft,
                currentY,
                marginLeft + printableWidth,
                currentY + rowHeight,
                bgPaint
            )

            val textPaint = Paint().apply {
                color = Color.BLACK
                textSize = fontSize
                isAntiAlias = true
            }

            var currentX = marginLeft
            for (i in 0 until numCols) {
                val colWidth = widths[i]
                val text = row.getOrNull(i).orEmpty()
                val align = aligns.getOrElse(i) { Paint.Align.LEFT }
                textPaint.textAlign = align

                val drawX = when (align) {
                    Paint.Align.LEFT -> currentX + 6f
                    Paint.Align.RIGHT -> currentX + colWidth - 6f
                    Paint.Align.CENTER -> currentX + (colWidth / 2f)
                }

                c.drawText(text, drawX, currentY + fontSize + 4f, textPaint)
                currentX += colWidth
            }

            val linePaint = Paint().apply {
                color = Color.rgb(220, 220, 220)
                strokeWidth = 0.5f
            }
            c.drawLine(
                marginLeft,
                currentY + rowHeight,
                marginLeft + printableWidth,
                currentY + rowHeight,
                linePaint
            )

            currentY += rowHeight
        }

        currentY += 6f
    }

    private fun drawTableHeader(
        headers: List<String>,
        widths: List<Float>,
        aligns: List<Paint.Align>,
        headerHeight: Float,
        headerBgColor: Int,
        headerTextColor: Int,
        fontSize: Float
    ) {
        ensureSpace(headerHeight + 10f)
        val c = getCurrentCanvas()

        val bgPaint = Paint().apply { color = headerBgColor }
        c.drawRect(
            marginLeft,
            currentY,
            marginLeft + printableWidth,
            currentY + headerHeight,
            bgPaint
        )

        val textPaint = Paint().apply {
            color = headerTextColor
            textSize = fontSize
            isFakeBoldText = true
            isAntiAlias = true
        }

        var currentX = marginLeft
        for (i in headers.indices) {
            val colWidth = widths[i]
            val text = headers[i]
            val align = aligns.getOrElse(i) { Paint.Align.LEFT }
            textPaint.textAlign = align

            val drawX = when (align) {
                Paint.Align.LEFT -> currentX + 6f
                Paint.Align.RIGHT -> currentX + colWidth - 6f
                Paint.Align.CENTER -> currentX + (colWidth / 2f)
            }

            c.drawText(text, drawX, currentY + fontSize + 5f, textPaint)
            currentX += colWidth
        }

        currentY += headerHeight
    }

    private fun finishCurrentPage(page: PdfDocument.Page) {
        val c = page.canvas

        val linePaint = Paint().apply {
            color = Color.LTGRAY
            strokeWidth = 0.5f
        }
        val footerY = pageHeight - marginBottom
        c.drawLine(marginLeft, footerY, pageWidth - marginRight, footerY, linePaint)

        val footerPaint = Paint().apply {
            color = Color.GRAY
            textSize = 9f
            isAntiAlias = true
        }

        footerText?.let { text ->
            footerPaint.textAlign = Paint.Align.LEFT
            c.drawText(text, marginLeft, footerY + 16f, footerPaint)
        }

        footerPaint.textAlign = Paint.Align.RIGHT
        c.drawText("Página $currentPageNumber", pageWidth - marginRight, footerY + 16f, footerPaint)

        pdfDocument.finishPage(page)
    }

    /**
     * Finalizes the PDF and saves it to [file].
     */
    fun saveToFile(file: File): File {
        currentPage?.let { finishCurrentPage(it) }
        currentPage = null
        canvas = null

        file.parentFile?.mkdirs()
        FileOutputStream(file).use { out ->
            pdfDocument.writeTo(out)
        }
        pdfDocument.close()
        return file
    }

    /**
     * Finalizes the PDF and writes it to [outputStream].
     */
    fun writeToStream(outputStream: OutputStream) {
        currentPage?.let { finishCurrentPage(it) }
        currentPage = null
        canvas = null

        pdfDocument.writeTo(outputStream)
        pdfDocument.close()
    }
}
