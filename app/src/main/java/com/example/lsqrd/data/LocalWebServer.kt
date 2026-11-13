package com.example.lsqrd.data

import android.net.InetAddresses
import fi.iki.elonen.NanoHTTPD
import java.io.IOException
import java.net.InetAddress
import java.net.NetworkInterface
import java.net.ServerSocket

class LocalWebServer(
    port: Int,
    private val credentialName: String,
    private val fields: List<CredentialField>,
    private val token: String
) : NanoHTTPD(port) {
    override fun serve(session: IHTTPSession): Response {
        if (session.uri != "/$token") {
            return newFixedLengthResponse(Response.Status.NOT_FOUND, MIME_PLAINTEXT, "Not found")
        }
        return newFixedLengthResponse(Response.Status.OK, "text/html;charset=utf-8", buildHtml())
    }

    private fun buildHtml(): String {
        val fieldsHtml = fields.joinToString("\n") { field ->
            """
                <div class="field">
                    <div class="label">${escapeHtml(field.label)}</div>
                    <div class="value-row">
                        <span class="value">${escapeHtml(field.value)}</span>
                        <button onclick="copyValue('${escapeJs(field.value)}', this)">Copy</button>
                    </div>
                </div>
            """.trimIndent()
        }
        return HTML_TEMPLATE.replace("{{title}}", escapeHtml(credentialName))
            .replace("{{fields}}", fieldsHtml)
    }

    private fun escapeHtml(s: String) =
        s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;")

    private fun escapeJs(s: String) =
        s.replace("\\", "\\\\").replace("'", "\\'").replace("\n", "\\n").replace("\r", "\\r")

    companion object {
        fun getLocalIpAddress(): String? = try {
            NetworkInterface.getNetworkInterfaces()?.toList()
                ?.flatMap { it.inetAddresses?.toList() ?: emptyList() }
                ?.firstOrNull { !it.isLoopbackAddress && it is InetAddress }?.hostAddress
        } catch (e: Exception) {
            null
        }

        fun findAvailablePort(): Int {
            for (port in 8080..8090) {
                try {
                    ServerSocket(port).close(); return port
                } catch (_: IOException) {
                }
            }
            return 8080
        }

        private val HTML_TEMPLATE = """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="utf-8">
                <meta name="viewport" content="width=device-width, initial-scale=1">
                <title>{{title}} — lsqrd</title>
                <style>
                    * { box-sizing: border-box; }
                    body {
                        font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif;
                        max-width: 520px;
                        margin: 40px auto;
                        padding: 0 16px;
                        background: #f0f2f5;
                        color: #1a1a2e;
                    }
                    h1 {
                        font-size: 1.5em;
                        margin-bottom: 24px;
                    }
                    .field {
                        background: white;
                        border-radius: 12px;
                        padding: 16px 20px;
                        margin: 10px 0;
                        box-shadow: 0 1px 4px rgba(0, 0, 0, .08);
                    }
                    .label {
                        font-size: .75em;
                        color: #888;
                        text-transform: uppercase;
                        letter-spacing: .05em;
                        margin-bottom: 8px;
                    }
                    .value-row {
                        display: flex;
                        align-items: center;
                        gap: 12px;
                    }
                    .value {
                        font-family: monospace;
                        font-size: 1em;
                        flex: 1;
                        word-break: break-all;
                    }
                    button {
                        background: #4c6ef5;
                        color: white;
                        border: none;
                        border-radius: 6px;
                        padding: 7px 16px;
                        cursor: pointer;
                        font-size: .85em;
                        white-space: nowrap;
                    }
                    button.copied {
                        background: #2ecc71;
                    }
                    .footer {
                        text-align: center;
                        color: #aaa;
                        font-size: .75em;
                        margin-top: 32px;
                    }
                </style>
            </head>
        <body>
            <h1>{{title}}</h1>
            {{fields}}
            <div class="footer">lsqrd · This page will expire automatically</div>
            <script>
                function copyValue(v, btn) {
                    if(navigator.clipboard) {
                        navigator.clipboard.writeText(v).then(()=>ok(btn)).catch(()=>fb(v, btn));
                    } else { fb(v, btn); }
                }
                function fb(v, btn) {
                    var t = document.createElement('textarea');
                    t.value = v;
                    t.style.cssText = 'position:fixed;opacity:0';
                    document.body.appendChild(t);
                    t.focus();
                    t.select();
                    try {
                        document.execCommand('copy');
                        ok(btn);
                    } catch(e) {}
                    document.body.removeChild(t);
                }
                function ok(btn) {
                    btn.textContent = 'Copied!';
                    btn.className = 'copied';
                    setTimeout(function(){btn.textContent='Copy'; btn.className=''}, 2000)
                }
            </script>
        </body>
        </html>
        """.trimIndent()
    }
}