package com.example.notesbysubject.data.sharing

import com.example.notesbysubject.data.local.ProjectWithDetails
import com.example.notesbysubject.data.local.validateMaterial
import com.example.notesbysubject.data.local.validateProject
import com.example.notesbysubject.data.local.validateStep
import java.io.ByteArrayOutputStream
import java.io.InputStream
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

const val MAX_PROJECT_FILE_BYTES = 256 * 1024
private const val PROJECT_FILE_FORMAT = "levelmate.project"

@Serializable
data class ProjectFile(val format: String, val version: Int, val project: TransferProject)

@Serializable
data class TransferProject(
    val name: String,
    val description: String = "",
    val deadline: String? = null,
    val materials: List<TransferMaterial> = emptyList(),
    val steps: List<TransferStep> = emptyList()
) {
    fun validate() {
        validateProject(name, description, deadline)
        require(materials.size <= 50 && steps.size <= 100) { "Too many materials or steps" }
        materials.forEach { validateMaterial(it.name, it.quantity) }
        steps.forEach {
            validateStep(it.text)
            require(it.position in 0..99) { "Invalid step position" }
        }
        require(steps.map { it.position }.distinct().size == steps.size) { "Duplicate step positions" }
    }
}

@Serializable
data class TransferMaterial(val name: String, val quantity: String)

@Serializable
data class TransferStep(val text: String, val done: Boolean = false, val position: Int)

class ProjectFileException(message: String, cause: Throwable? = null) : IllegalArgumentException(message, cause)

object ProjectFileCodec {
    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true; prettyPrint = true }

    fun encode(project: ProjectWithDetails): String {
        val transfer = TransferProject(
            name = project.project.name, description = project.project.description, deadline = project.project.deadline,
            materials = project.materials.sortedBy { it.id }.map { TransferMaterial(it.name, it.quantity) },
            steps = project.steps.sortedWith(compareBy({ it.position }, { it.id }))
                .mapIndexed { index, step -> TransferStep(step.text, step.done, index) }
        )
        transfer.validate()
        val text = json.encodeToString(ProjectFile(PROJECT_FILE_FORMAT, 1, transfer))
        require(text.encodeToByteArray().size <= MAX_PROJECT_FILE_BYTES) { "Project file is too large" }
        return text
    }

    fun decode(bytes: ByteArray): TransferProject {
        if (bytes.isEmpty()) throw ProjectFileException("The project file is empty.")
        if (bytes.size > MAX_PROJECT_FILE_BYTES) throw ProjectFileException("Choose a project file smaller than 256 KB.")
        val text = try { bytes.decodeToString(throwOnInvalidSequence = true) }
        catch (error: Exception) { throw ProjectFileException("The file does not contain valid text.", error) }
        checkDepth(text)
        val file = try { json.decodeFromString<ProjectFile>(text) }
        catch (error: Exception) { throw ProjectFileException("Choose a valid LevelMate project file.", error) }
        if (file.format != PROJECT_FILE_FORMAT) throw ProjectFileException("This is not a LevelMate project file.")
        if (file.version != 1) throw ProjectFileException("This project file uses an unsupported version.")
        try { file.project.validate() }
        catch (error: Exception) { throw ProjectFileException("The project contains invalid names, dates, materials or steps.", error) }
        return file.project.copy(steps = file.project.steps.sortedBy { it.position })
    }

    private fun checkDepth(text: String) {
        var depth = 0
        var inString = false
        var escaped = false
        for (character in text) {
            if (inString) {
                if (escaped) escaped = false
                else if (character == '\\') escaped = true
                else if (character == '"') inString = false
            } else {
                when (character) {
                    '"' -> inString = true
                    '{', '[' -> { depth++; if (depth > 20) throw ProjectFileException("The project file is too complex.") }
                    '}', ']' -> depth--
                }
            }
        }
    }
}

fun readProjectFileBytes(stream: InputStream): ByteArray {
    val output = ByteArrayOutputStream()
    val buffer = ByteArray(8192)
    while (true) {
        val count = stream.read(buffer)
        if (count < 0) break
        if (count == 0) {
            val next = stream.read()
            if (next < 0) break
            if (output.size() >= MAX_PROJECT_FILE_BYTES) throw ProjectFileException("Choose a project file smaller than 256 KB.")
            output.write(next)
        } else {
            if (output.size() + count > MAX_PROJECT_FILE_BYTES) throw ProjectFileException("Choose a project file smaller than 256 KB.")
            output.write(buffer, 0, count)
        }
    }
    return output.toByteArray()
}

fun projectFileName(name: String): String {
    val stem = name.map { if (it in "\\/:*?\"<>|" || it.isISOControl()) '_' else it }
        .joinToString("").trim(' ', '.').take(60).ifBlank { "project" }
    return "$stem.levelmate.json"
}
