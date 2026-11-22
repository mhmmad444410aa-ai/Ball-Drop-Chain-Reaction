package com.mhmmad.balldropchainreaction

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Paint
import android.graphics.RectF
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.graphics.painter.BitmapPainter

class ArtStore(context: Context) {
    private val bitmaps = hashMapOf<String, Bitmap>()
    private val assets = context.assets
    private val spritePaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
    fun get(key: String): Bitmap? {
        if (!bitmaps.containsKey(key)) {
            val path = "art/$key.webp"
            bitmaps[key] = assets.open(path).use { BitmapFactory.decodeStream(it) }
        }
        return bitmaps[key]
    }
    fun paint(key: String, canvas: android.graphics.Canvas, rect: RectF, angle: Float=0f, alpha: Int=255) {
        val image=get(key) ?: return
        val save=canvas.save()
        if (angle!=0f) canvas.rotate(angle,rect.centerX(),rect.centerY())
        spritePaint.alpha=alpha
        canvas.drawBitmap(image,null,rect,spritePaint)
        spritePaint.alpha=255
        canvas.restoreToCount(save)
    }
}
@Composable
fun ArtBackground(art: ArtStore, name: String) {
    val img=art.get(name)
    if(img!=null) Image(painter=BitmapPainter(img.asImageBitmap()),contentDescription=null,
        contentScale=ContentScale.Crop,modifier=Modifier.fillMaxSize())
}
