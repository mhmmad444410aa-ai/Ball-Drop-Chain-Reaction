package com.mhmmad.balldropchainreaction

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.Typography
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.TextUnit
import android.graphics.RectF
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.layout.ContentScale
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.compose.runtime.withFrameNanos
import kotlin.math.PI
import kotlin.math.min
import kotlin.math.sin

private enum class Screen { MENU, WORLDS, LEVELS, PLAY, SETTINGS, ABOUT, VICTORY, FAILED }
private val navy=Color(0xFF051C42)
private val gold=Color(0xFFFFCA54)
private val white=Color(0xFFFFF8EA)

class MainActivity: ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation=ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN)
        @Suppress("DEPRECATION")
        window.decorView.systemUiVisibility=(4 or 1024 or 4096)
        setContent { MaterialTheme(typography=Typography()) { BallDropApp() } }
    }
}

@Composable
private fun ButtonArt(text: String, onClick: ()->Unit, modifier: Modifier=Modifier,
                      primary: Boolean=true, fontSize: TextUnit=17.sp, enabled:Boolean=true, compact:Boolean=false) {
    val colors=if(primary) listOf(Color(0xFFFFE279),Color(0xFFFC9F13)) else listOf(Color(0xFF40B7F2),Color(0xFF1256BE))
    Box(modifier.clip(RoundedCornerShape(17.dp))
        .border(2.dp,if(primary) Color(0xFFFFF7C5) else Color(0xFF82E9FF),RoundedCornerShape(17.dp))
        .background(Brush.verticalGradient(colors))
        .clickable(enabled=enabled,onClick=onClick)
        .padding(horizontal=if(compact)10.dp else 18.dp,vertical=if(compact)5.dp else 11.dp),contentAlignment=Alignment.Center) {
        Text(text,color=if(primary) Color(0xFF51300A) else Color.White,fontSize=fontSize,fontWeight=FontWeight.ExtraBold,
            textAlign=TextAlign.Center,maxLines=1,overflow=TextOverflow.Ellipsis)
    }
}
@Composable
private fun Heading(text: String,size: TextUnit=34.sp) {
    Text(text,color=white,fontWeight=FontWeight.Black,fontSize=size,letterSpacing=1.sp,
        style=androidx.compose.ui.text.TextStyle(shadow=Shadow(color=Color(0xFF080A32),offset=Offset(2f,4f),blurRadius=8f)),
        textAlign=TextAlign.Center)
}
@Composable
private fun OverlayPanel(modifier: Modifier=Modifier,content:@Composable ColumnScope.()->Unit) {
    Column(modifier.clip(RoundedCornerShape(24.dp)).background(Color(0xE9072449))
        .border(2.dp,Color(0xFF3F9DD3),RoundedCornerShape(24.dp))
        .padding(18.dp),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(10.dp),content=content)
}

