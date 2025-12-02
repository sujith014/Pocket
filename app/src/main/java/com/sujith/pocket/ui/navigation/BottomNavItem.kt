package com.sujith.pocket.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmarks
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.twotone.BookmarkBorder
import androidx.compose.material.icons.twotone.SavedSearch
import androidx.compose.ui.graphics.vector.ImageVector

//
//data class BottomNavObject(
//    val route: AppRoutes,
//    val label: String,
//    val selectedIcon: ImageVector,
//    val unSelectedIcon: ImageVector,
//    val badgeCount: Int? = null
//)
//
//val bottomBarItems = listOf<BottomNavObject>(
//    BottomNavObject(
//        route = AppRoutes.Home,
//        label = "Home",
//        selectedIcon = Icons.Filled.Search,
//        unSelectedIcon = Icons.Outlined.Search,
//    ),
//    BottomNavObject(
//        route = AppRoutes.Bookmarks,
//        label = "Bookmarks",
//        selectedIcon = Icons.Filled.Bookmark,
//        unSelectedIcon = Icons.Outlined.Bookmark,
//        badgeCount = 0
//    )
//
//)


//sealed interface BottomNavRoutes {
//    @kotlinx.serialization.Serializable
//    object Reader : BottomNavRoutes
//
//    @Serializable
//    object Bookmarks : BottomNavRoutes
//}
//
//sealed class BottomNavItem(val route: String, val title: String, val icon: ImageVector) {
//    object Home : BottomNavItem( A"Reader", Icons.Default.Search)
//    object Bookmarks :
//        BottomNavItem(, "Bookmarks", Icons.Default.Bookmark)
//}


sealed class BottomNavItem(
    val route: AppRoutes,
    val title: String,
    val selectedIcon: ImageVector,
    val unSelectedIcon: ImageVector,
    val badgeCount: Int? = null
) {
    object Home : BottomNavItem(
        AppRoutes.Home,
        "Reader",
        Icons.Filled.Home,
        Icons.TwoTone.SavedSearch)

    object Bookmarks : BottomNavItem(
        AppRoutes.Bookmarks,
        "Bookmarks",
        Icons.Filled.Bookmarks,
        Icons.TwoTone.BookmarkBorder,

    )
}

val bottomItems = listOf(
    BottomNavItem.Home,
    BottomNavItem.Bookmarks
)


