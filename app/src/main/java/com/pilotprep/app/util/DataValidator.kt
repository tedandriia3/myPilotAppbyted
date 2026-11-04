package com.pilotprep.app.util

import com.pilotprep.app.data.model.*

object DataValidator {
    fun isValidQuestion(q: Question?): Boolean {
        q ?: return false
        if (q.questionText.isBlank()) return false
        if (q.options.isEmpty()) return false
        val idx = q.correctOptionIndex.toInt()
        if (idx < 0 || idx >= q.options.size) return false
        return true
    }

    fun isValidWeeklyTip(t: WeeklyTip?): Boolean {
        t ?: return false
        if (t.title.isBlank()) return false
        if (t.weekOfYear !in 1..52) return false
        if (t.introduction.isBlank() && t.fullContent.isBlank()) return false
        return true
    }

    fun isValidContentBlock(b: ContentBlock?): Boolean {
        b ?: return false
        if (b.type.isBlank()) return false
        return when (b.type) {
            "image" -> b.imageUrl.isNotBlank()
            "ul", "ol" -> b.items.isNotEmpty()
            else -> b.text.isNotBlank()
        }
    }

    fun isValidNeedToKnowContent(c: NeedToKnowContent?): Boolean {
        c ?: return false
        if (c.title.isBlank()) return false
        if (c.license.isBlank()) return false
        if (c.content.isEmpty()) return false
        return c.content.all { isValidContentBlock(it) }
    }

    fun isValidLesson(l: Lesson?): Boolean {
        l ?: return false
        if (l.title.isBlank()) return false
        if (l.subject.isBlank()) return false
        return true
    }

    fun isValidPodcast(podcast: Podcast?): Boolean {
        podcast ?: return false
        if (podcast.title.isBlank()) return false
        return true
    }
}

