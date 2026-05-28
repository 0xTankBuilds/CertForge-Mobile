package com.az104.study.ui.screens.studyguides

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

private sealed class MarkdownBlock {
    data class Heading(val text: String, val level: Int) : MarkdownBlock()
    data class Paragraph(val text: String) : MarkdownBlock()
    data class BulletItem(val text: String) : MarkdownBlock()
    data class NumberedItem(val text: String, val number: Int) : MarkdownBlock()
    data class CodeBlock(val code: String) : MarkdownBlock()
    data class Table(val headers: List<String>, val rows: List<List<String>>) : MarkdownBlock()
    data object HorizontalRule : MarkdownBlock()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudyGuideScreen(
    articleId: String,
    onBackClick: () -> Unit = {},
    viewModel: StudyGuideViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(articleId) {
        viewModel.loadGuide(articleId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.guide?.title ?: "Study Guide") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            if (state.guide != null) {
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
                        imageVector = Icons.Default.Check,
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
                            Text("Loading study guide...")
                        }
                    }
                }

                state.notFound -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "No study guide available for this article yet.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                state.guide != null -> {
                    MarkdownContent(state.guide!!.content)
                }
            }
        }
    }
}

@Composable
private fun MarkdownContent(content: String) {
    val blocks = remember(content) { parseMarkdown(content) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        contentPadding = PaddingValues(vertical = 12.dp)
    ) {
        itemsIndexed(blocks, key = { index, _ -> index }) { _, block ->
            when (block) {
                is MarkdownBlock.Heading -> HeadingBlock(block)
                is MarkdownBlock.Paragraph -> ParagraphBlock(block)
                is MarkdownBlock.BulletItem -> BulletBlock(block)
                is MarkdownBlock.NumberedItem -> NumberedBlock(block)
                is MarkdownBlock.CodeBlock -> CodeBlockView(block)
                is MarkdownBlock.Table -> TableBlock(block)
                is MarkdownBlock.HorizontalRule -> HorizontalRuleView()
            }
        }
    }
}

@Composable
private fun HeadingBlock(block: MarkdownBlock.Heading) {
    val style = when (block.level) {
        2 -> MaterialTheme.typography.titleLarge
        3 -> MaterialTheme.typography.titleMedium
        else -> MaterialTheme.typography.titleMedium
    }
    val paddingTop = if (block.level == 2) 16.dp else 12.dp

    Text(
        text = parseInlineMarkdown(block.text),
        style = style,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(top = paddingTop, bottom = 4.dp)
    )
}

@Composable
private fun ParagraphBlock(block: MarkdownBlock.Paragraph) {
    Text(
        text = parseInlineMarkdown(block.text),
        style = MaterialTheme.typography.bodyMedium,
        lineHeight = MaterialTheme.typography.bodyMedium.lineHeight,
        modifier = Modifier.padding(vertical = 4.dp)
    )
}

@Composable
private fun BulletBlock(block: MarkdownBlock.BulletItem) {
    Row(modifier = Modifier.padding(vertical = 2.dp)) {
        Text(
            text = "•",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(end = 8.dp)
        )
        Text(
            text = parseInlineMarkdown(block.text),
            style = MaterialTheme.typography.bodyMedium,
            lineHeight = MaterialTheme.typography.bodyMedium.lineHeight
        )
    }
}

@Composable
private fun CodeBlockView(block: MarkdownBlock.CodeBlock) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 1.dp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(
            text = block.code,
            style = MaterialTheme.typography.bodySmall,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.padding(12.dp)
        )
    }
}

@Composable
private fun NumberedBlock(block: MarkdownBlock.NumberedItem) {
    Row(modifier = Modifier.padding(vertical = 2.dp)) {
        Text(
            text = "${block.number}.",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(end = 8.dp)
        )
        Text(
            text = parseInlineMarkdown(block.text),
            style = MaterialTheme.typography.bodyMedium,
            lineHeight = MaterialTheme.typography.bodyMedium.lineHeight
        )
    }
}

@Composable
private fun TableBlock(table: MarkdownBlock.Table) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        )
    ) {
        Column(modifier = Modifier.padding(0.dp)) {
            // Header row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .padding(horizontal = 12.dp, vertical = 10.dp)
            ) {
                table.headers.forEachIndexed { i, header ->
                    Text(
                        text = parseInlineMarkdown(header.trim()),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = if (i < table.headers.lastIndex) 8.dp else 0.dp)
                    )
                }
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            // Data rows
            table.rows.forEachIndexed { rowIndex, row ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            if (rowIndex % 2 == 1) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                            else MaterialTheme.colorScheme.surface
                        )
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    row.forEachIndexed { cellIndex, cell ->
                        Text(
                            text = parseInlineMarkdown(cell.trim()),
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = if (cellIndex < row.lastIndex) 8.dp else 0.dp)
                        )
                    }
                }
                if (rowIndex < table.rows.lastIndex) {
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                        thickness = 0.5.dp
                    )
                }
            }
        }
    }
}

