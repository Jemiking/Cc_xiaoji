package com.ccxiaoji.feature.ledger.presentation.screen.category.components

import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.ccxiaoji.feature.ledger.presentation.viewmodel.CategoryTab
import com.ccxiaoji.ui.theme.DesignTokens

@Composable
fun CategoryTabRow(
    selectedTab: CategoryTab,
    onTabSelected: (CategoryTab) -> Unit,
    modifier: Modifier = Modifier
) {
    TabRow(
        selectedTabIndex = if (selectedTab == CategoryTab.EXPENSE) 0 else 1,
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        indicator = { tabPositions ->
            TabRowDefaults.Indicator(
                Modifier.tabIndicatorOffset(tabPositions[if (selectedTab == CategoryTab.EXPENSE) 0 else 1]),
                color = DesignTokens.BrandColors.Ledger
            )
        }
    ) {
        Tab(
            selected = selectedTab == CategoryTab.EXPENSE,
            onClick = { onTabSelected(CategoryTab.EXPENSE) },
            text = { 
                Text(
                    text = "支出分类",
                    color = if (selectedTab == CategoryTab.EXPENSE) {
                        DesignTokens.BrandColors.Ledger
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
        )
        Tab(
            selected = selectedTab == CategoryTab.INCOME,
            onClick = { onTabSelected(CategoryTab.INCOME) },
            text = { 
                Text(
                    text = "收入分类",
                    color = if (selectedTab == CategoryTab.INCOME) {
                        DesignTokens.BrandColors.Ledger
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
        )
    }
}