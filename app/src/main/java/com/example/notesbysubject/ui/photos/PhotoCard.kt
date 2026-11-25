package com.example.notesbysubject.ui.photos

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.notesbysubject.data.local.Photo
import java.io.File
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun PhotoCard(photo: Photo, onAssign: (() -> Unit)? = null, enabled: Boolean = true) {
    val context = LocalContext.current
    val file = File(photo.path)
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            if (file.isFile) {
                AsyncImage(
                    model = file, contentDescription = "Saved project photo",
                    modifier = Modifier.fillMaxWidth().height(180.dp), contentScale = ContentScale.Crop
                )
            } else { Text("Photo file is missing") }
            Text(file.name)
            Text(Instant.ofEpochMilli(photo.takenAt).atZone(ZoneId.systemDefault())
                .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")))
            OutlinedButton(enabled = file.isFile, onClick = {
                try { sharePhoto(context, photo.path) }
                catch (_: Exception) { Toast.makeText(context, "Could not share this photo. Check that it exists and a sharing app is installed.", Toast.LENGTH_LONG).show() }
            }) { Text("Share photo") }
            if (onAssign != null) {
                OutlinedButton(onClick = onAssign, enabled = enabled && file.isFile) { Text("Add to this project") }
            }
        }
    }
}
