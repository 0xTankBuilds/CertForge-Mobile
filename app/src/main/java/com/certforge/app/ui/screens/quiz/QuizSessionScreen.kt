package com.certforge.app.ui.screens.quiz

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizSessionScreen(
    domainId: String? = null,
    onQuizComplete: (String) -> Unit = {},
    onBackClick: () -> Unit = {},
    viewModel: QuizSessionViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.completedSessionId.collect { clientId ->
            onQuizComplete(clientId)
        }
    }

    LaunchedEffect(domainId) {
        viewModel.startQuiz(domainId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (!state.isLoading && state.questions.isNotEmpty()) {
                        Text("Q ${state.currentIndex + 1}/${state.totalQuestions}")
                    } else {
                        Text("Quiz")
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Exit")
                    }
                }
            )
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
                            Text("Loading questions...")
                        }
                    }
                }

                state.isEmpty -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
                            Text(
                                "No questions available",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Sync the app to download questions for this domain.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                state.currentQuestion != null -> {
                    QuestionView(
                        questionText = state.currentQuestion!!.questionText,
                        options = state.currentQuestion!!.options,
                        selectedAnswer = state.selectedAnswer,
                        correctAnswer = state.currentQuestion!!.correctAnswerIndex,
                        isAnswered = state.isAnswered,
                        onSelectAnswer = { viewModel.selectAnswer(it) },
                        onNext = { viewModel.nextQuestion() },
                        isLastQuestion = state.isLastQuestion,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}

@Composable
private fun QuestionView(
    questionText: String,
    options: List<String>,
    selectedAnswer: Int?,
    correctAnswer: Int,
    isAnswered: Boolean,
    onSelectAnswer: (Int) -> Unit,
    onNext: () -> Unit,
    isLastQuestion: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Question text
        Text(
            text = questionText,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // Options
        options.forEachIndexed { index, option ->
            OptionCard(
                index = index,
                text = option,
                isSelected = selectedAnswer == index,
                isCorrect = isAnswered && index == correctAnswer,
                isIncorrect = isAnswered && selectedAnswer == index && index != correctAnswer,
                enabled = !isAnswered,
                onClick = { onSelectAnswer(index) }
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        Spacer(modifier = Modifier.weight(1f))

        // Next/Finish button
        if (isAnswered) {
            Button(
                onClick = onNext,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
            ) {
                Text(if (isLastQuestion) "See Results" else "Next")
            }
        }
    }
}

@Composable
private fun OptionCard(
    index: Int,
    text: String,
    isSelected: Boolean,
    isCorrect: Boolean,
    isIncorrect: Boolean,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val containerColor by animateColorAsState(
        targetValue = when {
            isCorrect -> Color(0xFF4CAF50).copy(alpha = 0.15f)
            isIncorrect -> MaterialTheme.colorScheme.error.copy(alpha = 0.15f)
            isSelected -> MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        },
        label = "optionColor"
    )

    val borderColor by animateColorAsState(
        targetValue = when {
            isCorrect -> Color(0xFF4CAF50)
            isIncorrect -> MaterialTheme.colorScheme.error
            isSelected -> MaterialTheme.colorScheme.primary
            else -> Color.Transparent
        },
        label = "borderColor"
    )

    Card(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = if (isSelected || isCorrect || isIncorrect) {
            CardDefaults.outlinedCardBorder().copy(
                brush = androidx.compose.ui.graphics.SolidColor(borderColor),
                width = 2.dp
            )
        } else {
            CardDefaults.outlinedCardBorder()
        }
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${('A' + index)}.",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = when {
                    isCorrect -> Color(0xFF4CAF50)
                    isIncorrect -> MaterialTheme.colorScheme.error
                    isSelected -> MaterialTheme.colorScheme.primary
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                },
                modifier = Modifier.width(28.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = text.removePrefix("${('A' + index)}. ").removePrefix("${('A' + index)}."),
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}
