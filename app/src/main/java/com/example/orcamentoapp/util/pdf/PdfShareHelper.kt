package com.example.orcamentoapp.util.pdf

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import java.io.File

/**
 * Helper utility for sharing PDF files with external applications using [Intent.ACTION_SEND]
 * and [FileProvider.getUriForFile].
 */
object PdfShareHelper {

    /**
     * Shares the given [pdfFile] via Android system share chooser.
     *
     * @param context Android context
     * @param pdfFile PDF file to share
     * @param title Title for the share chooser intent
     */
    fun sharePdf(context: Context, pdfFile: File, title: String = "Compartilhar Orçamento") {
        val authority = "${context.packageName}.fileprovider"
        val uri = FileProvider.getUriForFile(context, authority, pdfFile)

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, title)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        val chooser = Intent.createChooser(intent, title).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(chooser)
    }
}
