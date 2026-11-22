package com.mhmmad.balldropchainreaction

import android.app.Activity
import android.os.Bundle
import android.content.Context
import android.graphics.*
import android.view.MotionEvent
import android.view.View
import org.jbox2d.common.Vec2
import org.jbox2d.collision.shapes.CircleShape
import org.jbox2d.collision.shapes.PolygonShape
import org.jbox2d.dynamics.*
import org.jbox2d.dynamics.joints.RevoluteJointDef
import kotlin.math.*

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.decorView.systemUiVisibility = 5894 or 1024 or 512
        setContentView(Board(this))
    }
}
private data class ButtonArea(val rect:RectF,val click:()->Unit)
private data class Tool(val kind:Int,val x:Float,val y:Float,val angle:Float)
private data class Part(val kind:Int,val body:Body,val w:Float,val h:Float)

private class Board(ctx:Context):View(ctx) {
    private val store=ctx.getSharedPreferences("progress",Context.MODE_PRIVATE)
    private val brush=Paint(Paint.ANTI_ALIAS_FLAG)
    private val buttons=mutableListOf<ButtonArea>()
    private val placed=mutableListOf<Tool>()
    private val parts=mutableListOf<Part>()
    private val springs=mutableListOf<Body>()
    private var lang=store.getInt("lang",0)
    private var unlocked=store.getInt("level",1).coerceIn(1,20)
    private var page=0
    private var level=1
    private var selected=0
    private var rotation=0f
    private var previewX=0f
    private var previewY=0f
    private var downX=0f
    private var downY=0f
    private var world=World(Vec2(0f,-10f))
    private var ball:Body?=null
    private var running=false
    private var showingHint=false
    private var elapsed=0f
    private var acc=0f
    private var lastNs=System.nanoTime()
    private val goalX=14.8f
    private var goalY=1.5f

