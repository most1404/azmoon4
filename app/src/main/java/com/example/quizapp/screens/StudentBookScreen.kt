package com.example.quizapp.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.quizapp.Book
import com.example.quizapp.Exam

/** انتخاب آزمون از یک کتاب */
@Composable
fun StudentBookScreen(book: Book, onSelect: (Exam) -> Unit, onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        TextButton(onClick = onBack) { Text("→ بازگشت") }
        Text("کتاب: ${book.name}", color = Color.Gray)
        Spacer(Modifier.height(8.dp))
        Text("آزمون را انتخاب کنید", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(16.dp))

        if (book.exams.isEmpty()) {
            Text("این کتاب هنوز آزمونی ندارد.", color = Color.Gray)
        }
        book.exams.forEach { exam ->
            Button(
                onClick = { onSelect(exam) },
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) {
                Text("${exam.title} (${exam.questions.size} سوال)")
            }
            Spacer(Modifier.height(12.dp))
        }
    }
}
