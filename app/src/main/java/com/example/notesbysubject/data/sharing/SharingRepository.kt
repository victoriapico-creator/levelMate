package com.example.notesbysubject.data.sharing

import android.content.Context
import com.example.notesbysubject.data.local.ProjectWithDetails
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.MetadataChanges
import com.google.firebase.firestore.SetOptions
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeout

data class SharedListState(
    val viewerUid: String? = null,
    val projects: List<SharedProject> = emptyList(),
    val loading: Boolean = false,
    val fromCache: Boolean = false,
    val error: String? = null
)

class SharingRepository(context: Context) {
    private val app = runCatching {
        FirebaseApp.getApps(context).firstOrNull { it.name == FirebaseApp.DEFAULT_APP_NAME }
            ?: FirebaseApp.initializeApp(context)
    }.getOrNull()
    val available = app != null
    private val auth = app?.let { FirebaseAuth.getInstance(it) }
    private val firestore = app?.let { FirebaseFirestore.getInstance(it) }

    fun observeUser(): Flow<FirebaseUser?> {
        val firebaseAuth = auth ?: return flowOf(null)
        return callbackFlow {
            val listener = FirebaseAuth.AuthStateListener { trySend(it.currentUser) }
            firebaseAuth.addAuthStateListener(listener)
            awaitClose { firebaseAuth.removeAuthStateListener(listener) }
        }
    }

    suspend fun login(email: String, password: String, createAccount: Boolean) = withTimeout(20_000) {
        val firebaseAuth = checkNotNull(auth) { "Sharing is not configured" }
        if (createAccount) firebaseAuth.createUserWithEmailAndPassword(email, password).awaitResult()
        else firebaseAuth.signInWithEmailAndPassword(email, password).awaitResult()
    }

    fun logout() { auth?.signOut() }

    fun observeShared(uid: String): Flow<SharedListState> = callbackFlow {
        val database = firestore
        if (database == null) { trySend(SharedListState(viewerUid = uid, error = "Project sharing is not available yet.")); close(); return@callbackFlow }
        trySend(SharedListState(viewerUid = uid, loading = true))
        val listener = database.collection("sharedProjects").whereArrayContains("memberUids", uid)
            .addSnapshotListener(MetadataChanges.INCLUDE) { snapshot, error ->
                if (error != null || snapshot == null) {
                    trySend(SharedListState(viewerUid = uid, error = "Could not load shared projects. Check your connection and try again."))
                } else {
                    var invalid = false
                    val projects = snapshot.documents.mapNotNull { document ->
                        try { parseSharedProject(document.id, document.data ?: emptyMap()).takeIf { uid in it.memberUids } }
                        catch (_: Exception) { invalid = true; null }
                    }.sortedBy { it.name.lowercase() }
                    trySend(SharedListState(
                        viewerUid = uid, projects = projects, fromCache = snapshot.metadata.isFromCache,
                        error = if (invalid) "Some shared projects contain invalid data and could not be opened." else null
                    ))
                }
            }
        awaitClose { listener.remove() }
    }

    suspend fun publish(project: ProjectWithDetails, recipientUid: String) = withTimeout(20_000) {
        val uid = checkNotNull(auth?.currentUser?.uid) { "Sign in first" }
        require(recipientUid.isNotBlank() && recipientUid.length <= 128 && recipientUid != uid) { "Enter the other person's UID" }
        val database = checkNotNull(firestore)
        val reference = database.collection("sharedProjects").document("${uid}_${project.project.id}")
        // A new shared copy has no readable document yet. Merge with arrayUnion
        // creates or updates it atomically without reading a missing document.
        // Firestore rules enforce ownership and the participant limit.
        val payload = sharedProjectPayload(project, uid, listOf(uid, recipientUid)).toMutableMap()
        payload["memberUids"] = FieldValue.arrayUnion(uid, recipientUid)
        payload["updatedAt"] = FieldValue.serverTimestamp()
        reference.set(payload, SetOptions.merge()).awaitResult()
    }

    suspend fun remove(project: SharedProject) = withTimeout(20_000) {
        val uid = checkNotNull(auth?.currentUser?.uid) { "Sign in first" }
        require(project.ownerUid == uid) { "Only the owner can remove a shared project" }
        val database = checkNotNull(firestore)
        val reference = database.collection("sharedProjects").document(project.id)
        database.runTransaction { transaction ->
            val current = transaction.get(reference)
            check(current.getString("ownerUid") == uid) { "Only the owner can remove a shared project" }
            transaction.delete(reference)
            true
        }.awaitResult()
    }
}

private suspend fun <T> Task<T>.awaitResult(): T = suspendCancellableCoroutine { continuation ->
    addOnCompleteListener { task ->
        if (continuation.isActive) {
            if (task.isSuccessful) continuation.resume(task.result)
            else if (task.isCanceled) continuation.cancel()
            else continuation.resumeWithException(task.exception ?: IllegalStateException("Firebase operation failed"))
        }
    }
}
