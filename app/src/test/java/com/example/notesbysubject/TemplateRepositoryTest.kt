package com.example.notesbysubject

import com.example.notesbysubject.data.templates.TemplateCatalog
import com.example.notesbysubject.data.templates.TemplateRepository
import com.example.notesbysubject.data.templates.TemplateSource
import com.example.notesbysubject.data.templates.templateJson
import java.io.IOException
import java.net.InetSocketAddress
import java.util.concurrent.atomic.AtomicReference
import com.sun.net.httpserver.HttpServer
import com.example.notesbysubject.data.templates.TemplateApi
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.ExperimentalSerializationApi
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class TemplateRepositoryTest {
    private val recorded = """{"note":"ignored","version":1,"templates":[]}"""

    @OptIn(ExperimentalSerializationApi::class)
    @Test fun realHttpCallUsesTheSharedClientAndLenientParser() = runBlocking {
        val userAgent = AtomicReference<String>()
        val server = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0)
        server.createContext("/") { exchange ->
            userAgent.set(exchange.requestHeaders.getFirst("User-Agent"))
            val body = recorded.toByteArray()
            exchange.responseHeaders.set("Content-Type", "application/json")
            exchange.sendResponseHeaders(200, body.size.toLong())
            exchange.responseBody.use { it.write(body) }
        }
        server.start()
        try {
            val api = Retrofit.Builder().baseUrl("http://127.0.0.1:${server.address.port}/")
                .client(httpClient)
                .addConverterFactory(templateJson.asConverterFactory("application/json".toMediaType()))
                .build().create(TemplateApi::class.java)
            val result = TemplateRepository({ error("Recorded copy should not be needed") }, { api.getTemplates() }, false).load()
            assertEquals(TemplateSource.REMOTE, result.source)
            assertTrue(result.catalog.templates.isEmpty())
            assertEquals("NotesBySubject/1.0 (CUNEF SAP student project)", userAgent.get())
        } finally { server.stop(0) }
    }

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
