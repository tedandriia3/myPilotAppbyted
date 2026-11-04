package com.pilotprep.app.ui.screens.auth

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.GoogleAuthProvider
import com.pilotprep.app.R
import com.pilotprep.app.viewmodels.AuthEvent
import com.pilotprep.app.viewmodels.AuthViewModel
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonalAuthScreen(
    navController: NavController,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    var confirmPasswordVisible by rememberSaveable { mutableStateOf(false) }
    var displayName by remember { mutableStateOf("") }
    var isSignUpMode by remember { mutableStateOf(false) }
    val authState by viewModel.uiState.collectAsStateWithLifecycle()

    // On écoute les événements ponctuels (comme les Toasts) du ViewModel
    LaunchedEffect(Unit) {
        viewModel.eventFlow.collectLatest { event ->
            when (event) {
                is AuthEvent.ShowToast -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // Affiche les erreurs de Firebase dans un Toast
    LaunchedEffect(authState.error) {
        authState.error?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
        }
    }

    // Redirige l'utilisateur vers le tableau de bord approprié s'il est connecté
    if (authState.isLoggedIn) {
        LaunchedEffect(authState.userRole) {
            val destination = when (authState.userRole) {
                "ADMIN" -> "establishment_dashboard"
                "PERSONAL" -> "personal_dashboard"
                else -> null // Ne rien faire si le rôle n'est pas encore chargé
            }
            if (destination != null) {
                navController.navigate(destination) {
                    popUpTo(0) { inclusive = true } // Vide tout l'historique
                }
            }
        }
    }

    // --- LOGIQUE POUR LA CONNEXION GOOGLE ---
    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
        onResult = { result ->
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)!!
                val credential = GoogleAuthProvider.getCredential(account.idToken!!, null)
                viewModel.signInWithGoogle(credential)
            } catch (e: ApiException) {
                e.printStackTrace()
                Toast.makeText(context, "Échec de la connexion Google", Toast.LENGTH_SHORT).show()
            }
        }
    )

    val gso = remember {
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
    }
    val googleSignInClient = remember { GoogleSignIn.getClient(context, gso) }
    // ---

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isSignUpMode) "Créer un Compte Personnel" else "Connexion Personnelle") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Retour")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            AnimatedVisibility(visible = isSignUpMode) {
                OutlinedTextField(
                    value = displayName,
                    onValueChange = { displayName = it },
                    label = { Text("Pseudo ou Nom complet") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                singleLine = true
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Mot de passe") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                            contentDescription = if (passwordVisible) "Masquer le mot de passe" else "Afficher le mot de passe"
                        )
                    }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true
            )
            AnimatedVisibility(visible = isSignUpMode) {
                Column {
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        label = { Text("Confirmer le mot de passe") },
                        modifier = Modifier.fillMaxWidth(),
                        visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                                Icon(
                                    imageVector = if (confirmPasswordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                    contentDescription = if (confirmPasswordVisible) "Masquer le mot de passe" else "Afficher le mot de passe"
                                )
                            }
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        singleLine = true
                    )
                    val passMismatch = isSignUpMode && confirmPassword.isNotEmpty() && confirmPassword != password
                    val passTooShort = isSignUpMode && password.isNotEmpty() && password.length < 6
                    if (passMismatch) {
                        Text("Les mots de passe ne correspondent pas", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    } else if (passTooShort) {
                        Text("Le mot de passe doit contenir au moins 6 caractères", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
            Spacer(Modifier.height(16.dp))

            if (authState.isLoading) {
                CircularProgressIndicator()
            } else {
                Button(
                    onClick = {
                        if (isSignUpMode) {
                            if (password.length >= 6 && password == confirmPassword && email.contains("@")) {
                                viewModel.signUpPersonal(email.trim(), password, displayName.trim())
                            }
                        } else {
                            viewModel.signIn(email.trim(), password)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = if (isSignUpMode) {
                        email.contains("@") && password.length >= 6 && password == confirmPassword && displayName.isNotBlank()
                    } else email.contains("@") && password.isNotBlank()
                ) {
                    Text(if (isSignUpMode) "Créer mon compte" else "Se connecter")
                }
            }

            Spacer(Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                TextButton(onClick = {
                    if (email.contains("@")) {
                        viewModel.requestPasswordReset(email.trim())
                    }
                }) { Text("Mot de passe oublié ?") }

                if (isSignUpMode) {
                    TextButton(onClick = { viewModel.sendEmailVerification() }) {
                        Text("Renvoyer email de vérif.")
                    }
                }
            }

            TextButton(onClick = { isSignUpMode = !isSignUpMode }) {
                Text(if (isSignUpMode) "J'ai déjà un compte" else "Créer un compte")
            }

            Spacer(Modifier.height(16.dp))
            Button(onClick = {
                googleSignInLauncher.launch(googleSignInClient.signInIntent)
            }) {
                Text("Continuer avec Google")
            }
        }
    }
}