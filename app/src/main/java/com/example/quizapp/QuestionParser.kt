package com.example.quizapp

/** یک مورد (سوال یا پاسخ) به‌همراه شمارهٔ آن در متن (اگر شماره‌دار بود) */
data class Item(val number: Int?, val text: String)

data class PairResult(val pairs: List<Pair<String, String>>, val error: String?)

/**
 * تبدیل متن یکجا به فهرست سوال‌ها (یا پاسخ‌ها).
 * قانون تشخیص:
 *  ۱) اگر خط اول با شماره شروع شود (مثل «1.» «۲)» «3-») هر شمارهٔ بزرگ‌تر از شمارهٔ قبلی یک مورد جدید است
 *     (شماره‌های کوچک‌تر، مثلاً گزینه‌های ۱) ۲) ۳) ۴) داخل سوال، جزو همان سوال می‌مانند)
 *  ۲) در غیر این صورت، موردها با خط خالی جدا می‌شوند
 *  ۳) اگر خط خالی هم نباشد، هر خط یک مورد است
 */
object QuestionParser {

    private val numberPrefix = Regex("^\\s*(\\d{1,4})\\s*[.)\\-–—:،]\\s*")

    private fun normalizeDigits(s: String): String {
        val sb = StringBuilder()
        for (ch in s) {
            when (ch) {
                in '۰'..'۹' -> sb.append('0' + (ch - '۰'))
                in '٠'..'٩' -> sb.append('0' + (ch - '٠'))
                else -> sb.append(ch)
            }
        }
        return sb.toString()
    }

    fun parseItems(text: String): List<Item> {
        val lines = text.replace("\r", "").split("\n")
        val firstLine = lines.firstOrNull { it.isNotBlank() } ?: return emptyList()

        // حالت ۱: شماره‌دار
        if (numberPrefix.containsMatchIn(normalizeDigits(firstLine))) {
            val numbers = mutableListOf<Int>()
            val texts = mutableListOf<StringBuilder>()
            for (line in lines) {
                if (line.isBlank()) continue
                val match = numberPrefix.find(normalizeDigits(line))
                val num = match?.groupValues?.get(1)?.toIntOrNull()
                if (match != null && num != null && (numbers.isEmpty() || num > numbers.last())) {
                    numbers.add(num)
                    // طول رشته بعد از نرمال‌سازی ارقام تغییر نمی‌کند
                    texts.add(StringBuilder(line.substring(match.range.last + 1).trim()))
                } else if (texts.isNotEmpty()) {
                    texts.last().append("\n").append(line.trim())
                }
            }
            return numbers.indices
                .map { Item(numbers[it], texts[it].toString().trim()) }
                .filter { it.text.isNotEmpty() }
        }

        // حالت ۲: جداشده با خط خالی
        if (lines.any { it.isBlank() }) {
            val items = mutableListOf<Item>()
            val current = mutableListOf<String>()
            for (line in lines) {
                if (line.isBlank()) {
                    if (current.isNotEmpty()) {
                        items.add(Item(null, current.joinToString("\n")))
                        current.clear()
                    }
                } else {
                    current.add(line.trim())
                }
            }
            if (current.isNotEmpty()) items.add(Item(null, current.joinToString("\n")))
            return items
        }

        // حالت ۳: هر خط یک مورد
        return lines.map { it.trim() }.filter { it.isNotEmpty() }.map { Item(null, it) }
    }

    fun parse(text: String): List<String> = parseItems(text).map { it.text }

    /**
     * جفت‌کردن سوال و پاسخ.
     * اگر هر دو فهرست شماره‌دار باشند، پاسخ «شمارهٔ n» دقیقاً به سوال «شمارهٔ n» وصل می‌شود
     * (نه بر اساس جایگاه). اگر شماره‌ها نخوانند، خطا برمی‌گردد تا پاسخ اشتباهی به سوال نچسبد.
     */
    fun pair(questions: List<Item>, answers: List<Item>): PairResult {
        if (questions.isEmpty()) return PairResult(emptyList(), null)

        val numbered = questions.all { it.number != null } &&
            answers.isNotEmpty() && answers.all { it.number != null }
        if (numbered) {
            val answerByNumber = answers.associateBy({ it.number!! }, { it.text })
            val hasDuplicates = answerByNumber.size != answers.size ||
                questions.map { it.number }.toSet().size != questions.size
            if (!hasDuplicates) {
                val missing = questions.mapNotNull { it.number }.filter { it !in answerByNumber }
                val extra = answerByNumber.keys.filter { k -> questions.none { it.number == k } }
                if (missing.isEmpty() && extra.isEmpty()) {
                    return PairResult(
                        questions.map { it.text to answerByNumber[it.number!!]!! },
                        null
                    )
                }
                val parts = mutableListOf<String>()
                if (missing.isNotEmpty()) parts.add("پاسخِ سوال‌های ${missing.joinToString("، ")} نوشته نشده")
                if (extra.isNotEmpty()) parts.add("پاسخ‌های ${extra.joinToString("، ")} سوالی ندارند")
                return PairResult(emptyList(), parts.joinToString(" | "))
            }
        }

        if (questions.size == answers.size) {
            return PairResult(questions.zip(answers) { q, a -> q.text to a.text }, null)
        }
        return PairResult(
            emptyList(),
            "تعداد سوال‌ها (${questions.size}) و پاسخ‌ها (${answers.size}) برابر نیست"
        )
    }
}