    private fun tr(en:String,ar:String,pt:String)=when(lang){1->ar;2->pt;else->en}
    private fun text(c:Canvas,s:String,x:Float,y:Float,size:Float,color:Int=Color.WHITE) {
        brush.shader=null
        brush.color=color
        brush.textAlign=Paint.Align.CENTER
        brush.typeface=Typeface.create("sans-serif-black",Typeface.BOLD)
        brush.textSize=size
        brush.style=Paint.Style.FILL
        c.drawText(s,x,y,brush)
    }
    private fun bg(c:Canvas) {
        brush.shader=LinearGradient(0f,0f,1600f,900f,intArrayOf(0xFF061B46.toInt(),0xFF06498F.toInt(),0xFF061F55.toInt()),null,Shader.TileMode.CLAMP)
        c.drawRect(0f,0f,1600f,900f,brush)
        brush.shader=null
        brush.color=0x2D8ABBFF
        brush.strokeWidth=1f
        for(i in 0..25)c.drawLine(i*64f,0f,i*64f,900f,brush)
        for(i in 0..14)c.drawLine(0f,i*64f,1600f,i*64f,brush)
        brush.shader=LinearGradient(0f,815f,0f,900f,0xFFF4B65D.toInt(),0xFF87451D.toInt(),Shader.TileMode.CLAMP)
        c.drawRect(0f,818f,1600f,900f,brush)
        brush.shader=null
    }
    private fun wood(c:Canvas,x:Float,y:Float,w:Float,h:Float,angle:Float=0f) {
        c.save();c.rotate(angle,x,y)
        brush.shader=LinearGradient(0f,y-h/2,0f,y+h/2,0xFFFBC873.toInt(),0xFF96501B.toInt(),Shader.TileMode.CLAMP)
        c.drawRoundRect(x-w/2,y-h/2,x+w/2,y+h/2,11f,11f,brush)
        brush.shader=null
        brush.color=0xFFE5A456.toInt()
        for(k in 1..3)c.drawLine(x-w/2+12f,y-h/2+k*h/4,x+w/2-12f,y-h/2+k*h/4,brush)
        brush.color=0xFF334057.toInt()
        c.drawCircle(x-w/2+17f,y,7f,brush)
        c.drawCircle(x+w/2-17f,y,7f,brush)
        c.restore()
    }
    private fun ballArt(c:Canvas,x:Float,y:Float,r:Float,col:Int) {
        brush.shader=RadialGradient(x-r/3,y-r/3,r*1.7f,intArrayOf(Color.WHITE,col,col,0xFF061B40.toInt()),floatArrayOf(0f,0.34f,0.74f,1f),Shader.TileMode.CLAMP)
        c.drawCircle(x,y,r,brush);brush.shader=null
        brush.color=0xEFFFFFFF.toInt()
        c.drawOval(x-r*.5f,y-r*.65f,x+r*.1f,y-r*.37f,brush)
    }
    private fun domino(c:Canvas,x:Float,y:Float,w:Float,h:Float,rot:Float=0f) {
        c.save();c.rotate(rot,x,y)
        brush.color=0xFFFFE7BC.toInt()
        c.drawRoundRect(x-w/2,y-h/2,x+w/2,y+h/2,7f,7f,brush)
        brush.color=0xFF303B4C.toInt()
        c.drawCircle(x,y-h/4,4f,brush);c.drawCircle(x,y+h/4,4f,brush)
        c.drawRect(x-w/2+4,y-1f,x+w/2-4,y+1f,brush)
        c.restore()
    }
    private fun springArt(c:Canvas,x:Float,y:Float,w:Float,h:Float) {
        brush.color=0xFF687890.toInt()
        c.drawRoundRect(x-w/2,y+h/3,x+w/2,y+h/2,6f,6f,brush)
        brush.style=Paint.Style.STROKE;brush.strokeWidth=7f;brush.color=Color.WHITE
        val path=Path();path.moveTo(x-w*.36f,y+h/3)
        for(i in 0..6)path.lineTo(x+(if(i%2==0)w*.33f else -w*.33f),y+h/3-i*h/10f)
        c.drawPath(path,brush);brush.style=Paint.Style.FILL
        brush.color=0xFFEA304E.toInt()
        c.drawRoundRect(x-w/2,y-h/2,x+w/2,y-h/3,7f,7f,brush)
    }
    private fun target(c:Canvas) {
        val x=goalX*100f;val y=900f-goalY*100f
        brush.color=0xFFFFD35C.toInt()
        c.drawRoundRect(x-50,y-65,x+50,y+65,18f,18f,brush)
        for(i in 0..3) {
            brush.color=intArrayOf(0xFFED3651.toInt(),Color.WHITE,0xFFED3651.toInt(),Color.WHITE)[i]
            c.drawCircle(x,y,43f-i*10f,brush)
        }
    }
    private fun button(c:Canvas,s:String,x:Float,y:Float,w:Float,h:Float,col:Int,action:()->Unit){
        brush.color=0xAA061229.toInt()
        c.drawRoundRect(x+4,y+8,x+w+4,y+h+8,21f,21f,brush)
        brush.shader=LinearGradient(0f,y,0f,y+h,intArrayOf(0xFFFFD16C.toInt(),col,col),null,Shader.TileMode.CLAMP)
        c.drawRoundRect(x,y,x+w,y+h,18f,18f,brush);brush.shader=null
        brush.style=Paint.Style.STROKE;brush.strokeWidth=4f;brush.color=0xFFFFDC8A.toInt()
        c.drawRoundRect(x,y,x+w,y+h,18f,18f,brush);brush.style=Paint.Style.FILL
        text(c,s,x+w/2,y+h/2+13f,min(36f,h*.44f))
        buttons.add(ButtonArea(RectF(x,y,x+w,y+h),action))
    }
    private fun decor(c:Canvas){
        wood(c,240f,380f,410f,53f,15f)
        wood(c,1380f,610f,365f,48f,-22f)
        springArt(c,185f,740f,135f,160f)
        ballArt(c,232f,250f,82f,0xFFED243D.toInt())
        ballArt(c,1290f,266f,61f,0xFF24A3FC.toInt())
        for(i in 0..4)domino(c,1130f+i*64f,715f,41f,105f,8f*i)
    }

