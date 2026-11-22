package com.example.notesbysubject.data.sharing

import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File

class ProjectTransferFiles(private val context: Context) {
    fun read(uri: Uri): TransferProject {
        val bytes = context.contentResolver.openInputStream(uri)?.use { readProjectFileBytes(it) }
            ?: throw ProjectFileException("Could not open the selected file.")
        return ProjectFileCodec.decode(bytes)
    }

    fun save(uri: Uri, text: String) {
        val stream = context.contentResolver.openOutputStream(uri, "wt")
            ?: throw ProjectFileException("Could not create the project file.")
        stream.use { it.write(text.encodeToByteArray()) }
    }

    fun prepareShare(text: String): File {
        val folder = File(context.filesDir, "notes/project_exports")
        check(folder.isDirectory || folder.mkdirs()) { "Could not create the export folder" }
        return File.createTempFile("project_", ".levelmate.json", folder).also { it.writeText(text, Charsets.UTF_8) }
    }
}

fun shareProjectFile(context: Context, file: File) {
    val folder = File(context.filesDir, "notes/project_exports").canonicalFile
    val actual = file.canonicalFile
    require(actual.isFile && actual.toPath().startsWith(folder.toPath())) { "Project file is no longer available" }
    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", actual)
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "application/json"
        putExtra(Intent.EXTRA_STREAM, uri)
        clipData = ClipData.newRawUri("LevelMate project", uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(intent, "Share project file"))
}
