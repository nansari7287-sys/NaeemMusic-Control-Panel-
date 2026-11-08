package dev.drakoxnaeem.control

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

private val neon = Color(0xFF19E6FF)
private val violet = Color(0xFF9B5CFF)
private val bg = Color(0xFF070B16)
private val panel = Color(0xFF10182A)
private val http = OkHttpClient.Builder().connectTimeout(10, TimeUnit.SECONDS)
    .readTimeout(15, TimeUnit.SECONDS).writeTimeout(15, TimeUnit.SECONDS).build()

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { ControlApp() }
    }
}

@Composable
private fun ControlApp() {
    var token by remember { mutableStateOf("") }
    var config by remember { mutableStateOf(JSONObject()) }
    var jsonText by remember { mutableStateOf("") }
    var version by remember { mutableIntStateOf(0) }
    var status by remember { mutableStateOf("Connect to your private control server") }
    var connected by remember { mutableStateOf(false) }
    var busy by remember { mutableStateOf(false) }
    var tab by remember { mutableStateOf("Overview") }
    val scope = rememberCoroutineScope()
    val base = BuildConfig.API_BASE_URL.trimEnd('/')

    suspend fun call(method: String, path: String, body: String? = null): String =
        withContext(Dispatchers.IO) {
            val b = Request.Builder().url(base + path).header("Authorization", "Bearer $token")
            if (method == "PUT") b.put((body ?: "{}").toRequestBody("application/json; charset=utf-8".toMediaType())) else b.get()
            http.newCall(b.build()).execute().use { r ->
                val text = r.body?.string().orEmpty()
                if (!r.isSuccessful) {
                    val reason = try { JSONObject(text).optString("error") } catch (_: Exception) { "" }
                    error(reason.ifBlank { "Server error ${r.code}" })
                }
                text
            }
        }

    fun load() {
        scope.launch {
            busy = true; status = "Connecting securely…"
            try {
                val d = JSONObject(call("GET", "/api/admin/config"))
                config = d.getJSONObject("config")
                jsonText = config.toString(2)
                version = d.optInt("version", 1)
                connected = true; status = "ONLINE  •  CONFIG v$version"
            } catch (e: Exception) { connected = false; status = e.message ?: "Connection failed" }
            finally { busy = false }
        }
    }

    fun publish() {
        scope.launch {
            busy = true; status = "Publishing configuration…"
            try {
                val edited = JSONObject(jsonText)
                val d = JSONObject(call("PUT", "/api/admin/config", JSONObject().put("config", edited).toString()))
                config = edited; version = d.optInt("version", version + 1)
                status = "PUBLISHED  •  CONFIG v$version"
            } catch (e: Exception) { status = e.message ?: "Publish failed" }
            finally { busy = false }
        }
    }

    MaterialTheme(colorScheme = darkColorScheme(
        primary = neon, secondary = violet, background = bg, surface = panel,
        onBackground = Color.White, onSurface = Color.White
    )) {
        Column(Modifier.fillMaxSize().background(bg)) {
            Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 18.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(42.dp).background(Color(0xFF14263A), RoundedCornerShape(13.dp))
                        .border(1.dp, neon.copy(alpha=.65f), RoundedCornerShape(13.dp)), contentAlignment = Alignment.Center) {
                        Text("N", color = neon, fontSize = 24.sp, fontWeight = FontWeight.Black)
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text("NAEEM  /  CONTROL", color = neon, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                        Text("Command Centre", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    }
                    Text("v1.$version", color = Color.LightGray, fontSize = 11.sp)
                }
                Spacer(Modifier.height(14.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(7.dp).background(if (connected) neon else Color(0xFFFFB44D), RoundedCornerShape(50)))
                    Spacer(Modifier.width(8.dp))
                    Text(status, color = if (connected) neon else Color(0xFFFFC477), fontSize = 10.sp, letterSpacing = 1.sp)
                }
            }
            Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf("Overview", "Appearance", "Features", "Advanced").forEach { item ->
                    val selected = tab == item
                    Text(item, modifier = Modifier.weight(1f).background(if (selected) Color(0xFF163044) else panel, RoundedCornerShape(10.dp))
                        .border(1.dp, if (selected) neon.copy(alpha=.7f) else Color(0xFF25324A), RoundedCornerShape(10.dp))
                        .clickable { tab = item }.padding(vertical = 12.dp),
                        color = if (selected) neon else Color.LightGray, fontSize = 11.sp, fontWeight = FontWeight.SemiBold,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                }
            }
            Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (!connected) {
                    NeonCard("ADMIN AUTHENTICATION", "Your control key stays in memory only.") {
                        OutlinedTextField(token, { token = it }, modifier = Modifier.fillMaxWidth(),
                            label = { Text("Private admin token") }, singleLine = true,
                            visualTransformation = PasswordVisualTransformation(),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = neon, focusedLabelColor = neon))
                        Spacer(Modifier.height(10.dp))
                        NeonButton("CONNECT TO SERVER", !busy && token.isNotBlank()) { load() }
                    }
                } else {
                    when (tab) {
                        "Overview" -> {
                            NeonCard("SYSTEM OVERVIEW", "Naeem Music remote configuration") {
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Metric("CONFIG", "v$version", Modifier.weight(1f))
                                    Metric("API", "ONLINE", Modifier.weight(1f))
                                    Metric("MODE", "REMOTE", Modifier.weight(1f))
                                }
                            }
                            NeonCard("QUICK CONTROL", "Edit settings in the Appearance and Features tabs.") {
                                Text("Changes are saved to the server only after Publish. Your Music APK must support each setting before it can take effect.", color = Color.LightGray, fontSize = 12.sp)
                                NeonButton("RELOAD SERVER SETTINGS", !busy) { load() }
                            }
                            NeonCard("SECURITY", "Authenticated admin session") {
                                Text("Token is not stored on the device. No remote code execution or hidden device access.", color = Color.LightGray, fontSize = 12.sp)
                                NeonButton("DISCONNECT", !busy) { connected = false; token = ""; status = "Disconnected" }
                            }
                        }
                        "Appearance" -> {
                            NeonCard("APP BRANDING", "Name, subtitle and accent colour") {
                                JsonField("App name", config, "branding", "appName") { config = it; jsonText = it.toString(2) }
                                JsonField("Subtitle", config, "branding", "subtitle") { config = it; jsonText = it.toString(2) }
                                JsonField("Primary colour (HEX)", config, "branding", "primaryColor") { config = it; jsonText = it.toString(2) }
                            }
                            NeonCard("THEME PRESETS", "Select an accent preset for supported app screens") {
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    listOf("#19E6FF", "#9B5CFF", "#FF3D9A", "#35E6A5").forEach { hex ->
                                        val c = Color(android.graphics.Color.parseColor(hex))
                                        Box(Modifier.weight(1f).height(42.dp).background(c, RoundedCornerShape(10.dp))
                                            .border(2.dp, Color.White.copy(alpha=.5f), RoundedCornerShape(10.dp))
                                            .clickable {
                                                val next = JSONObject(config.toString())
                                                val brand = next.optJSONObject("branding") ?: JSONObject().also { next.put("branding", it) }
                                                brand.put("primaryColor", hex); config = next; jsonText = next.toString(2)
                                            })
                                    }
                                }
                                Text("Theme presets work only where the Music app reads remote theme settings. A future APK integration is needed for full theme switching.", color = Color.LightGray, fontSize = 11.sp)
                            }
                            NeonCard("BACKGROUND MEDIA", "Remote image or video URL") {
                                JsonField("Media URL", config, "background", "url") { config = it; jsonText = it.toString(2) }
                                JsonField("Media type (image/video)", config, "background", "type") { config = it; jsonText = it.toString(2) }
                                JsonToggle("Enable background", config, "background", "enabled") { config = it; jsonText = it.toString(2) }
                                Text("Upload storage is not enabled yet. Use a public HTTPS media URL.", color = Color.LightGray, fontSize = 11.sp)
                            }
                        }
                        "Features" -> {
                            NeonCard("FEATURE SWITCHES", "Remote flags for supported Music app features") {
                                listOf("lyrics", "visualizer", "aiSearch", "experimentalUi").forEach { key ->
                                    JsonToggle(key, config, "features", key) { config = it; jsonText = it.toString(2) }
                                }
                            }
                            NeonCard("ANNOUNCEMENTS", "Message shown inside Naeem Music") {
                                JsonToggle("Enable announcement", config, "announcement", "enabled") { config = it; jsonText = it.toString(2) }
                                JsonField("Title", config, "announcement", "title") { config = it; jsonText = it.toString(2) }
                                JsonField("Message", config, "announcement", "message") { config = it; jsonText = it.toString(2) }
                            }
                            NeonCard("ADS MANAGER", "Requires a compatible ad SDK in the Music APK") {
                                JsonToggle("Enable ads", config, "ads", "enabled") { config = it; jsonText = it.toString(2) }
                                JsonToggle("Banner ads", config, "ads", "banner") { config = it; jsonText = it.toString(2) }
                                JsonToggle("Interstitial ads", config, "ads", "interstitial") { config = it; jsonText = it.toString(2) }
                            }
                        }
                        else -> {
                            NeonCard("ADVANCED CONFIGURATION", "Power-user JSON editor") {
                                OutlinedTextField(jsonText, { jsonText = it }, modifier = Modifier.fillMaxWidth().heightIn(min = 280.dp),
                                    minLines = 12, label = { Text("Configuration JSON") },
                                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = neon))
                                Text("Invalid JSON or unsupported fields will be rejected by the API.", color = Color.LightGray, fontSize = 11.sp)
                            }
                            NeonCard("VERSION HISTORY", "Recent saved configuration versions") {
                                Text("History API is available; a dedicated restore screen is a next-stage integration.", color = Color.LightGray, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
            if (connected) Row(Modifier.fillMaxWidth().background(Color(0xFF0B1220)).padding(12.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                NeonButton("DISCARD", !busy, Modifier.weight(1f)) { load() }
                NeonButton(if (busy) "SAVING…" else "PUBLISH", !busy, Modifier.weight(1f)) { publish() }
            }
        }
    }
}

@Composable private fun NeonCard(title: String, subtitle: String, content: @Composable ColumnScope.() -> Unit) {
    Column(Modifier.fillMaxWidth().background(panel, RoundedCornerShape(18.dp))
        .border(1.dp, Color(0xFF25415A), RoundedCornerShape(18.dp)).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(title, color = neon, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp)
        Text(subtitle, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
        content()
    }
}
@Composable private fun Metric(label: String, value: String, modifier: Modifier) {
    Column(modifier.background(Color(0xFF0A1220), RoundedCornerShape(12.dp)).padding(10.dp)) {
        Text(label, color = Color.Gray, fontSize = 9.sp); Text(value, color = neon, fontWeight = FontWeight.Bold, fontSize = 13.sp)
    }
}
@Composable private fun NeonButton(label: String, enabled: Boolean, modifier: Modifier = Modifier, action: () -> Unit) {
    Button(onClick = action, enabled = enabled, modifier = modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF12364A), contentColor = neon),
        shape = RoundedCornerShape(12.dp)) { Text(label, fontWeight = FontWeight.Bold, letterSpacing = 1.sp, fontSize = 11.sp) }
}
@Composable private fun JsonField(label: String, source: JSONObject, group: String, key: String, changed: (JSONObject) -> Unit) {
    val current = source.optJSONObject(group)?.optString(key, "") ?: ""
    OutlinedTextField(current, { value ->
        val next = JSONObject(source.toString())
        val obj = next.optJSONObject(group) ?: JSONObject().also { next.put(group, it) }
        obj.put(key, value); changed(next)
    }, modifier = Modifier.fillMaxWidth(), label = { Text(label) }, singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = neon))
}
@Composable private fun JsonToggle(label: String, source: JSONObject, group: String, key: String, changed: (JSONObject) -> Unit) {
    val checked = source.optJSONObject(group)?.optBoolean(key, false) ?: false
    Row(Modifier.fillMaxWidth().clickable {
        val next = JSONObject(source.toString())
        val obj = next.optJSONObject(group) ?: JSONObject().also { next.put(group, it) }
        obj.put(key, !checked); changed(next)
    }.padding(vertical = 5.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(label, Modifier.weight(1f), color = Color.White, fontSize = 13.sp)
        Switch(checked, onCheckedChange = { value ->
            val next = JSONObject(source.toString())
            val obj = next.optJSONObject(group) ?: JSONObject().also { next.put(group, it) }
            obj.put(key, value); changed(next)
        }, colors = SwitchDefaults.colors(checkedThumbColor = neon, checkedTrackColor = Color(0xFF16475A)))
    }
}
