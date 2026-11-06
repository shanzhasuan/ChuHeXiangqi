package com.example.immersivexiangqi.p20261105.content

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.immersivexiangqi.p20261105.game.*
import com.example.immersivexiangqi.p20261105.ui.theme.ChessClubControlSurface
import com.pico.spatial.ui.design.Button
import com.pico.spatial.ui.design.Text

internal fun modeSummary(game: ChessController): String =
    "${game.mode.title} · ${if(game.mode==GameMode.TRAINING) "一步将死" else game.difficulty.title} · ${if(game.mode==GameMode.MATCH) "自由对局" else "第 ${game.levelNumber} 关"}"

internal fun outcomeTitle(game: ChessController): String = when(game.result?.winner) {
    Side.RED -> if(game.mode==GameMode.MATCH) "你赢了 · 红方获胜" else "闯关成功 · 红方获胜"
    Side.BLACK -> if(game.mode==GameMode.MATCH) "本局结束 · 黑方获胜" else "本关失败 · 请再试一次"
    null -> "本局结束 · 和棋"
}

@Composable
internal fun BoardStatus(game: ChessController, ready: Boolean) {
    // design-style: design-surface chess-status-pane
    Column(Modifier.fillMaxSize().clip(RoundedCornerShape(16.dp)).background(ChessClubControlSurface.paper).padding(18.dp),
        verticalArrangement=Arrangement.spacedBy(6.dp)) {
        Text(modeSummary(game),fontSize=46.sp,color=ChessClubControlSurface.ink)
        Text(when {
            !ready -> "正在载入棋室…"
            game.result!=null -> "${outcomeTitle(game)} · ${game.result!!.message}"
            game.position.turn==Side.RED -> "轮到你（红方） · 金色是你的上一着"
            else -> "对方思考中… · 蓝色是对方的上一着"
        },fontSize=34.sp,color=ChessClubControlSurface.ink)
        game.recentBlackMove?.let { recent ->
            val m=recent.move
            val mark=if(game.position.cells[m.to]?.id==recent.piece.id) "蓝色标记" else "该棋子已被吃"
            Text("对方刚走：${recent.piece.label} ${m.from%9+1}路${m.from/9+1}行 → ${m.to%9+1}路${m.to/9+1}行 · $mark",
                fontSize=30.sp,color=ChessClubControlSurface.secondaryInk)
        }
    }
}

@Composable
internal fun GameEndPanel(game: ChessController, close: () -> Unit) {
    val result=game.result ?: return
    // design-style: design-surface chess-outcome-pane
    Column(Modifier.fillMaxSize().clip(RoundedCornerShape(22.dp)).background(ChessClubControlSurface.paper).padding(28.dp),
        verticalArrangement=Arrangement.spacedBy(14.dp)) {
        Text(outcomeTitle(game),fontSize=64.sp,color=ChessClubControlSurface.ink,
            modifier=Modifier.semantics {contentDescription="游戏结束：${outcomeTitle(game)}"})
        Text("${result.message} · 共走 ${game.moveCount} 步",fontSize=42.sp,color=ChessClubControlSurface.ink)
        Text(modeSummary(game),fontSize=34.sp,color=ChessClubControlSurface.secondaryInk)
        if(game.mode!=GameMode.MATCH && result.winner==Side.RED) {
            Text("已完成 ${game.completedHere} 关 · 本关最佳 ${game.bestMoves ?: 1} 步",fontSize=34.sp,color=ChessClubControlSurface.ink)
        }
        Row(horizontalArrangement=Arrangement.spacedBy(14.dp)) {
            Button(onClick={close();if(game.mode!=GameMode.MATCH && result.winner==Side.RED) game.nextLevel() else game.reset()},modifier=Modifier.weight(1f)) {
                Text(if(game.mode!=GameMode.MATCH && result.winner==Side.RED) "继续下一关" else "重新开始",fontSize=40.sp)
            }
            Button(onClick=close,modifier=Modifier.weight(1f)) {Text("关闭提示",fontSize=40.sp)}
        }
    }
}
