package com.certforge.app.ui.screens.domains

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DomainChaptersScreen(
    domainId: String,
    onArticleClick: (String) -> Unit = {},
    onStudyGuideClick: (String) -> Unit = {},
    onBackClick: () -> Unit = {},
    viewModel: DomainChaptersViewModel = hiltViewModel()
) {
    val domain by viewModel.domain.collectAsStateWithLifecycle()
    val chapters by viewModel.chapters.collectAsStateWithLifecycle()

    LaunchedEffect(domainId) {
        viewModel.loadDomain(domainId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(domain?.name ?: "Chapters") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (chapters.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Loading chapters...")
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(chapters, key = { it.chapter.id }) { cwp ->
                    val completed = cwp.completedAt != null
                    ChapterCard(
                        title = cwp.chapter.title,
                        completed = completed,
                        onToggleComplete = { viewModel.toggleChapterComplete(cwp.chapter.id, completed) },
                        onStudyGuide = { onStudyGuideClick(cwp.chapter.articleId) },
                        onArticle = { onArticleClick(cwp.chapter.articleId) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ChapterCard(
    title: String,
    completed: Boolean,
    onToggleComplete: () -> Unit,
    onStudyGuide: () -> Unit,
    onArticle: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onStudyGuide() }
    ) {
        Row(
            modifier = Modifier.padding(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onToggleComplete) {
                Icon(
                    if (completed) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                    contentDescription = if (completed) "Mark incomplete" else "Mark complete",
                    tint = if (completed) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(onClick = onArticle) {
                Icon(Icons.AutoMirrored.Filled.MenuBook, contentDescription = "Read article")
            }
        }
    }
}
