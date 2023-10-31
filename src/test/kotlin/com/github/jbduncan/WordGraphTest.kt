package com.github.jbduncan

import com.google.common.graph.ElementOrder
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class WordGraphTest {

    @Test
    fun newWordGraph() {
        assertThrows<IllegalArgumentException> { WordGraph(0) }
        assertThrows<IllegalArgumentException> { WordGraph(-1) }
        assertTrue(WordGraph(4).isDirected)
        assertFalse(WordGraph(4).allowsSelfLoops())
        assertEquals(ElementOrder.insertion<String>(), WordGraph(4).nodeOrder())
    }

    @Test
    fun include() {
        val wordGraph = WordGraph(4)

        wordGraph.include("neat", "near")

        assertEquals(setOf("neat", "near"), wordGraph.nodes())
        assertTrue(wordGraph.hasEdgeConnecting("neat", "near"))
        assertEquals(setOf("near"), wordGraph.adjacentNodes("neat"))
        assertEquals(setOf("near"), wordGraph.predecessors("neat"))

        wordGraph.include("bear")

        assertTrue(wordGraph.hasEdgeConnecting("near", "bear"))

        wordGraph.include("feat")

        assertTrue(wordGraph.hasEdgeConnecting("feat", "neat"))

        wordGraph.include("type")

        assertTrue(wordGraph.edges().none { edge -> edge.contains("type") })
    }

    @Test
    fun includeDifferentLengthWord() {
        val wordGraph = WordGraph(4)

        wordGraph.include("")

        assertTrue(wordGraph.nodes().isEmpty())
        assertThrows<IllegalArgumentException> { wordGraph.successors("") }
        assertThrows<IllegalArgumentException> { wordGraph.predecessors("") }
        assertThrows<IllegalArgumentException> { wordGraph.adjacentNodes("") }
    }

    @Test
    fun includeNonWord() {
        val wordGraph = WordGraph(4)

        wordGraph.include("notAWord0")

        assertTrue(wordGraph.nodes().isEmpty())
    }
}