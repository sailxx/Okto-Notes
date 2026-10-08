package com.okto.notes.data

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.webkit.MimeTypeMap
import java.io.File
import java.util.UUID

/** Копирует выбранные файлы во внутреннюю память приложения, чтобы вложения не зависели от источника. */
class Attachments(private val context: Context) {
    private val dir = File(context.filesDir, "attachments").apply { mkdirs() }

    fun file(a: Attachment) = File(dir, a.file)

    /** Копирует [uri] в хранилище; null — если файл не удалось прочитать. */
    fun import(uri: Uri): Attachment? {
    return try {
        val cr = context.contentResolver
        var name = ""
        cr.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use {
            if (it.moveToFirst()) name = it.getString(0).orEmpty()
        }
        val mime = cr.getType(uri)
            ?: MimeTypeMap.getSingleton().getMimeTypeFromExtension(name.substringAfterLast('.', "").lowercase())
            ?: "application/octet-stream"
        if (name.isBlank()) {
            val ext = MimeTypeMap.getSingleton().getExtensionFromMimeType(mime)
            name = "file" + (ext?.let { ".$it" } ?: "")
        }
        val stored = UUID.randomUUID().toString()
        val out = File(dir, stored)
        cr.openInputStream(uri)?.use { input -> out.outputStream().use { input.copyTo(it) } } ?: return null
        Attachment(stored, name, mime, out.length())
    } catch (e: Exception) {
        null
    }
    }

    /** Удаляет файлы, на которые не ссылается ни одна запись. */
    fun cleanup(entries: List<Entry>) {
        val used = entries.flatMap { it.attachments }.map { it.file }.toSet()
        dir.listFiles()?.forEach { if (it.name !in used) it.delete() }
    }
}
