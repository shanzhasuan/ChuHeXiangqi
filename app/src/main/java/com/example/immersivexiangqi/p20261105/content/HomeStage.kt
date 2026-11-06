package com.example.immersivexiangqi.p20261105.content

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.example.immersivexiangqi.p20261105.game.*
import com.pico.spatial.core.ecs.*
import com.pico.spatial.core.ecs.resource.*
import com.pico.spatial.core.math.Color4
import com.pico.spatial.core.math.EulerAngles
import com.pico.spatial.core.math.Vector3
import com.pico.spatial.ui.design.PicoTheme
import com.pico.spatial.ui.design.Text
import com.pico.spatial.ui.foundation.content.SpatialView

import com.pico.spatial.ui.foundation.gesture.TargetEntity
import com.pico.spatial.ui.foundation.gesture.detectSpatialTapGesture
import com.pico.spatial.ui.platform.LengthUnit
import com.pico.spatial.ui.platform.LocalPhysicalLengthConverter
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.yield
import kotlinx.coroutines.flow.collect

// Shared board height keeps its table and adjacent panels aligned.
internal const val CHESS_BOARD_CENTER_Y = 0.70f

private data class RecentMarkers(val ring:Entity,val beacon:Entity)

private class ChessSceneState {
    var root:Entity?=null
    var assembly:Entity?=null
    var clubRoom:ChessClubRoom?=null
    var statusPanel:Entity?=null
    var outcomePanel:Entity?=null
    var renderedOutcomeVisible:Boolean?=null
    var renderedStatus:String?=null
    val pieces=mutableMapOf<String,Entity>()
    val cells=mutableListOf<Entity>()
    val legalMarkers=mutableListOf<Entity>()
    val selectedMarkers=mutableListOf<Entity>()
    val targets=mutableMapOf<Long,Int>()
    val recentMarkers=mutableMapOf<Side,RecentMarkers>()
    var renderedPosition:Position?=null
    var renderedSelected:Int?=null
    var renderedHint:Move?=null
    var status by mutableStateOf("正在载入棋室…")
    var ready by mutableStateOf(false)
}

