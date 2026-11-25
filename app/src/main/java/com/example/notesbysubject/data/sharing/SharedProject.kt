package com.example.notesbysubject.data.sharing

import com.example.notesbysubject.data.local.ProjectWithDetails
import com.example.notesbysubject.data.local.validateMaterial
import com.example.notesbysubject.data.local.validateProject
import com.example.notesbysubject.data.local.validateStep

data class SharedMaterial(val name: String, val quantity: String)
data class SharedStep(val text: String, val done: Boolean, val position: Int)
data class SharedProject(
    val id: String,
    val ownerUid: String,
    val memberUids: List<String>,
    val name: String,
    val description: String,
    val deadline: String?,
    val materials: List<SharedMaterial>,
    val steps: List<SharedStep>
)

fun sharedProjectPayload(project: ProjectWithDetails, ownerUid: String, members: List<String>): Map<String, Any?> = mapOf(
    "ownerUid" to ownerUid,
    "memberUids" to members,
    "name" to project.project.name,
    "description" to project.project.description,
    "deadline" to project.project.deadline,
    "materials" to project.materials.sortedBy { it.id }.map { mapOf("name" to it.name, "quantity" to it.quantity) },
    "steps" to project.steps.sortedWith(compareBy({ it.position }, { it.id }))
        .mapIndexed { index, step -> mapOf("text" to step.text, "done" to step.done, "position" to index) }
)

fun parseSharedProject(id: String, data: Map<String, Any?>): SharedProject {
    val name = data["name"] as? String ?: error("Missing project name")
    val description = data["description"] as? String ?: error("Missing description")
    val deadline = data["deadline"]?.let { it as? String ?: error("Invalid deadline") }
    validateProject(name, description, deadline)
    val owner = data["ownerUid"] as? String ?: error("Missing owner")
    val members = (data["memberUids"] as? List<*>)?.map { it as? String ?: error("Invalid member") }
        ?: error("Missing members")
    require(owner.isNotBlank() && owner.length <= 128 && owner in members && members.size in 1..10)
    require(members.all { it.isNotBlank() && it.length <= 128 } && members.distinct().size == members.size) {
        "Invalid project participants"
    }
    val rawMaterials = data["materials"] as? List<*> ?: error("Missing materials")
    val rawSteps = data["steps"] as? List<*> ?: error("Missing steps")
    require(rawMaterials.size <= 50 && rawSteps.size <= 100)
    val materials = rawMaterials.map { raw ->
        val map = raw as? Map<*, *> ?: error("Invalid material")
        val material = SharedMaterial(map["name"] as? String ?: error("Invalid material name"), map["quantity"] as? String ?: error("Invalid quantity"))
        validateMaterial(material.name, material.quantity)
        material
    }
    val steps = rawSteps.map { raw ->
        val map = raw as? Map<*, *> ?: error("Invalid step")
        val position = map["position"] as? Number ?: error("Invalid step position")
        require(position.toDouble() in 0.0..99.0 && position.toDouble() == position.toInt().toDouble())
        val step = SharedStep(map["text"] as? String ?: error("Invalid step text"), map["done"] as? Boolean ?: error("Invalid done value"), position.toInt())
        validateStep(step.text)
        step
    }
    require(steps.map { it.position }.distinct().size == steps.size) { "Duplicate step positions" }
    return SharedProject(id, owner, members, name, description, deadline, materials, steps.sortedBy { it.position })
}
