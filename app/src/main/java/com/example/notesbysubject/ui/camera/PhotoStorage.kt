package com.example.notesbysubject.ui.camera

import android.content.Context
import androidx.camera.core.ImageCapture
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PhotoStorage {

    fun createPhotoFile(context: Context, projectId: Long? = null): File {
        require(projectId == null || projectId > 0) { "Invalid project id" }
        val folder = projectId?.let { "notes/projects/$it" } ?: "notes/general"
        val notesDir = File(context.filesDir, folder)
        check(notesDir.isDirectory || notesDir.mkdirs()) { "Could not create the photo folder" }
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        return File.createTempFile("photo_${timestamp}_", ".jpg", notesDir)
    }

    fun createOutputFileOptions(photoFile: File): ImageCapture.OutputFileOptions {
        return ImageCapture.OutputFileOptions.Builder(photoFile).build()
    }
}
