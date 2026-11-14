package com.example.quizapp

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.core.view.WindowCompat
import com.example.quizapp.screens.ExamEditorScreen
import com.example.quizapp.screens.HomeScreen
import com.example.quizapp.screens.ImportScreen
import com.example.quizapp.screens.QuizScreen
import com.example.quizapp.screens.SetupScreen
import com.example.quizapp.screens.StudentBookScreen
import com.example.quizapp.screens.TeacherBookScreen
import com.example.quizapp.screens.TeacherScreen
import com.example.quizapp.ui.theme.QuizAppTheme

class MainActivity : ComponentActivity() {
    // لینک آزمونی که از بیرون برنامه (ضربه روی لینک) رسیده است
    private val incomingLink = mutableStateOf<String?>(null)

    private fun linkFrom(intent: Intent?): String? =
        if (intent?.action == Intent.ACTION_VIEW) intent?.dataString else null

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        incomingLink.value = linkFrom(intent)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        incomingLink.value = linkFrom(intent)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            QuizAppTheme {
                // اجبار RTL برای کل رابط کاربری
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    AppRoot(
                        incomingLink = incomingLink.value,
                        onLinkHandled = { incomingLink.value = null }
                    )
                }
            }
        }
    }
}

/** صفحه‌های اپ */
sealed interface Screen {
    object Home : Screen
    data class Import(val prefill: String = "") : Screen
    object Teacher : Screen
    data class StudentBook(val bookName: String) : Screen
    data class Setup(val bookName: String, val exam: Exam) : Screen
    data class Quiz(val bookName: String, val exam: Exam, val count: Int) : Screen
    data class TeacherBook(val bookName: String) : Screen
    data class ExamEditor(val bookName: String, val examTitle: String?) : Screen
}

@Composable
fun AppRoot(incomingLink: String?, onLinkHandled: () -> Unit) {
    val context = LocalContext.current
    var books by remember { mutableStateOf(ExamStore.load(context)) }
    var screen by remember { mutableStateOf<Screen>(Screen.Home) }

    LaunchedEffect(incomingLink) {
        if (incomingLink != null) {
            screen = Screen.Import(incomingLink)
            onLinkHandled()
        }
    }

    fun updateBooks(newBooks: List<Book>) {
        ExamStore.save(context, newBooks)
        books = newBooks
    }

    fun parentOf(s: Screen): Screen = when (s) {
        is Screen.Home -> Screen.Home
        is Screen.Teacher -> Screen.Home
        is Screen.Import -> Screen.Home
        is Screen.StudentBook -> Screen.Home
        is Screen.Setup -> Screen.StudentBook(s.bookName)
        is Screen.Quiz -> Screen.Setup(s.bookName, s.exam)
        is Screen.TeacherBook -> Screen.Teacher
        is Screen.ExamEditor -> Screen.TeacherBook(s.bookName)
    }

    BackHandler(enabled = screen !is Screen.Home) {
        screen = parentOf(screen)
    }

    Surface(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.fillMaxSize().systemBarsPadding()) {
            when (val s = screen) {
                is Screen.Home -> HomeScreen(
                    books = books,
                    onOpenBook = { screen = Screen.StudentBook(it) },
                    onTeacher = { screen = Screen.Teacher },
                    onImport = { screen = Screen.Import() }
                )
                is Screen.StudentBook -> StudentBookScreen(
                    book = books.firstOrNull { it.name == s.bookName } ?: Book(s.bookName, emptyList()),
                    onSelect = { exam -> screen = Screen.Setup(s.bookName, exam) },
                    onBack = { screen = Screen.Home }
                )
                is Screen.Setup -> SetupScreen(
                    bookName = s.bookName,
                    exam = s.exam,
                    onStart = { count -> screen = Screen.Quiz(s.bookName, s.exam, count) },
                    onBack = { screen = Screen.StudentBook(s.bookName) }
                )
                is Screen.Quiz -> QuizScreen(
                    exam = s.exam,
                    count = s.count,
                    onFinish = { screen = Screen.StudentBook(s.bookName) }
                )
                is Screen.Import -> ImportScreen(
                    initialText = s.prefill,
                    onImported = { shared ->
                        updateBooks(ExamStore.saveExam(books, shared.book, null, shared.exam))
                        screen = Screen.Home
                    },
                    onBack = { screen = Screen.Home }
                )
                is Screen.Teacher -> TeacherScreen(
                    books = books,
                    onBooksChange = { updateBooks(it) },
                    onOpenBook = { screen = Screen.TeacherBook(it) },
                    onBack = { screen = Screen.Home }
                )
                is Screen.TeacherBook -> TeacherBookScreen(
                    books = books,
                    bookName = s.bookName,
                    onBooksChange = { updateBooks(it) },
                    onEdit = { title -> screen = Screen.ExamEditor(s.bookName, title) },
                    onBack = { screen = Screen.Teacher }
                )
                is Screen.ExamEditor -> ExamEditorScreen(
                    books = books,
                    bookName = s.bookName,
                    originalTitle = s.examTitle,
                    onBooksChange = { updateBooks(it) },
                    onDone = { screen = Screen.TeacherBook(s.bookName) },
                    onBack = { screen = Screen.TeacherBook(s.bookName) }
                )
            }
        }
    }
}
