package com.mindblowers.leasehub.ui.nav

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.mindblowers.leasehub.ui.sc.onboard.OnboardingScreen
import com.mindblowers.leasehub.ui.sc.auth.signin.LoginScreen
import com.mindblowers.leasehub.ui.sc.auth.signup.SignUpScreen
import com.mindblowers.leasehub.ui.sc.main.dashboard.DashboardScreen
import com.mindblowers.leasehub.ui.sc.main.dashboard.shop.ShopListScreen
import com.mindblowers.leasehub.ui.sc.main.dashboard.tenant.AddTenantScreen
import com.mindblowers.leasehub.ui.sc.main.PaymentScreen
import com.mindblowers.leasehub.ui.sc.main.dashboard.reports.ReportsScreen
import com.mindblowers.leasehub.ui.sc.main.dashboard.shop.ShopDetailScreen
import com.mindblowers.leasehub.ui.sc.main.dashboard.tenant.TenantDetailScreen
import com.mindblowers.leasehub.ui.sc.main.dashboard.tenant.TenantsScreen
import com.mindblowers.leasehub.ui.sc.main.dashboard.settings.SettingsScreen

// AppNavHost.kt
@Composable
fun AppNavHost(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = "dashboard"
    ) {
        composable("onboarding") { OnboardingScreen(navController) }
        composable("login") { LoginScreen(navController) }
        composable("signup") { SignUpScreen(navController) }
        composable("dashboard") { DashboardScreen(navController) }
        composable("shop_list") { ShopListScreen(navController) }
        composable("shop_detail/{shopId}") { backStackEntry ->
            ShopDetailScreen(navController, backStackEntry.arguments?.getString("shopId") ?: "")
        }
        composable("tenant_detail/{tenantId}") { backStackEntry ->
            TenantDetailScreen(navController, backStackEntry.arguments?.getString("shopId") ?: "")
        }
   //     composable("add_shop") { AddShopScreen(navController) }
//        composable("add_tenant") { AddTenantsScreen(navController) }
        composable("add_tenant/{shopId}") { AddTenantScreen(navController) }
        composable("payment/{shopId}") { backStackEntry ->
            PaymentScreen(navController, backStackEntry.arguments?.getString("shopId") ?: "")
        }
        composable("reports") { ReportsScreen(navController) }
        composable("tenants") { TenantsScreen(navController) }
        composable("settings") { SettingsScreen(){} }
    }
}