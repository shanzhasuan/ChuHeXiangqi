package com.example.immersivexiangqi.p20261105

import com.example.immersivexiangqi.p20261105.game.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import org.junit.Assert.*
import org.junit.Test

class GameFeedbackTest {
    private class Storage : GameStorage {
        val values=mutableMapOf<String,String>()
        override fun read(key:String)=values[key]
        override fun write(values:Map<String,String>){this.values.putAll(values)}
    }
    private fun answer(p:Position)=SearchResult(Rules.legal(p).first(),0,1,1)

    @Test(timeout=3000) fun fastOpponentStillProducesBothMovesInOrder()=runBlocking {
        val scope=CoroutineScope(SupervisorJob()+Dispatchers.Unconfined)
        try {
            val game=ChessController(Storage(),scope,search={p,_->answer(p)})
            game.tap(54);game.tap(45)
            val events=game.feedbackEvents.take(2).toList()
            assertEquals(listOf(Side.RED,Side.BLACK),events.map {(it as GameFeedbackEvent.MovePlayed).recent.piece.side})
            assertEquals(listOf(1,2),events.map {(it as GameFeedbackEvent.MovePlayed).recent.ply})
            assertEquals(Move(54,45),game.recentRedMove!!.move)
            assertNotNull(game.recentBlackMove)
            game.close()
        } finally {scope.cancel()}
    }

    @Test(timeout=3000) fun selectionInvalidMoveUndoAndRestoreDoNotReplayMoveSounds()=runBlocking {
        val storage=Storage();val scope=CoroutineScope(SupervisorJob()+Dispatchers.Unconfined)
        try {
            val game=ChessController(storage,scope,search={p,_->answer(p)})
            game.tap(54);assertFalse(game.tap(55));game.tap(45)
            val events=game.feedbackEvents.take(2).toList()
            assertEquals(2,events.size)
            val restored=ChessController(storage,scope,search={p,_->answer(p)})
            assertNotNull(restored.recentRedMove);assertNotNull(restored.recentBlackMove)
            restored.close()
            assertTrue(restored.feedbackEvents.toList().isEmpty())
            game.undo();assertNull(game.recentRedMove);assertNull(game.recentBlackMove)
            game.close();assertTrue(game.feedbackEvents.toList().isEmpty())
        } finally {scope.cancel()}
    }

    @Test(timeout=3000) fun mateEmitsAnEndEventOnceAndRestoredResultRemainsVisible()=runBlocking {
        val storage=Storage();val scope=CoroutineScope(SupervisorJob()+Dispatchers.Unconfined)
        try {
            val game=ChessController(storage,scope,search={p,_->answer(p)})
            game.chooseMode(GameMode.TRAINING)
            val move=game.level!!.solution!!;game.tap(move.from);game.tap(move.to)
            assertFalse(game.tap(move.to))
            val events=game.feedbackEvents.take(2).toList()
            assertTrue((events[0] as GameFeedbackEvent.MovePlayed).recent.gaveCheck)
            assertEquals(Side.RED,(events[1] as GameFeedbackEvent.GameEnded).result.winner)
            assertEquals(1L,game.outcomeRevision)
            game.close();assertTrue(game.feedbackEvents.toList().isEmpty())
            val restored=ChessController(storage,scope,search={p,_->answer(p)})
            assertEquals(Side.RED,restored.result!!.winner)
            restored.close();assertTrue(restored.feedbackEvents.toList().isEmpty())
        } finally {scope.cancel()}
    }

    @Test(timeout=3000) fun cancelledOpponentDoesNotEmitAStaleSoundEvent()=runBlocking {
        val scope=CoroutineScope(SupervisorJob()+Dispatchers.Unconfined);val gate=CompletableDeferred<Unit>()
        try {
            val game=ChessController(Storage(),scope,search={p,_->
                withContext(NonCancellable){gate.await()};answer(p)
            })
            game.tap(54);game.tap(45)
            val first=game.feedbackEvents.take(1).toList().single()
            game.reset();gate.complete(Unit);yield()
            assertNotEquals(first.epoch,game.feedbackEpoch)
            game.close();assertTrue(game.feedbackEvents.toList().isEmpty())
        } finally {gate.complete(Unit);scope.cancel()}
    }
}
