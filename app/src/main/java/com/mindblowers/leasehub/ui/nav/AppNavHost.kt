package com.mindblowers.leasehub.ui.nav

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.mindblowers.leasehub.ui.sc.auth.signin.LoginScreen
import com.mindblowers.leasehub.ui.sc.auth.signup.AuthViewModel
import com.mindblowers.leasehub.ui.sc.auth.signup.SignUpScreen
import com.mindblowers.leasehub.ui.sc.main.dashboard.DashboardScreen
import com.mindblowers.leasehub.ui.sc.main.dashboard.reports.ActivityReportsScreen
import com.mindblowers.leasehub.ui.sc.main.dashboard.reports.FinancialReportsScreen
import com.mindblowers.leasehub.ui.sc.main.dashboard.reports.LeaseReportsScreen
import com.mindblowers.leasehub.ui.sc.main.dashboard.reports.ReportsNavRoutes
import com.mindblowers.leasehub.ui.sc.main.dashboard.reports.ReportsScreen
import com.mindblowers.leasehub.ui.sc.main.dashboard.settings.SettingsScreen
import com.mindblowers.leasehub.ui.sc.main.dashboard.shop.ShopDetailScreen
import com.mindblowers.leasehub.ui.sc.main.dashboard.shop.ShopListScreen
import com.mindblowers.leasehub.ui.sc.main.dashboard.tenant.AddTenantScreen
import com.mindblowers.leasehub.ui.sc.main.dashboard.tenant.TenantDetailScreen
import com.mindblowers.leasehub.ui.sc.main.dashboard.tenant.TenantsScreen
import com.mindblowers.leasehub.ui.sc.onboard.OnboardingScreen

@Composable
fun AppNavHost(
    navController: NavHostController,
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val startDest =
        if (authViewModel.appPrefs.isNewUser()) "onboarding" else if (authViewModel.appPrefs.isLoggedIn()) "dashboard" else "signin"

    NavHost(
        navController = navController,
        startDestination = startDest
    ) {
        // Onboarding
        composable("onboarding") {
            OnboardingScreen() {
                navController.navigate("signin") {
                    popUpTo(navController.graph.startDestinationId) { inclusive = true }
                    launchSingleTop = true
                }
                authViewModel.appPrefs.setNotNewUser()
            }
        }

        // Sign Up / Login
        composable("signup") {

            SignUpScreen(
                onSignUpSuccess = {
                    // Navigate to dashboard and clear backstack
                    navController.navigate("dashboard") {
                        popUpTo("signup") { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    // Navigate to login and replace signup in backstack
                   // navController.popBackStack()
                    navController.navigate("signin") {
                         popUpTo("signup") { inclusive = true }
                    }
                }
            )
        }

        // Dashboard
        composable("dashboard") { DashboardScreen(navController) }
        composable("shop_list") { ShopListScreen(navController) }

        composable(
            route = "shop_detail/{shopId}",
            arguments = listOf(navArgument("shopId") { type = NavType.LongType })
        ) { backStackEntry ->
            val shopId = backStackEntry.arguments?.getLong("shopId") ?: 0L
            ShopDetailScreen(navController, shopId)
        }

        composable("tenant_detail/{tenantId}") { backStackEntry ->
            val tenantId = backStackEntry.arguments?.getString("tenantId")?.toLongOrNull()
            if (tenantId != null) {
                TenantDetailScreen(navController, tenantId)
            } else {
                LaunchedEffect(Unit) { navController.popBackStack() }
            }
        }
        composable("signin") {
            LoginScreen(
                onLoginSuccess = {
                    // Navigate to dashboard and clear backstack
                    navController.navigate("dashboard") {
                        popUpTo("signin") { inclusive = true }
                    }
                },
                onNavigateToSignUp = {
                    // Navigate to signup and replace login in backstack
                    navController.navigate("signup")
                }
            )
        }

        composable("add_tenant/{shopId}") { AddTenantScreen(navController) }
        composable("tenants") { TenantsScreen(navController) }

        composable("settings") {
            SettingsScreen(
                onLogout = {
                    navController.navigate("signup") {
                        popUpTo("dashboard") { inclusive = true }
                    }
                }
            )
        }

        // Reports
        composable(ReportsNavRoutes.Dashboard.route) { ReportsScreen(navController) }
        composable(ReportsNavRoutes.Financial.route) { FinancialReportsScreen() }
        composable(ReportsNavRoutes.Lease.route) { LeaseReportsScreen() }
        composable(ReportsNavRoutes.Activity.route) { ActivityReportsScreen() }
    }
}
