package com.example.immersivexiangqi.p20261105.content

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.util.Log
import com.example.immersivexiangqi.p20261105.R
import com.example.immersivexiangqi.p20261105.game.*
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeoutOrNull

internal class ChessSoundPlayer(context: Context) : AutoCloseable {
    private enum class Cue(val resource: Int, val durationMs: Long) {
        RED(R.raw.xiangqi_red_move,160), BLACK(R.raw.xiangqi_black_move,250),
        CAPTURE(R.raw.xiangqi_capture,220), CHECK(R.raw.xiangqi_check,340),
        WIN(R.raw.xiangqi_win,720), LOSE(R.raw.xiangqi_lose,720), DRAW(R.raw.xiangqi_draw,480)
    }
    private val pool = SoundPool.Builder().setMaxStreams(4).setAudioAttributes(
        AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build()
    ).build()
    private val ready = CompletableDeferred<Unit>()
    private val ids = mutableMapOf<Cue,Int>()
    private val failed = mutableSetOf<Int>()
    private var loading = Cue.entries.size
    private var closed = false
    init {
        pool.setOnLoadCompleteListener { _, sample, status ->
            if(status!=0) failed.add(sample)
            loading--
            if(loading==0) {
                ready.complete(Unit)
                Log.i("Xiangqi","audio_ready samples=${Cue.entries.size} failed=${failed.size}")
            }
        }
        Cue.entries.forEach { ids[it]=pool.load(context,it.resource,1) }
    }
    suspend fun play(event: GameFeedbackEvent, isCurrent: () -> Boolean) {
        if(withTimeoutOrNull(3000){ready.await();true}!=true || closed) return
        val cues = when(event) {
            is GameFeedbackEvent.MovePlayed -> buildList {
                add(if(event.recent.piece.side==Side.RED) Cue.RED else Cue.BLACK)
                if(event.recent.captured!=null) add(Cue.CAPTURE)
                if(event.recent.gaveCheck) add(Cue.CHECK)
            }
            is GameFeedbackEvent.GameEnded -> listOf(when(event.result.winner) {
                Side.RED->Cue.WIN; Side.BLACK->Cue.LOSE; null->Cue.DRAW
            })
        }
        for(cue in cues) {
            if(closed || !isCurrent()) return
            val sample=ids.getValue(cue)
            if(sample in failed) continue
            val stream=pool.play(sample,.88f,.88f,1,0,1f)
            Log.i("Xiangqi","audio_play cue=$cue stream=$stream")
            delay(cue.durationMs+20)
        }
    }
    override fun close() {
        if(closed) return
        closed=true
        pool.release()
    }
}