@Composable
fun HomeStage() {
    val context=LocalContext.current
    val scope=rememberCoroutineScope()
    val game=remember {ChessController(context,scope)}
    val sounds=remember {ChessSoundPlayer(context.applicationContext)}
    LaunchedEffect(game,sounds) {
        game.feedbackEvents.collect {event->sounds.play(event){event.epoch==game.feedbackEpoch}}
    }
    val state=remember {ChessSceneState()}
    val converter=LocalPhysicalLengthConverter.current
    var dismissedOutcome by remember {mutableStateOf<Long?>(null)}
    fun panelPixels(width:Float,height:Float):IntSize = with(converter) {
        IntSize(lengthToDp(width,LengthUnit.Meters).roundToPx(),lengthToDp(height,LengthUnit.Meters).roundToPx())
    }
    val panelWidth=converter.lengthToDp(0.78f,LengthUnit.Meters)
    val panelHeight=converter.lengthToDp(1.00f,LengthUnit.Meters)
    val pixels=with(converter){IntSize(panelWidth.roundToPx(),panelHeight.roundToPx())}
    DisposableEffect(Unit) {
        onDispose {sounds.close();game.close();state.root?.destroy();state.root=null;state.clubRoom=null;state.targets.clear();state.pieces.clear();state.recentMarkers.clear()}
    }
    SpatialView(
        modifier=Modifier.fillMaxSize().pointerInput(Unit) {
            detectSpatialTapGesture(context,TargetEntity.any {it.id in state.targets}) {event->
                state.targets[event.targetEntity?.id]?.let {game.tap(it)}
            }
        },
        attachments={
            AttachmentPanel(id="gameStatus",size=panelPixels(0.90f,0.28f)) {
                PicoTheme {BoardStatus(game,state.ready)}
            }
            AttachmentPanel(id="gameOutcome",size=panelPixels(1.02f,0.50f)) {
                PicoTheme {GameEndPanel(game){dismissedOutcome=game.outcomeRevision}}
            }
            AttachmentPanel(id="controls",size=pixels) {
                PicoTheme {
                    Column(Modifier.fillMaxSize()) {
                        if(!state.ready) Text(state.status,modifier=Modifier.padding(16.dp),color=PicoTheme.colorScheme.labelPrimary)
                        Box(Modifier.weight(1f).fillMaxWidth()) {
                            ChessControls(game,playable=state.ready,onShowResult={dismissedOutcome=null})
                        }
                    }
                }
            }
        },
        update=frame@{_,_->
            val showOutcome=state.ready && game.result!=null && dismissedOutcome!=game.outcomeRevision
            if(state.renderedOutcomeVisible!=showOutcome) {
                state.outcomePanel?.enabled=showOutcome
                state.renderedOutcomeVisible=showOutcome
            }
            val status=modeSummary(game)+(game.result?.let { " outcome=${it.winner} ${it.message}" } ?: " turn=${game.position.turn}")
            if(state.renderedStatus!=status) {
                state.statusPanel?.components?.get<AccessibilityComponent>()?.value=status
                state.outcomePanel?.components?.get<AccessibilityComponent>()?.value=game.result?.message ?: "无结束提示"
                state.renderedStatus=status
            }
            if(state.ready) {
                // Native component writes are needed when game state changes, not on every render frame.
                if(state.renderedPosition===game.position && state.renderedSelected==game.selected && state.renderedHint==game.hintMove) return@frame
                state.renderedPosition=game.position
                state.renderedSelected=game.selected
                state.renderedHint=game.hintMove
                val occupied=game.position.cells.mapIndexedNotNull {i,p->p?.let {it.id to i}}.toMap()
                state.pieces.forEach {(id,entity)->
                    val i=occupied[id];entity.enabled=i!=null
                    if(i!=null) entity.components.get<TransformComponent>()?.position=Vector3(-0.48f+(i%9)*0.12f,0.54f-(i/9)*0.12f,0.002f)
                }
                for((side,markers) in state.recentMarkers) {
                    val recent=if(side==Side.RED) game.recentRedMove else game.recentBlackMove
                    val square=recent?.move?.to
                    val visible=recent!=null && square!=null && game.position.cells[square]?.id==recent.piece.id
                    markers.ring.enabled=visible;markers.beacon.enabled=visible
                    if(square!=null && visible) {
                        val x=-0.48f+(square%9)*0.12f;val y=0.54f-(square/9)*0.12f
                        markers.ring.components.get<TransformComponent>()?.position=Vector3(x,y,0.037f)
                        markers.beacon.components.get<TransformComponent>()?.position=Vector3(x+0.047f,y+0.047f,0.055f)
                    }
                }
                val legal=game.legalDestinations
                state.cells.forEachIndexed {i,cell->
                    val p=game.position.cells[i]
                    cell.components.get<AccessibilityComponent>()?.value=(p?.let {"${if(it.side==Side.RED) "红" else "黑"}${it.label}"}?:"空位")+(if(i in legal) "，可落子" else "")+(if(game.selected==i) "，已选择" else "")+(if(game.hintMove?.to==i) "，提示落点" else "")
                    state.legalMarkers[i].enabled=i in legal
                    state.selectedMarkers[i].enabled=game.selected==i||game.hintMove?.to==i
                }
            }
        },
        initial=scene@{content,attachments->
            val owner=Entity();state.root=owner;content.addEntity(owner)
            Log.i("Xiangqi","panel_size dp=${panelWidth.value}x${panelHeight.value} pixels=$pixels")

            attachControls(content,attachments,owner)
            state.statusPanel=attachGameStatus(content,attachments,owner)
            state.outcomePanel=attachGameOutcome(content,attachments,owner)
            // Neutral fallback surrounds the enclosed room; detailed scenery is actual geometry.
            val backdrop=ModelEntity(MeshResource.createSphere(20f),UnlitMaterial.create().apply {
                setBaseColor(Color4(0.18f,0.17f,0.14f,1f))
                setCullingMode(MaterialCullingMode.FRONT)
            })
            owner.addChild(backdrop)
            try {
                state.clubRoom=ChessClubRoom.create(context,owner)
            } catch(e:CancellationException) {
                throw e
            } catch(e:Exception) {
                state.status="棋社环境载入失败，请重新进入"
                Log.e("Xiangqi","chessclub_load_failed",e)
                return@scene
            }
            if("XiangqiStudio.bundle" !in context.assets.list("scenes").orEmpty()) {
                state.status="棋室暂不可用"
                Log.w("Xiangqi","EDITOR_BUNDLE_PENDING scenes/XiangqiStudio.bundle")
            } else {
                var placement:Entity?=null
                try {
                    var bundle:AssetBundle?=null
                    val loaded=try {
                        withContext(Dispatchers.IO) {bundle=AssetBundle.load("asset://scenes/XiangqiStudio.bundle")}
                        val opened=requireNotNull(bundle)
                        check(opened.valid) {"Editor bundle did not open"}
                        // Editor AIPack invokes spatialbundle with scene-dir=Sources (see logs/editor-pack-invocation.log).
                        val modelName="Scenes/XiangqiTable"
                        Log.i("Xiangqi","bundle_opened valid=${opened.valid} model=$modelName")
                        withContext(Dispatchers.IO) {opened.loadModelSuspend(modelName)}.also {Log.i("Xiangqi","bundle_model_loaded") }
                    } finally {bundle?.close()}
                    val mounted=Entity();placement=mounted;owner.addChild(mounted);mounted.addChild(loaded)
                    val board=requireNotNull(loaded.findEntity("Board")){"Missing Editor node Board"}
                    val before=board.getVisualBounds(mounted)
                    // Source positions are Z-up. Read actual loaded bounds before deciding whether the loader already rotated them.
                    if(before.halfExtent.y>before.halfExtent.z*3f) loaded.components.get<TransformComponent>()?.eulerAngles=EulerAngles(pitch=-90f)
                    val after=board.getVisualBounds(mounted)
                    check(after.halfExtent.y<after.halfExtent.z){"Board orientation is unresolved"}
                    mounted.components.get<TransformComponent>()?.position=Vector3(0f,CHESS_BOARD_CENTER_Y,-1.35f)
                    state.clubRoom?.addTable(0f,-1.35f,after.halfExtent.x*2f+0.34f,after.halfExtent.z*2f+0.36f,CHESS_BOARD_CENTER_Y+after.center.y-after.halfExtent.y)
                    val placedBounds=board.getVisualBounds(owner)
                    Log.i("Xiangqi","board_placement center_y=$CHESS_BOARD_CENTER_Y owner_bounds=$placedBounds")
                    val assembly=requireNotNull(loaded.findEntity("ChessAssembly")){"Missing Editor node ChessAssembly"}
                    state.assembly=assembly
                    bindSceneAccessibility(content,assembly)
                    val glyphMesh=MeshResource.createPlane(0.064f,0.064f)
                    val glyphMaterials=mutableMapOf<String,UnlitMaterial>()
                    Position.initial().cells.filterNotNull().forEach {p->
                        val piece=requireNotNull(loaded.findEntity(p.id)){"Missing Editor piece ${p.id}"}
                        state.pieces[p.id]=piece
                        val material=glyphMaterials.getOrPut("${p.side}_${p.label}"){textMaterial(p.label,if(p.side==Side.RED) 0xffa52218.toInt() else 0xff22180e.toInt())}
                        val label=ModelEntity(glyphMesh,material)
                        piece.addChild(label);label.components.get<TransformComponent>()?.position=Vector3(0f,0f,0.0295f)
                        yield()
                    }
                    val river=ModelEntity(MeshResource.createPlane(0.40f,0.075f),textMaterial("楚 河    汉 界",0xff3d2515.toInt(),wide=true))
                    assembly.addChild(river);river.components.get<TransformComponent>()?.position=Vector3(0f,0f,0.003f)
                    val recentMesh=MeshResource.createPlane(0.120f,0.120f)
                    val beaconMesh=MeshResource.createSphere(0.008f)
                    for(side in Side.entries) {
                        val color=if(side==Side.RED) 0xffffb53c.toInt() else 0xff16c6ff.toInt()
                        val ring=ModelEntity(recentMesh,recentMoveMaterial(color))
                        val beacon=ModelEntity(beaconMesh,UnlitMaterial.create().apply {
                            setBaseColor(if(side==Side.RED) Color4(1f,0.67f,0.12f,1f) else Color4(0.08f,0.78f,1f,1f))
                        })
                        assembly.addChild(ring);assembly.addChild(beacon)
                        ring.enabled=false;beacon.enabled=false
                        state.recentMarkers[side]=RecentMarkers(ring,beacon)
                    }
                    val cellMesh=MeshResource.createPlane(0.112f,0.112f)
                    val clear=UnlitMaterial.create(BlendingMode.TRANSPARENT).apply {setBaseColor(Color4(0f,0f,0f,0f))}
                    val markerMesh=MeshResource.createPlane(0.108f,0.108f)
                    val green=ringMaterial(0xff33ad72.toInt());val gold=ringMaterial(0xffffb436.toInt())
                    val hitShape=ShapeResource.createBox(Vector3(0.114f,0.114f,0.004f))
                    val physics=PhysicsMaterialResource()
                    for(i in 0..89) {
                        val cell=createChessCell(cellMesh,clear,hitShape,physics,i)
                        assembly.addChild(cell)
                        state.cells.add(cell);state.targets[cell.id]=i
                        
                        val legal=ModelEntity(markerMesh,green);cell.addChild(legal);legal.enabled=false
                        val selected=ModelEntity(markerMesh,gold);cell.addChild(selected);selected.enabled=false
                        legal.components.get<TransformComponent>()?.position=Vector3(0f,0f,0.002f)
                        selected.components.get<TransformComponent>()?.position=Vector3(0f,0f,0.003f)
                        state.legalMarkers.add(legal);state.selectedMarkers.add(selected)
                        if(i%6==5) yield()
                    }
                    bindGridAccessibility(content,state.cells,game)
                    state.ready=true;state.status="3D棋盘已载入"
                    Log.i("Xiangqi","scene_loaded pieces=${state.pieces.size} cells=${state.cells.size} board_bounds=$after")
                } catch(e:CancellationException) {
                    throw e
                } catch(e:Exception) {
                    placement?.destroy()
                    state.ready=false
                    state.pieces.clear();state.cells.clear();state.targets.clear()
                    state.legalMarkers.clear();state.selectedMarkers.clear();state.assembly=null
                    state.status="棋室载入失败，请稍后重新进入"
                    Log.e("Xiangqi","editor_bundle_load_failed",e)
                }
            }
        }
    )
}

