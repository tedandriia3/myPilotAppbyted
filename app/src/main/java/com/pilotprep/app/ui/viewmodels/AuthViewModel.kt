package com.pilotprep.app.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.pilotprep.app.data.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

// L'état de l'UI, durable et observable
data class AuthUiState(
    val isLoggedIn: Boolean = false,
    val isLoading: Boolean = true,
    val user: FirebaseUser? = null,
    val userRole: String? = null,
    val error: String? = null
)

// Les événements ponctuels (pour les messages Toast, Snackbars, etc.)
sealed class AuthEvent {
    data class ShowToast(val message: String) : AuthEvent()
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repository: AuthRepository,
    private val db: FirebaseFirestore
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState = _uiState.asStateFlow()

    // On utilise un SharedFlow pour les événements qui ne doivent être consommés qu'une seule fois
    private val _eventFlow = MutableSharedFlow<AuthEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    init {
        checkCurrentUser()
    }

    private fun checkCurrentUser() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val user = repository.getCurrentUser()
            if (user != null) {
                try {
                    val userDoc = db.collection("users").document(user.uid).get().await()
                    val role = userDoc.getString("role")
                    _uiState.update { it.copy(isLoggedIn = true, isLoading = false, user = user, userRole = role) }
                } catch (e: Exception) {
                    _uiState.update { it.copy(isLoggedIn = true, isLoading = false, user = user, userRole = null, error = "Impossible de charger le profil.") }
                }
            } else {
                _uiState.update { it.copy(isLoggedIn = false, isLoading = false, user = null, userRole = null) }
            }
        }
    }

    fun refreshCurrentUser() {
        checkCurrentUser()
    }

    // Fonction d'aide pour gérer les connexions réussies
    private fun handleSuccessfulLogin() {
        viewModelScope.launch {
            _eventFlow.emit(AuthEvent.ShowToast("Connexion réussie !"))
            checkCurrentUser() // Recharge les données complètes de l'utilisateur (y compris le rôle)
        }
    }

    fun signUpPersonal(email: String, pass: String, displayName: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val result = repository.signUpWithEmailPersonal(email, pass, displayName)
            if (result.isSuccess) {
                handleSuccessfulLogin()
            } else {
                _uiState.update { it.copy(isLoading = false, error = result.exceptionOrNull()?.message) }
            }
        }
    }

    fun signUpEstablishment(email: String, pass: String, establishmentName: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val result = repository.signUpWithEmailEstablishment(email, pass, establishmentName)
            if (result.isSuccess) {
                handleSuccessfulLogin()
            } else {
                _uiState.update { it.copy(isLoading = false, error = result.exceptionOrNull()?.message) }
            }
        }
    }

    fun signIn(email: String, pass: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val result = repository.signInWithEmail(email, pass)
            if (result.isSuccess) {
                handleSuccessfulLogin()
            } else {
                _uiState.update { it.copy(isLoading = false, error = result.exceptionOrNull()?.message) }
            }
        }
    }

    fun signInWithGoogle(credential: AuthCredential) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val result = repository.signInWithGoogle(credential)
            if (result.isSuccess) {
                val user = repository.getCurrentUser()
                if (user != null) {
                    try {
                        val userDocRef = db.collection("users").document(user.uid)
                        if (userDocRef.get().await().exists().not()) {
                            userDocRef.set(mapOf(
                                "email" to user.email,
                                "role" to "PERSONAL",
                                "displayName" to user.displayName
                            )).await()
                        }
                    } catch (e: Exception) { /* Gérer l'erreur si besoin */ }
                }
                handleSuccessfulLogin()
            } else {
                _uiState.update { it.copy(isLoading = false, error = result.exceptionOrNull()?.message) }
            }
        }
    }

    fun signOut() {
        repository.signOut()
        _uiState.update { AuthUiState(isLoggedIn = false, isLoading = false) }
    }

    // --- Account management exposed to UI ---
    fun requestPasswordReset(email: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val res = repository.sendPasswordResetEmail(email)
            if (res.isSuccess) {
                _eventFlow.emit(AuthEvent.ShowToast("Email de réinitialisation envoyé"))
            } else {
                _uiState.update { it.copy(error = res.exceptionOrNull()?.message) }
            }
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    fun sendEmailVerification() {
        viewModelScope.launch {
            val res = repository.sendEmailVerification()
            if (res.isSuccess) {
                _eventFlow.emit(AuthEvent.ShowToast("Email de vérification envoyé"))
            } else {
                _uiState.update { it.copy(error = res.exceptionOrNull()?.message) }
            }
        }
    }

    fun changePassword(newPassword: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val res = repository.updatePassword(newPassword)
            if (res.isSuccess) {
                _eventFlow.emit(AuthEvent.ShowToast("Mot de passe mis à jour"))
            } else {
                _uiState.update { it.copy(error = res.exceptionOrNull()?.message) }
            }
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    fun deleteAccount(onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val res = repository.deleteAccount()
            if (res.isSuccess) {
                signOut()
                onSuccess()
            } else {
                _uiState.update { it.copy(error = res.exceptionOrNull()?.message, isLoading = false) }
            }
        }
    }

    fun changeDisplayName(newDisplayName: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val res = repository.updateDisplayName(newDisplayName)
            if (res.isSuccess) {
                // Recharger l'utilisateur courant pour refléter le nouveau pseudo
                val refreshed = repository.getCurrentUser()
                _uiState.update { it.copy(user = refreshed, isLoading = false) }
                _eventFlow.emit(AuthEvent.ShowToast("Pseudo mis à jour"))
            } else {
                _uiState.update { it.copy(error = res.exceptionOrNull()?.message, isLoading = false) }
            }
        }
    }

    suspend fun exportUserData(): Result<Map<String, Any>> {
        val uid = repository.getCurrentUser()?.uid ?: return Result.failure(IllegalStateException("No user"))
        return repository.exportUserData(uid)
    }
}