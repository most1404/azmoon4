package com.example.quizapp.screens

import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.quizapp.ExamText
import com.example.quizapp.SharedExam
import com.example.quizapp.ui.theme.GreenPrimary

/** دانش‌آموز متن آزمونِ ارسالی معلم را اینجا وارد می‌کند؛ آزمون داخل کتاب خودش ذخیره می‌شود */
@Composable
fun ImportScreen(initialText: String = "", onImported: (SharedExam) -> Unit, onBack: () -> Unit) {
    val context = LocalContext.current
    var text by remember(initialText) { mutableStateOf(initialText) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        TextButton(onClick = onBack) { Text("→ بازگشت") }
        Text("دریافت آزمون", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(8.dp))
        Text(
            "لینک آزمون (یا کل پیامی که معلم فرستاده) را کپی کنید و اینجا بچسبانید. آزمون با سوال‌ها و پاسخ‌ها ذخیره می‌شود.",
            color = Color.Gray,
            fontSize = 13.sp
        )
        Spacer(Modifier.height(12.dp))

        OutlinedButton(
            onClick = {
                val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = cm.primaryClip
                val pasted = if (clip != null && clip.itemCount > 0) {
                    clip.getItemAt(0).coerceToText(context)?.toString().orEmpty()
                } else ""
                if (pasted.isBlank()) {
                    Toast.makeText(context, "چیزی کپی نشده است", Toast.LENGTH_SHORT).show()
                } else {
                    text = pasted
                }
            },
            modifier = Modifier.fillMaxWidth().height(48.dp)
        ) {
            Text("📋 چسباندن از کلیپ‌بورد")
        }
        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            label = { Text("لینک آزمون") },
            minLines = 8,
            maxLines = 14,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(16.dp))

        Button(
            onClick = {
                val shared = ExamText.fromShareText(text)
                if (shared == null) {
                    Toast.makeText(
                        context,
                        "لینک آزمون معتبر نیست. باید همان لینکی باشد که معلم با دکمهٔ «ارسال» ساخته (کامل کپی شود).",
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    Toast.makeText(context, "آزمون «${shared.exam.title}» ذخیره شد", Toast.LENGTH_SHORT).show()
                    onImported(shared)
                }
            },
            enabled = text.isNotBlank(),
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = GreenPrimary,
                contentColor = Color.White
            )
        ) {
            Text("ذخیرهٔ آزمون", fontSize = 18.sp)
        }
    }
}
