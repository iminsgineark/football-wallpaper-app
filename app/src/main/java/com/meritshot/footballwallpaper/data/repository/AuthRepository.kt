package com.meritshot.footballwallpaper.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.meritshot.footballwallpaper.data.model.Result
import com.meritshot.footballwallpaper.data.model.User
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {
    val currentUser: FirebaseUser? get() = auth.currentUser
    val isLoggedIn: Boolean get() = auth.currentUser != null

    /** Observe auth state changes */
    val authStateFlow: Flow<FirebaseUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { trySend(it.currentUser) }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    /** Sign in with email + password */
    suspend fun signIn(email: String, password: String): Result<User> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val uid = result.user?.uid ?: throw Exception("UID is null")
            val user = fetchUserProfile(uid)
            Result.Success(user)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    /** Register new user */
    suspend fun register(email: String, password: String): Result<User> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val uid = result.user?.uid ?: throw Exception("UID is null")
            val user = User(id = uid, email = email, role = "user")
            firestore.collection("users").document(uid).set(user).await()
            Result.Success(user)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    /** Fetch user profile from Firestore */
    suspend fun fetchUserProfile(uid: String): User {
        val doc = firestore.collection("users").document(uid).get().await()
        return doc.toObject(User::class.java)?.copy(id = uid)
            ?: User(id = uid, email = auth.currentUser?.email ?: "", role = "user")
    }

    /** Get current user profile */
    suspend fun getCurrentUserProfile(): Result<User> {
        return try {
            val uid = auth.currentUser?.uid ?: throw Exception("Not logged in")
            Result.Success(fetchUserProfile(uid))
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    fun signOut() = auth.signOut()
}