private fun textMaterial(text:String,color:Int,wide:Boolean=false):UnlitMaterial {
    val width=if(wide) 1024 else 256
    val bitmap=Bitmap.createBitmap(width,256,Bitmap.Config.ARGB_8888)
    val paint=Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.color=color;textAlign=Paint.Align.CENTER;textSize=if(wide) 135f else 196f
        typeface=Typeface.create("sans-serif",Typeface.BOLD)
    }
    Canvas(bitmap).drawText(text,width/2f,128f-(paint.ascent()+paint.descent())/2f,paint)
    return UnlitMaterial.create(BlendingMode.TRANSPARENT).apply {setBaseColorTexture(TextureResource.create(bitmap))}
}
private fun ringMaterial(color:Int):UnlitMaterial {
    val bitmap=Bitmap.createBitmap(256,256,Bitmap.Config.ARGB_8888)
    Canvas(bitmap).drawCircle(128f,128f,117f,Paint(Paint.ANTI_ALIAS_FLAG).apply {this.color=color;style=Paint.Style.STROKE;strokeWidth=10f})
    return UnlitMaterial.create(BlendingMode.TRANSPARENT).apply {setBaseColorTexture(TextureResource.create(bitmap))}
}


private fun recentMoveMaterial(color:Int):UnlitMaterial {
    val bitmap=Bitmap.createBitmap(256,256,Bitmap.Config.ARGB_8888)
    val paint=Paint(Paint.ANTI_ALIAS_FLAG).apply {this.color=color;style=Paint.Style.STROKE;strokeWidth=18f}
    Canvas(bitmap).drawCircle(128f,128f,116f,paint)
    return UnlitMaterial.create(BlendingMode.TRANSPARENT).apply {
        setBaseColorTexture(TextureResource.create(bitmap))
    }
}
