package com.example.quizapp.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.quizapp.Book
import com.example.quizapp.R

/** صفحهٔ اصلی: انتخاب کتاب */
@Composable
fun HomeScreen(
    books: List<Book>,
    onOpenBook: (String) -> Unit,
    onTeacher: () -> Unit,
    onImport: () -> Unit
) {
    // کتاب‌های بدون آزمون به دانش‌آموز نشان داده نمی‌شوند
    val visibleBooks = books.filter { it.exams.isNotEmpty() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TextButton(onClick = onTeacher) { Text("👨‍🏫 پنل معلم") }
            TextButton(onClick = onImport) { Text("📥 دریافت آزمون") }
        }
        Spacer(Modifier.height(8.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(id = R.drawable.ic_app_logo),
                contentDescription = null,
                modifier = Modifier.size(48.dp)
            )
            Spacer(Modifier.width(12.dp))
            Text("آزمون‌ساز", style = MaterialTheme.typography.headlineMedium)
        }
        Spacer(Modifier.height(20.dp))

        if (visibleBooks.isEmpty()) {
            Text(
                "هنوز آزمونی وجود ندارد. از «پنل معلم» آزمون اضافه کنید یا با «دریافت آزمون» آزمون معلم را وارد کنید.",
                color = Color.Gray
            )
        } else {
            Text("کتاب را انتخاب کنید", color = Color.Gray)
            Spacer(Modifier.height(12.dp))
            visibleBooks.forEach { book ->
                Button(
                    onClick = { onOpenBook(book.name) },
                    modifier = Modifier.fillMaxWidth().height(56.dp)
                ) {
                    Text("${book.name} (${book.exams.size} آزمون)")
                }
                Spacer(Modifier.height(12.dp))
            }
        }
    }
}
