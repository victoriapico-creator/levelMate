package com.example.notesbysubject.data.templates

import com.example.notesbysubject.data.local.validateMaterial
import com.example.notesbysubject.data.local.validateProject
import com.example.notesbysubject.data.local.validateStep
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

val templateJson = Json { ignoreUnknownKeys = true }

@Serializable
data class TemplateCatalog(val version: Int, val templates: List<ProjectTemplate>) {
    fun validated(): TemplateCatalog {
        require(version == 1) { "Unsupported template format" }
        require(templates.size <= 30) { "Too many templates" }
        require(templates.map { it.id }.distinct().size == templates.size) { "Duplicate template ids" }
        templates.forEach { it.validate() }
        return this
    }
}

@Serializable
data class ProjectTemplate(
    val id: String,
    val name: String,
    val description: String = "",
    val materials: List<TemplateMaterial> = emptyList(),
    val steps: List<String> = emptyList()
) {
    fun validate() {
        require(id.isNotBlank() && id.length <= 80) { "Invalid template id" }
        validateProject(name, description, null)
        require(materials.size <= 50 && steps.size <= 100) { "Template is too large" }
        materials.forEach { validateMaterial(it.name, it.quantity) }
        steps.forEach { validateStep(it) }
    }
}

@Serializable
data class TemplateMaterial(val name: String, val quantity: String)