    private fun rectangle(x:Float,y:Float,w:Float,h:Float,angle:Float,dynamic:Boolean,kind:Int):Body {
        val def=BodyDef()
        def.type=if(dynamic)BodyType.DYNAMIC else BodyType.STATIC
        def.position.set(x,y)
        def.angle=Math.toRadians(angle.toDouble()).toFloat()
        val body=world.createBody(def)
        val shape=PolygonShape();shape.setAsBox(w/2,h/2)
        val fix=FixtureDef();fix.shape=shape;fix.density=1f;fix.friction=.52f;fix.restitution=.16f
        body.createFixture(fix)
        parts.add(Part(kind,body,w,h))
        if(kind==3)springs.add(body)
        return body
    }
    private fun rocker(x:Float,y:Float,rot:Float){
        val plank=rectangle(x,y,2.1f,.18f,rot,true,4)
        val support=world.createBody(BodyDef().apply{position.set(x,y)})
        val joint=RevoluteJointDef()
        joint.initialize(support,plank,Vec2(x,y))
        joint.enableLimit=true;joint.lowerAngle=-.68f;joint.upperAngle=.68f
        world.createJoint(joint)
    }
    private fun setStage(launch:Boolean=false){
        world=World(Vec2(0f,-10f))
        parts.clear();springs.clear();ball=null;running=launch;elapsed=0f;acc=0f
        val i=level-1;val stage=i%4;val region=i/4
        goalY=1.45f+stage%2*.55f
        rectangle(8f,.26f,16f,.52f,0f,false,0)
        rectangle(.18f,4.5f,.36f,9f,0f,false,0)
        rectangle(15.82f,4.5f,.36f,9f,0f,false,0)
        rectangle(2.1f,7.05f,3.0f,.24f,-14f,false,0)
        rectangle(5.1f+stage*.18f,5.75f-stage*.18f,2.5f,.22f,-13f,false,0)
        rectangle(9.0f-stage*.12f,3.95f+stage*.18f,2.35f,.22f,-11f,false,0)
        rectangle(12.1f+stage*.12f,2.44f,2.5f,.22f,-10f,false,0)
        if(region>0)for(j in 0 until region+stage%2)rectangle(5.5f+j*.43f,6.4f,.22f,.78f,0f,true,2)
        if(region>=2)rectangle(10.2f,1.0f,.8f,.45f,0f,false,3)
        if(region>=3)rocker(10.7f,4.25f,0f)
        if(region>=4)rectangle(7.5f,3.1f,1.5f,.20f,30f,false,0)
        for(p in placed)when(p.kind){
            0->rectangle(p.x,p.y,2f,.2f,p.angle,false,0)
            1->rectangle(p.x,p.y,.22f,.85f,p.angle,true,2)
            2->rectangle(p.x,p.y,.8f,.42f,p.angle,false,3)
            3->rocker(p.x,p.y,p.angle)
        }
        if(launch){
            val bd=BodyDef();bd.type=BodyType.DYNAMIC;bd.position.set(1.05f,7.88f);bd.bullet=true
            val b=world.createBody(bd);val shape=CircleShape();shape.m_radius=.29f
            val f=FixtureDef();f.shape=shape;f.density=1.1f;f.friction=.4f;f.restitution=.38f
            b.createFixture(f);ball=b
        }
        lastNs=System.nanoTime()
    }
    private fun enter(n:Int){level=n.coerceIn(1,20);placed.clear();selected=0;rotation=0f;showingHint=false;setStage();page=2}
    private fun advance(){
        val now=System.nanoTime()
        val dt=((now-lastNs)/1e9f).coerceIn(0f,.05f);lastNs=now
        if(page!=2||!running)return
        acc+=dt
        while(acc>=1f/60f){
            world.step(1f/60f,8,3);acc-=1f/60f;elapsed+=1f/60f
            val b=ball ?: continue
            for(s in springs)if(abs(s.position.x-b.position.x)<.55f && b.position.y>s.position.y && b.position.y-s.position.y<.68f && b.linearVelocity.y<1f){
                b.applyLinearImpulse(Vec2(0f,1.05f),b.worldCenter)
            }
            if(abs(b.position.x-goalX)<.50f && abs(b.position.y-goalY)<.65f){
                running=false;page=3;unlocked=max(unlocked,(level+1).coerceAtMost(20))
                store.edit().putInt("level",unlocked).apply()
            }
            if(elapsed>32f||b.position.y<-.8f)running=false
        }
    }
    override fun onDraw(c:Canvas) {
        super.onDraw(c)
        advance()
        val scale=min(width/1600f,height/900f)
        c.save();c.translate((width-1600*scale)/2,(height-900*scale)/2);c.scale(scale,scale)
        buttons.clear();bg(c)
        when(page){
            0->{
                decor(c)
                text(c,tr("BALL DROP","سقوط الكرات","QUEDA DE BOLAS"),800f,176f,92f,0xFFFFD36A.toInt())
                text(c,tr("CHAIN REACTION","التفاعل المتسلسل","REAÇÃO EM CADEIA"),800f,235f,49f,0xFFAADFFF.toInt())
                button(c,tr("PLAY","العب","JOGAR"),660f,345f,280f,93f,0xFF0B853C.toInt()){enter(unlocked)}
                button(c,tr("LEVELS","المراحل","FASES"),660f,465f,280f,80f,0xFF0875B6.toInt()){page=1}
                button(c,tr("SETTINGS","الإعدادات","OPÇÕES"),660f,570f,280f,80f,0xFF8A43AC.toInt()){page=4}
                button(c,tr("ABOUT","حول اللعبة","SOBRE"),698f,678f,205f,66f,0xFF99582C.toInt()){page=5}
            }
            1->{
                text(c,tr("20 LEVELS","20 مرحلة","20 FASES"),800f,100f,66f,0xFFFFC45C.toInt())
                for(i in 0 until 20){
                    val n=i+1;val x=250f+(i%5)*230;val y=165f+(i/5)*148
                    button(c,if(n<=unlocked)n.toString() else "X",x,y,183f,108f,if(n<=unlocked)0xFF056DA4.toInt() else 0xFF3B455B.toInt()){
                        if(n<=unlocked)enter(n)
                    }
                }
                button(c,tr("BACK","رجوع","VOLTAR"),25f,25f,170f,62f,0xFF874E2A.toInt()){page=0}
            }
            2->{
                text(c,"LEVEL "+level,340f,75f,43f,0xFFFFD567.toInt())
                text(c,tr("Place tools, then RUN","ضع الأدوات ثم شغّل","Coloque peças e inicie"),815f,74f,29f,0xFFB6DEFF.toInt())
                for(p in parts){
                    val x=p.body.position.x*100;val y=900-p.body.position.y*100
                    val r=-Math.toDegrees(p.body.angle.toDouble()).toFloat()
                    when(p.kind){
                        0,4->wood(c,x,y,p.w*100,p.h*100,r)
                        2->domino(c,x,y,p.w*100,p.h*100,r)
                        3->springArt(c,x,y,p.w*100,p.h*100)
                    }
                }
                target(c)
                val b=ball
                if(b==null)ballArt(c,105f,112f,29f,0xFFEF2445.toInt())
                else ballArt(c,b.position.x*100,900-b.position.y*100,29f,0xFFEF2445.toInt())
                if(previewX>0&&previewY in 100f..740f&&b==null){
                    brush.color=0xB0C5F4FF.toInt();brush.style=Paint.Style.STROKE;brush.strokeWidth=4f
                    c.drawCircle(previewX,previewY,47f,brush);brush.style=Paint.Style.FILL
                }
                if(showingHint&&b==null){brush.color=0xAAFFD542.toInt();c.drawCircle(740f,370f,34f,brush)}
                button(c,tr("BACK","رجوع","VOLTAR"),15f,16f,157f,63f,0xFF8A4B2E.toInt()){running=false;page=1}
                button(c,tr("RESET","إعادة","REINICIAR"),1135f,18f,195f,62f,0xFFA8532A.toInt()){setStage()}
                button(c,if(running)tr("PAUSE","إيقاف","PAUSAR") else tr("RUN","تشغيل","INICIAR"),1360f,18f,214f,67f,0xFF139B43.toInt()){
                    if(ball==null)setStage(true)else running=!running
                }
                if(ball==null){
                    val labels=arrayOf(tr("RAMP","منحدر","RAMPA"),tr("DOMINO","دومينو","DOMINÓ"),tr("SPRING","نابض","MOLA"),tr("SEESAW","أرجوحة","GANGORRA"))
                    for(i in 0..3)button(c,labels[i],250f+i*220,768f,201f,64f,if(i==selected)0xFF038CB8.toInt() else 0xFF31547C.toInt()){selected=i}
                    button(c,tr("UNDO","تراجع","DESFAZER"),12f,768f,201f,64f,0xFFAE6434.toInt()){
                        if(placed.isNotEmpty()){placed.removeAt(placed.lastIndex);setStage()}
                    }
                    button(c,tr("ROTATE","تدوير","GIRAR"),1154f,769f,179f,63f,0xFF9646A0.toInt()){rotation=(rotation+30f)%360}
                    button(c,tr("HINT","تلميح","DICA"),1350f,769f,227f,63f,0xFF1673AC.toInt()){showingHint=!showingHint}
                    text(c,placed.size.toString()+" / "+(2+(level-1)/7).coerceAtMost(5),1018f,810f,33f,0xFFFFDD7B.toInt())
                } else if(!running)text(c,tr("PAUSED OR RETRY","متوقف أو حاول مجددًا","PAUSADO OU TENTE NOVAMENTE"),800f,170f,30f)
            }
            3->{
                decor(c)
                text(c,"★ ★ ★",800f,300f,92f,0xFFFFD852.toInt())
                text(c,tr("LEVEL COMPLETE","أحسنت!","FASE CONCLUÍDA"),800f,416f,81f)
                button(c,tr("NEXT","التالي","PRÓXIMA"),650f,505f,300f,86f,0xFF087D40.toInt()){enter((level+1).coerceAtMost(20))}
                button(c,tr("LEVELS","المراحل","FASES"),650f,620f,300f,78f,0xFF1368AF.toInt()){page=1}
            }
            4->{
                text(c,tr("LANGUAGE","اللغة","IDIOMA"),800f,220f,80f,0xFFFFD36B.toInt())
                button(c,"English",240f,325f,341f,92f,0xFF0F79B0.toInt()){lang=0;store.edit().putInt("lang",lang).apply()}
                button(c,"العربية",629f,325f,341f,92f,0xFF0F79B0.toInt()){lang=1;store.edit().putInt("lang",lang).apply()}
                button(c,"Português",1018f,325f,341f,92f,0xFF0F79B0.toInt()){lang=2;store.edit().putInt("lang",lang).apply()}
                button(c,tr("BACK","رجوع","VOLTAR"),640f,610f,320f,84f,0xFF975026.toInt()){page=0}
            }
            else->{
                text(c,tr("ABOUT","حول اللعبة","SOBRE"),800f,230f,75f,0xFFFFCA6C.toInt())
                text(c,"Ball Drop: Chain Reaction",800f,356f,59f)
                text(c,"Offline JBox2D physics prototype • no ads • no music",800f,430f,33f,0xFFB9E7FF.toInt())
                text(c,"Not a signed release; premium sprites not yet imported",800f,489f,31f,0xFFFFDC9D.toInt())
                button(c,tr("BACK","رجوع","VOLTAR"),640f,610f,320f,84f,0xFF975026.toInt()){page=0}
            }
        }
        c.restore()
        postInvalidateOnAnimation()
    }
    override fun onTouchEvent(e:MotionEvent):Boolean {
        val scale=min(width/1600f,height/900f)
        if(scale<=0)return true
        val x=(e.x-(width-1600*scale)/2)/scale
        val y=(e.y-(height-900*scale)/2)/scale
        when(e.actionMasked){
            MotionEvent.ACTION_DOWN->{downX=x;downY=y;return true}
            MotionEvent.ACTION_MOVE->{
                if(page==2&&ball==null&&downY>735&&y<740){previewX=x;previewY=y}
                return true
            }
            MotionEvent.ACTION_UP->{
                val moved=hypot(x-downX,y-downY)>54f
                if(page==2&&ball==null&&y in 110f..740f&&placed.size<(2+(level-1)/7).coerceAtMost(5)&&
                    (moved||buttons.none{it.rect.contains(x,y)})){
                    val gx=(x/25f).roundToInt()*0.25f
                    val gy=((900-y)/25f).roundToInt()*0.25f
                    placed.add(Tool(selected,gx.coerceIn(.9f,15.1f),gy.coerceIn(.7f,8.5f),rotation))
                    setStage()
                } else buttons.lastOrNull{it.rect.contains(x,y)}?.click?.invoke()
                previewX=0f;previewY=0f
                invalidate();return true
            }
        }
        return true
    }
}
