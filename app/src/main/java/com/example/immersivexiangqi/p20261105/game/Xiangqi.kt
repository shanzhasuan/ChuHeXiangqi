package com.example.immersivexiangqi.p20261105.game

import kotlin.math.abs
import kotlin.random.Random

enum class Side { RED, BLACK; fun other() = if(this==RED) BLACK else RED }
enum class Kind(val value:Int) { KING(20000),ROOK(900),CANNON(450),HORSE(400),ELEPHANT(200),ADVISOR(200),PAWN(100) }
data class Piece(val id:String,val side:Side,val kind:Kind) {
    val label:String get()=when(kind) {
        Kind.KING->if(side==Side.RED) "帅" else "将"
        Kind.ADVISOR->if(side==Side.RED) "仕" else "士"
        Kind.ELEPHANT->if(side==Side.RED) "相" else "象"
        Kind.PAWN->if(side==Side.RED) "兵" else "卒"
        Kind.ROOK->"车";Kind.HORSE->"马";Kind.CANNON->"炮"
    }
}
data class Move(val from:Int,val to:Int)
data class Position(val cells:List<Piece?>,val turn:Side=Side.RED,val quietPlies:Int=0) {
    init { require(cells.size==90); require(quietPlies>=0) }
    fun after(m:Move):Position {
        require(m.from in 0..89 && m.to in 0..89 && m.from!=m.to)
        val piece=requireNotNull(cells[m.from]);val next=cells.toMutableList();val capture=next[m.to]!=null
        next[m.to]=piece;next[m.from]=null
        return Position(next,turn.other(),if(capture||piece.kind==Kind.PAWN) 0 else quietPlies+1)
    }
    fun key()=(if(turn==Side.RED) "r" else "b")+cells.joinToString("") { if(it==null) "." else ((if(it.side==Side.RED) 'A' else 'a').code+it.kind.ordinal).toChar().toString() }
    companion object {
        fun initial():Position {
            val c=MutableList<Piece?>(90){null}
            val back=listOf(Kind.ROOK,Kind.HORSE,Kind.ELEPHANT,Kind.ADVISOR,Kind.KING,Kind.ADVISOR,Kind.ELEPHANT,Kind.HORSE,Kind.ROOK)
            for(s in Side.entries) {
                val name=if(s==Side.RED) "Red" else "Black";val rank=if(s==Side.RED) 9 else 0
                for(x in 0..8) c[rank*9+x]=Piece("${name}_$x",s,back[x])
                for((i,x) in listOf(1,7).withIndex()) c[(if(s==Side.RED) 7 else 2)*9+x]=Piece("${name}_${9+i}",s,Kind.CANNON)
                for((i,x) in listOf(0,2,4,6,8).withIndex()) c[(if(s==Side.RED) 6 else 3)*9+x]=Piece("${name}_${11+i}",s,Kind.PAWN)
            }
            return Position(c)
        }
    }
}

object Rules {
    fun palace(s:Side,x:Int,y:Int)=x in 3..5 && y in (if(s==Side.RED) 7..9 else 0..2)
    fun legal(p:Position)=pseudo(p,p.turn).filter { p.cells[it.to]?.kind!=Kind.KING && !inCheck(p.after(it),p.turn) }
    fun pseudo(p:Position,side:Side):List<Move> {
        val out=ArrayList<Move>()
        for(from in 0..89) {
            val piece=p.cells[from]?:continue;if(piece.side!=side) continue
            val x=from%9;val y=from/9
            fun add(tx:Int,ty:Int,ok:Boolean=true) { if(ok&&tx in 0..8&&ty in 0..9&&p.cells[ty*9+tx]?.side!=side) out.add(Move(from,ty*9+tx)) }
            when(piece.kind) {
                Kind.ROOK,Kind.CANNON->for((dx,dy) in listOf(1 to 0,-1 to 0,0 to 1,0 to -1)) {
                    var tx=x+dx;var ty=y+dy;var screen=false
                    while(tx in 0..8&&ty in 0..9) {
                        val occupied=p.cells[ty*9+tx]
                        if(piece.kind==Kind.ROOK) { add(tx,ty);if(occupied!=null) break }
                        else if(!screen) { if(occupied==null) add(tx,ty) else screen=true }
                        else if(occupied!=null) { add(tx,ty);break }
                        tx+=dx;ty+=dy
                    }
                }
                Kind.HORSE->for((dx,dy) in listOf(2 to 1,2 to -1,-2 to 1,-2 to -1,1 to 2,-1 to 2,1 to -2,-1 to -2)) {
                    val lx=x+if(abs(dx)==2) dx/2 else 0;val ly=y+if(abs(dy)==2) dy/2 else 0
                    if(lx in 0..8&&ly in 0..9) add(x+dx,y+dy,p.cells[ly*9+lx]==null)
                }
                Kind.ELEPHANT->for((dx,dy) in listOf(2 to 2,2 to -2,-2 to 2,-2 to -2)) {
                    val ex=x+dx/2;val ey=y+dy/2;val ty=y+dy
                    if(ex in 0..8&&ey in 0..9) add(x+dx,ty,p.cells[ey*9+ex]==null&&(if(side==Side.RED) ty>=5 else ty<=4))
                }
                Kind.ADVISOR->for((dx,dy) in listOf(1 to 1,1 to -1,-1 to 1,-1 to -1)) add(x+dx,y+dy,palace(side,x+dx,y+dy))
                Kind.KING->{
                    for((dx,dy) in listOf(1 to 0,-1 to 0,0 to 1,0 to -1)) add(x+dx,y+dy,palace(side,x+dx,y+dy))
                    for(dy in listOf(-1,1)) { var ty=y+dy;while(ty in 0..9) { val target=p.cells[ty*9+x];if(target!=null) { if(target.side!=side&&target.kind==Kind.KING) add(x,ty);break };ty+=dy } }
                }
                Kind.PAWN->{add(x,y+if(side==Side.RED) -1 else 1);if(if(side==Side.RED) y<=4 else y>=5) {add(x-1,y);add(x+1,y)}}
            }
        }
        return out
    }
    fun inCheck(p:Position,side:Side):Boolean {
        val king=p.cells.indexOfFirst { it?.side==side&&it.kind==Kind.KING }
        return king<0||pseudo(p,side.other()).any { it.to==king }
    }
    fun valid(p:Position):Boolean {
        val pieces=p.cells.filterNotNull()
        if(pieces.map {it.id}.toSet().size!=pieces.size) return false
        if(Side.entries.any { s->p.cells.count { it?.side==s&&it.kind==Kind.KING }!=1 }) return false
        for(side in Side.entries) for(kind in Kind.entries) {
            val maximum=when(kind) {Kind.KING->1;Kind.PAWN->5;else->2}
            if(pieces.count {it.side==side&&it.kind==kind}>maximum) return false
        }
        for((i,piece) in p.cells.withIndex()) if(piece!=null) {
            if(piece.kind in listOf(Kind.KING,Kind.ADVISOR)&&!palace(piece.side,i%9,i/9)) return false
            if(piece.kind==Kind.ELEPHANT&&(if(piece.side==Side.RED) i/9<5 else i/9>4)) return false
        }
        return !inCheck(p,p.turn.other())
    }
}

