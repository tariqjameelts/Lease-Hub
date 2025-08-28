package com.mindblowers.leasehub.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.mindblowers.leasehub.data.prefs.ThemeOption
import com.mindblowers.leasehub.data.repository.SettingsRepo
import com.mindblowers.leasehub.ui.nav.AppNavHost
import com.mindblowers.leasehub.ui.theme.LeaseHubTheme
import dagger.hilt.android.AndroidEntryPoint
import jakarta.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var settingsRepo: SettingsRepo


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {

            val themeOption by settingsRepo.themeOption.collectAsState()
            val dynamicColor by settingsRepo.dynamicColor.collectAsState()

            val darkTheme = when (themeOption) {
                ThemeOption.LIGHT -> false
                ThemeOption.DARK -> true
                ThemeOption.SYSTEM -> isSystemInDarkTheme()
            }

            LeaseHubTheme(
                darkTheme = darkTheme,
                dynamicColor = dynamicColor
            ){
                val navController = rememberNavController()
                AppNavHost(navController)
            }
        }
    }
}