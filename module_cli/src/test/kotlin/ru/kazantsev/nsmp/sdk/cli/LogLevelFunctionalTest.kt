package ru.kazantsev.nsmp.sdk.cli

import org.junit.jupiter.api.Test
import java.io.File
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class LogLevelFunctionalTest {

    @Test
    fun `debug log level enables debug logs`() {
        val result = runCliProcess("pull", "--log-level", "debug")

        assertEquals(1, result.exitCode)
        assertContains(result.output, "[DEBUG] CLI - args: pull --log-level debug")
        assertContains(result.output, "[DEBUG] CLI - start parse")
    }

    @Test
    fun `info log level suppresses debug logs`() {
        val result = runCliProcess("pull", "--log-level", "info")

        assertEquals(1, result.exitCode)
        assertContains(result.output, "[INFO] CLI - CLI command started")
        assertFalse(result.output.contains("[DEBUG] CLI - args:"))
        assertFalse(result.output.contains("[DEBUG] CLI - start parse"))
    }

    private fun runCliProcess(vararg args: String): ProcessResult {
        val javaExecutable = buildString {
            append(System.getProperty("java.home"))
            append(File.separator)
            append("bin")
            append(File.separator)
            append("java")
            if (System.getProperty("os.name").startsWith("Windows")) {
                append(".exe")
            }
        }

        val command = mutableListOf(
            javaExecutable,
            "-cp",
            System.getProperty("java.class.path"),
            "ru.kazantsev.nsmp.sdk.MainKt"
        )
        command += args

        val process = ProcessBuilder(command)
            .redirectErrorStream(true)
            .start()

        val output = process.inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
        val exitCode = process.waitFor()
        return ProcessResult(exitCode, output)
    }

    private data class ProcessResult(
        val exitCode: Int,
        val output: String
    )
}
