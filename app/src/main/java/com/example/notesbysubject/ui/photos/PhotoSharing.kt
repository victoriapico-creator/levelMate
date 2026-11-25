package com.example.notesbysubject.ui.photos

import android.content.ClipData
import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import java.io.File

fun sharePhoto(context: Context, path: String) {
    val file = File(path).canonicalFile
    val notes = File(context.filesDir, "notes").canonicalFile
    require(file.isFile && file.toPath().startsWith(notes.toPath())) { "This photo is no longer available" }
    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = if (file.extension.equals("png", ignoreCase = true)) "image/png" else "image/jpeg"
        putExtra(Intent.EXTRA_STREAM, uri)
        clipData = ClipData.newRawUri("Project photo", uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(intent, "Share photo"))
}
