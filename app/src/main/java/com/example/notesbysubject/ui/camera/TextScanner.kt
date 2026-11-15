package com.example.notesbysubject.ui.camera

import android.content.Context
import android.net.Uri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.io.File
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.suspendCancellableCoroutine

object TextScanner {

    suspend fun scanImage(context: Context, photoFile: File): List<RecognizedLine> =
        suspendCancellableCoroutine { continuation ->
            try {
                val inputImage = InputImage.fromFilePath(context, Uri.fromFile(photoFile))
                val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

                recognizer.process(inputImage)
                    .addOnSuccessListener { visionText ->
                        val lines = mutableListOf<RecognizedLine>()
                        for (block in visionText.textBlocks) {
                            for (line in block.lines) {
                                val text = line.text
                                val confidence = line.confidence
                                lines.add(RecognizedLine(text, confidence))
                            }
                        }
                        recognizer.close()
                        if (continuation.isActive) {
                            continuation.resume(lines)
                        }
                    }
                    .addOnFailureListener { e ->
                        recognizer.close()
                        if (continuation.isActive) {
                            continuation.resumeWithException(e)
                        }
                    }
            } catch (e: Exception) {
                if (continuation.isActive) {
                    continuation.resumeWithException(e)
                }
            }
        }
}
