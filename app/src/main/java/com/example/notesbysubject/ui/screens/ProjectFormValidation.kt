package com.example.notesbysubject.ui.screens

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.format.ResolverStyle

fun validateName(name: String): String? {
    if (name.isBlank()) {
        return "Name is required"
    }
    if (name.length > 40) {
        return "Name must be 40 characters or less"
    }
    return null
}

fun validateDescription(description: String): String? {
    if (description.length > 200) {
        return "Description must be 200 characters or less"
    }
    return null
}

fun validateDeadline(deadline: String): String? {
    if (deadline.isBlank()) {
        return null
    }
    return try {
        val formatter = DateTimeFormatter.ofPattern("dd/MM/uuuu")
            .withResolverStyle(ResolverStyle.STRICT)
        LocalDate.parse(deadline.trim(), formatter)
        null
    } catch (e: DateTimeParseException) {
        "Deadline must be a valid date in dd/MM/yyyy format"
    } catch (e: Exception) {
        "Deadline must be a valid date in dd/MM/yyyy format"
    }
}
