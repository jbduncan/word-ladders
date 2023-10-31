package com.github.jbduncan

import com.google.common.graph.AbstractGraph
import com.google.common.graph.ElementOrder
import com.google.common.graph.EndpointPair
import com.google.common.graph.GraphBuilder
import com.google.common.graph.MutableGraph
import org.jgrapht.alg.shortestpath.AllDirectedPaths
import org.jgrapht.alg.shortestpath.BFSShortestPath

class WordGraph(private val wordLength: Int) : AbstractGraph<String>() {
    private val delegate: MutableGraph<String>

    init {
        require(this.wordLength > 0)
        this.delegate = GraphBuilder.directed().build()
    }

    // Add the word to the graph and connect it to its 'Hamming distance - 1' neighbours
    fun include(word: String): WordGraph {
        if (word.isEmpty() || word.length != wordLength) {
            return this
        }

        delegate.addNode(word)
        neighbours(word).forEach {
            delegate.putEdge(word, it)
            delegate.putEdge(it, word)
        }

        return this
    }

    // Add the words to the graph and connect each word to its 'Hamming distance - 1' neighbours
    fun include(vararg words: String): WordGraph {
        words.forEach(::include)
        return this
    }

    private fun neighbours(word: String): List<String> {
        val result = mutableListOf<String>()
        word.indices.forEach { i ->
            ('a'..'z').forEach { newChar ->
                val newWord = buildString(word.length) {
                    word.forEachIndexed { j, existingChar ->
                        if (i == j) {
                            append(newChar)
                        } else {
                            append(existingChar)
                        }
                    }
                }
                if (word != newWord && delegate.nodes().contains(newWord)) {
                    // Neighbour found
                    result.add(newWord)
                }
            }
        }
        return result.toList()
    }

    fun allShortestPaths(source: String, target: String): Sequence<List<String>> {
        val jgrapht: org.jgrapht.Graph<String, EndpointPair<String>> = JgraphtAdapter(this)

        val shortestPathLength = BFSShortestPath(jgrapht).getPath(source, target).length
        val allShortestPaths = AllDirectedPaths(jgrapht).getAllPaths(source, target, false, shortestPathLength)

        return allShortestPaths.asSequence().map { it.vertexList }
    }

    override fun nodes(): Set<String> = delegate.nodes()

    override fun adjacentNodes(node: String): Set<String> = delegate.adjacentNodes(node)

    override fun predecessors(node: String): Set<String> = delegate.predecessors(node)

    override fun successors(node: String): Set<String> = delegate.successors(node)

    override fun isDirected(): Boolean = delegate.isDirected

    override fun allowsSelfLoops(): Boolean = delegate.allowsSelfLoops()

    override fun nodeOrder(): ElementOrder<String> = delegate.nodeOrder()
}
