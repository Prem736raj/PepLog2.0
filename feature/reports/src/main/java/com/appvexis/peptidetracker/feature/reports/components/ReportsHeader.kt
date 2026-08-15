package com.appvexis.peptidetracker.feature.reports.components

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.appvexis.peptidetracker.core.model.Protocol
import com.appvexis.peptidetracker.core.ui.components.PepLogChip
import com.appvexis.peptidetracker.core.ui.theme.OutfitFontFamily
import com.appvexis.peptidetracker.core.ui.theme.PepLogTheme
import com.appvexis.peptidetracker.feature.reports.model.ReportsTab
import com.appvexis.peptidetracker.feature.reports.model.TimeRangeFilter

/**
 * Top control bar with protocol selector, time range filter chips, and tab header.
 */
@Composable
fun ReportsHeader(
    protocols: List<Protocol>,
    selectedProtocolId: String?,
    onSelectProtocol: (String?) -> Unit,
    selectedTab: ReportsTab,
    onTabSelected: (ReportsTab) -> Unit,
    selectedTimeRange: TimeRangeFilter,
    onTimeRangeSelected: (TimeRangeFilter) -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    var expandedDropdown by remember { mutableStateOf(false) }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Protocol Selector & Refresh Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Dropdown Selector Button
            Box {
                val selectedName = if (selectedProtocolId == null) {
                    "All Protocols"
                } else {
                    protocols.find { it.id == selectedProtocolId }?.name ?: "All Protocols"
                }

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(PepLogTheme.colors.surface)
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = selectedName,
                        fontFamily = OutfitFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                        color = PepLogTheme.colors.textPrimary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    IconButton(
                        onClick = { expandedDropdown = true },
                        modifier = Modifier.size(20.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "Select Protocol",
                            tint = PepLogTheme.colors.primary
                        )
                    }
                }

                DropdownMenu(
                    expanded = expandedDropdown,
                    onDismissRequest = { expandedDropdown = false },
                    modifier = Modifier.background(PepLogTheme.colors.surfaceHigh)
                ) {
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = "All Protocols (Aggregate)",
                                color = if (selectedProtocolId == null) PepLogTheme.colors.primary else PepLogTheme.colors.textPrimary,
                                fontWeight = if (selectedProtocolId == null) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        onClick = {
                            onSelectProtocol(null)
                            expandedDropdown = false
                        }
                    )
                    protocols.forEach { proto ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = proto.name,
                                    color = if (selectedProtocolId == proto.id) PepLogTheme.colors.primary else PepLogTheme.colors.textPrimary,
                                    fontWeight = if (selectedProtocolId == proto.id) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            onClick = {
                                onSelectProtocol(proto.id)
                                expandedDropdown = false
                            }
                        )
                    }
                }
            }

            // Time Range Filter Chips
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TimeRangeFilter.entries.forEach { range ->
                    PepLogChip(
                        text = range.label,
                        selected = range == selectedTimeRange,
                        onClick = { onTimeRangeSelected(range) }
                    )
                }

                IconButton(
                    onClick = onRefresh,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Recompute Analytics",
                        tint = PepLogTheme.colors.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // Scrollable Tab Row
        PrimaryScrollableTabRow(
            selectedTabIndex = selectedTab.ordinal,
            containerColor = PepLogTheme.colors.background,
            contentColor = PepLogTheme.colors.primary,
            edgePadding = 0.dp,
            divider = {}
        ) {
            ReportsTab.entries.forEach { tab ->
                val selected = tab == selectedTab
                Tab(
                    selected = selected,
                    onClick = { onTabSelected(tab) },
                    text = {
                        Text(
                            text = tab.title,
                            fontFamily = OutfitFontFamily,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 14.sp,
                            color = if (selected) PepLogTheme.colors.primary else PepLogTheme.colors.textSecondary
                        )
                    }
                )
            }
        }
    }
}
