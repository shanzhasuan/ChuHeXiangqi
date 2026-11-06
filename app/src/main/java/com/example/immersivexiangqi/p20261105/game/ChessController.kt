package com.example.immersivexiangqi.p20261105.game

import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow

private class PreferenceGameStorage(context: Context) : GameStorage {
    private val prefs = context.getSharedPreferences("xiangqi_progress", Context.MODE_PRIVATE)
    override fun read(key: String): String? = prefs.all[key]?.toString()
    override fun write(values: Map<String, String>) {
        prefs.edit().apply { values.forEach { (key, value) -> putString(key, value) } }.apply()
    }
}

private suspend fun searchMove(position: Position, difficulty: Difficulty): SearchResult =
    withContext(Dispatchers.Default) {
        val context = currentCoroutineContext()
        XiangqiAI(difficulty).choose(position) { context.ensureActive() }
    }

class ChessController(
    private val storage: GameStorage,
    private val scope: CoroutineScope,
    private val search: suspend (Position, Difficulty) -> SearchResult = ::searchMove,
    private val log: (String) -> Unit = {}
) {
    constructor(context: Context, scope: CoroutineScope) : this(PreferenceGameStorage(context), scope, log = { Log.i("Xiangqi", it) })

    private val feedbackChannel = Channel<GameFeedbackEvent>(Channel.BUFFERED)
    val feedbackEvents = feedbackChannel.receiveAsFlow()
    val feedbackEpoch get() = generation
    var outcomeRevision by mutableStateOf(0L); private set
    var recentRedMove by mutableStateOf<RecentMove?>(null); private set
    var recentBlackMove by mutableStateOf<RecentMove?>(null); private set
    private var session = GameSession()
    private var operation: Job? = null
    private var generation = 0
    private var closed = false
    private var legalMoves = Rules.legal(session.position)

    var position by mutableStateOf(session.position); private set
    var selected by mutableStateOf<Int?>(null); private set
    var difficulty by mutableStateOf(Difficulty.BEGINNER); private set
    var mode by mutableStateOf(GameMode.MATCH); private set
    var levelNumber by mutableStateOf(1); private set
    var busy by mutableStateOf(false); private set
    var notice by mutableStateOf("请选择红方棋子，再选择落点"); private set
    var result by mutableStateOf<GameResult?>(null); private set
    var lastMove by mutableStateOf<Move?>(null); private set
    var hintMove by mutableStateOf<Move?>(null); private set
    var aiFailed by mutableStateOf(false); private set
    var completed by mutableStateOf(readNumber("completed")); private set
    var completedHere by mutableStateOf(0); private set
    var bestMoves by mutableStateOf<Int?>(null); private set
    var level by mutableStateOf<Level?>(null); private set

    val legalDestinations get() = selected?.let { from ->
        legalMoves.filter { it.from == from }.map { it.to }.toSet()
    } ?: emptySet()
    val moveCount get() = session.history.size
    val canUndo get() = !closed && session.history.isNotEmpty()
    val canHint get() = !closed && !busy && result == null && position.turn == Side.RED && (mode == GameMode.MATCH || level != null)
    val canRetryOpponent get() = !closed && !busy && result == null && position.turn == Side.BLACK && aiFailed
    val lastMoveDescription get() = session.history.lastOrNull()?.let {
        val piece = it.before.cells[it.move.from]!!
        "${if (piece.side == Side.RED) "红" else "黑"}${piece.label}：${square(it.move.from)} → ${square(it.move.to)}"
    }

    init {
        val raw = storage.read("session_v1")
        val saved = GameSaveCodec.decode(raw)
        if (saved != null) {
            mode = saved.mode
            difficulty = saved.difficulty
            levelNumber = saved.levelNumber
            session = saved.restoreSession()
            if (mode != GameMode.MATCH) {
                val solution = if (mode == GameMode.TRAINING) Rules.legal(saved.initial).firstOrNull {
                    val next = saved.initial.after(it)
                    Rules.inCheck(next, Side.BLACK) && Rules.legal(next).isEmpty()
                } else null
                level = Level(levelNumber, difficulty, saved.seed ?: levelNumber, saved.initial,
                    if (mode == GameMode.TRAINING) "一步将死 · 双车马锁宫" else "击败${difficulty.title}对手，完成第 $levelNumber 关", solution)
            }
        }
        sync()
        if (saved != null) notice = "已恢复对局 · $notice"
        else if (!raw.isNullOrEmpty()) notice = "旧对局无法恢复，已重新开局"
        if (result == null && position.turn == Side.BLACK) think()
    }

    private fun readNumber(key: String): Int = storage.read(key)?.toIntOrNull()?.coerceAtLeast(0) ?: 0
    private fun square(index: Int) = "${index % 9 + 1}路${index / 9 + 1}行"
    private fun contextKey(): String = "${mode.name}_${if (mode == GameMode.TRAINING) Difficulty.BEGINNER.name else difficulty.name}"

    fun chooseDifficulty(value: Difficulty) {
        if (closed || mode == GameMode.TRAINING || value == difficulty) return
        difficulty = value
        levelNumber = if (mode == GameMode.MATCH) 1 else readNumber("level_${contextKey()}").coerceIn(1, 9999)
        reset()
    }

    fun chooseMode(value: GameMode) {
        if (closed || value == mode) return
        mode = value
        levelNumber = if (mode == GameMode.MATCH) 1 else readNumber("level_${contextKey()}").coerceIn(1, 9999)
        reset()
    }

    private fun cancelPending() {
        generation++
        operation?.cancel()
        operation = null
        busy = false
        aiFailed = false
    }

    fun reset() {
        if (closed) return
        cancelPending()
        selected = null
        hintMove = null
        level = null
        session = GameSession()
        sync(persist = false)
        when (mode) {
            GameMode.MATCH -> { notice = "红方先行 · 请选择棋子"; save() }
            GameMode.TRAINING -> {
                level = LevelGenerator.training(levelNumber)
                session = GameSession(level!!.position, oneMoveMate = true)
                sync()
                notice = level!!.objective
            }
            GameMode.CHALLENGE -> {
                val token = generation
                val number = levelNumber
                val strength = difficulty
                busy = true
                notice = "正在准备第 $number 关…"
                operation = scope.launch {
                    try {
                        val prepared = withContext(Dispatchers.Default) {
                            val context = currentCoroutineContext()
                            LevelGenerator.challenge(number, strength, checkCancellation = { context.ensureActive() })
                        }
                        if (token == generation && !closed) {
                            level = prepared
                            session = GameSession(prepared.position)
                            sync()
                            notice = prepared.objective
                        }
                    } catch (cancelled: CancellationException) {
                        throw cancelled
                    } catch (error: Exception) {
                        if (token == generation && !closed) notice = "关卡准备失败，请重新生成"
                    } finally {
                        if (token == generation && !closed) busy = false
                    }
                }
            }
        }
    }

    fun nextLevel() {
        if (closed || mode == GameMode.MATCH) return
        if (levelNumber == 9999) { notice = "已到最后一关，可重新练习"; return }
        levelNumber++
        reset()
    }

    fun tap(index: Int): Boolean {
        if (index !in 0..89 || !canHint) return false
        val piece = position.cells[index]
        if (piece?.side == Side.RED) {
            hintMove = null
            selected = if (selected == index) null else index
            notice = if (selected == null) "已取消选择" else "${piece.label} · 高亮位置可走"
            return true
        }
        val from = selected ?: run { notice = "请先选择红方棋子"; return false }
        if (!commitMove(Move(from, index))) { notice = "此步不合法，请选择高亮落点"; return false }
        selected = null
        hintMove = null
        if (result == null) think()
        return true
    }

    private fun recentMove(side: Side): RecentMove? {
        val index = session.history.indexOfLast { it.before.turn == side }
        if (index < 0) return null
        val entry = session.history[index]
        return RecentMove(entry.move, entry.before.cells[entry.move.from]!!,
            entry.before.cells[entry.move.to], entry.gaveCheck, index + 1)
    }

    private fun commitMove(move: Move): Boolean {
        if (!session.play(move)) return false
        sync()
        val moved = requireNotNull(recentMove(session.history.last().before.turn))
        feedbackChannel.trySend(GameFeedbackEvent.MovePlayed(moved, generation))
        log("feedback_move side=${moved.piece.side} from=${move.from} to=${move.to} capture=${moved.captured!=null} check=${moved.gaveCheck} ply=${moved.ply}")
        result?.let {
            outcomeRevision++
            feedbackChannel.trySend(GameFeedbackEvent.GameEnded(it, generation))
            log("feedback_game_ended winner=${it.winner} reason=${it.message}")
        }
        return true
    }

    private fun sync(persist: Boolean = true) {
        legalMoves = if (session.result == null) Rules.legal(session.position) else emptyList()
        position = session.position
        result = session.result
        lastMove = session.history.lastOrNull()?.move
        recentRedMove = recentMove(Side.RED)
        recentBlackMove = recentMove(Side.BLACK)
        notice = result?.let {
            val outcome = when {
                mode == GameMode.TRAINING && it.winner != Side.RED -> "本题未通过"
                it.winner == Side.RED -> "红方获胜"
                it.winner == Side.BLACK -> "黑方获胜"
                else -> "和棋"
            }
            "$outcome · ${it.message}"
        } ?: if (Rules.inCheck(position, position.turn)) {
            if (position.turn == Side.RED) "将军！请解除将军" else "黑方被将军 · 对手思考中…"
        } else if (position.turn == Side.RED) "轮到红方 · 请选择棋子" else "黑方思考中…"
        if (result?.winner == Side.RED && mode != GameMode.MATCH) {
            val key = progressKey(mode, difficulty, levelNumber)
            val writes = mutableMapOf<String, String>()
            if (storage.read(key) != "true") {
                completed++
                writes[key] = "true"
                writes["completed"] = completed.toString()
                writes["completed_${contextKey()}"] = (readNumber("completed_${contextKey()}") + 1).toString()
            }
            val humanMoves = (moveCount + 1) / 2
            val oldBest = storage.read("best_$key")?.toIntOrNull()
            if (oldBest == null || humanMoves < oldBest) writes["best_$key"] = humanMoves.toString()
            if (writes.isNotEmpty()) storage.write(writes)
        }
        completedHere = if (mode == GameMode.MATCH) 0 else readNumber("completed_${contextKey()}")
        bestMoves = if (mode == GameMode.MATCH) null else storage.read("best_${progressKey(mode, difficulty, levelNumber)}")?.toIntOrNull()
        log("state mode=$mode difficulty=$difficulty turn=${position.turn} ply=$moveCount result=$result")
        if (persist) save()
    }

    private fun save() {
        // An unfinished generation must not replace the last resumable game.
        if (mode != GameMode.MATCH && level == null) return
        val snapshot = GameSnapshot(mode, difficulty, levelNumber, level?.seed, session.initial, session.history.map { it.move })
        storage.write(mapOf("session_v1" to GameSaveCodec.encode(snapshot), "level_${contextKey()}" to levelNumber.toString()))
    }

    private fun think() {
        if (closed || busy || result != null || position.turn != Side.BLACK) return
        val token = generation
        val before = position
        val strength = difficulty
        busy = true
        aiFailed = false
        operation = scope.launch {
            try {
                val answer = search(before, strength)
                if (token == generation && before == position && !closed) {
                    check(answer.move != null && commitMove(answer.move)) { "AI did not return a legal move" }
                    log("ai depth=${answer.depth} nodes=${answer.nodes}")
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                if (token == generation && !closed) {
                    aiFailed = true
                    log("ai_failed type=${error.javaClass.simpleName} message=${error.message}")
                    notice = "对手思考中断，请重试或悔棋"
                }
            } finally {
                if (token == generation && !closed) busy = false
            }
        }
    }

    fun retryOpponent() {
        if (canRetryOpponent) think()
    }

    fun hint() {
        if (!canHint) return
        level?.solution?.takeIf { mode == GameMode.TRAINING && moveCount == 0 }?.let {
            showHint(it)
            return
        }
        val token = generation
        val before = position
        busy = true
        operation = scope.launch {
            try {
                val answer = search(before, Difficulty.INTERMEDIATE)
                if (token == generation && before == position && !closed) answer.move?.let { showHint(it) }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                if (token == generation && !closed) notice = "暂时无法给出提示，请重试"
            } finally {
                if (token == generation && !closed) busy = false
            }
        }
    }

    private fun showHint(move: Move) {
        if (move !in legalMoves) return
        selected = move.from
        hintMove = move
        notice = "提示：${position.cells[move.from]?.label} → ${square(move.to)}"
    }

    fun undo() {
        if (closed) return
        cancelPending()
        selected = null
        hintMove = null
        if (session.undoTurn()) sync() else notice = "还没有可悔的棋步"
    }

    fun close() {
        if (closed) return
        save()
        closed = true
        cancelPending()
        feedbackChannel.close()
    }
}
