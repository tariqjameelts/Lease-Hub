package com.mindblowers.leasehub.ui.sc.auth.signup

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.mindblowers.leasehub.data.entities.User

@Composable
fun SignUpScreen(
    navController: NavController,
    viewModel: AuthViewModel = hiltViewModel(),
    onSignUpSuccess: () -> Unit
) {
    val context = LocalContext.current

    var fullName by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }

    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
        }
    }

    fun validateForm(): Boolean {
        if (fullName.isBlank()) {
            Toast.makeText(context, "Please enter your full name", Toast.LENGTH_SHORT).show()
            return false
        }
        if (username.isBlank()) {
            Toast.makeText(context, "Please enter a username", Toast.LENGTH_SHORT).show()
            return false
        }
        val regex = Regex("^[a-z_]+$")
        if (!regex.matches(username)) {
            Toast.makeText(
                context,
                "Username must contain only lowercase English letters and underscores",
                Toast.LENGTH_LONG
            ).show()
            return false
        }
        return true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Create Account",
                style = MaterialTheme.typography.headlineMedium
            )

            OutlinedTextField(
                value = fullName,
                onValueChange = { fullName = it },
                label = { Text("Full Name") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    keyboardType = KeyboardType.Text
                ),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Username") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.None,
                    keyboardType = KeyboardType.Ascii
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = {
                    if (validateForm()) {
                        val user = User(
                            fullName = fullName,
                            username = username
                        )
                        viewModel.signUp(user) { success ->
                            if (success) {
                                onSignUpSuccess()
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Sign Up")
                }
            }
            TextButton(onClick = { navController.navigate("signin") }) {
                Text("Don't have an account? Sign Up")
            }
        }
    }
}
