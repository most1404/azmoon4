package com.example.quizapp

import android.util.Base64
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.util.zip.Deflater
import java.util.zip.Inflater

data class SharedExam(val book: String, val exam: Exam)

/**
 * ارسال آزمون به‌صورت «لینک» و دریافت آن.
 * کل آزمون (سوال‌ها، پاسخ‌ها و تاریخ بارگذاری) فشرده و داخل لینک گذاشته می‌شود؛
 * پس بدون سرور و بدون اینترنت کار می‌کند. لینک: azmoon://exam?d=...
 */
object ExamText {
    private const val LINK_PREFIX = "azmoon://exam?d="
    private val linkRegex = Regex("azmoon://exam\\?d=([A-Za-z0-9_\\-]+)")

    // قالب متنی قدیمی (برای سازگاری با آزمون‌هایی که قبلاً ارسال شده‌اند)
    private const val BOOK_PREFIX = "کتاب:"
    private const val TITLE_PREFIX = "آزمون:"
    private const val Q_MARK = "[سوالات]"
    private const val A_MARK = "[پاسخنامه]"

    /** شماره‌گذاری: «1. متن» در هر خط */
    fun numbered(items: List<String>): String =
        items.mapIndexed { i, s -> "${i + 1}. $s" }.joinToString("\n")

    // ---------- لینک ----------

    fun toShareLink(bookName: String, exam: Exam): String {
        val qArr = JSONArray()
        for (q in exam.questions) {
            qArr.put(JSONObject().put("q", q.q).put("a", q.a).put("d", q.addedAt))
        }
        val json = JSONObject()
            .put("v", 1)
            .put("b", bookName)
            .put("t", exam.title)
            .put("q", qArr)
            .toString()

        val deflater = Deflater(Deflater.BEST_COMPRESSION)
        deflater.setInput(json.toByteArray(Charsets.UTF_8))
        deflater.finish()
        val out = ByteArrayOutputStream()
        val buf = ByteArray(4096)
        while (!deflater.finished()) {
            val n = deflater.deflate(buf)
            out.write(buf, 0, n)
        }
        deflater.end()

        val encoded = Base64.encodeToString(
            out.toByteArray(),
            Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING
        )
        return LINK_PREFIX + encoded
    }

    /** پیامی که برای دانش‌آموز فرستاده می‌شود: توضیح کوتاه + لینک */
    fun toShareText(bookName: String, exam: Exam): String =
        "📚 کتاب: $bookName\n" +
            "📝 آزمون: ${exam.title} (${exam.questions.size} سوال)\n\n" +
            "برای دریافت آزمون، این پیام را کپی کنید و در برنامهٔ آزمون‌ساز، بخش «دریافت آزمون» وارد کنید " +
            "(یا اگر روی لینک زیر ضربه زد، مستقیم باز می‌شود):\n\n" +
            toShareLink(bookName, exam) + "\n"

    fun fromLink(text: String): SharedExam? {
        val encoded = linkRegex.find(text)?.groupValues?.get(1) ?: return null
        return try {
            val compressed = Base64.decode(encoded, Base64.URL_SAFE or Base64.NO_WRAP)
            val inflater = Inflater()
            inflater.setInput(compressed)
            val out = ByteArrayOutputStream()
            val buf = ByteArray(4096)
            while (!inflater.finished()) {
                val n = inflater.inflate(buf)
                if (n == 0 && (inflater.needsInput() || inflater.needsDictionary())) break
                out.write(buf, 0, n)
            }
            inflater.end()

            val obj = JSONObject(out.toString("UTF-8"))
            val qArr = obj.getJSONArray("q")
            val now = System.currentTimeMillis()
            val questions = mutableListOf<Question>()
            for (i in 0 until qArr.length()) {
                val qObj = qArr.getJSONObject(i)
                val d = qObj.optLong("d", 0L)
                questions.add(Question(qObj.getString("q"), qObj.getString("a"), if (d > 0L) d else now))
            }
            if (questions.isEmpty()) return null
            val book = obj.optString("b", "").trim().ifEmpty { "کتاب دریافتی" }
            val title = obj.optString("t", "").trim().ifEmpty { "آزمون دریافتی" }
            SharedExam(book, Exam(title, questions))
        } catch (e: Exception) {
            null
        }
    }

    // ---------- قالب متنی قدیمی ----------

    private fun fromLegacyText(text: String): SharedExam? {
        val qIdx = text.indexOf(Q_MARK)
        val aIdx = text.indexOf(A_MARK)
        if (qIdx < 0 || aIdx < 0 || aIdx < qIdx) return null

        val headerLines = text.substring(0, qIdx).lines()
        val book = headerLines.firstOrNull { it.contains(BOOK_PREFIX) }
            ?.substringAfter(BOOK_PREFIX)?.trim()
            ?.takeIf { it.isNotEmpty() } ?: "کتاب دریافتی"
        val title = headerLines.firstOrNull { it.contains(TITLE_PREFIX) }
            ?.substringAfter(TITLE_PREFIX)?.trim()
            ?.takeIf { it.isNotEmpty() } ?: "آزمون دریافتی"

        val result = QuestionParser.pair(
            QuestionParser.parseItems(text.substring(qIdx + Q_MARK.length, aIdx)),
            QuestionParser.parseItems(text.substring(aIdx + A_MARK.length))
        )
        if (result.error != null || result.pairs.isEmpty()) return null

        val now = System.currentTimeMillis()
        return SharedExam(book, Exam(title, result.pairs.map { (q, a) -> Question(q, a, now) }))
    }

    /** اگر متن معتبر نباشد null برمی‌گرداند */
    fun fromShareText(text: String): SharedExam? = fromLink(text) ?: fromLegacyText(text)
}
