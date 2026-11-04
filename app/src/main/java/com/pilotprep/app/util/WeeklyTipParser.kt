package com.pilotprep.app.util

sealed interface WeeklyTipBlock {
    data class H2(val text: String) : WeeklyTipBlock
    data class Paragraph(val text: String) : WeeklyTipBlock
    data class UnorderedList(val items: List<String>) : WeeklyTipBlock
}

fun parseWeeklyTipMarkdownBlocks(raw: String): List<WeeklyTipBlock> {
    if (raw.isBlank()) return emptyList()
    val lines = raw.lines()
    val blocks = mutableListOf<WeeklyTipBlock>()
    var ulBuffer = mutableListOf<String>()
    fun flushUl() {
        if (ulBuffer.isNotEmpty()) {
            blocks.add(WeeklyTipBlock.UnorderedList(ulBuffer.toList()))
            ulBuffer = mutableListOf()
        }
    }
    for (line in lines) {
        val t = line.trim()
        if (t.isEmpty()) { flushUl(); continue }
        when {
            t.startsWith("## ") -> { flushUl(); blocks.add(WeeklyTipBlock.H2(t.removePrefix("## ").trim())) }
            t.startsWith("- ") || t.startsWith("* ") -> ulBuffer.add(t.drop(2).trim())
            else -> { flushUl(); blocks.add(WeeklyTipBlock.Paragraph(t)) }
        }
    }
    flushUl()
    return blocks
}

