package com.example.notesbysubject

import com.example.notesbysubject.data.local.deadlineForForm
import com.example.notesbysubject.data.local.deadlineFromForm
import com.example.notesbysubject.data.local.validateProject
import com.example.notesbysubject.ui.screens.validateDeadline
import com.example.notesbysubject.ui.screens.validateName
import org.junit.Assert.*
import org.junit.Test

class ProjectValidationTest {
    @Test fun dateConversionPreservesOptionalAndLeapDates() {
        assertNull(deadlineFromForm("   "))
        assertEquals("", deadlineForForm(null))
        assertEquals("2028-02-29", deadlineFromForm("29/02/2028"))
        assertEquals("29/02/2028", deadlineForForm("2028-02-29"))
    }

    @Test fun impossibleDatesAreRejected() {
        listOf("31/02/2026", "29/02/2027", "abc").forEach {
            assertNotNull(validateDeadline(it))
            assertThrows(IllegalArgumentException::class.java) { deadlineFromForm(it) }
        }
    }

    @Test fun projectNameBoundaries() {
        assertNotNull(validateName("   "))
        assertNull(validateName("x".repeat(40)))
        assertNotNull(validateName("x".repeat(41)))
        assertThrows(IllegalArgumentException::class.java) { validateProject("", "", null) }
    }
}