@Composable
private fun HorizontalRuleView() {
    HorizontalDivider(
        modifier = Modifier.padding(vertical = 8.dp),
        color = MaterialTheme.colorScheme.outlineVariant
    )
}

// --- Parsing ---

private val numberedListRegex = Regex("^(\\d+)\\.\\s+(.*)")

private fun parseMarkdown(text: String): List<MarkdownBlock> {
    val lines = text.split("\n")
    val blocks = mutableListOf<MarkdownBlock>()
    var i = 0

    while (i < lines.size) {
        val line = lines[i]

        // Table block
        if (line.trimStart().startsWith("|") && !line.trimStart().startsWith("```")) {
            val tableLines = mutableListOf<String>()
            while (i < lines.size && lines[i].trimStart().startsWith("|")) {
                tableLines.add(lines[i].trim())
                i++
            }
            if (tableLines.size >= 2) {
                val parsed = parseTable(tableLines)
                if (parsed != null) {
                    blocks.add(parsed)
                    continue
                }
            }
            // Fallback: treat as individual lines
            tableLines.forEach { blocks.add(MarkdownBlock.Paragraph(it)) }
            continue
        }

        // Code block (fenced)
        if (line.trimStart().startsWith("```")) {
            val codeLines = mutableListOf<String>()
            i++
            while (i < lines.size && !lines[i].trimStart().startsWith("```")) {
                codeLines.add(lines[i])
                i++
            }
            blocks.add(MarkdownBlock.CodeBlock(codeLines.joinToString("\n")))
            i++ // skip closing ```
            continue
        }

        val trimmed = line.trim()

        when {
            trimmed.startsWith("### ") -> {
                blocks.add(MarkdownBlock.Heading(trimmed.removePrefix("### "), 3))
            }
            trimmed.startsWith("## ") -> {
                blocks.add(MarkdownBlock.Heading(trimmed.removePrefix("## "), 2))
            }
            trimmed.startsWith("# ") -> {
                blocks.add(MarkdownBlock.Heading(trimmed.removePrefix("# "), 1))
            }
            trimmed.startsWith("- ") || trimmed.startsWith("* ") -> {
                val content = trimmed.removePrefix("- ").removePrefix("* ")
                blocks.add(MarkdownBlock.BulletItem(content))
            }
            trimmed == "---" || trimmed == "***" || trimmed == "___" -> {
                blocks.add(MarkdownBlock.HorizontalRule)
            }
            trimmed.isEmpty() -> { /* skip blank lines */ }
            else -> {
                val numbered = numberedListRegex.find(trimmed)
                if (numbered != null) {
                    val number = numbered.groupValues[1].toIntOrNull() ?: 1
                    val content = numbered.groupValues[2]
                    blocks.add(MarkdownBlock.NumberedItem(content, number))
                } else {
                    blocks.add(MarkdownBlock.Paragraph(trimmed))
                }
            }
        }
        i++
    }

    return blocks
}

/**
 * Parses a markdown table block. The first row is the header, the second
 * row is the alignment separator (skipped), and subsequent rows are data.
 */
private fun parseTable(lines: List<String>): MarkdownBlock.Table? {
    if (lines.size < 2) return null
    val headers = parseTableRow(lines[0]) ?: return null

    // Second row is the separator (e.g. |---|:---|) — skip it
    val dataStart = if (lines.size > 1 && lines[1].contains("---")) 2 else 1
    if (dataStart >= lines.size) return MarkdownBlock.Table(headers, emptyList())

    val rows = mutableListOf<List<String>>()
    for (j in dataStart until lines.size) {
        val cells = parseTableRow(lines[j])
        if (cells != null) {
            // Pad or trim to match header count
            val normalized = if (cells.size < headers.size) {
                cells + List(headers.size - cells.size) { "" }
            } else {
                cells.take(headers.size)
            }
            rows.add(normalized)
        }
    }
    return MarkdownBlock.Table(headers, rows)
}

/** Splits a `|`-delimited table row into cell values. */
private fun parseTableRow(line: String): List<String>? {
    val trimmed = line.trim()
    if (!trimmed.startsWith("|") || !trimmed.endsWith("|")) return null
    val inner = trimmed.removeSurrounding("|")
    return inner.split("|").map { it.trim() }
}

/** Parses inline markdown: bold (**text**) and inline code (`code`). */
private fun parseInlineMarkdown(text: String) = buildAnnotatedString {
    // Match either **bold** or `inline code`
    val pattern = Regex("\\*\\*(.+?)\\*\\*|`([^`]+)`")
    var lastIndex = 0
    for (match in pattern.findAll(text)) {
        append(text.substring(lastIndex, match.range.first))
        when {
            match.groupValues[1].isNotEmpty() -> {
                withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                    append(match.groupValues[1])
                }
            }
            match.groupValues[2].isNotEmpty() -> {
                withStyle(SpanStyle(
                    fontFamily = FontFamily.Monospace,
                    background = Color(0xFFE8E8E8)
                )) {
                    append(match.groupValues[2])
                }
            }
        }
        lastIndex = match.range.last + 1
    }
    append(text.substring(lastIndex))
}
