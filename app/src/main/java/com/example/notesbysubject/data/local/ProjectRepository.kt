package com.example.notesbysubject.data.local

import androidx.room.withTransaction
import com.example.notesbysubject.ui.screens.validateDeadline
import com.example.notesbysubject.ui.screens.validateDescription
import com.example.notesbysubject.ui.screens.validateName
import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class ProjectRepository(private val database: LevelMateDatabase) {
    val dao = database.projectDao()
    val projects = dao.observeProjects()
    val generalPhotos = dao.observeGeneralPhotos()

    suspend fun create(name: String, description: String, deadline: String?): Long {
        validateProject(name, description, deadline)
        return dao.insertProject(Project(name = name.trim(), description = description.trim(), deadline = deadline))
    }

    suspend fun update(project: Project) {
        validateProject(project.name, project.description, project.deadline)
        check(dao.updateProject(project.copy(name = project.name.trim(), description = project.description.trim())) == 1) {
            "Project no longer exists"
        }
    }

    suspend fun createWithContents(
        name: String, description: String, deadline: String?,
        materials: List<Pair<String, String>>, steps: List<Pair<String, Boolean>>
    ): Long = database.withTransaction {
        validateProject(name, description, deadline)
        require(materials.size <= 50 && steps.size <= 100) { "Too many materials or steps" }
        materials.forEach { (name, quantity) -> validateMaterial(name, quantity) }
        steps.forEach { (text, _) -> validateStep(text) }
        val id = create(name, description, deadline)
        materials.forEach { (name, quantity) -> dao.insertMaterial(Material(projectId = id, name = name.trim(), quantity = quantity.trim())) }
        steps.forEachIndexed { index, (text, done) -> dao.insertStep(Step(projectId = id, text = text.trim(), done = done, position = index)) }
        id
    }

    suspend fun addMaterial(projectId: Long, name: String, quantity: String) = database.withTransaction {
        validateMaterial(name, quantity)
        val project = checkNotNull(dao.getProject(projectId)) { "Project no longer exists" }
        require(project.materials.size < 50) { "A project can have at most 50 materials" }
        dao.insertMaterial(Material(projectId = projectId, name = name.trim(), quantity = quantity.trim()))
    }

    suspend fun addStep(projectId: Long, text: String) = database.withTransaction {
        validateStep(text)
        val project = checkNotNull(dao.getProject(projectId)) { "Project no longer exists" }
        require(project.steps.size < 100) { "A project can have at most 100 steps" }
        val position = (project.steps.maxOfOrNull { it.position } ?: -1) + 1
        dao.insertStep(Step(projectId = projectId, text = text.trim(), position = position))
    }

    suspend fun addPhoto(file: File, projectId: Long?, notesDirectory: File): Long = database.withTransaction {
        val canonicalFile = file.canonicalFile
        require(canonicalFile.isFile && canonicalFile.toPath().startsWith(notesDirectory.canonicalFile.toPath())) {
            "The photo must exist in the notes folder"
        }
        val existingProject = projectId?.takeIf { dao.getProject(it) != null }
        dao.insertPhoto(Photo(projectId = existingProject, path = canonicalFile.path, takenAt = canonicalFile.lastModified()))
    }

    suspend fun importExistingPhotos(notesDirectory: File) {
        if (!notesDirectory.isDirectory) return
        notesDirectory.walkTopDown().filter { it.isFile && it.extension.lowercase() in setOf("jpg", "jpeg", "png") }
            .forEach { addPhoto(it, null, notesDirectory) }
    }
}

fun deadlineFromForm(value: String): String? {
    require(validateDeadline(value) == null) { "Invalid deadline" }
    return value.trim().takeIf { it.isNotEmpty() }?.let {
        LocalDate.parse(it, DateTimeFormatter.ofPattern("dd/MM/uuuu")).toString()
    }
}

fun deadlineForForm(value: String?): String = value?.let {
    LocalDate.parse(it).format(DateTimeFormatter.ofPattern("dd/MM/uuuu"))
} ?: ""

fun validateProject(name: String, description: String, deadline: String?) {
    require(validateName(name) == null) { validateName(name) ?: "Invalid name" }
    require(validateDescription(description) == null) { "Description must be 200 characters or less" }
    if (deadline != null) {
        require(LocalDate.parse(deadline).toString() == deadline) { "Deadline must use yyyy-MM-dd" }
    }
}

fun validateMaterial(name: String, quantity: String) {
    require(name.isNotBlank() && name.length <= 80) { "Material name must contain 1 to 80 characters" }
    require(quantity.isNotBlank() && quantity.length <= 40) { "Quantity must contain 1 to 40 characters, including units" }
}

fun validateStep(text: String) {
    require(text.isNotBlank() && text.length <= 300) { "Step must contain 1 to 300 characters" }
}
