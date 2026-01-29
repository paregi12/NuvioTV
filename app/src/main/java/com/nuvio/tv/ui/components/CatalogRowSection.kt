package com.nuvio.tv.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.unit.dp
import androidx.tv.foundation.lazy.list.TvLazyRow
import androidx.tv.foundation.lazy.list.itemsIndexed
import androidx.tv.foundation.lazy.list.rememberTvLazyListState
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.nuvio.tv.domain.model.CatalogRow
import com.nuvio.tv.ui.theme.NuvioColors

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun CatalogRowSection(
    catalogRow: CatalogRow,
    rowIndex: Int,
    isRestoreFocus: Boolean,
    onItemClick: (String, String, String) -> Unit,
    onRowFocused: (Int) -> Unit,
    onLoadMore: () -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberTvLazyListState()

    // Track the focused item index within this row
    var focusedItemIndex by rememberSaveable { mutableIntStateOf(0) }

    // Create focus requesters for each item
    val focusRequesters = remember(catalogRow.items.size) {
        List(catalogRow.items.size) { FocusRequester() }
    }

    // Restore focus when returning to this screen
    LaunchedEffect(isRestoreFocus, catalogRow.items.size) {
        if (isRestoreFocus && catalogRow.items.isNotEmpty()) {
            val safeIndex = focusedItemIndex.coerceIn(0, catalogRow.items.size - 1)
            try {
                focusRequesters.getOrNull(safeIndex)?.requestFocus()
            } catch (_: Exception) {
                // Focus request might fail if view is not ready
            }
        }
    }

    val shouldLoadMore by remember {
        derivedStateOf {
            val lastVisibleItem = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            val totalItems = listState.layoutInfo.totalItemsCount
            lastVisibleItem >= totalItems - 5 && catalogRow.hasMore && !catalogRow.isLoading
        }
    }

    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore) {
            onLoadMore()
        }
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 48.dp, end = 48.dp, bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = catalogRow.catalogName,
                    style = MaterialTheme.typography.headlineMedium,
                    color = NuvioColors.TextPrimary
                )
                Text(
                    text = "from ${catalogRow.addonName}",
                    style = MaterialTheme.typography.labelMedium,
                    color = NuvioColors.TextTertiary
                )
            }
        }

        TvLazyRow(
            state = listState,
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 48.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            itemsIndexed(
                items = catalogRow.items,
                key = { _, item -> "${catalogRow.type}_${catalogRow.catalogId}_${item.id}" }
            ) { index, item ->
                ContentCard(
                    item = item,
                    onClick = { onItemClick(item.id, item.type.toApiString(), catalogRow.addonBaseUrl) },
                    modifier = Modifier
                        .then(
                            if (index < focusRequesters.size) {
                                Modifier.focusRequester(focusRequesters[index])
                            } else {
                                Modifier
                            }
                        )
                        .onFocusChanged { focusState ->
                            if (focusState.isFocused) {
                                focusedItemIndex = index
                                onRowFocused(rowIndex)
                            }
                        }
                )
            }

            if (catalogRow.isLoading) {

                item {

                    Box(
                        modifier = Modifier
                            .width(150.dp)
                            .height(225.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        LoadingIndicator()
                    }
                }

            }
        }
    }
}
