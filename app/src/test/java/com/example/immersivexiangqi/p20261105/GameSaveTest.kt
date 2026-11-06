package com.example.immersivexiangqi.p20261105

import com.example.immersivexiangqi.p20261105.game.*
import org.junit.Assert.*
import org.junit.Test

class GameSaveTest {
    @Test fun snapshotRoundTripRestoresPositionAndMoveHistory() {
        val game = GameSession()
        repeat(4) { assertTrue(game.play(Rules.legal(game.position).first())) }
        val snapshot = GameSnapshot(GameMode.MATCH, Difficulty.ADVANCED, 1, null, game.initial, game.history.map { it.move })
        val restored = GameSaveCodec.decode(GameSaveCodec.encode(snapshot))!!.restoreSession()
        assertEquals(game.position, restored.position)
        assertEquals(game.history, restored.history)
        assertEquals(game.result, restored.result)
        assertTrue(restored.undoTurn())
        assertEquals(2, restored.history.size)
    }

    @Test fun finishedRepetitionSurvivesRestore() {
        val game = GameSession()
        val cycle = listOf(Move(82,63),Move(1,18),Move(63,82),Move(18,1))
        repeat(2) { cycle.forEach { game.play(it) } }
        val saved = GameSnapshot(GameMode.MATCH,Difficulty.BEGINNER,1,null,game.initial,game.history.map { it.move })
        val restored = GameSaveCodec.decode(GameSaveCodec.encode(saved))!!.restoreSession()
        assertEquals(game.result, restored.result)
        assertEquals(game.position, restored.position)
    }

    @Test fun failedTrainingAttemptSurvivesRestore() {
        val lesson = LevelGenerator.training(2)
        val game = GameSession(lesson.position,oneMoveMate=true)
        val move = Rules.legal(game.position).first { Rules.legal(game.position.after(it)).isNotEmpty() }
        game.play(move)
        val saved = GameSnapshot(GameMode.TRAINING,Difficulty.ADVANCED,2,2,game.initial,listOf(move))
        val restored = GameSaveCodec.decode(GameSaveCodec.encode(saved))!!.restoreSession()
        assertEquals(Side.BLACK, restored.result?.winner)
        assertTrue(restored.undoTurn())
        assertEquals(game.initial, restored.position)
    }

    @Test fun unknownVersionInvalidMovesAndUnknownAssetsFailClosed() {
        val saved = GameSaveCodec.encode(GameSnapshot(GameMode.MATCH,Difficulty.BEGINNER,1,null,Position.initial(),emptyList()))
        assertNull(GameSaveCodec.decode(saved.replace("XIANGQI/1","XIANGQI/9")))
        assertNull(GameSaveCodec.decode(saved + "90-1"))
        assertNull(GameSaveCodec.decode(saved.replace("Red_0","Foreign_0")))
        assertNull(GameSaveCodec.decode(saved.replace("Red_8","Red_0")))
        assertNull(GameSaveCodec.decode(saved.replace("|1|-","|0|-")))
        assertNull(GameSaveCodec.decode("x".repeat(262145)))
    }

    @Test fun androidXmlTrailingWhitespaceDoesNotInvalidateAnEmptyGame() {
        val snapshot=GameSnapshot(GameMode.MATCH,Difficulty.BEGINNER,1,null,Position.initial(),emptyList())
        val encoded=GameSaveCodec.encode(snapshot)
        val legacyXmlText=encoded.substringBeforeLast("\n")+"\n    "
        assertEquals(Position.initial(),GameSaveCodec.decode(legacyXmlText)!!.restoreSession().position)
        assertEquals(Position.initial(),GameSaveCodec.decode(encoded+"\n    ")!!.restoreSession().position)
    }

    @Test fun trainingCompletionKeyDoesNotDependOnAiDifficulty() {
        assertEquals(progressKey(GameMode.TRAINING,Difficulty.BEGINNER,1),progressKey(GameMode.TRAINING,Difficulty.ADVANCED,1))
        assertNotEquals(progressKey(GameMode.CHALLENGE,Difficulty.BEGINNER,1),progressKey(GameMode.CHALLENGE,Difficulty.ADVANCED,1))
    }
}
