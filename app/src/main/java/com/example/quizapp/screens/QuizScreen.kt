package com.example.quizapp.screens

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.quizapp.Exam
import com.example.quizapp.PersianDate
import com.example.quizapp.ui.theme.BlueSecondary
import com.example.quizapp.ui.theme.GreenDark
import com.example.quizapp.ui.theme.GreenLight
import com.example.quizapp.ui.theme.GreenPrimary

/** اگر تعداد سوال‌های انتخاب‌شده کمتر از این عدد باشد، سوال‌ها تصادفی پرسیده می‌شوند */
const val RANDOM_THRESHOLD = 50

private val RedWrong = Color(0xFFC62828)

/**
 * هر سوال در یک صفحه؛ پایین: «پاسخ»، سپس (بعد از دیدن پاسخ) «صحیح گفتم / اشتباه گفتم»، و «سوال بعدی».
 * در پایان تعداد «صحیح گفتم»‌ها نمایش داده می‌شود.
 */
@Composable
fun QuizScreen(exam: Exam, count: Int, onFinish: () -> Unit) {
    var attempt by remember { mutableIntStateOf(0) }

    // کمتر از ۵۰ سوال: تصادفی (هر سوال همراه پاسخ خودش جابه‌جا می‌شود). ۵۰ به بالا: به ترتیب آزمون
    val questions = remember(exam, count, attempt) {
        if (count < RANDOM_THRESHOLD) exam.questions.shuffled().take(count)
        else exam.questions.take(count)
    }
    val total = questions.size

    var index by remember(exam, count, attempt) { mutableIntStateOf(0) }
    var showAnswer by remember(exam, count, attempt) { mutableStateOf(false) }
    var finished by remember(exam, count, attempt) { mutableStateOf(false) }
    // نتیجهٔ هر سوال: true = صحیح گفتم، false = اشتباه گفتم، نبودن = بی‌پاسخ
    val results = remember(exam, count, attempt) { mutableStateMapOf<Int, Boolean>() }

    if (finished) {
        QuizResult(
            total = total,
            correct = results.values.count { it },
            wrong = results.values.count { !it },
            onRetry = { attempt += 1 },
            onFinish = onFinish
        )
    } else {
        val current = index.coerceIn(0, total - 1)
        val question = questions[current]
        val isLast = current == total - 1
        val graded = results[current]

        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            // بخش قابل اسکرول: سوال و پاسخ
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    "${exam.title} — سوال ${current + 1} از $total",
                    color = Color.Gray,
                    fontSize = 13.sp
                )
                Spacer(Modifier.height(12.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = question.q,
                            color = Color.Black,
                            fontSize = 18.sp,
                            lineHeight = 28.sp
                        )
                        Spacer(Modifier.height(10.dp))
                        Text(
                            "📅 تاریخ بارگذاری: ${PersianDate.format(question.addedAt)}",
                            color = Color.Gray,
                            fontSize = 12.sp
                        )
                    }
                }

                // پاسخ زیر سوال نمایش داده می‌شود
                if (showAnswer) {
                    Spacer(Modifier.height(16.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = GreenLight)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text("پاسخ:", color = GreenDark, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = question.a,
                                color = GreenDark,
                                fontSize = 18.sp,
                                lineHeight = 28.sp
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // دکمهٔ پاسخ
            Button(
                onClick = { showAnswer = true },
                enabled = !showAnswer,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = GreenPrimary,
                    contentColor = Color.White,
                    disabledContainerColor = Color(0xFFBDBDBD),
                    disabledContentColor = Color(0xFF616161)
                )
            ) {
                Text("پاسخ", fontSize = 18.sp)
            }

            // دو دکمهٔ جدید: بعد از دیدن پاسخ باز می‌شوند
            if (showAnswer) {
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = { results[current] = true },
                        modifier = Modifier.weight(1f).height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (graded == false) Color(0xFFE0E0E0) else GreenPrimary,
                            contentColor = if (graded == false) Color(0xFF757575) else Color.White
                        )
                    ) {
                        Text("✅ صحیح گفتم", fontSize = 15.sp)
                    }
                    Button(
                        onClick = { results[current] = false },
                        modifier = Modifier.weight(1f).height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (graded == true) Color(0xFFE0E0E0) else RedWrong,
                            contentColor = if (graded == true) Color(0xFF757575) else Color.White
                        )
                    ) {
                        Text("❌ اشتباه گفتم", fontSize = 15.sp)
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // سوال بعدی (در سوال آخر: پایان آزمون). بعد از دیدن پاسخ، باید یکی از دو گزینه انتخاب شود
            Button(
                onClick = {
                    if (isLast) {
                        finished = true
                    } else {
                        index = current + 1
                        showAnswer = false
                    }
                },
                enabled = !showAnswer || graded != null,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = BlueSecondary,
                    contentColor = Color.Black,
                    disabledContainerColor = Color(0xFFE0E0E0),
                    disabledContentColor = Color(0xFF9E9E9E)
                )
            ) {
                Text(if (isLast) "پایان آزمون" else "سوال بعدی", fontSize = 18.sp)
            }
        }
    }
}

@Composable
private fun QuizResult(
    total: Int,
    correct: Int,
    wrong: Int,
    onRetry: () -> Unit,
    onFinish: () -> Unit
) {
    val unanswered = total - correct - wrong
    val percent = if (total == 0) 0 else correct * 100 / total

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("پایان آزمون 🎉", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = GreenLight)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("✅ تعداد «صحیح گفتم»", color = GreenDark, fontSize = 16.sp)
                Spacer(Modifier.height(6.dp))
                Text(
                    "$correct از $total",
                    color = GreenDark,
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(6.dp))
                Text("$percent٪", color = GreenDark, fontSize = 18.sp)
            }
        }
        Spacer(Modifier.height(16.dp))
        Text("❌ اشتباه گفتم: $wrong", fontSize = 16.sp)
        if (unanswered > 0) {
            Spacer(Modifier.height(4.dp))
            Text("⏭ بدون پاسخ: $unanswered", fontSize = 16.sp, color = Color.Gray)
        }
        Spacer(Modifier.height(32.dp))

        Button(
            onClick = onRetry,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary, contentColor = Color.White)
        ) {
            Text("🔁 آزمون مجدد", fontSize = 18.sp)
        }
        Spacer(Modifier.height(12.dp))
        OutlinedButton(
            onClick = onFinish,
            modifier = Modifier.fillMaxWidth().height(56.dp)
        ) {
            Text("بازگشت", fontSize = 18.sp)
        }
    }
}
