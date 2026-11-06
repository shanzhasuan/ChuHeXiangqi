package com.example.immersivexiangqi.p20261105.game

/** A move that actually committed; selection, hints and restore never produce move events. */
data class RecentMove(
    val move: Move,
    val piece: Piece,
    val captured: Piece?,
    val gaveCheck: Boolean,
    val ply: Int
)

sealed interface GameFeedbackEvent {
    val epoch: Int
    data class MovePlayed(val recent: RecentMove, override val epoch: Int) : GameFeedbackEvent
    data class GameEnded(val result: GameResult, override val epoch: Int) : GameFeedbackEvent
}
