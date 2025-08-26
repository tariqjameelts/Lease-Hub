package com.mindblowers.leasehub.ui.nav

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.mindblowers.leasehub.ui.sc.onboard.OnboardingScreen
import com.mindblowers.leasehub.ui.sc.auth.signin.LoginScreen
import com.mindblowers.leasehub.ui.sc.auth.signup.AuthViewModel
import com.mindblowers.leasehub.ui.sc.auth.signup.SignUpScreen
import com.mindblowers.leasehub.ui.sc.main.dashboard.DashboardScreen
import com.mindblowers.leasehub.ui.sc.main.dashboard.reports.ActivityReportsScreen
import com.mindblowers.leasehub.ui.sc.main.dashboard.reports.FinancialReportsScreen
import com.mindblowers.leasehub.ui.sc.main.dashboard.reports.LeaseReportsScreen
import com.mindblowers.leasehub.ui.sc.main.dashboard.reports.ReportsNavRoutes
import com.mindblowers.leasehub.ui.sc.main.dashboard.shop.ShopListScreen
import com.mindblowers.leasehub.ui.sc.main.dashboard.tenant.AddTenantScreen
import com.mindblowers.leasehub.ui.sc.main.dashboard.reports.ReportsScreen
import com.mindblowers.leasehub.ui.sc.main.dashboard.shop.ShopDetailScreen
import com.mindblowers.leasehub.ui.sc.main.dashboard.tenant.TenantDetailScreen
import com.mindblowers.leasehub.ui.sc.main.dashboard.tenant.TenantsScreen
import com.mindblowers.leasehub.ui.sc.main.dashboard.settings.SettingsScreen

// AppNavHost.kt
@Composable
fun AppNavHost(
    navController: NavHostController,
    authViewModel: AuthViewModel = hiltViewModel()
    ) {

   val startDest = if (authViewModel.appPrefs.isLoggedIn())  "dashboard" else "onboarding"

    NavHost(
        navController = navController,
        startDestination = startDest
    ) {
        composable("onboarding") { OnboardingScreen(navController) }
        composable("signup") {
            SignUpScreen(){
                navController.navigate("dashboard") {
                 popUpTo("signup") { inclusive = true }
                 }
            }
        }
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
                // Handle invalid tenantId if necessary, e.g., show a toast or navigate back
                LaunchedEffect(Unit) {
                    navController.popBackStack()
                }
            }
        }

        //     composable("add_shop") { AddShopScreen(navController) }
//        composable("add_tenant") { AddTenantsScreen(navController) }
        composable("add_tenant/{shopId}") { AddTenantScreen(navController) }
        /*composable("payment/{shopId}") { backStackEntry ->
            PaymentScreen(navController, backStackEntry.arguments?.getString("shopId") ?: "")
        }*/
        //composable("reports") { ReportsScreen(navController) }
        composable("tenants") { TenantsScreen(navController) }
        composable("settings") { SettingsScreen(){} }
        composable(ReportsNavRoutes.Dashboard.route) {
            ReportsScreen(navController)
        }

        composable(ReportsNavRoutes.Financial.route) {
            FinancialReportsScreen()
        }

        composable(ReportsNavRoutes.Lease.route) {
            LeaseReportsScreen()
        }

        composable(ReportsNavRoutes.Activity.route) {
            ActivityReportsScreen()
        }
    }
}