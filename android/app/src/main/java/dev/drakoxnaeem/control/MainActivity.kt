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
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
class MainActivity:ComponentActivity(){
 override fun onCreate(savedInstanceState:Bundle?){super.onCreate(savedInstanceState);setContent{MaterialTheme{ControlHome()}}}
}
@Composable private fun ControlHome(){
 var token by remember{mutableStateOf("")}
 var status by remember{mutableStateOf("Enter admin token and connect.")}
 var config by remember{mutableStateOf("No configuration loaded")}
 var busy by remember{mutableStateOf(false)}
 val scope=rememberCoroutineScope()
 Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),verticalArrangement=Arrangement.spacedBy(14.dp)){
  Text("Naeem Music",style=MaterialTheme.typography.labelLarge,color=MaterialTheme.colorScheme.primary)
  Text("Control Panel",style=MaterialTheme.typography.headlineLarge)
  Text("Developer · DrakoXNaeem")
  OutlinedTextField(token,{token=it},label={Text("Admin token")},modifier=Modifier.fillMaxWidth(),singleLine=true)
  Button(enabled=token.isNotBlank()&&!busy,modifier=Modifier.fillMaxWidth(),onClick={scope.launch{
   busy=true
   try{
    val body=withContext(Dispatchers.IO){
     val request=Request.Builder().url(BuildConfig.API_BASE_URL.trimEnd('/')+"/api/admin/config").header("Authorization","Bearer "+token).build()
     OkHttpClient().newCall(request).execute().use{response->if(!response.isSuccessful)error("API "+response.code);response.body?.string()?:error("Empty response")}
    }
    val json=JSONObject(body);config=json.getJSONObject("config").toString(2);status="Connected · config v"+json.optInt("version")
   }catch(e:Exception){status=e.message?:"Connection failed"}finally{busy=false}
  }}){Text(if(busy)"Connecting…" else "Connect")}
  Text(status,style=MaterialTheme.typography.bodySmall)
  Text("Current remote configuration",style=MaterialTheme.typography.titleMedium)
  Text(config,style=MaterialTheme.typography.bodySmall)
  Text("V1 Android foundation. Editing and publishing screens follow after backend deployment and secure sign-in.")
 }
}
