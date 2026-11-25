package com.example.notesbysubject

import android.database.sqlite.SQLiteConstraintException
import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import com.example.notesbysubject.data.local.LevelMateDatabase
import com.example.notesbysubject.data.local.Material
import com.example.notesbysubject.data.local.Photo
import com.example.notesbysubject.data.local.Project
import com.example.notesbysubject.data.local.ProjectRepository
import com.example.notesbysubject.data.local.Step
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class ProjectDatabaseTest {
    private lateinit var database: LevelMateDatabase

    @Before fun open() {
        database = Room.inMemoryDatabaseBuilder(
            InstrumentationRegistry.getInstrumentation().targetContext, LevelMateDatabase::class.java
        ).build()
    }

    @After fun close() = database.close()

    @Test fun emptyDatabaseAndMissingProject() = runBlocking {
        assertTrue(database.projectDao().observeProjects().first().isEmpty())
        assertNull(database.projectDao().getProject(99))
    }

    @Test fun deletingProjectCascadesContentsAndKeepsPhotosAsGeneral() = runBlocking {
        val dao = database.projectDao()
        val id = dao.insertProject(Project(name = "Shelf"))
        dao.insertMaterial(Material(projectId = id, name = "Wood", quantity = "2 m"))
        dao.insertStep(Step(projectId = id, text = "Cut", position = 0))
        dao.insertPhoto(Photo(projectId = id, path = "/notes/photo.jpg"))
        assertEquals(1, dao.getProject(id)!!.materials.size)
        dao.deleteProject(id)
        assertNull(dao.getProject(id))
        assertNull(dao.observeGeneralPhotos().first().single().projectId)
        database.openHelper.readableDatabase.query("SELECT COUNT(*) FROM steps").use {
            assertTrue(it.moveToFirst()); assertEquals(0, it.getInt(0))
        }
        database.openHelper.readableDatabase.query("SELECT COUNT(*) FROM materials").use {
            assertTrue(it.moveToFirst()); assertEquals(0, it.getInt(0))
        }
    }

    @Test fun invalidForeignKeyIsRejected() = runBlocking {
        try {
            database.projectDao().insertStep(Step(projectId = 99, text = "Orphan"))
            fail("An orphan step must be rejected")
        } catch (_: SQLiteConstraintException) { }
    }

    @Test fun invalidTemplateLeavesNoPartialProject() = runBlocking {
        try {
            ProjectRepository(database).createWithContents("Shelf", "", null, listOf("Wood" to ""), emptyList())
            fail("Empty quantity must be rejected")
        } catch (_: IllegalArgumentException) { }
        assertTrue(database.projectDao().observeProjects().first().isEmpty())
    }

    @Test fun completedProjectsDoNotGenerateReminders() = runBlocking {
        val dao = database.projectDao()
        val id = dao.insertProject(Project(name = "Shelf", deadline = "2026-11-22"))
        val stepId = dao.insertStep(Step(projectId = id, text = "Finish"))
        assertEquals(1, dao.dueProjects("2026-11-22").size)
        dao.setStepDone(stepId, true)
        assertTrue(dao.dueProjects("2026-11-22").isEmpty())
    }
}
