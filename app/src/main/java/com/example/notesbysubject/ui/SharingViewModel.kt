package com.example.notesbysubject.ui

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.notesbysubject.appContainer
import com.example.notesbysubject.data.sharing.SharedListState
import com.example.notesbysubject.data.sharing.SharedProject
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class SharingViewModel(application: Application) : AndroidViewModel(application) {
    private val container = application.appContainer
    private val sharing = container.sharing
    val configured = sharing.available
    val user = sharing.observeUser().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
    private val retry = MutableStateFlow(0)
    val sharedProjects = combine(user, retry) { user, _ -> user }.flatMapLatest { user ->
        if (user == null) flowOf(SharedListState()) else sharing.observeShared(user.uid)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SharedListState())
    var busy by mutableStateOf(false)
        private set
    var message by mutableStateOf<String?>(null)
        private set
    var importedId by mutableStateOf<Long?>(null)
        private set

    private fun perform(action: suspend () -> Unit) {
        if (busy) return
        busy = true
        message = null
        viewModelScope.launch {
            try { action() }
            catch (_: TimeoutCancellationException) { message = "The request timed out. Check your connection and retry." }
            catch (cancelled: CancellationException) { throw cancelled }
            catch (_: FirebaseAuthInvalidCredentialsException) { message = "Check your email and password." }
            catch (_: FirebaseAuthUserCollisionException) { message = "An account already exists for that email. Sign in instead." }
            catch (invalid: IllegalArgumentException) { message = invalid.message ?: "Invalid input" }
            catch (_: Exception) { message = "Could not complete the request. Check your connection, account and project permissions, then retry." }
            finally { busy = false }
        }
    }

    fun authenticate(email: String, password: String, create: Boolean) = perform {
        sharing.login(email, password, create)
        message = if (create) "Account created" else "Signed in"
    }

    fun logout() { sharing.logout(); message = null }
    fun retry() { retry.value++ }
    fun consumeImportedProject() { importedId = null }

    fun publish(projectId: Long, recipientUid: String) = perform {
        val project = checkNotNull(container.projects.dao.getProject(projectId)) { "Project no longer exists" }
        sharing.publish(project, recipientUid.trim())
        message = "Shared project updated. Its participants can now read this copy."
    }

    fun remove(project: SharedProject) = perform { sharing.remove(project); message = "Shared copy removed" }

    fun import(project: SharedProject) = perform {
        importedId = container.projects.createWithContents(
            project.name, project.description, project.deadline,
            project.materials.map { it.name to it.quantity }, project.steps.sortedBy { it.position }.map { it.text to it.done }
        )
    }
}
