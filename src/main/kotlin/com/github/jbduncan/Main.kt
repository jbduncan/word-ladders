package com.github.jbduncan

import picocli.CommandLine
import java.io.IOException
import java.io.Reader
import java.io.Writer
import java.nio.charset.Charset
import java.util.concurrent.Callable
import kotlin.system.exitProcess

fun main(args: Array<String>) {
    val charset = System.console()?.charset() ?: Charset.defaultCharset()
    val stdIn = System.`in`.reader(charset)
    val stdOut = System.out.writer(charset)
    val stdErr = System.err.writer(charset)
    exitProcess(Main(stdIn, stdOut, stdErr, args)())
}

class Main(
    private val stdIn: Reader, private val stdOut: Writer, private val stdErr: Writer, private val args: Array<String>
) {
    operator fun invoke(): Int = CommandLine(MyApp(stdIn, stdOut, stdErr)).execute(*args)
}

@CommandLine.Command(
    name = "word-ladders", version = ["0.0.1"], mixinStandardHelpOptions = true, description = ["Solves word ladders."]
)
class MyApp(
    private val stdIn: Reader, private val stdOut: Writer, private val stdErr: Writer
) : Callable<Int> {

    @CommandLine.Spec
    private lateinit var spec: CommandLine.Model.CommandSpec

    @CommandLine.Option(
        names = ["--first"],
        paramLabel = "FIRST",
        description = ["first word in word ladder (required - length must match last)"],
        required = true
    )
    private lateinit var first: String

    @CommandLine.Option(
        names = ["--last"],
        paramLabel = "LAST",
        description = ["last word in word ladder (required - length must match first)"],
        required = true
    )
    private lateinit var last: String

    override fun call(): Int {
        validate()

        val wordGraph = WordGraph(first.length).include(first).include(last)

        try {
            stdIn.buffered().forEachLine(wordGraph::include)
        } catch (ex: IOException) {
            stdErr.println("Failed to read word list: " + ex.message)
            return 1
        }

        wordGraph.allShortestPaths(first, last).map(::toPathString).forEach(stdOut::println)

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
                spec.commandLine(), "first and last must have the same length"
            )
        }

        if (!first.isWord() || !last.isWord()) {
            throw CommandLine.ParameterException(
                spec.commandLine(), "word must not contain punctuation"
            )
        }
    }

    private fun toPathString(path: List<String>) = path.joinToString(separator = " ", prefix = "[", postfix = "]")
}

private fun String.isWord(): Boolean = all { it in ('a'..'z') }

@Throws(IOException::class)
private fun Writer.println(value: String) = this.append(value).append(System.lineSeparator()).flush()
