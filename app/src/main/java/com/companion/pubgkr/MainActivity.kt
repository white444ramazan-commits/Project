package com.companion.pubgkr

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.StatFs
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import kotlin.math.hypot
import kotlin.random.Random

private val bg = Color(0xFF0B1018)
private val panel = Color(0xFF151E2B)
private val accent = Color(0xFF48D7B0)

class MainActivity : ComponentActivity() {
 override fun onCreate(savedInstanceState: Bundle?) { super.onCreate(savedInstanceState); setContent { App(this) } }
}

@Composable private fun App(context: Context) {
 var page by remember { mutableStateOf("Главная") }
 MaterialTheme(colorScheme = darkColorScheme(primary=accent, background=bg, surface=panel)) {
  Row(Modifier.fillMaxSize().background(bg)) {
   NavigationRail(containerColor=Color(0xFF0E1621)) {
    listOf("Главная","Aim Trainer","ESP Simulator","Sensitivity","Root","Диагностика").forEach {
     NavigationRailItem(selected=page==it,onClick={page=it},icon={},label={Text(it,fontSize=10.sp)})
    }
   }
   Column(Modifier.fillMaxSize().padding(20.dp)) {
    Text("PUBG MOBILE KR 4.7",color=accent)
    Text(page,color=Color.White,fontSize=26.sp)
    Spacer(Modifier.height(16.dp))
    when(page) {
     "Главная" -> PanelCard("Companion","Офлайн-инструменты тренировок и диагностики. Приложение не подключается к процессу PUBG.")
     "Aim Trainer" -> AimScreen()
     "ESP Simulator" -> EspScreen()
     "Sensitivity" -> SensitivityScreen(context)
     "Root" -> RootScreen()
     else -> Diagnostics(context)
    }
   }
  }
 }
}

@Composable private fun PanelCard(title:String,body:String) {
 Card(colors=CardDefaults.cardColors(containerColor=panel),shape=RoundedCornerShape(16.dp)) {
  Column(Modifier.padding(16.dp)) { Text(title,color=accent); Spacer(Modifier.height(8.dp)); Text(body,color=Color.LightGray) }
 }
}

@Composable private fun AimScreen() {
 var x by remember { mutableFloatStateOf(.5f) }; var y by remember { mutableFloatStateOf(.5f) }
 var hits by remember { mutableIntStateOf(0) }; var misses by remember { mutableIntStateOf(0) }
 var radius by remember { mutableFloatStateOf(34f) }; var active by remember { mutableStateOf(false) }
 var start by remember { mutableLongStateOf(System.currentTimeMillis()) }; var last by remember { mutableLongStateOf(0) }
 LaunchedEffect(active) { while(active) { delay(900); x=Random.nextFloat()*.8f+.1f; y=Random.nextFloat()*.75f+.12f; start=System.currentTimeMillis() } }
 Column {
  Text("Попадания $hits   Промахи $misses   Точность ${if(hits+misses==0) 0 else hits*100/(hits+misses)}%",color=Color.White)
  Text("Размер мишени",color=Color.LightGray); Slider(radius,{radius=it},valueRange=18f..55f)
  Canvas(Modifier.fillMaxWidth().weight(1f).background(Color(0xFF101A27),RoundedCornerShape(14.dp)).pointerInput(x,y,radius) {
   detectTapGestures { p -> if(hypot(p.x-x*size.width,p.y-y*size.height)<=radius) { hits++; last=System.currentTimeMillis()-start; x=Random.nextFloat()*.8f+.1f; y=Random.nextFloat()*.75f+.12f; start=System.currentTimeMillis() } else misses++ }
  }) { val p=Offset(x*size.width,y*size.height); drawCircle(accent,radius,p); drawCircle(Color.White,radius*.55f,p,style=androidx.compose.ui.graphics.drawscope.Stroke(2.dp.toPx())) }
  Text("Реакция: $last мс",color=Color.LightGray)
  Row { Button(onClick={active=!active}) { Text(if(active) "Пауза" else "Старт") }; Spacer(Modifier.width(8.dp)); OutlinedButton(onClick={hits=0;misses=0;last=0;active=false}) { Text("Сброс") } }
 }
}