@Composable
private fun BallDropApp() {
    val context=LocalContext.current
    val art=remember { ArtStore(context) }
    val store=remember { SaveStore(context) }
    val audio=remember { GameAudio(context) }
    DisposableEffect(Unit) { onDispose { audio.release() } }
    val save by store.flow.collectAsState(initial=SaveState())
    val coroutine=rememberCoroutineScope()
    var screen by rememberSaveable { mutableStateOf(Screen.MENU) }
    var selectedWorld by rememberSaveable { mutableIntStateOf(0) }
    var levelId by rememberSaveable { mutableIntStateOf(1) }
    var lastScore by rememberSaveable { mutableIntStateOf(0) }
    val lang=save.language
    val tr:(String)->String={Texts.get(lang,it)}
    CompositionLocalProvider(LocalLayoutDirection provides if(lang=="ar") LayoutDirection.Rtl else LayoutDirection.Ltr) {
        Box(Modifier.fillMaxSize().background(navy)) {
            when(screen) {
                Screen.MENU -> {
                    ArtBackground(art,"menu")
                    MenuMotionOverlay(art)
                    BoxWithConstraints(Modifier.fillMaxSize()) {
                        val compact=maxHeight<300.dp || maxWidth<680.dp
                        Column(Modifier.fillMaxSize().padding(if(compact)4.dp else 10.dp),horizontalAlignment=Alignment.CenterHorizontally,
                            verticalArrangement=Arrangement.Center) {
                            Heading(tr("title"),if(compact)24.sp else 39.sp)
                            Text(tr("subtitle"),color=gold,fontSize=if(compact)12.sp else 19.sp,
                                fontWeight=FontWeight.Bold,letterSpacing=if(compact)1.sp else 3.sp)
                            Spacer(Modifier.height(if(compact)3.dp else 8.dp))
                            Row(horizontalArrangement=Arrangement.spacedBy(if(compact)6.dp else 12.dp)) {
                                ButtonArt("▶  ${tr("play")}",{levelId=1;screen=Screen.PLAY},
                                    Modifier.width(if(compact)120.dp else 170.dp),compact=compact,fontSize=if(compact)12.sp else 17.sp)
                                ButtonArt("◆  ${tr("continue")}",{levelId=save.unlocked;screen=Screen.PLAY},
                                    Modifier.width(if(compact)126.dp else 178.dp),primary=false,compact=compact,fontSize=if(compact)12.sp else 17.sp)
                            }
                            Spacer(Modifier.height(if(compact)4.dp else 8.dp))
                            Row(horizontalArrangement=Arrangement.spacedBy(if(compact)6.dp else 12.dp)) {
                                ButtonArt(tr("worlds"),{screen=Screen.WORLDS},Modifier.width(if(compact)83.dp else 130.dp),primary=false,fontSize=if(compact)10.sp else 14.sp,compact=compact)
                                ButtonArt(tr("settings"),{screen=Screen.SETTINGS},Modifier.width(if(compact)94.dp else 140.dp),primary=false,fontSize=if(compact)10.sp else 14.sp,compact=compact)
                                ButtonArt(tr("about"),{screen=Screen.ABOUT},Modifier.width(if(compact)81.dp else 105.dp),primary=false,fontSize=if(compact)10.sp else 14.sp,compact=compact)
                            }
                            Spacer(Modifier.height(if(compact)3.dp else 8.dp))
                            Text(tr("offline"),color=white,fontSize=if(compact)9.sp else 12.sp,fontWeight=FontWeight.Medium)
                        }
                    }
                }
                Screen.WORLDS -> {
                    ArtBackground(art,"worlds")
                    Column(Modifier.fillMaxSize().padding(12.dp),horizontalAlignment=Alignment.CenterHorizontally) {
                        Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically) {
                            ButtonArt("‹ ${tr("back")}",{screen=Screen.MENU},Modifier.width(108.dp),primary=false,fontSize=14.sp)
                            Box(Modifier.weight(1f),contentAlignment=Alignment.Center){Heading(tr("worlds"),27.sp)}
                            Spacer(Modifier.width(108.dp))
                        }
                        Spacer(Modifier.height(10.dp))
                        Row(Modifier.fillMaxSize(),horizontalArrangement=Arrangement.SpaceEvenly,verticalAlignment=Alignment.CenterVertically) {
                            LevelCatalog.worldNames.forEachIndexed { index, name ->
                                val accessible=save.unlocked >= index*4+1
                                Column(Modifier.weight(1f).padding(5.dp).clip(RoundedCornerShape(18.dp))
                                    .background(if(accessible) Color(0xDF0C4773) else Color(0xD91B243B))
                                    .border(2.dp,if(accessible)gold else Color.Gray,RoundedCornerShape(18.dp))
                                    .clickable(enabled=accessible){selectedWorld=index;screen=Screen.LEVELS}
                                    .padding(10.dp),horizontalAlignment=Alignment.CenterHorizontally) {
                                    Heading(if(accessible)"${index+1}" else "🔒",28.sp)
                                    Spacer(Modifier.height(5.dp))
                                    Text(Texts.world(lang,index),color=white,textAlign=TextAlign.Center,fontSize=12.sp,fontWeight=FontWeight.Bold,maxLines=2)
                                    Text("${index*4+1}–${index*4+4}",color=gold,fontSize=11.sp)
                                }
                            }
                        }
                    }
                }
                Screen.LEVELS -> {
                    ArtBackground(art,"worlds")
                    Column(Modifier.fillMaxSize().padding(12.dp),horizontalAlignment=Alignment.CenterHorizontally) {
                        Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically) {
                            ButtonArt("‹ ${tr("back")}",{screen=Screen.WORLDS},Modifier.width(110.dp),primary=false,fontSize=14.sp)
                            Box(Modifier.weight(1f),contentAlignment=Alignment.Center) { Heading(Texts.world(lang,selectedWorld),24.sp) }
                            Spacer(Modifier.width(110.dp))
                        }
                        Spacer(Modifier.height(12.dp))
                        Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceEvenly,verticalAlignment=Alignment.CenterVertically) {
                            (selectedWorld*4+1..selectedWorld*4+4).forEach { index ->
                                val open=index<=save.unlocked
                                val stars=save.stars[index] ?: 0
                                Column(Modifier.weight(1f).padding(horizontal=4.dp).clip(RoundedCornerShape(17.dp))
                                    .background(if(open) Color(0xEB195D8E) else Color(0xEB252C3A))
                                    .border(2.dp,if(open)gold else Color.DarkGray,RoundedCornerShape(17.dp))
                                    .clickable(enabled=open){ levelId=index;screen=Screen.PLAY }
                                    .padding(15.dp),horizontalAlignment=Alignment.CenterHorizontally) {
                                    Heading(if(open)"$index" else "🔒",30.sp)
                                    Text(if(open)"★".repeat(stars)+"☆".repeat(3-stars) else tr("locked"),
                                        color=gold,fontWeight=FontWeight.Bold,fontSize=17.sp)
                                }
                            }
                        }
                    }
                }
                Screen.PLAY -> PhysicsGame(art,audio,LevelCatalog.levels[levelId-1],lang,save.sound,
                    onBack={screen=Screen.LEVELS;selectedWorld=(levelId-1)/4},
                    onWin={stars->lastScore=stars;coroutine.launch{store.recordWin(levelId,stars)};audio.play("win",save.sound);screen=Screen.VICTORY},
                    onFail={screen=Screen.FAILED})
                Screen.VICTORY -> {
                    ArtBackground(art,"victory")
                    VictoryMotionOverlay(art)
                    Column(Modifier.fillMaxSize(),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.Center) {
                        Heading(tr("complete"),33.sp)
                        Text("★".repeat(lastScore)+"☆".repeat(3-lastScore),color=gold,fontSize=36.sp)
                        Text(tr("finish"),color=white,fontSize=15.sp)
                        Spacer(Modifier.height(12.dp))
                        Row(horizontalArrangement=Arrangement.spacedBy(14.dp)) {
                            ButtonArt(tr("levels"),{screen=Screen.LEVELS;selectedWorld=(levelId-1)/4},Modifier.width(148.dp),primary=false)
                            ButtonArt(tr("next"),{levelId=(levelId+1).coerceAtMost(20);screen=Screen.PLAY},Modifier.width(174.dp))
                        }
                    }
                }
                Screen.FAILED -> {
                    ArtBackground(art,"workshop")
                    Column(Modifier.fillMaxSize(),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.Center) {
                        OverlayPanel {
                            Heading(tr("failed"),32.sp)
                            Text(tr("try"),color=white,fontSize=15.sp)
                            Row(horizontalArrangement=Arrangement.spacedBy(12.dp)) {
                                ButtonArt(tr("levels"),{screen=Screen.LEVELS;selectedWorld=(levelId-1)/4},primary=false)
                                ButtonArt(tr("reset"),{screen=Screen.PLAY})
                            }
                        }
                    }
                }
                Screen.SETTINGS -> {
                    ArtBackground(art,"settings")
                    Column(Modifier.fillMaxSize().padding(12.dp),horizontalAlignment=Alignment.CenterHorizontally) {
                        Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically) {
                            ButtonArt("‹ ${tr("back")}",{screen=Screen.MENU},primary=false,fontSize=14.sp)
                            Box(Modifier.weight(1f),contentAlignment=Alignment.Center) { Heading(tr("settings"),29.sp) }
                        }
                        Spacer(Modifier.height(8.dp))
                        OverlayPanel {
                            Text(tr("language"),color=white,fontSize=18.sp,fontWeight=FontWeight.Bold)
                            Row(horizontalArrangement=Arrangement.spacedBy(9.dp)) {
                                listOf("en" to "English", "ar" to "العربية", "pt" to "Português").forEach { (code,label) ->
                                    ButtonArt(label,{coroutine.launch{store.setLanguage(code)}},Modifier.width(95.dp),primary=lang==code,fontSize=11.sp,compact=true)
                                }
                            }
                            Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(18.dp)) {
                                Text(tr("sound"),color=white,fontSize=17.sp)
                                ButtonArt(if(save.sound)"ON" else "OFF",{coroutine.launch{store.setSound(!save.sound)}},Modifier.width(96.dp),primary=save.sound)
                            }
                            Text(tr("sound_note"),color=gold,fontSize=12.sp)
                        }
                    }
                }
                Screen.ABOUT -> {
                    ArtBackground(art,"settings")
                    Column(Modifier.fillMaxSize().padding(12.dp),horizontalAlignment=Alignment.CenterHorizontally) {
                        ButtonArt("‹ ${tr("back")}",{screen=Screen.MENU},primary=false)
                        Spacer(Modifier.height(14.dp))
                        OverlayPanel(Modifier.fillMaxWidth(.8f)) {
                            Heading(tr("about"),26.sp)
                            Text(tr("about_text"),color=white,fontSize=15.sp,textAlign=TextAlign.Center)
                            Text(tr("privacy"),color=gold,fontSize=12.sp,textAlign=TextAlign.Center)
                        }
                    }
                }
            }
        }
    }
}

