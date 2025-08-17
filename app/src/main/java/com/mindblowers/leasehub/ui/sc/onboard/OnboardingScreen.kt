package com.mindblowers.leasehub.ui.sc.onboard

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.mindblowers.leasehub.R
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(navController: NavHostController) {
    val pages = listOf(
        OnboardPage(
            title = "Easily manage shop leases",
            description = "Track tenants, leases, and all important dates in one place.",
            imageRes = R.drawable.onboard1
        ),
        OnboardPage(
            title = "Track payments & due dates",
            description = "Stay on top of rent collections with automatic reminders.",
            imageRes = R.drawable.onboard2
        ),
        OnboardPage(
            title = "Generate reports instantly",
            description = "Get detailed insights and export reports anytime.",
            imageRes = R.drawable.onboard3
        )
    )

    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Welcome to Lease Hub") },
                actions = {
                    if (pagerState.currentPage < pages.size - 1) {
                        TextButton(onClick = { navController.navigate("login") }) {
                            Text("Skip")
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            HorizontalPager(state = pagerState) { page ->
                OnboardingPageContent(pageData = pages[page])
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (pagerState.currentPage > 0) {
                    TextButton(onClick = {
                        scope.launch { pagerState.animateScrollToPage(pagerState.currentPage - 1) }
                    }) {
                        Text("Back")
                    }
                } else {
                    Spacer(modifier = Modifier.width(64.dp))
                }

                Button(onClick = {
                    if (pagerState.currentPage < pages.size - 1) {
                        scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                    } else {
                        navController.navigate("login")
                    }
                }) {
                    Text(if (pagerState.currentPage == pages.size - 1) "Get Started" else "Next")
                }
            }
        }
    }
}

@Composable
fun OnboardingPageContent(pageData: OnboardPage) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = pageData.imageRes),
            contentDescription = pageData.title,
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
        )
        Spacer(Modifier.height(24.dp))
        Text(pageData.title, style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(8.dp))
        Text(pageData.description, style = MaterialTheme.typography.bodyMedium)
    }
}

data class OnboardPage(
    val title: String,
    val description: String,
    val imageRes: Int
)
