package com.jordydev.cashflow.util

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Build
import android.os.Environment
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.jordydev.cashflow.data.local.TransactionEntity
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
fun exportTransactionsToPdf(context: Context, transactions: List<TransactionEntity>) {
    if (transactions.isEmpty()) {
        Toast.makeText(context, "No transactions to export.", Toast.LENGTH_SHORT).show()
        return
    }

    val pdfDocument = PdfDocument()
    val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
    val page = pdfDocument.startPage(pageInfo)
    val canvas = page.canvas

    val paint = Paint().apply { textSize = 14f }
    var y = 40f

    paint.isFakeBoldText = true
    canvas.drawText("CashFlow - Exported Transactions", 40f, y, paint)
    paint.isFakeBoldText = false
    y += 30f

    transactions.forEachIndexed { index, txn ->
        val text = "${index + 1}. ${txn.title} - $${txn.amount} ${txn.type.name} on ${txn.date}"
        canvas.drawText(text, 40f, y, paint)
        y += 24f
    }

    pdfDocument.finishPage(page)

    val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))
    val fileName = "CashFlow_Transactions_$timestamp.pdf"

    val contentValues = android.content.ContentValues().apply {
        put(android.provider.MediaStore.Downloads.DISPLAY_NAME, fileName)
        put(android.provider.MediaStore.Downloads.MIME_TYPE, "application/pdf")
        put(android.provider.MediaStore.Downloads.IS_PENDING, 1)
    }

    val resolver = context.contentResolver
    val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        android.provider.MediaStore.Downloads.getContentUri(android.provider.MediaStore.VOLUME_EXTERNAL_PRIMARY)
    } else {
        TODO("VERSION.SDK_INT < Q")
    }
    val uri = resolver.insert(collection, contentValues)

    try {
        uri?.let {
            resolver.openOutputStream(it)?.use { outStream ->
                pdfDocument.writeTo(outStream)
            }

            contentValues.clear()
            contentValues.put(android.provider.MediaStore.Downloads.IS_PENDING, 0)
            resolver.update(uri, contentValues, null, null)

            Toast.makeText(context, "✅ PDF exported to Downloads", Toast.LENGTH_LONG).show()
        } ?: run {
            Toast.makeText(context, "❌ Failed to create file", Toast.LENGTH_LONG).show()
        }
    } catch (e: Exception) {
        Toast.makeText(context, "❌ Export failed: ${e.message}", Toast.LENGTH_LONG).show()
    }

    pdfDocument.close()
}

