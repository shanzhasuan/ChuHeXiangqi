package com.example.immersivexiangqi.p20261105

import com.example.immersivexiangqi.p20261105.game.*
import org.junit.Assert.*
import org.junit.Test
import java.util.concurrent.CancellationException

class XiangqiSessionTest {
    private fun position(vararg pieces: Triple<Int, Side, Kind>): Position {
        val cells = MutableList<Piece?>(90) { null }
        pieces.forEach { (index, side, kind) -> cells[index] = Piece("piece_$index", side, kind) }
        return Position(cells)
    }

    @Test fun quietDrawIsRecognizedWhenOpeningASession() {
        val game = GameSession(Position.initial().copy(quietPlies = 120))
        assertNull(game.result?.winner)
        assertNotNull(game.result)
        assertFalse(game.play(Rules.legal(game.position).first()))
    }

    @Test fun mateTakesPriorityOverQuietDraw() {
        val lesson = LevelGenerator.training(1)
        val terminal = lesson.position.after(lesson.solution!!).copy(quietPlies = 120)
        val game = GameSession(terminal)
        assertEquals(Side.RED, game.result?.winner)
        assertEquals("将死", game.result?.message)
    }

    @Test fun stalemateIsALossEvenWithoutCheck() {
        val p = position(Triple(85, Side.RED, Kind.KING), Triple(4, Side.BLACK, Kind.KING),
            Triple(49, Side.BLACK, Kind.PAWN), Triple(75, Side.BLACK, Kind.ROOK), Triple(77, Side.BLACK, Kind.ROOK))
        assertTrue(Rules.valid(p))
        assertFalse(Rules.inCheck(p, Side.RED))
        assertTrue(Rules.legal(p).isEmpty())
        assertEquals(Side.BLACK, GameSession(p).result?.winner)
    }

    @Test fun repeatedQuietCyclesDrawAndUndoReopensTheGame() {
        val game = GameSession()
        val cycle = listOf(Move(82,63), Move(1,18), Move(63,82), Move(18,1))
        repeat(2) { cycle.forEach { assertTrue(game.play(it)) } }
        assertNotNull(game.result)
        assertNull(game.result?.winner)
        assertTrue(game.undoTurn())
        assertNull(game.result)
        assertEquals(6, game.history.size)
        assertTrue(game.play(Move(63,82)))
    }

    @Test fun unilateralPerpetualCheckLoses() {
        val p = position(Triple(85,Side.RED,Kind.KING),Triple(4,Side.BLACK,Kind.KING),
            Triple(49,Side.RED,Kind.PAWN),Triple(12,Side.RED,Kind.ROOK))
        val game = GameSession(p)
        val cycle = listOf(Move(12,13),Move(4,3),Move(13,12),Move(3,4))
        repeat(2) { cycle.forEach { assertTrue(game.play(it)) } }
        assertEquals(Side.BLACK, game.result?.winner)
        assertEquals("单方长将判负", game.result?.message)
    }

    @Test fun oneMoveLessonFailsForANonMatingMoveAndCanBeUndone() {
        val lesson = LevelGenerator.training(1)
        val move = Rules.legal(lesson.position).first { Rules.legal(lesson.position.after(it)).isNotEmpty() }
        val game = GameSession(lesson.position, oneMoveMate = true)
        assertTrue(game.play(move))
        assertEquals(Side.BLACK, game.result?.winner)
        assertFalse(game.play(Rules.legal(game.position).first()))
        assertTrue(game.undoTurn())
        assertEquals(lesson.position, game.position)
        assertNull(game.result)
        assertTrue(game.play(lesson.solution!!))
        assertEquals(Side.RED, game.result?.winner)
    }

    @Test fun stalemateDoesNotSolveAMateLesson() {
        val p = position(Triple(85,Side.RED,Kind.KING),Triple(4,Side.BLACK,Kind.KING),
            Triple(49,Side.RED,Kind.PAWN),Triple(12,Side.RED,Kind.ROOK),Triple(14,Side.RED,Kind.ROOK))
        val ordinary=GameSession(p)
        assertTrue(ordinary.play(Move(49,40)))
        assertEquals(Side.RED,ordinary.result?.winner)
        val lesson=GameSession(p,oneMoveMate=true)
        assertTrue(lesson.play(Move(49,40)))
        assertEquals(Side.BLACK,lesson.result?.winner)
    }

    @Test fun duplicatePieceIdentityIsInvalid() {
        val p = Position.initial()
        val cells = p.cells.toMutableList()
        cells[89] = cells[81]!!.copy()
        assertFalse(Rules.valid(p.copy(cells = cells)))
    }

    @Test(expected = CancellationException::class)
    fun aiSearchCooperatesWithCancellationInsideTheSearch() {
        var checks = 0
        XiangqiAI(Difficulty.ADVANCED).choose(Position.initial()) {
            if (++checks == 3) throw CancellationException("cancel search")
        }
    }

    @Test(expected = CancellationException::class)
    fun challengeGenerationCooperatesWithCancellation() {
        LevelGenerator.challenge(1, Difficulty.ADVANCED, checkCancellation = {
            throw CancellationException("cancel generation")
        })
    }
}
