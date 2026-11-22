package com.example.notesbysubject.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.produceState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow

data class ObservedData<T>(val data: T? = null, val loading: Boolean = true, val error: String? = null)

@Composable
fun <T> observeData(flow: Flow<T>, retry: Int = 0): State<ObservedData<T>> =
    produceState(initialValue = ObservedData<T>(), key1 = flow, key2 = retry) {
        value = ObservedData()
        try { flow.collect { value = ObservedData(data = it, loading = false) } }
        catch (cancelled: CancellationException) { throw cancelled }
        catch (_: Exception) { value = ObservedData(loading = false, error = "Could not load saved data. Please retry.") }
    }
