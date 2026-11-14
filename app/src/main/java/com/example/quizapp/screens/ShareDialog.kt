package com.example.quizapp.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.quizapp.Exam
import com.example.quizapp.ExamText

fun copyToClipboard(context: Context, text: String) {
    val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    cm.setPrimaryClip(ClipData.newPlainText("exam", text))
}

/** پیش‌نمایش متنی که ارسال می‌شود + کپی و ارسال */
@Composable
fun ShareExamDialog(bookName: String, exam: Exam, onDismiss: () -> Unit) {
    val context = LocalContext.current
    val text = remember(bookName, exam) { ExamText.toShareText(bookName, exam) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("ارسال آزمون") },
        text = {
            Column {
                Text("این پیام (همراه لینک آزمون) برای دانش‌آموز ارسال می‌شود:", fontSize = 13.sp, color = Color.Gray)
                Spacer(Modifier.height(8.dp))
                Column(
                    modifier = Modifier
                        .heightIn(max = 240.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(text, fontSize = 13.sp)
                }
            }
        },
        confirmButton = {
            Row {
                TextButton(onClick = {
                    copyToClipboard(context, text)
                    Toast.makeText(context, "لینک آزمون کپی شد", Toast.LENGTH_SHORT).show()
                }) { Text("کپی لینک") }
                TextButton(onClick = {
                    // برای اطمینان، متن در کلیپ‌بورد هم قرار می‌گیرد
                    copyToClipboard(context, text)
                    val sendIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_SUBJECT, exam.title)
                        putExtra(Intent.EXTRA_TEXT, text)
                    }
                    context.startActivity(Intent.createChooser(sendIntent, "ارسال آزمون"))
                    onDismiss()
                }) { Text("ارسال") }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("بستن") }
        }
    )
}
