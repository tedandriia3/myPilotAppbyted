package com.pilotprep.app.data

import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val db: FirebaseFirestore
) {
    private val auth: FirebaseAuth = Firebase.auth

    fun getCurrentUser() = auth.currentUser

    fun hasUser(): Boolean = auth.currentUser != null

    fun signOut() {
        auth.signOut()
    }

    suspend fun signUpWithEmailPersonal(email: String, pass: String, displayName: String): Result<Unit> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, pass).await()
            val user = result.user
            if (user != null) {
                // 1. On met à jour le profil de l'utilisateur dans Firebase Authentication
                // pour y stocker le pseudo.
                val profileUpdates = userProfileChangeRequest {
                    this.displayName = displayName
                }
                user.updateProfile(profileUpdates).await()

                // 2. On crée le document utilisateur dans Firestore avec le rôle et le pseudo.
                db.collection("users").document(user.uid)
                    .set(mapOf(
                        "email" to email,
                        "role" to "PERSONAL",
                        "displayName" to displayName
                    ))
                    .await()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signUpWithEmailEstablishment(email: String, pass: String, establishmentName: String): Result<Unit> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, pass).await()
            val user = result.user
            if (user != null) {
                val profileUpdates = userProfileChangeRequest {
                    this.displayName = establishmentName // Le nom de l'établissement comme pseudo
                }
                user.updateProfile(profileUpdates).await()

                // 1. On crée le document utilisateur avec le rôle "ADMIN"
                db.collection("users").document(user.uid)
                    .set(mapOf(
                        "email" to email,
                        "role" to "ADMIN",
                        "displayName" to establishmentName
                    ))
                    .await()
                // 2. On crée le document de l'établissement qui lui est lié
                db.collection("establishments")
                    .add(mapOf("adminUid" to user.uid, "name" to establishmentName))
                    .await()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signInWithEmail(email: String, pass: String): Result<Unit> {
        return try {
            auth.signInWithEmailAndPassword(email, pass).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signInWithGoogle(credential: AuthCredential): Result<Unit> {
        return try {
            auth.signInWithCredential(credential).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- Account management ---
    suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        return try {
            auth.sendPasswordResetEmail(email).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun sendEmailVerification(): Result<Unit> {
        return try {
            val user = auth.currentUser ?: return Result.failure(IllegalStateException("No user"))
            user.sendEmailVerification().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updatePassword(newPassword: String): Result<Unit> {
        return try {
            val user = auth.currentUser ?: return Result.failure(IllegalStateException("No user"))
            user.updatePassword(newPassword).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteAccount(): Result<Unit> {
        return try {
            val user = auth.currentUser ?: return Result.failure(IllegalStateException("No user"))
            user.delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateDisplayName(newDisplayName: String): Result<Unit> {
        return try {
            val user = auth.currentUser ?: return Result.failure(IllegalStateException("No user"))
            val profileUpdates = userProfileChangeRequest { this.displayName = newDisplayName }
            user.updateProfile(profileUpdates).await()
            // Mettre à jour aussi le document Firestore s'il existe
            db.collection("users").document(user.uid)
                .update(mapOf("displayName" to newDisplayName)).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun exportUserData(userId: String): Result<Map<String, Any>> {
        return try {
            val userDoc = db.collection("users").document(userId).get().await().data ?: emptyMap()
            val subjectStats = db.collection("subject_stats").whereEqualTo("userId", userId).get().await().documents.mapNotNull { it.data }
            val sessions = db.collection("study_sessions").whereEqualTo("userId", userId).get().await().documents.mapNotNull { it.data }
            val export = mapOf(
                "user" to userDoc,
                "subject_stats" to subjectStats,
                "study_sessions" to sessions
            )
            Result.success(export)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}