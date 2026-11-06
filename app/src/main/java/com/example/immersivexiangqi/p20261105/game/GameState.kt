package com.example.immersivexiangqi.p20261105.game

enum class GameMode(val title: String) {
    MATCH("人机对弈"), CHALLENGE("闯关挑战"), TRAINING("残局学棋")
}

interface GameStorage {
    fun read(key: String): String?
    fun write(values: Map<String, String>)
}

data class GameSnapshot(
    val mode: GameMode,
    val difficulty: Difficulty,
    val levelNumber: Int,
    val seed: Int?,
    val initial: Position,
    val moves: List<Move>
) {
    fun restoreSession(): GameSession {
        require(levelNumber in 1..9999)
        if (mode == GameMode.MATCH) require(initial == Position.initial())
        val game = GameSession(initial, oneMoveMate = mode == GameMode.TRAINING)
        for (move in moves) require(game.play(move)) { "Invalid saved move" }
        return game
    }
}

object GameSaveCodec {
    private const val MAGIC = "XIANGQI/1"
    private const val MAX_TEXT = 262144
    private const val MAX_MOVES = 20000
    private val pieces = Position.initial().cells.filterNotNull().associateBy { it.id }

    fun encode(snapshot: GameSnapshot): String {
        return listOf(
            MAGIC,
            "${snapshot.mode.name}|${snapshot.difficulty.name}|${snapshot.levelNumber}|${snapshot.seed ?: "-"}",
            "${snapshot.initial.turn.name}|${snapshot.initial.quietPlies}",
            snapshot.initial.cells.joinToString(",") { it?.id ?: "." },
            snapshot.moves.joinToString(";") { "${it.from}-${it.to}" }.ifEmpty { "-" }
        ).joinToString("\n")
    }

    fun decode(text: String?): GameSnapshot? = runCatching {
        require(text != null && text.length <= MAX_TEXT)
        val lines = text.replace("\r\n", "\n").trimEnd().split('\n')
        require(lines.size in 4..5 && lines[0] == MAGIC)
        val metadata = lines[1].split('|')
        val position = lines[2].split('|')
        require(metadata.size == 4 && position.size == 2)
        val ids = lines[3].split(',')
        require(ids.size == 90)
        val cells = ids.map { if (it == ".") null else requireNotNull(pieces[it]) }
        val moveText = lines.getOrElse(4) { "" }.trim()
        val moves = if (moveText.isEmpty() || moveText == "-") emptyList() else {
            val tokens = moveText.split(';')
            require(tokens.size <= MAX_MOVES)
            tokens.map {
                val pair = it.split('-')
                require(pair.size == 2)
                Move(pair[0].toInt(), pair[1].toInt())
            }
        }
        val snapshot = GameSnapshot(
            GameMode.valueOf(metadata[0]), Difficulty.valueOf(metadata[1]),
            metadata[2].toInt(), if (metadata[3] == "-") null else metadata[3].toInt(),
            Position(cells, Side.valueOf(position[0]), position[1].toInt()), moves
        )
        snapshot.restoreSession()
        snapshot
    }.getOrNull()
}

fun progressKey(mode: GameMode, difficulty: Difficulty, number: Int): String {
    require(mode != GameMode.MATCH && number in 1..9999)
    val effective = if (mode == GameMode.TRAINING) Difficulty.BEGINNER else difficulty
    return "won_${mode.name}_${effective.name}_$number"
}
