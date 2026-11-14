package com.example.quizapp.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.quizapp.Book
import com.example.quizapp.ExamStore
import com.example.quizapp.ui.theme.GreenPrimary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** پنل معلم — مرحلهٔ ۱: مدیریت کتاب‌ها */
@Composable
fun TeacherScreen(
    books: List<Book>,
    onBooksChange: (List<Book>) -> Unit,
    onOpenBook: (String) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var newBook by remember { mutableStateOf("") }
    var renameTarget by remember { mutableStateOf<Book?>(null) }
    var renameText by remember { mutableStateOf("") }
    var deleteTarget by remember { mutableStateOf<Book?>(null) }
    var pendingRestore by remember { mutableStateOf<List<Book>?>(null) }

    // پشتیبان‌گیری: ذخیرهٔ همهٔ کتاب‌ها و آزمون‌ها در یک فایل (پوشهٔ دلخواه، مثلاً Drive یا حافظهٔ گوشی)
    val backupLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                context.contentResolver.openOutputStream(uri)?.use {
                    it.write(ExamStore.toBackupJson(books).toByteArray(Charsets.UTF_8))
                }
                Toast.makeText(context, "پشتیبان ذخیره شد", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(context, "ذخیرهٔ پشتیبان انجام نشد", Toast.LENGTH_LONG).show()
            }
        }
    }
    val restoreLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            val text = try {
                context.contentResolver.openInputStream(uri)?.use { it.readBytes().toString(Charsets.UTF_8) }
            } catch (e: Exception) {
                null
            }
            val restored = text?.let { ExamStore.fromBackupJson(it) }
            if (restored == null) {
                Toast.makeText(context, "فایل پشتیبان معتبر نیست", Toast.LENGTH_LONG).show()
            } else {
                pendingRestore = restored
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        TextButton(onClick = onBack) { Text("→ بازگشت") }
        Text("پنل معلم", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(8.dp))
        Text(
            "ابتدا کتاب را بسازید، سپس داخل آن آزمون‌ها را اضافه کنید.",
            color = Color.Gray,
            fontSize = 13.sp
        )
        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = newBook,
            onValueChange = { newBook = it },
            label = { Text("نام کتاب (مثلاً عربی هفتم)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))
        Button(
            onClick = {
                val name = newBook.trim()
                if (books.any { it.name == name }) {
                    Toast.makeText(context, "این کتاب قبلاً ساخته شده", Toast.LENGTH_SHORT).show()
                } else {
                    onBooksChange(ExamStore.addBook(books, name))
                    newBook = ""
                }
            },
            enabled = newBook.isNotBlank(),
            modifier = Modifier.fillMaxWidth().height(52.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = GreenPrimary,
                contentColor = Color.White
            )
        ) {
            Text("➕ افزودن کتاب", fontSize = 16.sp)
        }

        Spacer(Modifier.height(16.dp))
        Text("پشتیبان‌گیری", color = Color.Gray, fontSize = 13.sp)
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = {
                    val stamp = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
                    backupLauncher.launch("azmoonsaz-backup-$stamp.json")
                },
                modifier = Modifier.weight(1f).height(48.dp)
            ) { Text("💾 گرفتن پشتیبان") }
            OutlinedButton(
                onClick = { restoreLauncher.launch(arrayOf("*/*")) },
                modifier = Modifier.weight(1f).height(48.dp)
            ) { Text("♻️ بازیابی") }
        }

        Spacer(Modifier.height(24.dp))
        if (books.isNotEmpty()) {
            Text("کتاب‌ها (برای ورود، روی نام کتاب بزنید)", color = Color.Gray, fontSize = 13.sp)
            Spacer(Modifier.height(8.dp))
        }
        books.forEach { book ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = { onOpenBook(book.name) },
                    modifier = Modifier.weight(1f).height(52.dp)
                ) {
                    Text("${book.name} (${book.exams.size} آزمون)")
                }
                TextButton(onClick = {
                    renameTarget = book
                    renameText = book.name
                }) { Text("✏️") }
                TextButton(onClick = { deleteTarget = book }) { Text("🗑") }
            }
            Spacer(Modifier.height(8.dp))
        }
    }

    val renaming = renameTarget
    if (renaming != null) {
        AlertDialog(
            onDismissRequest = { renameTarget = null },
            title = { Text("ویرایش نام کتاب") },
            text = {
                OutlinedTextField(
                    value = renameText,
                    onValueChange = { renameText = it },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    val name = renameText.trim()
                    if (name.isEmpty() || (name != renaming.name && books.any { it.name == name })) {
                        Toast.makeText(context, "نام معتبر نیست یا تکراری است", Toast.LENGTH_SHORT).show()
                    } else {
                        onBooksChange(ExamStore.renameBook(books, renaming.name, name))
                        renameTarget = null
                    }
                }) { Text("ذخیره") }
            },
            dismissButton = {
                TextButton(onClick = { renameTarget = null }) { Text("انصراف") }
            }
        )
    }

    val deleting = deleteTarget
    if (deleting != null) {
        ConfirmDeleteDialog(
            title = "آیا مطمئن هستید؟",
            message = "کتاب «${deleting.name}» با همهٔ ${deleting.exams.size} آزمونِ آن حذف می‌شود و قابل بازگشت نیست.",
            onConfirm = {
                onBooksChange(ExamStore.deleteBook(books, deleting.name))
                deleteTarget = null
            },
            onDismiss = { deleteTarget = null }
        )
    }

    val restoring = pendingRestore
    if (restoring != null) {
        AlertDialog(
            onDismissRequest = { pendingRestore = null },
            title = { Text("بازیابی پشتیبان") },
            text = {
                Text(
                    "این پشتیبان ${restoring.size} کتاب دارد.\n" +
                        "«ادغام»: به داده‌های فعلی اضافه می‌شود (آزمونِ هم‌نام با نسخهٔ پشتیبان عوض می‌شود).\n" +
                        "«جایگزینی»: همهٔ داده‌های فعلی پاک و با پشتیبان عوض می‌شود."
                )
            },
            confirmButton = {
                Row {
                    TextButton(onClick = {
                        onBooksChange(ExamStore.merge(books, restoring))
                        pendingRestore = null
                        Toast.makeText(context, "پشتیبان ادغام شد", Toast.LENGTH_SHORT).show()
                    }) { Text("ادغام") }
                    TextButton(onClick = {
                        onBooksChange(restoring)
                        pendingRestore = null
                        Toast.makeText(context, "پشتیبان بازیابی شد", Toast.LENGTH_SHORT).show()
                    }) { Text("جایگزینی") }
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingRestore = null }) { Text("انصراف") }
            }
        )
    }
}
