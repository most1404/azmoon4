package com.example.quizapp.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.quizapp.Book
import com.example.quizapp.Exam
import com.example.quizapp.ExamStore
import com.example.quizapp.ui.theme.GreenPrimary

/** پنل معلم — مرحلهٔ ۲: آزمون‌های یک کتاب */
@Composable
fun TeacherBookScreen(
    books: List<Book>,
    bookName: String,
    onBooksChange: (List<Book>) -> Unit,
    onEdit: (String?) -> Unit,
    onBack: () -> Unit
) {
    val exams = books.firstOrNull { it.name == bookName }?.exams ?: emptyList()
    var shareTarget by remember { mutableStateOf<Exam?>(null) }
    var deleteTarget by remember { mutableStateOf<Exam?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        TextButton(onClick = onBack) { Text("→ بازگشت") }
        Text("کتاب: $bookName", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(16.dp))

        Button(
            onClick = { onEdit(null) },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = GreenPrimary,
                contentColor = Color.White
            )
        ) {
            Text("➕ افزودن آزمون", fontSize = 16.sp)
        }
        Spacer(Modifier.height(20.dp))

        if (exams.isEmpty()) {
            Text("هنوز آزمونی در این کتاب نیست.", color = Color.Gray)
        }
        exams.forEach { exam ->
            Column(modifier = Modifier.fillMaxWidth()) {
                Text("${exam.title} (${exam.questions.size} سوال)", fontSize = 16.sp)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = { onEdit(exam.title) }) { Text("✏️ ویرایش") }
                    TextButton(onClick = { shareTarget = exam }) { Text("📤 ارسال") }
                    TextButton(onClick = { deleteTarget = exam }) { Text("🗑 حذف") }
                }
            }
            Spacer(Modifier.height(8.dp))
        }
    }

    val sharing = shareTarget
    if (sharing != null) {
        ShareExamDialog(bookName = bookName, exam = sharing, onDismiss = { shareTarget = null })
    }

    val deleting = deleteTarget
    if (deleting != null) {
        ConfirmDeleteDialog(
            title = "آیا مطمئن هستید؟",
            message = "آزمون «${deleting.title}» با همهٔ سوال‌ها و پاسخ‌هایش حذف می‌شود و قابل بازگشت نیست.",
            onConfirm = {
                onBooksChange(ExamStore.deleteExam(books, bookName, deleting.title))
                deleteTarget = null
            },
            onDismiss = { deleteTarget = null }
        )
    }
}
