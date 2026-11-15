package com.example.notesbysubject.ui.camera

import android.content.Context
import androidx.camera.core.ImageCapture
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PhotoStorage {

    fun createPhotoFile(context: Context): File {
        val notesDir = File(context.filesDir, "notes/general")
        if (!notesDir.exists()) {
            notesDir.mkdirs()
        }
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val fileName = "photo_$timestamp.jpg"
        return File(notesDir, fileName)
    }

    fun createOutputFileOptions(photoFile: File): ImageCapture.OutputFileOptions {
        return ImageCapture.OutputFileOptions.Builder(photoFile).build()
    }
}
