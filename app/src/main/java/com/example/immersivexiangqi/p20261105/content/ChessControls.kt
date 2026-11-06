package com.example.immersivexiangqi.p20261105.content

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
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

@Composable
fun ChessControls(game:ChessController,playable:Boolean=true,onShowResult:()->Unit={}) {
    var showRules by remember {mutableStateOf(false)}
    // design-style: design-surface chess-controls-pane
    Column(Modifier.fillMaxSize().clip(RoundedCornerShape(20.dp)).background(ChessClubControlSurface.paper).padding(20.dp),
        verticalArrangement=Arrangement.spacedBy(10.dp)) {
        Text("楚河 · 沉浸象棋",fontSize=44.sp,color=ChessClubControlSurface.ink)
        Text(modeSummary(game),fontSize=36.sp,color=ChessClubControlSurface.ink)
        if(game.mode!=GameMode.MATCH) {
            Text("第 ${game.levelNumber} 关 · 已完成 ${game.completedHere} 关",fontSize=30.sp,color=ChessClubControlSurface.secondaryInk)
        }
        Row(horizontalArrangement=Arrangement.spacedBy(6.dp)) {
            GameMode.entries.forEach {m->
                Button(onClick={game.chooseMode(m)},modifier=Modifier.weight(1f).semantics {contentDescription="切换${m.title}"}) {
                    ControlLabel((if(game.mode==m) "✓ " else "")+m.title)
                }
            }
        }
        if(game.mode!=GameMode.TRAINING) {
            Row(horizontalArrangement=Arrangement.spacedBy(8.dp)) {
                Difficulty.entries.forEach {d->Button(onClick={game.chooseDifficulty(d)},modifier=Modifier.weight(1f)) {
                    ControlLabel((if(game.difficulty==d) "✓ " else "")+d.title)
                }}
            }
        }
        Text(game.notice,fontSize=30.sp,color=ChessClubControlSurface.ink)
        Row(horizontalArrangement=Arrangement.spacedBy(8.dp)) {
            Button(onClick={game.undo()},enabled=playable&&game.canUndo,modifier=Modifier.weight(1f)) {ControlLabel("悔棋")}
            Button(onClick={game.hint()},enabled=playable&&game.canHint,modifier=Modifier.weight(1f)) {ControlLabel(if(game.busy) "思考中…" else "走棋提示")}
        }
        Row(horizontalArrangement=Arrangement.spacedBy(8.dp)) {
            Button(onClick={game.reset()},modifier=Modifier.weight(1f)) {ControlLabel(if(game.mode==GameMode.MATCH) "重新开局" else "重试本关")}
            if(game.result!=null) {
                Button(onClick=onShowResult,modifier=Modifier.weight(1f)) {ControlLabel("查看结果")}
            } else {
                Button(onClick={showRules=!showRules},modifier=Modifier.weight(1f)) {ControlLabel(if(showRules) "收起规则" else "象棋规则")}
            }
        }
        if(game.mode!=GameMode.MATCH) {
            Button(onClick={game.nextLevel()},modifier=Modifier.fillMaxWidth()) {ControlLabel(if(game.result?.winner==Side.RED) "下一关" else "生成下一关")}
        }
        if(game.canRetryOpponent) {
            Button(onClick={game.retryOpponent()},enabled=playable,modifier=Modifier.fillMaxWidth()) {ControlLabel("重试对手走棋")}
        }
        Column(Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()),verticalArrangement=Arrangement.spacedBy(10.dp)) {
            Text("${if(game.result!=null) "对局已结束" else if(game.position.turn==Side.RED) "红方回合" else "黑方回合"} · 已走 ${game.moveCount} 步",fontSize=28.sp,color=ChessClubControlSurface.secondaryInk)
            Text("金色：你的上一着 · 蓝色：对方的上一着",fontSize=26.sp,color=ChessClubControlSurface.secondaryInk)
            game.lastMoveDescription?.let {Text("上一着：$it",fontSize=28.sp,color=ChessClubControlSurface.secondaryInk)}
            if(game.mode!=GameMode.MATCH) {
                Text(game.level?.objective.orEmpty(),fontSize=28.sp,color=ChessClubControlSurface.secondaryInk)
                game.bestMoves?.let {Text("本关最佳 $it 步 · 累计完成 ${game.completed} 关",fontSize=28.sp,color=ChessClubControlSurface.secondaryInk)}
            }
            if(showRules) {
                Text("红方先行，先选棋子，再选高亮落点。",fontSize=28.sp,color=ChessClubControlSurface.ink)
                Text("车走直线；马走日、不能蹩马腿；相象走田、不能塞象眼且不能过河。仕士斜走一步，将帅直走一步，都在九宫内。",fontSize=28.sp,color=ChessClubControlSurface.secondaryInk)
                Text("炮走直线，隔一个棋子吃子。兵卒只向前，过河后可横走，不能后退。将帅不能照面，也不能走入被将军的位置。",fontSize=28.sp,color=ChessClubControlSurface.secondaryInk)
                Text("将死或困毙判负；单方长将判负。其余三次重复局面及60回合无吃子或兵卒移动采用休闲判和规则，暂不实现比赛级长捉裁判。",fontSize=28.sp,color=ChessClubControlSurface.secondaryInk)
            }
        }
    }
}
@Composable
private fun ControlLabel(text:String) {Text(text,fontSize=30.sp)}
