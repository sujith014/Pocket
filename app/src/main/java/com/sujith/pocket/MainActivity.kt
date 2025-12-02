package com.sujith.pocket

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.newjetpackcomposecourse.ExtensionFun.ObserveAsEvent
import com.sujith.pocket.ui.extensions.SnackbarController
import com.sujith.pocket.ui.navigation.AppNavController
import com.sujith.pocket.ui.navigation.AppRoutes
import com.sujith.pocket.ui.navigation.bottomItems
import com.sujith.pocket.ui.theme.PocketTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch


@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Handle shared links
        val sharedUrl = when {
            intent?.action == android.content.Intent.ACTION_SEND -> {
                intent?.getStringExtra(android.content.Intent.EXTRA_TEXT)
                    ?: intent?.getCharSequenceExtra(android.content.Intent.EXTRA_TEXT)?.toString()
            }
            intent?.action == android.content.Intent.ACTION_VIEW -> {
                intent?.data?.toString()
            }
            else -> null
        }
        
        setContent {
            PocketTheme {
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route
                
                // Navigate to WebViewScreen if shared URL exists
                LaunchedEffect(sharedUrl) {
                    if (!sharedUrl.isNullOrEmpty()) {
                        navController.navigate(AppRoutes.WebViewScreen(sharedUrl)) {
                            popUpTo(navController.graph.startDestinationRoute!!) {
                                inclusive = false
                            }
                            launchSingleTop = true
                        }
                    }
                }


                // Snackbar Controller
                val snackbarHostState = remember { SnackbarHostState() }
                val scope = rememberCoroutineScope()

                ObserveAsEvent(flow = SnackbarController.events, snackbarHostState) { events ->
                    scope.launch {
                        snackbarHostState.currentSnackbarData?.dismiss()

                        val result = snackbarHostState.showSnackbar(
                            message = events.message,
                            actionLabel = events.action?.label,
                            duration = events.duration
                        )

                        if (result == SnackbarResult.ActionPerformed) {
                            events.action?.action?.invoke()
                        }
                    }
                }
                // Snackbar Controller End X


                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    snackbarHost = {
                        SnackbarHost(hostState = snackbarHostState)
                    },
                    bottomBar = {
                        val isBottomBarVisible =
                            currentRoute?.startsWith(AppRoutes.WebViewScreen::class.qualifiedName ?: "") == true

                        if (!isBottomBarVisible) {
//                            BottomBar(navController)
                            AnimatedBottomBar(navController)
                        }
                    }
                ) { innerPadding ->
                    AppNavController(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        navController = navController
                    )
                }
            }
        }
    }
}

@Composable
fun AnimatedBottomBar(navController: NavHostController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar(
        tonalElevation = 8.dp,
        modifier = Modifier
            .shadow(8.dp, RoundedCornerShape(20.dp))
            .clip(RoundedCornerShape(20.dp))
    ) {
        bottomItems.forEach { item ->

            val isSelected = currentRoute == item.route::class.qualifiedName

            // Animations
            val iconSize by animateDpAsState(
                targetValue = if (isSelected) 28.dp else 22.dp,
                animationSpec = tween(300)
            )

            val indicatorColor by animateColorAsState(
                targetValue = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                else Color.Transparent,
                animationSpec = tween(300)
            )

            val textColor by animateColorAsState(
                targetValue = if (isSelected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurfaceVariant,
                animationSpec = tween(250)
            )

            NavigationBarItem(
                selected = isSelected,
                onClick = {
                    if (!isSelected) {
                        navController.navigate(item.route) {
                            popUpTo(navController.graph.startDestinationRoute!!) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                alwaysShowLabel = false, // More modern look
                label = {
                    AnimatedVisibility(
                        visible = isSelected,
                        enter = fadeIn() + slideInVertically { it / 4 },
                        exit = fadeOut() + slideOutVertically { it / 4 }
                    ) {
                        Text(
                            text = item.title,
                            color = textColor,
                            fontWeight = FontWeight.Medium
                        )
                    }
                },
                icon = {
                    Box(
                        Modifier
                            .background(
                                color = indicatorColor,
                                shape = RoundedCornerShape(50)
                            )
                            .padding(10.dp)
                    ) {
                        BadgedBox(
                            badge = {
                                item.badgeCount?.let {
                                    Badge { Text(it.toString()) }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = if (isSelected) item.selectedIcon else item.unSelectedIcon,
                                contentDescription = item.title,
                                modifier = Modifier.size(iconSize),
                                tint = textColor
                            )
                        }
                    }
                }
            )
        }
    }
}


@Composable
fun BottomBar(navController: NavHostController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar {
        bottomItems.forEach { item ->

            val isSelected = currentRoute == item.route::class.qualifiedName

            NavigationBarItem(
                selected = isSelected,
                onClick = {
                    if (!isSelected) {
                        navController.navigate(item.route) {
                            popUpTo(navController.graph.startDestinationRoute!!) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                icon = {
                    BadgedBox(
                        badge = {
                            if (item.badgeCount != null) {
                                Badge {
                                    Text(text = item.badgeCount.toString())
                                }
                            }
                        }
                    ) {
                        Icon(
                            imageVector = if (isSelected)
                                item.selectedIcon
                            else
                                item.unSelectedIcon,
                            contentDescription = item.title,
                        )
                    }
                },
                label = {
                    Text(text = item.title)
                }
            )
        }
    }

}