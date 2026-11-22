package com.example.notesbysubject.data.templates

import android.content.Context
import com.example.notesbysubject.httpClient
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.ExperimentalSerializationApi
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit
import retrofit2.http.GET

// Change this single line to false for the published JSON.
const val USE_RECORDED_TEMPLATES = true

interface TemplateApi {
    @GET("victoriapico-creator/levelMate/codex/levelmate-complete/docs/templates.json")
    suspend fun getTemplates(): TemplateCatalog
}

enum class TemplateSource { RECORDED, REMOTE, RECORDED_FALLBACK }
data class TemplateResult(val catalog: TemplateCatalog, val source: TemplateSource)

class TemplateRepository(
    private val readRecorded: () -> String,
    private val fetchRemote: suspend () -> TemplateCatalog,
    private val useRecorded: Boolean = USE_RECORDED_TEMPLATES
) {
    suspend fun load(): TemplateResult = withContext(Dispatchers.IO) {
        fun recorded() = templateJson.decodeFromString<TemplateCatalog>(readRecorded()).validated()
        if (useRecorded) {
            TemplateResult(recorded(), TemplateSource.RECORDED)
        } else {
            try {
                TemplateResult(fetchRemote().validated(), TemplateSource.REMOTE)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                TemplateResult(recorded(), TemplateSource.RECORDED_FALLBACK)
            }
        }
    }

    companion object {
        @OptIn(ExperimentalSerializationApi::class)
        fun create(context: Context): TemplateRepository {
            val api = Retrofit.Builder()
                .baseUrl("https://raw.githubusercontent.com/")
                .client(httpClient)
                .addConverterFactory(templateJson.asConverterFactory("application/json".toMediaType()))
                .build().create(TemplateApi::class.java)
            return TemplateRepository(
                readRecorded = { context.assets.open("recorded/project_templates.json").bufferedReader().use { it.readText() } },
                fetchRemote = { api.getTemplates() }
            )
        }
    }
}
