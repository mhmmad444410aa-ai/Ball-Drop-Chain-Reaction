package com.mhmmad.balldropchainreaction

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool

/** Short original WAV effects; there is deliberately no music player. */
class GameAudio(context: Context) {
    private val soundPool=SoundPool.Builder().setMaxStreams(4).setAudioAttributes(
        AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_GAME).setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build()
    ).build()
    private val sounds=hashMapOf<String,Int>()
    init {
        for (name in listOf("tap","bounce","domino","win")) {
            val afd=context.assets.openFd("sfx/$name.wav")
            sounds[name]=soundPool.load(afd,1)
            afd.close()
        }
    }
    fun play(name: String, enabled: Boolean) { if(enabled) sounds[name]?.let { soundPool.play(it,.45f,.45f,0,0,1f) } }
    fun release()=soundPool.release()
}
