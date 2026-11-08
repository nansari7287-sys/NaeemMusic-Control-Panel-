package dev.drakoxnaeem.control

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

private val http = OkHttpClient.Builder()
    .connectTimeout(10, TimeUnit.SECONDS)
    .readTimeout(15, TimeUnit.SECONDS)
    .writeTimeout(15, TimeUnit.SECONDS)
    .build()

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MaterialTheme { ControlHome() } }
    }
}

@Composable
private fun ControlHome() {
    var token by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("Admin token enter karke Connect karein.") }
    var configText by remember { mutableStateOf("") }
    var version by remember { mutableIntStateOf(0) }
    var busy by remember { mutableStateOf(false) }
    var connected by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val base = BuildConfig.API_BASE_URL.trimEnd('/')

    suspend fun request(method: String, path: String, body: String? = null): String =
        withContext(Dispatchers.IO) {
            val builder = Request.Builder()
                .url(base + path)
                .header("Authorization", "Bearer $token")
            when (method) {
                "PUT" -> builder.put(
                    (body ?: "{}").toRequestBody("application/json; charset=utf-8".toMediaType())
                )
                else -> builder.get()
            }
            http.newCall(builder.build()).execute().use { response ->
                val responseText = response.body?.string().orEmpty()
                if (!response.isSuccessful) {
                    val message = try { JSONObject(responseText).optString("error") }
                    catch (_: Exception) { "" }
                    error(message.ifBlank { "Server error: HTTP ${response.code}" })
                }
                responseText
            }
        }

    fun connect() {
        scope.launch {
            busy = true
            status = "Connecting…"
            try {
                val raw = request("GET", "/api/admin/config")
                val json = JSONObject(raw)
                configText = json.getJSONObject("config").toString(2)
                version = json.optInt("version", 1)
                connected = true
                status = "Connected · config v$version"
            } catch (e: Exception) {
                connected = false
                status = e.message ?: "Connection failed"
            } finally { busy = false }
        }
    }

    fun publish() {
        scope.launch {
            busy = true
            status = "Validating and publishing…"
            try {
                val config = JSONObject(configText)
                val payload = JSONObject().put("config", config).toString()
                val raw = request("PUT", "/api/admin/config", payload)
                val result = JSONObject(raw)
                version = result.optInt("version", version + 1)
                status = "Published successfully · v$version"
            } catch (e: Exception) {
                status = e.message ?: "Publish failed"
            } finally { busy = false }
        }
    }

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("𝑵𝒂𝒆𝒆𝒎 Music", color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.titleMedium)
        Text("Control Panel", style = MaterialTheme.typography.headlineLarge)
        Text("Remote settings · Admin access")
        OutlinedTextField(
            value = token, onValueChange = { token = it },
            label = { Text("Admin token") }, singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(), enabled = !busy
        )
        Button(
            onClick = { connect() }, enabled = token.isNotBlank() && !busy,
            modifier = Modifier.fillMaxWidth()
        ) { Text(if (busy) "Please wait…" else "Connect / Reload") }
        Text(status, style = MaterialTheme.typography.bodySmall)
        if (connected) {
            Text("Configuration JSON · version $version",
                style = MaterialTheme.typography.titleMedium)
            Text(
                "Neeche JSON edit karein. Save karne se pehle valid JSON hona zaroori hai.",
                style = MaterialTheme.typography.bodySmall
            )
            OutlinedTextField(
                value = configText,
                onValueChange = { configText = it },
                modifier = Modifier.fillMaxWidth().heightIn(min = 300.dp),
                minLines = 14,
                label = { Text("Remote configuration") },
                enabled = !busy
            )
            Button(
                onClick = { publish() }, enabled = !busy && configText.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) { Text("PUBLISH CHANGES") }
            OutlinedButton(
                onClick = { connect() }, enabled = !busy,
                modifier = Modifier.fillMaxWidth()
            ) { Text("Discard edits / Reload server config") }
        }
        Text(
            "Security: token is kept only in this screen's memory and is not saved on the device. " +
                "Only use your private admin token. Remote settings cannot install code or change the APK.",
            style = MaterialTheme.typography.bodySmall
        )
    }
}
