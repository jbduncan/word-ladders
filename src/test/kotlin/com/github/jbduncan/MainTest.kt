package com.github.jbduncan

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.io.IOException
import java.io.Reader
import java.io.StringReader
import java.io.StringWriter

class MainTest {
    @Test
    fun main() {
        val input = StringReader(DICTIONARY)
        val output = StringWriter()
        val err = StringWriter()
        val args = arrayOf("--first=head", "--last=tail")

        val exitCode = Main(input, output, err, args)()

        val validAnswers = setOf(
            "[head heal hell hall hail tail]",
            "[head heal hell hall tall tail]",
            "[head heal hell tell tall tail]",
            "[head heal neal neil nail tail]",
            "[head heal teal tell tall tail]",
            "[head hear heir hair hail tail]",
            "[head held hell hall hail tail]",
            "[head held hell hall tall tail]",
            "[head held hell tell tall tail]",
            "[head read reid raid rail tail]"
        )
        assertTrue(output.toString().trim() in validAnswers)
        assertEquals(0, exitCode)
    }

    @Test
    fun stdInSuffersFromIOException() {
        val input = object : Reader() {
            override fun read(cbuf: CharArray, off: Int, len: Int): Int {
                throw IOException("wuh oh")
            }

            override fun close() {}
        }
        val output = StringWriter()
        val err = StringWriter()
        val args = arrayOf("--first=head", "--last=tail")

        val exitCode = Main(input, output, err, args)()

        assertEquals(1, exitCode)
        assertEquals("Failed to read word list: wuh oh", err.toString().trim())
    }
}