package com.certforge.app.ui.screens.quiz

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizResultsScreen(
    sessionClientId: String,
    onBackToQuizSelect: () -> Unit = {},
    onRetryIncorrect: () -> Unit = {},
    viewModel: QuizResultsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(sessionClientId) {
        viewModel.loadResults(sessionClientId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Quiz Results") },
                navigationIcon = {
                    IconButton(onClick = onBackToQuizSelect) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        when {
            state.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            state.notFound -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Results not found.", style = MaterialTheme.typography.bodyMedium)
                }
            }

            state.result != null -> {
                val result = state.result!!
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 16.dp)
                ) {
                    // Score card
                    item {
                        ScoreCard(
                            score = result.score,
                            correctCount = result.correctCount,
                            totalQuestions = result.totalQuestions,
                            answeredQuestions = result.answeredQuestions
                        )
                    }

                    // Incorrect count helper
                    val incorrectCount = result.answeredQuestions - result.correctCount

                    // Retry button
                    if (incorrectCount > 0) {
                        item {
                            OutlinedButton(
                                onClick = onRetryIncorrect,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Retry $incorrectCount Incorrect")
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Question Review",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Question review list
                    val attemptList = result.attempts
                    itemsIndexed(attemptList) { index, attempt ->
                        val question = result.questions[attempt.questionId]
                        if (question != null) {
                            QuestionReviewCard(
                                number = index + 1,
                                questionText = question.questionText,
                                options = question.options,
                                selectedIndex = attempt.selectedAnswerIndex,
                                correctIndex = question.correctAnswerIndex,
                                isCorrect = attempt.isCorrect
                            )
                        }
                    }

                    item { Spacer(modifier = Modifier.height(16.dp)) }
                }
            }
        }
    }
}

@Composable
private fun ScoreCard(
    score: Int,
    correctCount: Int,
    totalQuestions: Int,
    answeredQuestions: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                shape = CircleShape,
                color = if (score >= 70) Color(0xFF4CAF50).copy(alpha = 0.15f)
                       else MaterialTheme.colorScheme.error.copy(alpha = 0.15f),
                modifier = Modifier.size(120.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "$score%",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (score >= 70) Color(0xFF4CAF50) else MaterialTheme.colorScheme.error
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "$correctCount correct out of $answeredQuestions answered",
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center
            )
            if (totalQuestions > answeredQuestions) {
                Text(
                    text = "$answeredQuestions of $totalQuestions total questions answered",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun QuestionReviewCard(
    number: Int,
    questionText: String,
    options: List<String>,
    selectedIndex: Int,
    correctIndex: Int,
    isCorrect: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isCorrect)
                Color(0xFF4CAF50).copy(alpha = 0.05f)
            else
                MaterialTheme.colorScheme.error.copy(alpha = 0.05f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    if (isCorrect) Icons.Default.Check else Icons.Default.Close,
                    contentDescription = null,
                    tint = if (isCorrect) Color(0xFF4CAF50) else MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Question $number",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = questionText,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            options.forEachIndexed { i, option ->
                val label = "${('A' + i)}. ${option.removePrefix("${('A' + i)}. ").removePrefix("${('A' + i)}.")}"
                val isSelected = i == selectedIndex
                val isAnswer = i == correctIndex

                val color = when {
                    isAnswer && isSelected -> Color(0xFF4CAF50)
                    isAnswer -> Color(0xFF4CAF50).copy(alpha = 0.7f)
                    isSelected -> MaterialTheme.colorScheme.error
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                }
                val weight = if (isAnswer || isSelected) FontWeight.Bold else FontWeight.Normal

                Text(
                    text = label,
                    style = MaterialTheme.typography.bodySmall,
                    color = color,
                    fontWeight = weight,
                    modifier = Modifier.padding(vertical = 2.dp)
                )
            }
        }
    }
}
