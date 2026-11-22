package com.example.notesbysubject

import com.example.notesbysubject.data.local.Material
import com.example.notesbysubject.data.local.Photo
import com.example.notesbysubject.data.local.Project
import com.example.notesbysubject.data.local.ProjectWithDetails
import com.example.notesbysubject.data.local.Step
import com.example.notesbysubject.data.sharing.MAX_PROJECT_FILE_BYTES
import com.example.notesbysubject.data.sharing.ProjectFileCodec
import com.example.notesbysubject.data.sharing.ProjectFileException
import com.example.notesbysubject.data.sharing.projectFileName
import com.example.notesbysubject.data.sharing.readProjectFileBytes
import java.io.ByteArrayInputStream
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import org.junit.Assert.*
import org.junit.Test

class ProjectFileTest {
    private val original = ProjectWithDetails(
        Project(42, "Estantería", "A small shelf", "2026-11-22"),
        listOf(Material(15, 42, "Wood", "2 m")),
        listOf(Step(9, 42, "Paint", false, 10), Step(8, 42, "Cut", true, 2)),
        listOf(Photo(7, 42, "/private/notes/photo.jpg", 123))
    )

    private fun file(project: String, version: Int = 1, format: String = "levelmate.project") =
        """{"format":"$format","version":$version,"project":$project}""".encodeToByteArray()

    @Test fun roundTripPreservesContentAndOrdersSteps() {
        val result = ProjectFileCodec.decode(ProjectFileCodec.encode(original).encodeToByteArray())
        assertEquals("Estantería", result.name)
        assertEquals("2026-11-22", result.deadline)
        assertEquals("2 m", result.materials.single().quantity)
        assertEquals(listOf("Cut", "Paint"), result.steps.map { it.text })
        assertEquals(listOf(true, false), result.steps.map { it.done })
        assertEquals(listOf(0, 1), result.steps.map { it.position })
    }

    @Test fun fileContainsNoPhotosLocalIdsPathsOrAccounts() {
        val text = ProjectFileCodec.encode(original)
        val json = Json.parseToJsonElement(text).jsonObject
        assertEquals(setOf("format", "version", "project"), json.keys)
        assertEquals(setOf("name", "description", "deadline", "materials", "steps"), json.getValue("project").jsonObject.keys)
        listOf("/private/", "projectId", "ownerUid", "memberUids", "\"photos\"", "\"id\"").forEach { assertFalse(text.contains(it)) }
    }

    @Test fun optionalDeadlineAndEmptyCollectionsAreSupported() {
        val project = original.copy(project = original.project.copy(deadline = null), materials = emptyList(), steps = emptyList())
        val decoded = ProjectFileCodec.decode(ProjectFileCodec.encode(project).encodeToByteArray())
        assertNull(decoded.deadline)
        assertTrue(decoded.materials.isEmpty())
        assertTrue(decoded.steps.isEmpty())
    }

    @Test fun unknownFieldsWithinSupportedVersionAreIgnored() {
        val text = ProjectFileCodec.encode(original).replaceFirst("{", "{\"futureField\":true,")
        assertEquals("Estantería", ProjectFileCodec.decode(text.encodeToByteArray()).name)
    }

    @Test fun unsupportedOrMissingHeadersAreRejected() {
        assertThrows(ProjectFileException::class.java) { ProjectFileCodec.decode(file("""{"name":"Shelf"}""", version = 2)) }
        assertThrows(ProjectFileException::class.java) { ProjectFileCodec.decode(file("""{"name":"Shelf"}""", format = "other.app")) }
        assertThrows(ProjectFileException::class.java) { ProjectFileCodec.decode("""{"project":{"name":"Shelf"}}""".encodeToByteArray()) }
    }

    @Test fun impossibleDatesAreRejectedBeforeImport() {
        assertThrows(ProjectFileException::class.java) { ProjectFileCodec.decode(file("""{"name":"Shelf","deadline":"2026-02-31"}""")) }
    }

    @Test fun missingNamesAndInvalidMaterialsAreRejected() {
        assertThrows(ProjectFileException::class.java) { ProjectFileCodec.decode(file("""{"name":" "}""")) }
        assertThrows(ProjectFileException::class.java) { ProjectFileCodec.decode(file("""{"name":"Shelf","materials":[{"name":"Wood","quantity":""}]}""")) }
    }

    @Test fun duplicateOrNegativeStepPositionsAreRejected() {
        assertThrows(ProjectFileException::class.java) {
            ProjectFileCodec.decode(file("""{"name":"Shelf","steps":[{"text":"A","position":0},{"text":"B","position":0}]}"""))
        }
        assertThrows(ProjectFileException::class.java) {
            ProjectFileCodec.decode(file("""{"name":"Shelf","steps":[{"text":"A","position":-1}]}"""))
        }
    }

    @Test fun tooManyStepsAreRejected() {
        val steps = (0..100).joinToString(",") { """{"text":"Step","position":$it}""" }
        assertThrows(ProjectFileException::class.java) { ProjectFileCodec.decode(file("""{"name":"Shelf","steps":[$steps]}""")) }
    }

    @Test fun oversizedStreamsAreRejectedWhileReading() {
        val bytes = ByteArray(MAX_PROJECT_FILE_BYTES + 1) { 32 }
        assertThrows(ProjectFileException::class.java) { readProjectFileBytes(ByteArrayInputStream(bytes)) }
        assertArrayEquals(byteArrayOf(1, 2, 3), readProjectFileBytes(ByteArrayInputStream(byteArrayOf(1, 2, 3))))
    }

    @Test fun emptyMalformedAndInvalidUtf8FilesAreRejected() {
        listOf(byteArrayOf(), "not json".encodeToByteArray(), byteArrayOf(0xc3.toByte(), 0x28)).forEach {
            assertThrows(ProjectFileException::class.java) { ProjectFileCodec.decode(it) }
        }
    }

    @Test fun excessiveNestingIsRejectedButBracketsInsideTextAreSafe() {
        val nested = "{\"extra\":" + "[".repeat(25) + "0" + "]".repeat(25) + "}"
        assertThrows(ProjectFileException::class.java) { ProjectFileCodec.decode(nested.encodeToByteArray()) }
        val project = original.copy(project = original.project.copy(description = "[".repeat(60) + "\\\"quoted\\\"" + "]".repeat(60)))
        assertEquals(project.project.description, ProjectFileCodec.decode(ProjectFileCodec.encode(project).encodeToByteArray()).description)
    }

    @Test fun suggestedFilenamesCannotContainPathSeparators() {
        val name = projectFileName("../folder/Bad:name?")
        assertFalse(name.contains('/'))
        assertFalse(name.contains('\\'))
        assertTrue(name.endsWith(".levelmate.json"))
        assertEquals("project.levelmate.json", projectFileName("..."))
    }
}
