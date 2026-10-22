package com.messmate.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.messmate.app.ui.theme.MessMatePalette

/** A destination shared by the phone navigation bar and the expanded-window navigation rail. */
data class AdaptiveDestination(
    val label: String,
    val icon: ImageVector,
)

/**
 * Responsive application frame. Compact windows use bottom navigation; expanded windows use a
 * navigation rail and a centered reading-width content area. The 600dp breakpoint is based on
 * the actual available window width, so split-screen and resizable windows adapt as well.
 */
@Composable
fun AdaptiveAppShell(
    selectedIndex: Int,
    destinations: List<AdaptiveDestination>,
    onDestinationSelected: (Int) -> Unit,
    content: @Composable (PaddingValues) -> Unit,
) {
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val expanded = maxWidth >= 600.dp

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = MessMatePalette.Canvas,
            bottomBar = {
                if (!expanded) {
                    NavigationBar(containerColor = MessMatePalette.Paper, tonalElevation = 0.dp) {
                        destinations.forEachIndexed { index, destination ->
                            NavigationBarItem(
                                selected = selectedIndex == index,
                                onClick = { onDestinationSelected(index) },
                                icon = { androidx.compose.material3.Icon(destination.icon, contentDescription = destination.label) },
                                label = {
                                    Text(destination.label, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = MessMatePalette.Forest,
                                    selectedTextColor = MessMatePalette.Forest,
                                    indicatorColor = MessMatePalette.Sage,
                                    unselectedIconColor = MessMatePalette.Muted,
                                    unselectedTextColor = MessMatePalette.Muted,
                                ),
                            )
                        }
                    }
                }
            },
        ) { scaffoldPadding ->
            Row(
                modifier = Modifier.fillMaxSize().padding(scaffoldPadding),
                horizontalArrangement = Arrangement.Start,
            ) {
                if (expanded) {
                    NavigationRail(containerColor = MessMatePalette.Paper) {
                        destinations.forEachIndexed { index, destination ->
                            NavigationRailItem(
                                selected = selectedIndex == index,
                                onClick = { onDestinationSelected(index) },
                                icon = { androidx.compose.material3.Icon(destination.icon, contentDescription = destination.label) },
                                label = { Text(destination.label, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, maxLines = 1) },
                                colors = NavigationRailItemDefaults.colors(
                                    selectedIconColor = MessMatePalette.Forest,
                                    selectedTextColor = MessMatePalette.Forest,
                                    indicatorColor = MessMatePalette.Sage,
                                    unselectedIconColor = MessMatePalette.Muted,
                                    unselectedTextColor = MessMatePalette.Muted,
                                ),
                            )
                        }
                    }
                }

                Box(Modifier.weight(1f).fillMaxHeight()) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .fillMaxHeight()
                            .widthIn(max = if (expanded) 960.dp else 760.dp)
                            .fillMaxWidth(),
                    ) {
                        // Scaffold insets are already applied to the row above.
                        content(PaddingValues(0.dp))
                    }
                }
            }
        }
    }
}

/** Supplies one or two content columns based on the page's measured width. */
@Composable
fun ResponsiveContentArea(
    modifier: Modifier = Modifier,
    content: @Composable (columns: Int) -> Unit,
) {
    BoxWithConstraints(modifier) {
        val columns = if (maxWidth >= 800.dp) 2 else 1
        Box(Modifier.fillMaxSize()) { content(columns) }
    }
}

/** Adds a row-based adaptive grid to a LazyColumn without changing the phone layout. */
fun LazyListScope.responsiveRows(
    count: Int,
    columns: Int,
    itemContent: @Composable (index: Int) -> Unit,
) {
    val safeColumns = columns.coerceAtLeast(1)
    if (safeColumns == 1) {
        items(count) { index -> itemContent(index) }
    } else {
        items((count + safeColumns - 1) / safeColumns) { rowIndex ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                repeat(safeColumns) { columnIndex ->
                    val itemIndex = rowIndex * safeColumns + columnIndex
                    if (itemIndex < count) {
                        Box(Modifier.weight(1f)) { itemContent(itemIndex) }
                    } else {
                        Spacer(Modifier.weight(1f))
                    }
                }
            }
        }
    }
}
