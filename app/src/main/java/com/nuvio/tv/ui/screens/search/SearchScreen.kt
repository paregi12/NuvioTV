package com.nuvio.tv.ui.screens.search

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.unit.dp
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextFieldDefaults
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.tv.foundation.lazy.list.TvLazyColumn
import androidx.tv.foundation.lazy.list.itemsIndexed
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.nuvio.tv.ui.components.CatalogRowSection
import com.nuvio.tv.ui.components.ErrorState
import com.nuvio.tv.ui.components.LoadingIndicator
import com.nuvio.tv.ui.theme.NuvioColors

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun SearchScreen(
    viewModel: SearchViewModel = hiltViewModel(),
    onNavigateToDetail: (String, String, String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    val searchFocusRequester = remember { FocusRequester() }

    // Track focused row index to restore focus after navigation
    var focusedRowIndex by rememberSaveable { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        try {
            searchFocusRequester.requestFocus()
        } catch (_: Exception) {
            // Focus request might fail if view is not ready
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(NuvioColors.Background),
        contentAlignment = Alignment.TopCenter
    ) {
        TvLazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(28.dp)
        ) {
            item {
                OutlinedTextField(
                    value = uiState.query,
                    onValueChange = { viewModel.onEvent(SearchEvent.QueryChanged(it)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 48.dp)
                        .focusRequester(searchFocusRequester),
                    singleLine = true,
                    placeholder = {
                        Text(
                            text = "Search movies & series (installed addons)",
                            color = NuvioColors.TextTertiary
                        )
                    },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = NuvioColors.BackgroundCard,
                        unfocusedContainerColor = NuvioColors.BackgroundCard,
                        focusedIndicatorColor = NuvioColors.FocusRing,
                        unfocusedIndicatorColor = NuvioColors.Border,
                        focusedTextColor = NuvioColors.TextPrimary,
                        unfocusedTextColor = NuvioColors.TextPrimary,
                        cursorColor = NuvioColors.FocusRing
                    )
                )
            }

            when {
                uiState.query.trim().length < 2 -> {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 48.dp, vertical = 12.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Text(
                                text = "Type at least 2 characters to search.",
                                style = MaterialTheme.typography.titleMedium,
                                color = NuvioColors.TextTertiary
                            )
                        }
                    }
                }

                uiState.isSearching && uiState.catalogRows.isEmpty() -> {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 80.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            LoadingIndicator()
                        }
                    }
                }

                uiState.error != null && uiState.catalogRows.isEmpty() -> {
                    item {
                        ErrorState(
                            message = uiState.error ?: "Search failed",
                            onRetry = { viewModel.onEvent(SearchEvent.Retry) }
                        )
                    }
                }

                uiState.catalogRows.isEmpty() -> {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 48.dp, vertical = 12.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Text(
                                text = "No results.",
                                style = MaterialTheme.typography.titleMedium,
                                color = NuvioColors.TextTertiary
                            )
                        }
                    }
                }

                else -> {
                    itemsIndexed(
                        items = uiState.catalogRows,
                        key = { _, item -> "${item.addonId}_${item.type}_${item.catalogId}_${uiState.query.trim()}" }
                    ) { index, catalogRow ->
                        CatalogRowSection(
                            catalogRow = catalogRow,
                            rowIndex = index,
                            isRestoreFocus = index == focusedRowIndex,
                            onItemClick = { id, type, addonBaseUrl ->
                                onNavigateToDetail(id, type, addonBaseUrl)
                            },
                            onRowFocused = { rowIndex ->
                                focusedRowIndex = rowIndex
                            },
                            onLoadMore = {
                                viewModel.onEvent(
                                    SearchEvent.LoadMoreCatalog(
                                        catalogId = catalogRow.catalogId,
                                        addonId = catalogRow.addonId,
                                        type = catalogRow.type.toApiString()
                                    )
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}
