package com.example.immersivexiangqi.p20261105

import com.example.immersivexiangqi.p20261105.game.*
import kotlinx.coroutines.*
import org.junit.Assert.*
import org.junit.Test

class ChessControllerTest {
    private class MemoryStorage : GameStorage {
        val values = mutableMapOf<String,String>()
        override fun read(key:String)=values[key]
        override fun write(values:Map<String,String>) { this.values.putAll(values) }
    }
    private fun legalAnswer(position:Position)=SearchResult(Rules.legal(position).first(),0,1,1)

    @Test fun selectingCurrentModeOrDifficultyDoesNotDiscardTheGame() {
        val storage=MemoryStorage();val scope=CoroutineScope(SupervisorJob()+Dispatchers.Unconfined)
        try {
            val game=ChessController(storage,scope,search={p,_->legalAnswer(p)})
            assertTrue(game.tap(54));assertTrue(game.tap(45))
            val before=game.position
            game.chooseMode(GameMode.MATCH);game.chooseDifficulty(Difficulty.BEGINNER)
            assertEquals(before,game.position);assertEquals(2,game.moveCount)
            val restored=ChessController(storage,scope,search={p,_->legalAnswer(p)})
            assertEquals(before,restored.position);assertEquals(2,restored.moveCount)
            game.close();restored.close()
        } finally {scope.cancel()}
    }

    @Test fun aiFailureCanBeRetriedWithoutLosingThePlayersMove() {
        val storage=MemoryStorage();val scope=CoroutineScope(SupervisorJob()+Dispatchers.Unconfined);var attempts=0
        try {
            val game=ChessController(storage,scope,search={p,_->
                attempts++;if(attempts==1) error("simulated search failure");legalAnswer(p)
            })
            assertTrue(game.tap(54));assertTrue(game.tap(45))
            assertEquals(Side.BLACK,game.position.turn);assertFalse(game.busy);assertTrue(game.canRetryOpponent)
            assertEquals(1,game.moveCount)
            game.retryOpponent()
            assertEquals(Side.RED,game.position.turn);assertEquals(2,game.moveCount)
            assertFalse(game.aiFailed);assertFalse(game.busy)
            game.close()
        } finally {scope.cancel()}
    }

    @Test fun resetPreventsASupersededSearchFromChangingTheBoard()= runBlocking {
        val storage=MemoryStorage();val scope=CoroutineScope(SupervisorJob()+Dispatchers.Unconfined)
        val gate=CompletableDeferred<Unit>()
        try {
            val game=ChessController(storage,scope,search={p,_->
                withContext(NonCancellable) {gate.await()}
                legalAnswer(p)
            })
            game.tap(54);game.tap(45);assertTrue(game.busy)
            game.reset();assertEquals(Position.initial(),game.position);assertFalse(game.busy)
            gate.complete(Unit);yield()
            assertEquals(Position.initial(),game.position);assertEquals(0,game.moveCount)
            assertFalse(game.busy)
            game.close()
        } finally {gate.complete(Unit);scope.cancel()}
    }

    @Test fun trainingHintsSolveTheLessonAndProgressIsCountedOnce() {
        val storage=MemoryStorage();val scope=CoroutineScope(SupervisorJob()+Dispatchers.Unconfined);var aiCalls=0
        try {
            val game=ChessController(storage,scope,search={p,_->aiCalls++;legalAnswer(p)})
            game.chooseMode(GameMode.TRAINING)
            game.hint();val hint=game.hintMove!!
            assertEquals(hint.from,game.selected)
            assertTrue(game.tap(hint.to));assertEquals(Side.RED,game.result?.winner)
            assertEquals(1,game.completed);assertEquals(1,game.completedHere);assertEquals(1,game.bestMoves)
            game.chooseDifficulty(Difficulty.ADVANCED)
            assertEquals(Side.RED,game.result?.winner)
            game.reset();val solution=game.level!!.solution!!
            game.tap(solution.from);game.tap(solution.to)
            assertEquals(1,game.completed);assertEquals(1,game.completedHere)
            assertEquals(0,aiCalls)
            val restored=ChessController(storage,scope,search={p,_->legalAnswer(p)})
            assertEquals(1,restored.completed);assertEquals(Side.RED,restored.result?.winner)
            game.close();restored.close()
        } finally {scope.cancel()}
    }

    @Test fun trainingFailureNeverStartsAnOpponentSearch() {
        val scope=CoroutineScope(SupervisorJob()+Dispatchers.Unconfined);var calls=0
        try {
            val game=ChessController(MemoryStorage(),scope,search={p,_->calls++;legalAnswer(p)})
            game.chooseMode(GameMode.TRAINING)
            val move=Rules.legal(game.position).first {Rules.legal(game.position.after(it)).isNotEmpty()}
            game.tap(move.from);game.tap(move.to)
            assertEquals(Side.BLACK,game.result?.winner);assertEquals(0,calls);assertEquals(0,game.completed)
            game.undo();assertNull(game.result);assertEquals(0,game.moveCount)
            game.close();assertFalse(game.tap(54))
        } finally {scope.cancel()}
    }

    @Test fun corruptSavedStateStartsANewPlayableSession() {
        val storage=MemoryStorage();storage.values["session_v1"]="corrupt"
        val scope=CoroutineScope(SupervisorJob()+Dispatchers.Unconfined)
        try {
            val game=ChessController(storage,scope,search={p,_->legalAnswer(p)})
            assertEquals(Position.initial(),game.position);assertTrue(game.canHint)
            assertNotNull(GameSaveCodec.decode(storage.read("session_v1")))
            game.close()
        } finally {scope.cancel()}
    }
}
