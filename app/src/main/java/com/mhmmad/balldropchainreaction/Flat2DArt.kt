package com.mhmmad.balldropchainreaction

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Shader
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Flat, original 2D rendering for the actual playable scene. No bitmap, simulated
 * 3D perspective, or external illustrations. Uses one scale shared with Box2D.
 */
internal object Flat2DArt {
    private val ink = Color.rgb(35, 55, 82)
    private val cream = Color.rgb(255, 249, 230)
    private val gold = Color.rgb(255, 202, 67)
    private val coral = Color.rgb(255, 113, 99)
    private val ocean = Color.rgb(42, 161, 187)
    private val wood = Color.rgb(207, 133, 74)
    private fun paint(c: Int, alpha: Int = 255, style: Paint.Style = Paint.Style.FILL,
                      width: Float = 2f): Paint =
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = c
            this.alpha = alpha.coerceIn(0, 255)
            this.style = style
            strokeWidth = width
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
        }

    fun backdrop(c: Canvas, w: Float, h: Float, world: Int, tick: Int) {
        val skies = arrayOf(
            intArrayOf(0xFFB2E6EE.toInt(), 0xFFE8F8E7.toInt()),
            intArrayOf(0xFFFED4A8.toInt(), 0xFFFFF0D8.toInt()),
            intArrayOf(0xFFC5D7FF.toInt(), 0xFFE8F1FF.toInt()),
            intArrayOf(0xFFC8F5E0.toInt(), 0xFFF0FBEA.toInt()),
            intArrayOf(0xFFE2D4F4.toInt(), 0xFFFFEBE8.toInt())
        )
        val colors = skies[world.coerceIn(0, 4)]
        val sky = paint(colors[0]).apply {
            shader = LinearGradient(0f, 0f, 0f, h, colors[0], colors[1], Shader.TileMode.CLAMP)
        }
        c.drawRect(0f, 0f, w, h, sky)
        val sunX = w * .83f
        val sunY = h * .26f
        c.drawCircle(sunX, sunY, h * .105f, paint(0xFFFFE8A2.toInt(), 225))
        c.drawCircle(sunX, sunY, h * .068f, paint(0xFFFFF8DC.toInt()))
        val cloud = paint(Color.WHITE, 125)
        for (i in 0..3) {
            val x = w * (.12f + i * .29f) + sin((tick * .004f + i).toDouble()).toFloat() * w * .012f
            val y = h * (.15f + (i % 2) * .13f)
            c.drawRoundRect(RectF(x - w*.057f, y - h*.022f, x + w*.063f, y + h*.024f), h*.025f, h*.025f, cloud)
            c.drawCircle(x - w*.020f, y - h*.02f, h*.033f, cloud)
            c.drawCircle(x + w*.014f, y - h*.032f, h*.044f, cloud)
        }
        // Parallax-like layers are still geometrically flat 2D shapes.
        hill(c, w, h, .72f, 0xFFB2D8BA.toInt(), .075f)
        hill(c, w, h, .82f, 0xFF78BCAB.toInt(), .12f)
        hill(c, w, h, .93f, 0xFF3C8D91.toInt(), .075f)
        val floor = paint(0xFF286D77.toInt())
        c.drawRect(0f, h*.94f, w, h, floor)
        val trim = paint(0xFFFFD482.toInt(), 210)
        c.drawRect(0f, h*.937f, w, h*.946f, trim)
        // Thin line-work reinforces that this is a side-view 2D world.
        val strokes = paint(0xFFEFFEF3.toInt(), 35, Paint.Style.STROKE, 1.5f)
        for (i in 0..11) {
            val x = (i + .5f) * w / 12f
            c.drawLine(x, h*.955f, x + w*.015f, h*.982f, strokes)
        }
    }

    private fun hill(c: Canvas, w: Float, h: Float, height: Float, color: Int, swell: Float) {
        val path = Path()
        path.moveTo(0f, h)
        path.lineTo(0f, h*height)
        for(i in 0..30) {
            val x = i * w / 30f
            val y = h*(height + swell * sin((i / 30f * PI * 3.8).toFloat().toDouble()).toFloat())
            path.lineTo(x,y)
        }
        path.lineTo(w,h)
        path.close()
        c.drawPath(path, paint(color))
    }

    fun decoration(c: Canvas, x: Float, y: Float, r: Float, angle: Float) {
        actor(c,"gear",x,y,r*2,r*2,angle,130)
    }

    fun goal(c: Canvas, x: Float, y: Float, s: Float, pulse: Float) {
        c.drawCircle(x,y,s*(.56f+pulse*.05f),paint(gold,75))
        c.drawCircle(x,y,s*.43f,paint(cream))
        c.drawCircle(x,y,s*.43f,paint(ink,255,Paint.Style.STROKE,s*.065f))
        c.drawCircle(x,y,s*.31f,paint(gold))
        c.drawCircle(x,y,s*.21f,paint(Color.WHITE,210))
        val star=Path()
        for(i in 0..9) {
            val a = -PI/2 + i*PI/5
            val rr=if(i%2==0)s*.15f else s*.068f
            val px=x+(cos(a)*rr).toFloat()
            val py=y+(sin(a)*rr).toFloat()
            if(i==0)star.moveTo(px,py) else star.lineTo(px,py)
        }
        star.close()
        c.drawPath(star,paint(ink))
    }

    fun actor(c: Canvas, image: String, x: Float, y: Float, width: Float, height: Float,
              rotation: Float, alpha: Int = 255) {
        if(width<=0f || height<=0f)return
        c.save()
        c.translate(x,y)
        c.rotate(rotation)
        val w=width/2f
        val h=height/2f
        when {
            image.startsWith("ball_") -> {
                val shade=when(image) {
                    "ball_red" -> coral
                    "ball_yellow" -> gold
                    "ball_green" -> 0xFF83D88D.toInt()
                    "ball_purple" -> 0xFFB78ADF.toInt()
                    else -> 0xFF72CAF1.toInt()
                }
                val rad=minOf(w,h)*.87f
                c.drawCircle(0f,0f,rad,paint(ink,alpha))
                c.drawCircle(0f,0f,rad*.85f,paint(shade,alpha))
                c.drawCircle(-rad*.30f,-rad*.30f,rad*.19f,paint(Color.WHITE,(alpha*.86f).toInt()))
            }
            image=="plank"||image=="plank_long"||image=="plank_short"||image=="ramp" -> {
                val r=h*.42f
                c.drawRoundRect(RectF(-w,-h,w,h),r,r,paint(ink,alpha))
                c.drawRoundRect(RectF(-w+h*.19f,-h+h*.20f,w-h*.19f,h-h*.20f),r,r,paint(wood,alpha))
                val line=paint(0xFFFFE2A0.toInt(),(alpha*.78f).toInt(),Paint.Style.STROKE,h*.17f)
                c.drawLine(-w+h*.6f,-h*.25f,w-h*.6f,-h*.25f,line)
                val dot=paint(ink,(alpha*.70f).toInt())
                c.drawCircle(-w+h*.6f,0f,h*.16f,dot)
                c.drawCircle(w-h*.6f,0f,h*.16f,dot)
            }
            image=="domino" -> {
                c.drawRoundRect(RectF(-w,-h,w,h),w*.28f,w*.28f,paint(ink,alpha))
                c.drawRoundRect(RectF(-w*.73f,-h*.90f,w*.73f,h*.90f),w*.24f,w*.24f,paint(cream,alpha))
                val dots=paint(coral,alpha)
                c.drawCircle(0f,-h*.48f,w*.22f,dots)
                c.drawCircle(-w*.28f,h*.35f,w*.17f,dots)
                c.drawCircle(w*.28f,h*.35f,w*.17f,dots)
            }
            image=="spring" -> {
                c.drawRoundRect(RectF(-w,-h,w,h),w*.20f,w*.20f,paint(ink,alpha))
                c.drawRoundRect(RectF(-w*.84f,-h*.83f,w*.84f,h*.83f),w*.16f,w*.16f,paint(0xFF5CD9CD.toInt(),alpha))
                val coil=paint(cream,alpha,Paint.Style.STROKE,maxOf(2f,w*.11f))
                val path=Path()
                path.moveTo(-w*.63f,h*.56f)
                for (i in 0..6) {
                    val xx=-w*.63f + i*w*.21f
                    val yy=if(i%2==0)-h*.49f else h*.52f
                    path.lineTo(xx,yy)
                }
                path.lineTo(w*.65f,-h*.42f)
                c.drawPath(path,coil)
            }
            image=="gear" -> {
                val rr=minOf(w,h)*.71f
                val teeth=paint(ink,(alpha*.8f).toInt())
                for(i in 0..9) {
                    c.save()
                    c.rotate(i*40f)
                    c.drawRoundRect(RectF(-rr*.22f,-rr*1.29f,rr*.22f,-rr*.63f),rr*.12f,rr*.12f,teeth)
                    c.restore()
                }
                c.drawCircle(0f,0f,rr,paint(ink,alpha))
                c.drawCircle(0f,0f,rr*.77f,paint(0xFF79C6B4.toInt(),alpha))
                c.drawCircle(0f,0f,rr*.27f,paint(ink,alpha))
                c.drawCircle(0f,0f,rr*.15f,paint(cream,alpha))
            }
            image=="tube" || image=="pipe_elbow" -> {
                c.drawRoundRect(RectF(-w,-h,w,h),w*.2f,w*.2f,paint(ink,alpha))
                c.drawRoundRect(RectF(-w*.65f,-h*.88f,w*.65f,h*.88f),w*.12f,w*.12f,paint(ocean,alpha))
                c.drawRect(-w*.22f,-h*.8f,w*.09f,h*.8f,paint(cream,(alpha*.65f).toInt()))
            }
            else -> {
                c.drawRoundRect(RectF(-w,-h,w,h),h*.2f,h*.2f,paint(ink,alpha))
                c.drawRoundRect(RectF(-w*.83f,-h*.77f,w*.83f,h*.77f),h*.2f,h*.2f,paint(gold,alpha))
            }
        }
        c.restore()
    }
}
