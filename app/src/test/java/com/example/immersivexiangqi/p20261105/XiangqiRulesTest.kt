package com.example.immersivexiangqi.p20261105

import com.example.immersivexiangqi.p20261105.game.*
import org.junit.Assert.*
import org.junit.Test

class XiangqiRulesTest {
    private fun position(vararg pieces:Triple<Int,Side,Kind>,turn:Side=Side.RED):Position {
        val c=MutableList<Piece?>(90){null};for((i,s,k) in pieces) c[i]=Piece("p$i",s,k)
        return Position(c,turn)
    }
    private infix fun Pair<Int,Side>.to(kind:Kind)=Triple(first,second,kind)
    @Test fun initialHas32PiecesAnd44LegalMoves() {
        val p=Position.initial();assertEquals(32,p.cells.count {it!=null});assertTrue(Rules.valid(p));assertEquals(44,Rules.legal(p).size)
    }
    @Test fun horseLegBlocksCorrespondingJumps() {
        val p=position(85 to Side.RED to Kind.KING,4 to Side.BLACK to Kind.KING,49 to Side.RED to Kind.PAWN,40 to Side.RED to Kind.HORSE,41 to Side.RED to Kind.PAWN)
        val m=Rules.pseudo(p,Side.RED).filter {it.from==40}
        assertFalse(Move(40,51) in m);assertFalse(Move(40,33) in m);assertTrue(Move(40,21) in m)
    }
    @Test fun elephantCannotCrossRiverOrJumpBlockedEye() {
        val p=position(85 to Side.RED to Kind.KING,4 to Side.BLACK to Kind.KING,49 to Side.RED to Kind.PAWN,47 to Side.RED to Kind.ELEPHANT,57 to Side.RED to Kind.PAWN)
        val m=Rules.pseudo(p,Side.RED).filter {it.from==47};assertFalse(Move(47,31) in m);assertFalse(Move(47,67) in m)
    }
    @Test fun cannonNeedsExactlyOneScreen() {
        val p=position(85 to Side.RED to Kind.KING,4 to Side.BLACK to Kind.KING,49 to Side.RED to Kind.PAWN,54 to Side.RED to Kind.CANNON,56 to Side.RED to Kind.PAWN,60 to Side.BLACK to Kind.ROOK)
        assertTrue(Move(54,60) in Rules.pseudo(p,Side.RED));assertFalse(Move(54,57) in Rules.pseudo(p,Side.RED))
        assertFalse(Move(54,60) in Rules.pseudo(p.copy(cells=p.cells.toMutableList().also {it[56]=null}),Side.RED))
    }
    @Test fun exposingGeneralsIsIllegal() {
        val p=position(85 to Side.RED to Kind.KING,4 to Side.BLACK to Kind.KING,49 to Side.RED to Kind.ROOK)
        assertFalse(Move(49,48) in Rules.legal(p));assertTrue(Move(49,40) in Rules.legal(p))
    }
    @Test fun pawnCrossingUnlocksSidewaysNeverBackward() {
        val p=position(85 to Side.RED to Kind.KING,4 to Side.BLACK to Kind.KING,49 to Side.RED to Kind.PAWN,38 to Side.RED to Kind.PAWN)
        assertEquals(listOf(Move(49,40)),Rules.pseudo(p,Side.RED).filter {it.from==49})
        val m=Rules.pseudo(p,Side.RED).filter {it.from==38};assertTrue(Move(38,37) in m);assertFalse(Move(38,47) in m)
    }
    @Test fun challengesAreLegalAndReproducible() {
        for(d in Difficulty.entries) for(n in 1..8) {val a=LevelGenerator.challenge(n,d);assertEquals(a.position,LevelGenerator.challenge(n,d).position);assertEquals(Side.RED,a.position.turn);assertTrue(Rules.valid(a.position));assertTrue(Rules.legal(a.position).isNotEmpty())}
    }
    @Test fun trainingHasVerifiedMate() {
        for(n in 1..4) {val level=LevelGenerator.training(n);val g=GameSession(level.position);assertTrue(g.play(level.solution!!));assertEquals(Side.RED,g.result?.winner);assertEquals("将死",g.result?.message)}
    }
    @Test fun aiReturnsLegalMove() {
        val p=Position.initial();val r=XiangqiAI(Difficulty.BEGINNER).choose(p);assertTrue(r.move in Rules.legal(p));assertTrue(r.nodes>0)
    }
    @Test fun undoRestoresBothSides() {
        val g=GameSession();val initial=g.position;g.play(Rules.legal(g.position).first());g.play(Rules.legal(g.position).first());assertTrue(g.undoTurn());assertEquals(initial,g.position);assertTrue(g.history.isEmpty());assertNull(g.result)
    }
}
