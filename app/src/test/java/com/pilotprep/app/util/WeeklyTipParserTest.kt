package com.pilotprep.app.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class WeeklyTipParserTest {

    @Test
    fun parseWeeklyTipMarkdownBlocks_parsesH2ParagraphAndList() {
        val input = """
            ## Titre
            Ceci est un paragraphe.
            - Premier
            - Second
        """.trimIndent()

        val blocks = parseWeeklyTipMarkdownBlocks(input)
        assertEquals(3, blocks.size)
        assertTrue(blocks[0] is WeeklyTipBlock.H2)
        assertTrue(blocks[1] is WeeklyTipBlock.Paragraph)
        assertTrue(blocks[2] is WeeklyTipBlock.UnorderedList)
        val list = blocks[2] as WeeklyTipBlock.UnorderedList
        assertEquals(listOf("Premier", "Second"), list.items)
    }

    @Test
    fun parseWeeklyTipMarkdownBlocks_emptyReturnsEmpty() {
        val blocks = parseWeeklyTipMarkdownBlocks("")
        assertTrue(blocks.isEmpty())
    }
}

