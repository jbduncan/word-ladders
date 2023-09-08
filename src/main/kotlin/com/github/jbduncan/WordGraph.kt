package com.github.jbduncan

import com.google.common.graph.AbstractGraph
import com.google.common.graph.ElementOrder
import com.google.common.graph.GraphBuilder
import com.google.common.graph.MutableGraph

class WordGraph(private val wordLength: Int) : AbstractGraph<String>() {
    private val undirectedGraph: MutableGraph<String>

    init {
        require(this.wordLength > 0)
        this.undirectedGraph = GraphBuilder.undirected().build()
    }

    // Add the word to the graph and connect it to its 'Hamming distance - 1' neighbours
    fun include(word: String) {
        if (word.isEmpty() || word.length != wordLength) {
            return
        }

        undirectedGraph.addNode(word)
        neighbours(word).forEach {
            undirectedGraph.putEdge(word, it)
        }
    }

    private fun neighbours(word: String): List<String> {
        val result = mutableListOf<String>()
        word.indices.forEach { i ->
            ('a'..'z').forEach { newChar ->
                val newWord = buildString(word.length) {
                    word.forEachIndexed { j, existingChar ->
                        append(
                            if (i == j) {
                                newChar
                            } else {
                                existingChar
                            }
                        )
                    }
                }
                if (word != newWord && undirectedGraph.nodes().contains(newWord)) {
                    // Neighbour found
                    result.add(newWord)
                }
            }
        }
        return result.toList()
    }

    override fun successors(node: String): Set<String> = undirectedGraph.successors(node)

    override fun predecessors(node: String): Set<String> = undirectedGraph.predecessors(node)

    override fun nodes(): Set<String> = undirectedGraph.nodes()

    override fun isDirected(): Boolean = undirectedGraph.isDirected

    override fun allowsSelfLoops(): Boolean = undirectedGraph.allowsSelfLoops()

    override fun nodeOrder(): ElementOrder<String> = undirectedGraph.nodeOrder()

    override fun adjacentNodes(node: String): Set<String> = undirectedGraph.adjacentNodes(node)
}
