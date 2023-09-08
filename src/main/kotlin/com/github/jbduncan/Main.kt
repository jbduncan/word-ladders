package com.github.jbduncan

import com.google.mu.util.graph.ShortestPath
import picocli.CommandLine
import java.io.IOException
import java.io.Reader
import java.io.Writer
import java.nio.charset.Charset
import java.util.concurrent.Callable
import kotlin.streams.asSequence
import kotlin.system.exitProcess

fun main(args: Array<String>) {
    val stdIn = System.`in`.reader(Charset.defaultCharset())
    val stdOut = System.out.writer(Charset.defaultCharset())
    val stdErr = System.err.writer(Charset.defaultCharset())
    exitProcess(Main(stdIn, stdOut, stdErr, args)())
}

class Main(
    private val stdIn: Reader,
    private val stdOut: Writer,
    private val stdErr: Writer,
    private val args: Array<String>
) {
    operator fun invoke(): Int =
        CommandLine(MyApp(stdIn, stdOut, stdErr))
            .execute(*args)
}

@CommandLine.Command(
    name = "word-ladders",
    version = ["0.0.1"],
    mixinStandardHelpOptions = true,
    description = ["Solves word ladders."])
class MyApp(
    private val stdIn: Reader,
    private val stdOut: Writer,
    private val stdErr: Writer
) : Callable<Int> {

    @CommandLine.Spec
    private lateinit var spec : CommandLine.Model.CommandSpec

    @CommandLine.Option(
        names = ["--first"],
        paramLabel = "FIRST",
        description = ["first word in word ladder (required - length must match last)"],
        required = true)
    private lateinit var first: String

    @CommandLine.Option(
        names = ["--last"],
        paramLabel = "LAST",
        description = ["last word in word ladder (required - length must match first)"],
        required = true)
    private lateinit var last: String

    override fun call(): Int {
        validate()

        val wordGraph = WordGraph(first.length)
        wordGraph.include(first)
        wordGraph.include(last)

        try {
            stdIn.buffered().forEachLine { wordGraph.include(it) }
        } catch (ex: IOException) {
            stdErr.println("Failed to read word list: " + ex.message)
            return 1
        }

        ShortestPath.unweightedShortestPathsFrom(first) { node ->
            wordGraph.adjacentNodes(node).stream()
        }
            .filter { path -> path.to() == last }
            .limit(1)
            .map { path ->
                path.stream()
                    .keys()
                    .asSequence()
                    .joinToString(separator = " ", prefix = "[", postfix = "]")
            }
            .forEach(stdOut::println)

        return 0
    }

    private fun validate() {
        if (first.isEmpty()) {
            throw CommandLine.ParameterException(spec.commandLine(), "first must not be empty")
        }
        if (last.isEmpty()) {
            throw CommandLine.ParameterException(spec.commandLine(), "last must not be empty")
        }
        if (first.length != last.length) {
            throw CommandLine.ParameterException(
                spec.commandLine(),
                "first and last must have the same length"
            )
        }

        if (arrayOf(first, last).any { !it.isWord() }) {
            throw CommandLine.ParameterException(
                spec.commandLine(),
                "word must not contain punctuation"
            )
        }
    }
}

private fun String.isWord(): Boolean {
    return all { it in ('a'..'z') }
}


@Throws(IOException::class)
private fun Writer.println(value: String) {
    this.append(value).append(System.lineSeparator()).flush()
}