@Composable private fun EspScreen() {
 var count by remember { mutableIntStateOf(8) }; var distance by remember { mutableStateOf(true) }
 val dots=remember(count) { List(count) { Offset(Random.nextFloat()*.8f+.1f,Random.nextFloat()*.8f+.1f) } }
 Column {
  Text("Автономные тестовые маркеры, не связанные с игрой.",color=Color.LightGray)
  Text("Количество: $count"); Slider(count.toFloat(),{count=it.toInt()},valueRange=2f..20f)
  Row { Text("Дистанции"); Switch(distance,{distance=it}) }
  Canvas(Modifier.fillMaxWidth().weight(1f).background(Color(0xFF101A27),RoundedCornerShape(14.dp))) {
   dots.forEach { drawCircle(accent,9.dp.toPx(),Offset(it.x*size.width,it.y*size.height)); if(distance) drawLine(accent.copy(alpha=.5f),Offset(size.width/2,size.height),Offset(it.x*size.width,it.y*size.height),1.dp.toPx()) }
  }
 }
}

@Composable private fun SensitivityScreen(context:Context) {
 val prefs=remember { context.getSharedPreferences("sensitivity",Context.MODE_PRIVATE) }
 var name by remember { mutableStateOf("My profile") }; var camera by remember { mutableFloatStateOf(100f) }; var ads by remember { mutableFloatStateOf(100f) }; var gyro by remember { mutableFloatStateOf(250f) }
 var saved by remember { mutableStateOf(prefs.getString("profiles","") ?: "") }
 Column {
  OutlinedTextField(name,{name=it},label={Text("Название профиля")})
  Text("Camera: ${camera.toInt()}"); Slider(camera,{camera=it},valueRange=0f..300f)
  Text("ADS: ${ads.toInt()}"); Slider(ads,{ads=it},valueRange=0f..300f)
  Text("Gyroscope: ${gyro.toInt()}"); Slider(gyro,{gyro=it},valueRange=0f..400f)
  Button(onClick={ saved += "\n$name | Camera ${camera.toInt()} | ADS ${ads.toInt()} | Gyro ${gyro.toInt()}"; prefs.edit().putString("profiles",saved).apply() }) { Text("Сохранить профиль") }
  Text(saved,color=Color.LightGray)
  Text("Значения хранятся локально и не применяются автоматически к PUBG.",color=Color.Gray,fontSize=12.sp)
 }
}

@Composable private fun RootScreen() {
 var status by remember { mutableStateOf("Нажмите кнопку для проверки.") }; var busy by remember { mutableStateOf(false) }; val scope= rememberCoroutineScope()
 Column {
  PanelCard("Root diagnostics","Запрос su выполняется только по нажатию. Игровые файлы и процессы не изменяются.")
  Spacer(Modifier.height(12.dp))
  Button(enabled=!busy,onClick={ busy=true; scope.launch { status=runCatching { val p=Runtime.getRuntime().exec(arrayOf("su","-c","id")); val out=BufferedReader(InputStreamReader(p.inputStream)).readText(); val code=p.waitFor(); if(code==0 && out.contains("uid=0")) "Root доступен: $out" else "Root не предоставлен (код $code)" }.getOrElse { "Ошибка: ${it.message}" }; busy=false } }) { Text(if(busy) "Проверка…" else "Проверить Root") }
  Text(status,color=Color.LightGray)
  Text("KernelSU подтверждается через разрешённый su-доступ; отдельный KernelSU API не вызывается.",color=Color.Gray,fontSize=12.sp)
 }
}

@Composable private fun Diagnostics(context:Context) {
 val am=context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager; val mi=ActivityManager.MemoryInfo(); am.getMemoryInfo(mi)
 val fs=StatFs(Environment.getDataDirectory().path)
 val temps=File("/sys/class/thermal").listFiles()?.filter { it.name.startsWith("thermal_zone") }?.mapNotNull { runCatching { File(it,"temp").readText().trim().toFloatOrNull() }.getOrNull() }?.firstOrNull()
 Column(verticalArrangement=Arrangement.spacedBy(10.dp)) {
  PanelCard("Устройство","${Build.MANUFACTURER} ${Build.MODEL}\nAndroid ${Build.VERSION.RELEASE} · API ${Build.VERSION.SDK_INT}\nABI ${Build.SUPPORTED_ABIS.joinToString()}")
  PanelCard("RAM","Свободно ${mi.availMem/1048576} MB / ${mi.totalMem/1048576} MB")
  PanelCard("Хранилище","Свободно ${fs.availableBytes/1073741824} GB / ${fs.totalBytes/1073741824} GB")
  PanelCard("Температура",temps?.let { "${if(it>1000) it/1000 else it} °C" } ?: "Датчик недоступен")
 }
}
