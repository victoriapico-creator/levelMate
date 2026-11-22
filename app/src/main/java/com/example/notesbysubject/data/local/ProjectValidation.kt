package com.example.notesbysubject.data.local

import com.example.notesbysubject.ui.screens.validateDeadline
import com.example.notesbysubject.ui.screens.validateDescription
import com.example.notesbysubject.ui.screens.validateName
import java.time.LocalDate
import java.time.format.DateTimeFormatter

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