/** Lightweight decorative animation; physically simulated interactions occur in PhysicsGame. */
@Composable
private fun MenuMotionOverlay(art: ArtStore) {
    var frame by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) {
        while(isActive) withFrameNanos { frame++ }
    }
    androidx.compose.foundation.Canvas(Modifier.fillMaxSize()) {
        val tick=frame.toFloat()
        drawIntoCanvas { c ->
            val n=c.nativeCanvas
            val radius=min(size.width,size.height)*.055f
            val cx=size.width*.31f;val cy=size.height*.85f
            art.paint("gear",n,RectF(cx-radius,cy-radius,cx+radius,cy+radius),tick*1.1f,160)
            val flash=(160+90*sin(tick*.08f)).toInt().coerceIn(0,255)
            val gx=size.width*.83f;val gy=size.height*.48f
            val s=radius*.55f
            art.paint("spark",n,RectF(gx-s,gy-s,gx+s,gy+s),tick*.8f,flash)
            val bx=size.width*(.61f+.06f*sin(tick*.027f))
            val by=size.height*(.86f-.025f*abs(sin(tick*.043f)))
            art.paint("ball_blue",n,RectF(bx-radius*.48f,by-radius*.48f,bx+radius*.48f,by+radius*.48f),tick*2f,170)
        }
    }
}
@Composable
private fun VictoryMotionOverlay(art: ArtStore) {
    var frame by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) { while(isActive) withFrameNanos { frame++ } }
    androidx.compose.foundation.Canvas(Modifier.fillMaxSize()) {
        drawIntoCanvas { c ->
            val n=c.nativeCanvas
            for(i in 0..3) {
                val x=size.width*(.25f+i*.16f);val y=size.height*(.17f+.035f*sin(frame*.08f+i))
                val r=size.height*.06f
                art.paint("spark",n,RectF(x-r,y-r,x+r,y+r),frame*2f,160)
            }
        }
    }
}
