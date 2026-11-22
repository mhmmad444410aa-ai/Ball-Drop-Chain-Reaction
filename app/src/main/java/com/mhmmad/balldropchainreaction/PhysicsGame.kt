package com.mhmmad.balldropchainreaction

import android.graphics.Paint
import android.graphics.RectF
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.ui.input.pointer.awaitFirstDown
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.TextUnit
import kotlinx.coroutines.isActive
import androidx.compose.runtime.withFrameNanos
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin

private data class Viewport(val scale: Float,val left: Float,val bottom: Float) {
    fun x(x: Float): Float = left+x*scale
    fun y(y: Float): Float = bottom-y*scale
    fun toWorld(offset: Offset) = Pair((offset.x-left)/scale,(bottom-offset.y)/scale)
}
private fun viewport(width: Float,height: Float,zoom: Float): Viewport {
    val s=min(width/16f,height/9f)*zoom
    return Viewport(s,(width-16f*s)/2f,(height+9f*s)/2f)
}

@Composable
fun PhysicsGame(art: ArtStore,audio: GameAudio,level: Level,lang: String,sound: Boolean,
                onBack: ()->Unit,onWin: (Int)->Unit,onFail: ()->Unit) {
    var scene by remember(level.id) { mutableStateOf(PhysicsScene(level)) }
    var placed by remember(level.id) { mutableStateOf(emptyList<Placed>()) }
    var selected by remember(level.id) { mutableStateOf(level.inventory.first()) }
    var angle by remember(level.id) { mutableFloatStateOf(-12f) }
    var zoom by remember(level.id) { mutableFloatStateOf(1f) }
    var ghost by remember(level.id) { mutableStateOf<Placed?>(null) }
    var canvasSize by remember { mutableStateOf(IntSize(1600,900)) }
    var frames by remember { mutableIntStateOf(0) }
    val t:(String)->String={Texts.get(lang,it)}
    val compact=LocalConfiguration.current.screenHeightDp < 310
    val isEditing = scene.status==PhysicsScene.Status.READY
    fun reset() {
        scene.destroy();placed=emptyList();ghost=null
        scene=PhysicsScene(level);frames++
    }
    fun toolLeft(kind: Tool): Int = level.inventory.count{it==kind}-placed.count{it.kind==kind}
    fun place(x: Float,y: Float,kind: Tool,rot: Float) {
        if(!isEditing||toolLeft(kind)<=0) return
        val snapX=(x*4).roundToInt()/4f
        val snapY=(y*4).roundToInt()/4f
        if(snapX !in 1f..15f || snapY !in 1f..8.3f) return
        val appliedAngle=if(kind in setOf(Tool.PLANK,Tool.DOMINO,Tool.SEESAW)) rot else 0f
        placed=placed+Placed(kind,snapX,snapY,appliedAngle)
        audio.play("tap",sound)
    }
    fun placeFromPixel(pos:Offset) {
        val v=viewport(canvasSize.width.toFloat(),canvasSize.height.toFloat(),zoom)
        val (x,y)=v.toWorld(pos)
        place(x,y,selected,angle)
    }
    // Visual refresh is independent of the fixed-step physics simulation.
    LaunchedEffect(scene) {
        var previous=0L
        var accumulator=0f
        var lastCollision=0
        while(isActive) {
            withFrameNanos { now ->
                if(previous==0L) previous=now
                val frameDelta=((now-previous)/1_000_000_000f).coerceIn(0f,.07f)
                previous=now
                accumulator+=frameDelta
                var steps=0
                while(accumulator>=1f/60f && steps<4) {
                    scene.step()
                    accumulator-=1f/60f
                    steps++
                }
                if(steps==4) accumulator=0f // avoid spiral of death
                frames++
            }
            if(scene.collisions>lastCollision) {
                audio.play("bounce",sound)
                lastCollision=scene.collisions
            }
            when(scene.status) {
                PhysicsScene.Status.WON -> { onWin(scene.score()); break }
                PhysicsScene.Status.LOST -> { onFail(); break }
                else -> Unit
            }
        }
    }
    Box(Modifier.fillMaxSize().background(Color(0xFF061634))) {
        androidx.compose.foundation.Canvas(Modifier.fillMaxSize()
            .onSizeChanged { canvasSize=it }
            .pointerInput(level.id, selected,angle,placed,scene,zoom) {
                awaitEachGesture {
                    val down=awaitFirstDown(requireUnconsumed=false)
                    var last=down.position
                    var dragging=false
                    do {
                        val event=awaitPointerEvent()
                        val change=event.changes.first()
                        if(change.pressed) {
                            last=change.position
                            if((last-down.position).getDistance()>viewConfiguration.touchSlop) dragging=true
                            if(dragging && isEditing && toolLeft(selected)>0) {
                                val vp=viewport(size.width.toFloat(),size.height.toFloat(),zoom)
                                val (x,y)=vp.toWorld(last)
                                ghost=Placed(selected,(x*4).roundToInt()/4f,(y*4).roundToInt()/4f,if(selected in setOf(Tool.PLANK,Tool.DOMINO,Tool.SEESAW)) angle else 0f)
                            }
                            change.consume()
                        }
                    } while(change.pressed)
                    if(isEditing) placeFromPixel(if(dragging)last else down.position)
                    ghost=null
                }
            }) {
            val refresh=frames
            drawIntoCanvas { canvas ->
                val n=canvas.nativeCanvas
                art.paint("playfield",n,RectF(0f,0f,size.width,size.height))
                val v=viewport(size.width,size.height,zoom)
                // Decorative physical floor, separated from the simulated static floor.
                art.paint("platform",n,RectF(v.x(0f),v.y(.55f),v.x(16f),v.y(-.28f)))
                val pulse=(sin(refresh*.095f)+1f)/2f
                val glow=Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color=android.graphics.Color.argb((50+90*pulse).toInt(),255,198,21)
                }
                n.drawCircle(v.x(level.targetX),v.y(level.targetY),v.scale*(.64f+pulse*.1f),glow)
                art.paint("target",n,RectF(v.x(level.targetX-.43f),v.y(level.targetY+.62f),
                    v.x(level.targetX+.43f),v.y(level.targetY-.62f)))
                for(actor in scene.actors) {
                    val b=actor.body
                    val cx=v.x(b.position.x);val cy=v.y(b.position.y)
                    val halfW=actor.width*v.scale*.5f;val halfH=actor.height*v.scale*.5f
                    val dynamicRotation=-(b.angle*180f/PI.toFloat())
                    val springFlex=if(actor.image=="spring") 1f-.30f*scene.springCompression(b) else 1f
                    art.paint(actor.image,n,RectF(cx-halfW,cy-halfH*springFlex,cx+halfW,cy+halfH*springFlex),dynamicRotation)
                }
                // Each player-placed body is rendered from its own asset even before simulation.
                if(isEditing) {
                    for(p in placed) drawTool(n,art,v,p,255)
                    ghost?.let{ drawTool(n,art,v,it,150) }
                }
                if(scene.status==PhysicsScene.Status.RUNNING) {
                    val impactAge=scene.time-scene.lastImpactTime
                    if(impactAge in 0f..0.25f) {
                        val fade=((1f-impactAge/.25f)*255).toInt()
                        val ix=v.x(scene.lastImpactX);val iy=v.y(scene.lastImpactY)
                        val side=.52f*v.scale
                        art.paint("spark",n,RectF(ix-side,iy-side,ix+side,iy+side),0f,fade)
                    }
                    val ball=scene.ballPosition()
                    val trail=Paint(Paint.ANTI_ALIAS_FLAG).apply { color=android.graphics.Color.argb(90,255,240,120);strokeWidth=v.scale*.075f }
                    n.drawLine(v.x(ball.x-.18f),v.y(ball.y+.10f),v.x(ball.x-.55f),v.y(ball.y+.17f),trail)
                }
            }
        }
        Row(Modifier.align(Alignment.TopCenter).fillMaxWidth()
            .background(Color(0xE90B244B)).padding(horizontal=9.dp,vertical=5.dp),
            horizontalArrangement=Arrangement.spacedBy(6.dp),verticalAlignment=Alignment.CenterVertically) {
            SmallGameButton("‹",onBack)
            Text("${t("levels")} ${level.id}: ${Texts.level(lang,level.id)}",color=Color.White,fontSize=13.sp,fontWeight=FontWeight.Bold,modifier=Modifier.weight(1f),maxLines=1)
            Text("${scene.time.toInt()}s",color=Color(0xFFFFD44F),fontSize=12.sp)
            SmallGameButton("−",{zoom=(zoom-.15f).coerceAtLeast(.75f)})
            SmallGameButton("+",{zoom=(zoom+.15f).coerceAtMost(1.25f)})
            SmallGameButton(t("reset"),{reset()})
        }
        Column(Modifier.align(Alignment.BottomCenter).fillMaxWidth()
            .background(Color(0xE9072749)).border(1.dp,Color(0xFF429EDB))
            .padding(horizontal=6.dp,vertical=if(compact)2.dp else 5.dp),
            verticalArrangement=Arrangement.spacedBy(2.dp)) {
            Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(4.dp),verticalAlignment=Alignment.CenterVertically) {
                Text(t("tool"),color=Color(0xFFFECA59),fontSize=if(compact)10.sp else 13.sp,fontWeight=FontWeight.Black)
                Row(Modifier.weight(1f).horizontalScroll(rememberScrollState()),horizontalArrangement=Arrangement.spacedBy(5.dp)) {
                    level.inventory.distinct().forEach { tool ->
                        val remaining=toolLeft(tool)
                        ToolCell(art,tool,selected==tool,remaining,compact){if(isEditing) selected=tool}
                    }
                }
                SmallGameButton(if(scene.status==PhysicsScene.Status.RUNNING)t("pause") else t("run"),{
                    when(scene.status) {
                        PhysicsScene.Status.READY -> { scene.destroy();scene=PhysicsScene(level,placed);scene.run();audio.play("tap",sound) }
                        PhysicsScene.Status.RUNNING -> scene.pause()
                        PhysicsScene.Status.PAUSED -> scene.run()
                        else -> Unit
                    }
                },highlight=true,compact=compact)
            }
            Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(5.dp),verticalAlignment=Alignment.CenterVertically) {
                SmallGameButton(t("undo"),{if(isEditing && placed.isNotEmpty()) {placed=placed.dropLast(1);audio.play("tap",sound)}},enabled=isEditing,compact=compact)
                SmallGameButton(t("rotate"),{
                    if(isEditing) {
                        if(placed.isNotEmpty()) {
                            val last=placed.last()
                            val next=(last.angle+15f).let { if(it>60f) -60f else it }
                            placed=placed.dropLast(1)+last.copy(angle=next)
                        } else angle=(angle+15f).let { if(it>60f) -60f else it }
                    }
                },enabled=isEditing && ((placed.lastOrNull()?.kind ?: selected) in setOf(Tool.PLANK,Tool.DOMINO,Tool.SEESAW)),compact=compact)
                SmallGameButton(t("hint"),{
                    if(isEditing && toolLeft(level.hint.kind)>0) {
                        val h=level.hint; place(h.x,h.y,h.kind,h.angle)
                        selected=h.kind
                    }
                },enabled=isEditing,compact=compact)
                Spacer(Modifier.weight(1f))
                if(!compact) Text(t("objective")+"  •  "+t("place"),color=Color(0xFFA6D9F9),fontSize=10.sp,maxLines=1)
            }
        }
    }
}
private fun drawTool(n:android.graphics.Canvas,art:ArtStore,v:Viewport,p:Placed,alpha:Int) {
    val dimensions=when(p.kind) {
        Tool.PLANK -> 2.6f to .42f
        Tool.DOMINO -> .38f to .96f
        Tool.SPRING -> 1.0f to .8f
        Tool.SEESAW -> 2.35f to .8f
        Tool.PIPE -> 1.25f to 1.15f
    }
    val cx=v.x(p.x); val cy=v.y(p.y)
    val hw=dimensions.first*v.scale*.5f;val hh=dimensions.second*v.scale*.5f
    art.paint(p.kind.asset,n,RectF(cx-hw,cy-hh,cx+hw,cy+hh),-p.angle,alpha)
}
@Composable
private fun SmallGameButton(text:String,onClick:()->Unit,enabled:Boolean=true,highlight:Boolean=false,compact:Boolean=false) {
    Box(Modifier.clip(RoundedCornerShape(11.dp))
        .background(if(!enabled)Color(0xFF3C4A57) else if(highlight)Color(0xFFF2A52F) else Color(0xFF186EAD))
        .border(1.dp,if(highlight)Color(0xFFFFE78B) else Color(0xFF69B8E6),RoundedCornerShape(11.dp))
        .clickable(enabled=enabled,onClick=onClick)
        .padding(horizontal=if(compact)7.dp else 11.dp,vertical=if(compact)3.dp else 7.dp),contentAlignment=Alignment.Center) {
        Text(text,color=Color.White,fontSize=if(compact)10.sp else 12.sp,fontWeight=FontWeight.ExtraBold,maxLines=1)
    }
}
@Composable
private fun ToolCell(art:ArtStore,tool:Tool,selected:Boolean,remaining:Int,compact:Boolean,onClick:()->Unit) {
    val sprite=art.get(tool.asset)
    Row(Modifier.clip(RoundedCornerShape(9.dp))
        .background(if(selected)Color(0xFF177AAF) else Color(0xFF133A65))
        .border(1.dp,if(selected)Color(0xFFFFD45F) else Color(0xFF38698D),RoundedCornerShape(9.dp))
        .clickable(enabled=remaining>0,onClick=onClick).padding(horizontal=4.dp,vertical=2.dp),verticalAlignment=Alignment.CenterVertically) {
        if(sprite!=null) androidx.compose.foundation.Image(painter=androidx.compose.ui.graphics.painter.BitmapPainter(sprite.asImageBitmap()),
            contentDescription=tool.name,modifier=Modifier.size(if(compact)24.dp else 35.dp))
        Text("$remaining",color=if(remaining>0)Color.White else Color.Gray,fontSize=if(compact)10.sp else 12.sp,fontWeight=FontWeight.Bold)
    }
}
