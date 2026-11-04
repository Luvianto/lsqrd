package com.example.lsqrd.data

import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.net.HttpURLConnection
import java.net.URL

class LocalWebServerTest {
    private val token = "testtoken"
    private var port = 0
    private lateinit var server: LocalWebServer

    private val testFields = listOf(
        CredentialField(
            id = 1,
            credentialId = 1,
            label = "Email",
            value = "test@example.com",
            isSecret = false
        ),
        CredentialField(
            id = 2,
            credentialId = 2,
            label = "Password",
            value = "p@ssw0rd!",
            isSecret = true
        ),
    )

    @Before
    fun setup() {
        port = LocalWebServer.findAvailablePort()
        server = LocalWebServer(port, "Github", testFields, token)
        server.start()
    }

    @After
    fun teardown() {
        runCatching { server.stop() }
    }

    // ── Helper ─────────────────────────────────────────────

    private fun fetch(path: String): Pair<Int, String> {
        val conn = URL("http://localhost:$port$path").openConnection() as HttpURLConnection
        conn.connectTimeout = 3000
        conn.readTimeout = 3000
        val code = conn.responseCode
        val body = try {
            conn.getInputStream().bufferedReader().readText()
        } catch (_: Exception) {
            ""
        }
        conn.disconnect()
        return code to body
    }

    @Test
    fun correct_token_returns_200() {
        val (code, _) = fetch("/$token")
        assertEquals(200, code)
    }

    @Test
    fun wrong_token_returns_404() {
        val (code, _) = fetch("/wrongToken")
        assertEquals(404, code)
    }

    @Test
    fun root_token_returns_404() {
        val (code, _) = fetch("/")
        assertEquals(404, code)
    }

    @Test
    fun html_contains_credential_name() {
        val (_, body) = fetch("/$token")
        assertTrue("HTML should contain credential name", body.contains("Github"))
    }

    @Test
    fun html_contains_field_labels_and_values() {
        val (_, body) = fetch("/$token")
        assertTrue(body.contains("Email"))
        assertTrue(body.contains("test@example.com"))
        assertTrue(body.contains("Password"))
        assertTrue(body.contains("p@ssw0rd!"))
    }

    @Test
    fun html_escapes_special_characters() {
        val specialPort = LocalWebServer.findAvailablePort()
        val specialServer = LocalWebServer(
            port = specialPort,
            credentialName = "Title <Test> & \"Quoted\"",
            fields =  listOf(
                CredentialField(
                    id = 3,
                    credentialId = 1,
                    label = "Key <name>",
                    value = "val & \"v\"",
                    isSecret = false
                ),
            ),
            token = "specialToken"
        )
        specialServer.start()

        val (_, body) = run {
            val conn = URL("http://localhost:$specialPort/specialToken").openConnection() as HttpURLConnection
            conn.connectTimeout=3000
            conn.readTimeout=3000
            val code = conn.responseCode
            val b = conn.getInputStream().bufferedReader().readText()
            conn.disconnect()
            code to b
        }

        assertTrue(body.contains("Title &lt;Test&gt; &amp; &quot;Quoted&quot;"))
        assertTrue(body.contains("Key &lt;name&gt;"))
        assertTrue(body.contains("val &amp; &quot;v&quot;"))

        specialServer.stop()
    }

    @Test
    fun find_available_port_is_in_valid_range() {
        val p = LocalWebServer.findAvailablePort()
        assertTrue("Port should be between 8080 and 9090", p in 8080..9090)
    }
}