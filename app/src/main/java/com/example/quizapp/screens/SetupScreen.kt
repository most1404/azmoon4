package com.example.quizapp.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.quizapp.Exam
import com.example.quizapp.ui.theme.GreenPrimary

/** دانش‌آموز تعداد سوال‌ها را مشخص می‌کند */
@Composable
fun SetupScreen(bookName: String, exam: Exam, onStart: (Int) -> Unit, onBack: () -> Unit) {
    val total = exam.questions.size
    var countText by remember(exam) { mutableStateOf(total.toString()) }
    val count = countText.toIntOrNull()
    val valid = count != null && count in 1..total

    Column(
        modifier = Modifier.fillMaxSize().imePadding().padding(16.dp)
    ) {
        TextButton(onClick = onBack) { Text("→ بازگشت") }
        Spacer(Modifier.height(8.dp))
        Text("کتاب: $bookName", color = Color.Gray)
        Text(exam.title, style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(4.dp))
        Text("تعداد کل سوال‌ها: $total", color = Color.Gray)
        Spacer(Modifier.height(24.dp))

        OutlinedTextField(
            value = countText,
            onValueChange = { countText = it.filter { ch -> ch.isDigit() } },
            label = { Text("چند سوال می‌خواهید؟ (۱ تا $total)") },
            singleLine = true,
            isError = !valid,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))
        if (valid && count != null) {
            Text(
                if (count < RANDOM_THRESHOLD) "🔀 سوال‌ها به‌صورت تصادفی پرسیده می‌شوند"
                else "سوال‌ها به ترتیب آزمون پرسیده می‌شوند",
                color = Color.Gray,
                fontSize = 13.sp
            )
        }
        Spacer(Modifier.height(24.dp))

        Button(
            onClick = { if (count != null) onStart(count) },
            enabled = valid,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = GreenPrimary,
                contentColor = Color.White
            )
        ) {
            Text("شروع آزمون", fontSize = 18.sp)
        }
    }
}
