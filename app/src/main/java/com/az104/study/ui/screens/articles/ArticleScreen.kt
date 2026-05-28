package com.az104.study.ui.screens.articles

import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArticleScreen(
    articleId: String,
    onBackClick: () -> Unit = {},
    onStudyGuideClick: () -> Unit = {},
    viewModel: ArticleViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(articleId) {
        viewModel.loadArticle(articleId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.article?.title ?: "Article") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (state.hasStudyGuide) {
                        IconButton(onClick = onStudyGuideClick) {
                            Icon(Icons.AutoMirrored.Filled.MenuBook, contentDescription = "Study Guide")
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            if (state.article != null) {
                val completed = state.isCompleted
                FloatingActionButton(
                    onClick = { viewModel.markComplete() },
                    containerColor = if (completed)
                        MaterialTheme.colorScheme.primaryContainer
                    else
                        MaterialTheme.colorScheme.primary,
                    contentColor = if (completed)
                        MaterialTheme.colorScheme.onPrimaryContainer
                    else
                        MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(
                        imageVector = if (completed) Icons.Default.Check else Icons.Default.Check,
                        contentDescription = if (completed) "Mark incomplete" else "Mark complete"
                    )
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                state.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator()
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Loading article...")
                        }
                    }
                }

                state.error != null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = state.error ?: "Unknown error",
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            OutlinedButton(onClick = { viewModel.loadArticle(articleId) }) {
                                Text("Retry")
                            }
                        }
                    }
                }

                state.article?.html != null -> {
                    AndroidView<android.view.View>(
                        modifier = Modifier.fillMaxSize(),
                        factory = { context ->
                            try {
                                WebView(context).apply {
                                    settings.javaScriptEnabled = false
                                    settings.loadWithOverviewMode = true
                                    settings.useWideViewPort = true
                                    settings.domStorageEnabled = false
                                    webViewClient = WebViewClient()
                                }
                            } catch (e: Exception) {
                                android.view.View(context)
                            }
                        },
                        update = { view ->
                            val webView = view as? WebView ?: return@AndroidView
                            try {
                                state.article?.html?.let { html ->
                                    webView.stopLoading()
                                    webView.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null)
                                }
                            } catch (_: Exception) {
                                // WebView can throw on malformed content
                            }
                        }
                    )
                }

                state.article != null && state.article?.html == null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator()
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Loading article content...")
                        }
                    }
                }
            }
        }
    }
}
