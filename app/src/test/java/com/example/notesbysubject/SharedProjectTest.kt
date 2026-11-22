package com.example.notesbysubject

import com.example.notesbysubject.data.local.Material
import com.example.notesbysubject.data.local.Photo
import com.example.notesbysubject.data.local.Project
import com.example.notesbysubject.data.local.ProjectWithDetails
import com.example.notesbysubject.data.local.Step
import com.example.notesbysubject.data.sharing.parseSharedProject
import com.example.notesbysubject.data.sharing.sharedProjectPayload
import org.junit.Assert.*
import org.junit.Test

class SharedProjectTest {
    private val project = ProjectWithDetails(
        Project(1, "Shelf", "", null),
        listOf(Material(1, 1, "Wood", "2 m")),
        listOf(Step(1, 1, "Cut", true, 0)),
        listOf(Photo(1, 1, "/private/notes/photo.jpg", 1))
    )

    @Test fun sharedSnapshotHasNoLocalIdsOrPhotoPaths() {
        val payload = sharedProjectPayload(project, "owner", listOf("owner", "reader"))
        assertFalse(payload.containsKey("photos"))
        assertFalse(payload.toString().contains("/private/"))
        assertFalse(payload.toString().contains("projectId"))
        val parsed = parseSharedProject("shared-id", payload)
        assertTrue(parsed.steps.single().done)
        assertEquals("2 m", parsed.materials.single().quantity)
        assertNull(parsed.deadline)
    }

    @Test fun malformedRemoteDataIsRejected() {
        val payload = sharedProjectPayload(project, "owner", listOf("owner")) + ("steps" to listOf(mapOf("text" to "Cut", "done" to "not a boolean", "position" to 0)))
        assertThrows(Exception::class.java) { parseSharedProject("bad", payload) }
    }

    @Test fun ownerMustBeAMember() {
        val payload = sharedProjectPayload(project, "owner", listOf("someone-else"))
        assertThrows(IllegalArgumentException::class.java) { parseSharedProject("bad", payload) }
    }
}