enum class Difficulty(val title:String,val depth:Int,val nodes:Int,val milliseconds:Long) {
    BEGINNER("初级",2,1800,450),INTERMEDIATE("中级",3,14000,1100),ADVANCED("高级",4,65000,2400)
}
data class SearchResult(val move:Move?,val score:Int,val nodes:Int,val depth:Int)
class XiangqiAI(private val level:Difficulty) {
    private var nodes=0;private var deadline=0L
    private var checkCancellation:()->Unit = {}
    private class BudgetReached:RuntimeException()
    fun choose(p:Position,checkCancellation:()->Unit = {}):SearchResult {
        this.checkCancellation=checkCancellation
        checkCancellation()
        nodes=0;deadline=System.nanoTime()+level.milliseconds*1_000_000
        val moves=ordered(p,Rules.legal(p));if(moves.isEmpty()) return SearchResult(null,-100000,0,0)
        if(p.quietPlies>=120) return SearchResult(null,0,0,0)
        var best=moves.first();var score=0;var completed=0
        for(depth in 1..level.depth) {
            checkCancellation()
            var roundBest=best;var roundScore=Int.MIN_VALUE;var alpha=-200000
            try {
                for(m in listOf(best)+moves.filter { it!=best }) { checkCancellation();val s=-search(p.after(m),depth-1,-200000,-alpha,1);if(s>roundScore){roundScore=s;roundBest=m};alpha=maxOf(alpha,s) }
                best=roundBest;score=roundScore;completed=depth
            } catch(_:BudgetReached){break}
        }
        return SearchResult(best,score,nodes,completed)
    }
    private fun search(p:Position,depth:Int,a:Int,b:Int,ply:Int):Int {
        nodes++;if(nodes%64==0) checkCancellation()
        if(nodes>level.nodes||(nodes%64==0&&System.nanoTime()>deadline)) throw BudgetReached()
        val moves=Rules.legal(p);if(moves.isEmpty()) return -100000+ply;if(p.quietPlies>=120) return 0
        if(depth==0) return evaluate(p)
        var alpha=a;var best=-200000
        for(m in ordered(p,moves)) { val s=-search(p.after(m),depth-1,-b,-alpha,ply+1);best=maxOf(best,s);alpha=maxOf(alpha,s);if(alpha>=b) break }
        return best
    }
    private fun ordered(p:Position,m:List<Move>)=m.sortedByDescending { (p.cells[it.to]?.kind?.value?:0)*10-p.cells[it.from]!!.kind.value/20 }
    private fun evaluate(p:Position):Int {
        var red=0
        for((i,piece) in p.cells.withIndex()) if(piece!=null) {
            val advance=if(piece.side==Side.RED) 9-i/9 else i/9;val center=4-abs(i%9-4)
            val bonus=when(piece.kind) {Kind.PAWN->advance*9+if(advance>=5) 65+center*7 else 0;Kind.HORSE->center*12+minOf(advance,6)*4;Kind.ROOK->center*4+minOf(advance,7)*3;Kind.CANNON->center*7;else->0}
            red+=(piece.kind.value+bonus)*if(piece.side==Side.RED) 1 else -1
        }
        return red*if(p.turn==Side.RED) 1 else -1
    }
}
data class GameResult(val winner:Side?,val message:String)
data class HistoryEntry(val before:Position,val move:Move,val gaveCheck:Boolean)
class GameSession(val initial:Position=Position.initial(),private val oneMoveMate:Boolean=false) {
    var position=initial;private set
    private val entries=ArrayList<HistoryEntry>()
    val history:List<HistoryEntry> get()=entries
    private val occurrences=hashMapOf(initial.key() to arrayListOf(0))
    var result:GameResult?=null;private set
    init {
        require(Rules.valid(initial)) {"Invalid initial position"}
        require(!oneMoveMate||initial.turn==Side.RED)
        result=terminalPosition()
    }
    private fun terminalPosition():GameResult? = when {
        Rules.legal(position).isEmpty()->GameResult(position.turn.other(),if(Rules.inCheck(position,position.turn)) "将死" else "困毙：无合法着法")
        position.quietPlies>=120->GameResult(null,"连续60回合无吃子或兵卒移动，判和")
        else->null
    }
    fun play(m:Move):Boolean {
        if(result!=null||m !in Rules.legal(position)) return false
        val before=position;position=position.after(m);entries.add(HistoryEntry(before,m,Rules.inCheck(position,position.turn)))
        val seen=occurrences.getOrPut(position.key()){arrayListOf()};seen.add(history.size)
        val replies=Rules.legal(position)
        val mate=replies.isEmpty()&&Rules.inCheck(position,position.turn)
        result=when {
            oneMoveMate&&history.size==1&&!mate->GameResult(Side.BLACK,"未能一步将死，请重试或悔棋")
            replies.isEmpty()->GameResult(position.turn.other(),if(mate) "将死" else "困毙：无合法着法")
            seen.size>=3->{
                val cycle=history.subList(seen[seen.size-3],history.size)
                val perpetual=Side.entries.filter {s->val own=cycle.filter {it.before.turn==s};own.isNotEmpty()&&own.all {it.gaveCheck}}
                if(perpetual.size==1) GameResult(perpetual.first().other(),"单方长将判负") else GameResult(null,"重复局面，休闲规则判和")
            }
            position.quietPlies>=120->GameResult(null,"连续60回合无吃子或兵卒移动，判和")
            else->null
        }
        return true
    }
    fun undoTurn():Boolean {
        if(history.isEmpty()) return false
        val n=if(position.turn==Side.RED&&history.size>=2) 2 else 1
        val retained=history.dropLast(n).map {it.move};val initial=history.first().before
        position=initial;entries.clear();result=terminalPosition();occurrences.clear();occurrences[initial.key()]=arrayListOf(0)
        for(m in retained) play(m)
        return true
    }
}
data class Level(val number:Int,val difficulty:Difficulty,val seed:Int,val position:Position,val objective:String,val solution:Move?=null)
object LevelGenerator {
    fun challenge(number:Int,difficulty:Difficulty,seed:Int=number*7919+difficulty.ordinal*104729,checkCancellation:()->Unit = {}):Level {
        require(number in 1..9999)
        val random=Random(seed);var p=Position.initial()
        val pairs=when(difficulty){Difficulty.BEGINNER->2;Difficulty.INTERMEDIATE->5;Difficulty.ADVANCED->9}
        repeat(pairs*2) {
            checkCancellation()
            val move=Rules.legal(p).shuffled(random).firstOrNull {
                checkCancellation();Rules.legal(p.after(it)).isNotEmpty()
            }
            if(move!=null) p=p.after(move)
        }
        if(p.turn!=Side.RED||!Rules.valid(p)) p=Position.initial()
        return Level(number,difficulty,seed,p,"击败${difficulty.title}对手，完成第 $number 关")
    }
    fun training(number:Int):Level {
        require(number in 1..9999)
        val mirror=number%2==0;fun square(x:Int,y:Int)=y*9+if(mirror) 8-x else x
        val c=MutableList<Piece?>(90){null}
        c[square(4,9)]=Piece("Red_4",Side.RED,Kind.KING);c[square(4,0)]=Piece("Black_4",Side.BLACK,Kind.KING)
        c[square(2,2)]=Piece("Red_1",Side.RED,Kind.HORSE);c[square(4,5)]=Piece("Red_13",Side.RED,Kind.PAWN);c[square(3,1)]=Piece("Red_0",Side.RED,Kind.ROOK);c[square(5,1)]=Piece("Red_8",Side.RED,Kind.ROOK)
        val p=Position(c);check(Rules.valid(p))
        val solution=Rules.legal(p).firstOrNull {Rules.inCheck(p.after(it),Side.BLACK)&&Rules.legal(p.after(it)).isEmpty()};check(solution!=null)
        return Level(number,Difficulty.BEGINNER,number,p,"一步将死 · 双车马锁宫",solution)
    }
}
