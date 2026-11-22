package com.example.notesbysubject

import com.example.notesbysubject.data.templates.TemplateCatalog
import com.example.notesbysubject.data.templates.TemplateRepository
import com.example.notesbysubject.data.templates.TemplateSource
import com.example.notesbysubject.data.templates.templateJson
import java.io.IOException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class TemplateRepositoryTest {
    private val recorded = """{"note":"ignored","version":1,"templates":[]}"""

    @Test fun recordedModeNeverCallsNetwork() = runBlocking {
        val repository = TemplateRepository({ recorded }, { error("Network must not be called") }, true)
        assertEquals(TemplateSource.RECORDED, repository.load().source)
    }

    @Test fun offlineFallsBackToRecorded() = runBlocking {
        val repository = TemplateRepository({ recorded }, { throw IOException("Offline") }, false)
        assertEquals(TemplateSource.RECORDED_FALLBACK, repository.load().source)
    }

    @Test fun incompatibleRemoteFormatFallsBack() = runBlocking {
        val repository = TemplateRepository({ recorded }, { TemplateCatalog(99, emptyList()) }, false)
        assertEquals(TemplateSource.RECORDED_FALLBACK, repository.load().source)
    }

    @Test fun cancellationIsNotReportedAsOffline() = runBlocking {
        try {
            TemplateRepository({ recorded }, { throw CancellationException() }, false).load()
            fail("Cancellation must propagate")
        } catch (_: CancellationException) { }
    }

    @Test fun unknownFieldsAndEmptyCatalogAreSupported() {
        assertTrue(templateJson.decodeFromString<TemplateCatalog>(recorded).validated().templates.isEmpty())
    }
}
