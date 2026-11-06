package com.example.immersivexiangqi.p20261105.content

import com.example.immersivexiangqi.p20261105.game.ChessController
import com.pico.spatial.core.ecs.*
import com.pico.spatial.core.container.SpatialViewContent
import com.pico.spatial.core.ecs.resource.*
import com.pico.spatial.core.math.Vector3
import com.pico.spatial.core.math.EulerAngles
import com.pico.spatial.ui.foundation.content.setAccessibilityActions
import com.pico.spatial.ui.foundation.content.SpatialViewAttachments

// Literal bindings allow the current CLI source auditor to prove all 90 grid owners.
internal fun bindSceneAccessibility(content:SpatialViewContent,owner:Entity) {
    owner.components.set(AccessibilityComponent(label="楚河象棋场景",value="沉浸式棋室").apply {isAccessibilityElement=true})
    content.setAccessibilityActions(entity=owner,semanticId="xiangqi.scene") {}
}
internal fun attachControls(content:SpatialViewContent,attachments:SpatialViewAttachments,owner:Entity) {
    attachments.entity("controls")?.let {panel ->
        owner.addChild(panel)
        panel.components.get<TransformComponent>()?.apply {
            position=Vector3(0.98f,CHESS_BOARD_CENTER_Y+0.47f,-1.55f)
            eulerAngles=EulerAngles(pitch=-12f,yaw=-30f)
        }

        panel.components.set(AccessibilityComponent(label="象棋控制面板",value="对局、难度与规则").apply {isAccessibilityElement=true})
        content.setAccessibilityActions(entity=panel,semanticId="xiangqi.controls") {}
    }
}
internal fun bindGridAccessibility(content:SpatialViewContent,cells:List<Entity>,game:ChessController) {
    val cell0=cells[0]
    cell0.components.set(AccessibilityComponent(label="棋盘交点1路1行",value="空位").apply {isAccessibilityElement=true})
    content.setAccessibilityActions(entity=cell0,semanticId="xiangqi.cell.0") { onClick(label="选择交点"){game.tap(0)} }

    val cell1=cells[1]
    cell1.components.set(AccessibilityComponent(label="棋盘交点2路1行",value="空位").apply {isAccessibilityElement=true})
    content.setAccessibilityActions(entity=cell1,semanticId="xiangqi.cell.1") { onClick(label="选择交点"){game.tap(1)} }

    val cell2=cells[2]
    cell2.components.set(AccessibilityComponent(label="棋盘交点3路1行",value="空位").apply {isAccessibilityElement=true})
    content.setAccessibilityActions(entity=cell2,semanticId="xiangqi.cell.2") { onClick(label="选择交点"){game.tap(2)} }

    val cell3=cells[3]
    cell3.components.set(AccessibilityComponent(label="棋盘交点4路1行",value="空位").apply {isAccessibilityElement=true})
    content.setAccessibilityActions(entity=cell3,semanticId="xiangqi.cell.3") { onClick(label="选择交点"){game.tap(3)} }

    val cell4=cells[4]
    cell4.components.set(AccessibilityComponent(label="棋盘交点5路1行",value="空位").apply {isAccessibilityElement=true})
    content.setAccessibilityActions(entity=cell4,semanticId="xiangqi.cell.4") { onClick(label="选择交点"){game.tap(4)} }

    val cell5=cells[5]
    cell5.components.set(AccessibilityComponent(label="棋盘交点6路1行",value="空位").apply {isAccessibilityElement=true})
    content.setAccessibilityActions(entity=cell5,semanticId="xiangqi.cell.5") { onClick(label="选择交点"){game.tap(5)} }

    val cell6=cells[6]
    cell6.components.set(AccessibilityComponent(label="棋盘交点7路1行",value="空位").apply {isAccessibilityElement=true})
    content.setAccessibilityActions(entity=cell6,semanticId="xiangqi.cell.6") { onClick(label="选择交点"){game.tap(6)} }

    val cell7=cells[7]
    cell7.components.set(AccessibilityComponent(label="棋盘交点8路1行",value="空位").apply {isAccessibilityElement=true})
    content.setAccessibilityActions(entity=cell7,semanticId="xiangqi.cell.7") { onClick(label="选择交点"){game.tap(7)} }

    val cell8=cells[8]
    cell8.components.set(AccessibilityComponent(label="棋盘交点9路1行",value="空位").apply {isAccessibilityElement=true})
    content.setAccessibilityActions(entity=cell8,semanticId="xiangqi.cell.8") { onClick(label="选择交点"){game.tap(8)} }

    val cell9=cells[9]
    cell9.components.set(AccessibilityComponent(label="棋盘交点1路2行",value="空位").apply {isAccessibilityElement=true})
    content.setAccessibilityActions(entity=cell9,semanticId="xiangqi.cell.9") { onClick(label="选择交点"){game.tap(9)} }

    val cell10=cells[10]
    cell10.components.set(AccessibilityComponent(label="棋盘交点2路2行",value="空位").apply {isAccessibilityElement=true})
    content.setAccessibilityActions(entity=cell10,semanticId="xiangqi.cell.10") { onClick(label="选择交点"){game.tap(10)} }

    val cell11=cells[11]
    cell11.components.set(AccessibilityComponent(label="棋盘交点3路2行",value="空位").apply {isAccessibilityElement=true})
    content.setAccessibilityActions(entity=cell11,semanticId="xiangqi.cell.11") { onClick(label="选择交点"){game.tap(11)} }

    val cell12=cells[12]
    cell12.components.set(AccessibilityComponent(label="棋盘交点4路2行",value="空位").apply {isAccessibilityElement=true})
    content.setAccessibilityActions(entity=cell12,semanticId="xiangqi.cell.12") { onClick(label="选择交点"){game.tap(12)} }

    val cell13=cells[13]
    cell13.components.set(AccessibilityComponent(label="棋盘交点5路2行",value="空位").apply {isAccessibilityElement=true})
    content.setAccessibilityActions(entity=cell13,semanticId="xiangqi.cell.13") { onClick(label="选择交点"){game.tap(13)} }

    val cell14=cells[14]
    cell14.components.set(AccessibilityComponent(label="棋盘交点6路2行",value="空位").apply {isAccessibilityElement=true})
    content.setAccessibilityActions(entity=cell14,semanticId="xiangqi.cell.14") { onClick(label="选择交点"){game.tap(14)} }

    val cell15=cells[15]
    cell15.components.set(AccessibilityComponent(label="棋盘交点7路2行",value="空位").apply {isAccessibilityElement=true})
    content.setAccessibilityActions(entity=cell15,semanticId="xiangqi.cell.15") { onClick(label="选择交点"){game.tap(15)} }

    val cell16=cells[16]
    cell16.components.set(AccessibilityComponent(label="棋盘交点8路2行",value="空位").apply {isAccessibilityElement=true})
    content.setAccessibilityActions(entity=cell16,semanticId="xiangqi.cell.16") { onClick(label="选择交点"){game.tap(16)} }

    val cell17=cells[17]
    cell17.components.set(AccessibilityComponent(label="棋盘交点9路2行",value="空位").apply {isAccessibilityElement=true})
    content.setAccessibilityActions(entity=cell17,semanticId="xiangqi.cell.17") { onClick(label="选择交点"){game.tap(17)} }

    val cell18=cells[18]
    cell18.components.set(AccessibilityComponent(label="棋盘交点1路3行",value="空位").apply {isAccessibilityElement=true})
    content.setAccessibilityActions(entity=cell18,semanticId="xiangqi.cell.18") { onClick(label="选择交点"){game.tap(18)} }

    val cell19=cells[19]
    cell19.components.set(AccessibilityComponent(label="棋盘交点2路3行",value="空位").apply {isAccessibilityElement=true})
    content.setAccessibilityActions(entity=cell19,semanticId="xiangqi.cell.19") { onClick(label="选择交点"){game.tap(19)} }

    val cell20=cells[20]
    cell20.components.set(AccessibilityComponent(label="棋盘交点3路3行",value="空位").apply {isAccessibilityElement=true})
    content.setAccessibilityActions(entity=cell20,semanticId="xiangqi.cell.20") { onClick(label="选择交点"){game.tap(20)} }

    val cell21=cells[21]
    cell21.components.set(AccessibilityComponent(label="棋盘交点4路3行",value="空位").apply {isAccessibilityElement=true})
    content.setAccessibilityActions(entity=cell21,semanticId="xiangqi.cell.21") { onClick(label="选择交点"){game.tap(21)} }

    val cell22=cells[22]
    cell22.components.set(AccessibilityComponent(label="棋盘交点5路3行",value="空位").apply {isAccessibilityElement=true})
    content.setAccessibilityActions(entity=cell22,semanticId="xiangqi.cell.22") { onClick(label="选择交点"){game.tap(22)} }

    val cell23=cells[23]
    cell23.components.set(AccessibilityComponent(label="棋盘交点6路3行",value="空位").apply {isAccessibilityElement=true})
    content.setAccessibilityActions(entity=cell23,semanticId="xiangqi.cell.23") { onClick(label="选择交点"){game.tap(23)} }

    val cell24=cells[24]
    cell24.components.set(AccessibilityComponent(label="棋盘交点7路3行",value="空位").apply {isAccessibilityElement=true})
    content.setAccessibilityActions(entity=cell24,semanticId="xiangqi.cell.24") { onClick(label="选择交点"){game.tap(24)} }

    val cell25=cells[25]
    cell25.components.set(AccessibilityComponent(label="棋盘交点8路3行",value="空位").apply {isAccessibilityElement=true})
    content.setAccessibilityActions(entity=cell25,semanticId="xiangqi.cell.25") { onClick(label="选择交点"){game.tap(25)} }

    val cell26=cells[26]
    cell26.components.set(AccessibilityComponent(label="棋盘交点9路3行",value="空位").apply {isAccessibilityElement=true})
    content.setAccessibilityActions(entity=cell26,semanticId="xiangqi.cell.26") { onClick(label="选择交点"){game.tap(26)} }

    val cell27=cells[27]
    cell27.components.set(AccessibilityComponent(label="棋盘交点1路4行",value="空位").apply {isAccessibilityElement=true})
    content.setAccessibilityActions(entity=cell27,semanticId="xiangqi.cell.27") { onClick(label="选择交点"){game.tap(27)} }

    val cell28=cells[28]
    cell28.components.set(AccessibilityComponent(label="棋盘交点2路4行",value="空位").apply {isAccessibilityElement=true})
    content.setAccessibilityActions(entity=cell28,semanticId="xiangqi.cell.28") { onClick(label="选择交点"){game.tap(28)} }

    val cell29=cells[29]
    cell29.components.set(AccessibilityComponent(label="棋盘交点3路4行",value="空位").apply {isAccessibilityElement=true})
    content.setAccessibilityActions(entity=cell29,semanticId="xiangqi.cell.29") { onClick(label="选择交点"){game.tap(29)} }

    val cell30=cells[30]
    cell30.components.set(AccessibilityComponent(label="棋盘交点4路4行",value="空位").apply {isAccessibilityElement=true})
    content.setAccessibilityActions(entity=cell30,semanticId="xiangqi.cell.30") { onClick(label="选择交点"){game.tap(30)} }

    val cell31=cells[31]
    cell31.components.set(AccessibilityComponent(label="棋盘交点5路4行",value="空位").apply {isAccessibilityElement=true})
    content.setAccessibilityActions(entity=cell31,semanticId="xiangqi.cell.31") { onClick(label="选择交点"){game.tap(31)} }

    val cell32=cells[32]
    cell32.components.set(AccessibilityComponent(label="棋盘交点6路4行",value="空位").apply {isAccessibilityElement=true})
    content.setAccessibilityActions(entity=cell32,semanticId="xiangqi.cell.32") { onClick(label="选择交点"){game.tap(32)} }

    val cell33=cells[33]
    cell33.components.set(AccessibilityComponent(label="棋盘交点7路4行",value="空位").apply {isAccessibilityElement=true})
    content.setAccessibilityActions(entity=cell33,semanticId="xiangqi.cell.33") { onClick(label="选择交点"){game.tap(33)} }

    val cell34=cells[34]
    cell34.components.set(AccessibilityComponent(label="棋盘交点8路4行",value="空位").apply {isAccessibilityElement=true})
    content.setAccessibilityActions(entity=cell34,semanticId="xiangqi.cell.34") { onClick(label="选择交点"){game.tap(34)} }

    val cell35=cells[35]
    cell35.components.set(AccessibilityComponent(label="棋盘交点9路4行",value="空位").apply {isAccessibilityElement=true})
    content.setAccessibilityActions(entity=cell35,semanticId="xiangqi.cell.35") { onClick(label="选择交点"){game.tap(35)} }

    val cell36=cells[36]
    cell36.components.set(AccessibilityComponent(label="棋盘交点1路5行",value="空位").apply {isAccessibilityElement=true})
    content.setAccessibilityActions(entity=cell36,semanticId="xiangqi.cell.36") { onClick(label="选择交点"){game.tap(36)} }

    val cell37=cells[37]
    cell37.components.set(AccessibilityComponent(label="棋盘交点2路5行",value="空位").apply {isAccessibilityElement=true})
    content.setAccessibilityActions(entity=cell37,semanticId="xiangqi.cell.37") { onClick(label="选择交点"){game.tap(37)} }

    val cell38=cells[38]
    cell38.components.set(AccessibilityComponent(label="棋盘交点3路5行",value="空位").apply {isAccessibilityElement=true})
    content.setAccessibilityActions(entity=cell38,semanticId="xiangqi.cell.38") { onClick(label="选择交点"){game.tap(38)} }

    val cell39=cells[39]
    cell39.components.set(AccessibilityComponent(label="棋盘交点4路5行",value="空位").apply {isAccessibilityElement=true})
    content.setAccessibilityActions(entity=cell39,semanticId="xiangqi.cell.39") { onClick(label="选择交点"){game.tap(39)} }

    val cell40=cells[40]
    cell40.components.set(AccessibilityComponent(label="棋盘交点5路5行",value="空位").apply {isAccessibilityElement=true})
    content.setAccessibilityActions(entity=cell40,semanticId="xiangqi.cell.40") { onClick(label="选择交点"){game.tap(40)} }

    val cell41=cells[41]
    cell41.components.set(AccessibilityComponent(label="棋盘交点6路5行",value="空位").apply {isAccessibilityElement=true})
    content.setAccessibilityActions(entity=cell41,semanticId="xiangqi.cell.41") { onClick(label="选择交点"){game.tap(41)} }

    val cell42=cells[42]
    cell42.components.set(AccessibilityComponent(label="棋盘交点7路5行",value="空位").apply {isAccessibilityElement=true})
    content.setAccessibilityActions(entity=cell42,semanticId="xiangqi.cell.42") { onClick(label="选择交点"){game.tap(42)} }

    val cell43=cells[43]
    cell43.components.set(AccessibilityComponent(label="棋盘交点8路5行",value="空位").apply {isAccessibilityElement=true})
    content.setAccessibilityActions(entity=cell43,semanticId="xiangqi.cell.43") { onClick(label="选择交点"){game.tap(43)} }

    val cell44=cells[44]
    cell44.components.set(AccessibilityComponent(label="棋盘交点9路5行",value="空位").apply {isAccessibilityElement=true})
    content.setAccessibilityActions(entity=cell44,semanticId="xiangqi.cell.44") { onClick(label="选择交点"){game.tap(44)} }

    val cell45=cells[45]
    cell45.components.set(AccessibilityComponent(label="棋盘交点1路6行",value="空位").apply {isAccessibilityElement=true})
    content.setAccessibilityActions(entity=cell45,semanticId="xiangqi.cell.45") { onClick(label="选择交点"){game.tap(45)} }

    val cell46=cells[46]
    cell46.components.set(AccessibilityComponent(label="棋盘交点2路6行",value="空位").apply {isAccessibilityElement=true})
    content.setAccessibilityActions(entity=cell46,semanticId="xiangqi.cell.46") { onClick(label="选择交点"){game.tap(46)} }

    val cell47=cells[47]
    cell47.components.set(AccessibilityComponent(label="棋盘交点3路6行",value="空位").apply {isAccessibilityElement=true})
    content.setAccessibilityActions(entity=cell47,semanticId="xiangqi.cell.47") { onClick(label="选择交点"){game.tap(47)} }

    val cell48=cells[48]
    cell48.components.set(AccessibilityComponent(label="棋盘交点4路6行",value="空位").apply {isAccessibilityElement=true})
    content.setAccessibilityActions(entity=cell48,semanticId="xiangqi.cell.48") { onClick(label="选择交点"){game.tap(48)} }

    val cell49=cells[49]
    cell49.components.set(AccessibilityComponent(label="棋盘交点5路6行",value="空位").apply {isAccessibilityElement=true})
    content.setAccessibilityActions(entity=cell49,semanticId="xiangqi.cell.49") { onClick(label="选择交点"){game.tap(49)} }

    val cell50=cells[50]
    cell50.components.set(AccessibilityComponent(label="棋盘交点6路6行",value="空位").apply {isAccessibilityElement=true})
    content.setAccessibilityActions(entity=cell50,semanticId="xiangqi.cell.50") { onClick(label="选择交点"){game.tap(50)} }

    val cell51=cells[51]
    cell51.components.set(AccessibilityComponent(label="棋盘交点7路6行",value="空位").apply {isAccessibilityElement=true})
    content.setAccessibilityActions(entity=cell51,semanticId="xiangqi.cell.51") { onClick(label="选择交点"){game.tap(51)} }

    val cell52=cells[52]
    cell52.components.set(AccessibilityComponent(label="棋盘交点8路6行",value="空位").apply {isAccessibilityElement=true})
    content.setAccessibilityActions(entity=cell52,semanticId="xiangqi.cell.52") { onClick(label="选择交点"){game.tap(52)} }

    val cell53=cells[53]
    cell53.components.set(AccessibilityComponent(label="棋盘交点9路6行",value="空位").apply {isAccessibilityElement=true})
    content.setAccessibilityActions(entity=cell53,semanticId="xiangqi.cell.53") { onClick(label="选择交点"){game.tap(53)} }

    val cell54=cells[54]
    cell54.components.set(AccessibilityComponent(label="棋盘交点1路7行",value="空位").apply {isAccessibilityElement=true})
    content.setAccessibilityActions(entity=cell54,semanticId="xiangqi.cell.54") { onClick(label="选择交点"){game.tap(54)} }

    val cell55=cells[55]
    cell55.components.set(AccessibilityComponent(label="棋盘交点2路7行",value="空位").apply {isAccessibilityElement=true})
    content.setAccessibilityActions(entity=cell55,semanticId="xiangqi.cell.55") { onClick(label="选择交点"){game.tap(55)} }

    val cell56=cells[56]
    cell56.components.set(AccessibilityComponent(label="棋盘交点3路7行",value="空位").apply {isAccessibilityElement=true})
    content.setAccessibilityActions(entity=cell56,semanticId="xiangqi.cell.56") { onClick(label="选择交点"){game.tap(56)} }

    val cell57=cells[57]
    cell57.components.set(AccessibilityComponent(label="棋盘交点4路7行",value="空位").apply {isAccessibilityElement=true})
    content.setAccessibilityActions(entity=cell57,semanticId="xiangqi.cell.57") { onClick(label="选择交点"){game.tap(57)} }

    val cell58=cells[58]
    cell58.components.set(AccessibilityComponent(label="棋盘交点5路7行",value="空位").apply {isAccessibilityElement=true})
    content.setAccessibilityActions(entity=cell58,semanticId="xiangqi.cell.58") { onClick(label="选择交点"){game.tap(58)} }

    val cell59=cells[59]
    cell59.components.set(AccessibilityComponent(label="棋盘交点6路7行",value="空位").apply {isAccessibilityElement=true})
    content.setAccessibilityActions(entity=cell59,semanticId="xiangqi.cell.59") { onClick(label="选择交点"){game.tap(59)} }

    val cell60=cells[60]
    cell60.components.set(AccessibilityComponent(label="棋盘交点7路7行",value="空位").apply {isAccessibilityElement=true})
    content.setAccessibilityActions(entity=cell60,semanticId="xiangqi.cell.60") { onClick(label="选择交点"){game.tap(60)} }

    val cell61=cells[61]
    cell61.components.set(AccessibilityComponent(label="棋盘交点8路7行",value="空位").apply {isAccessibilityElement=true})
    content.setAccessibilityActions(entity=cell61,semanticId="xiangqi.cell.61") { onClick(label="选择交点"){game.tap(61)} }

    val cell62=cells[62]
    cell62.components.set(AccessibilityComponent(label="棋盘交点9路7行",value="空位").apply {isAccessibilityElement=true})
    content.setAccessibilityActions(entity=cell62,semanticId="xiangqi.cell.62") { onClick(label="选择交点"){game.tap(62)} }

    val cell63=cells[63]
    cell63.components.set(AccessibilityComponent(label="棋盘交点1路8行",value="空位").apply {isAccessibilityElement=true})
    content.setAccessibilityActions(entity=cell63,semanticId="xiangqi.cell.63") { onClick(label="选择交点"){game.tap(63)} }

    val cell64=cells[64]
    cell64.components.set(AccessibilityComponent(label="棋盘交点2路8行",value="空位").apply {isAccessibilityElement=true})
    content.setAccessibilityActions(entity=cell64,semanticId="xiangqi.cell.64") { onClick(label="选择交点"){game.tap(64)} }

    val cell65=cells[65]
    cell65.components.set(AccessibilityComponent(label="棋盘交点3路8行",value="空位").apply {isAccessibilityElement=true})
    content.setAccessibilityActions(entity=cell65,semanticId="xiangqi.cell.65") { onClick(label="选择交点"){game.tap(65)} }

    val cell66=cells[66]
    cell66.components.set(AccessibilityComponent(label="棋盘交点4路8行",value="空位").apply {isAccessibilityElement=true})
    content.setAccessibilityActions(entity=cell66,semanticId="xiangqi.cell.66") { onClick(label="选择交点"){game.tap(66)} }

    val cell67=cells[67]
    cell67.components.set(AccessibilityComponent(label="棋盘交点5路8行",value="空位").apply {isAccessibilityElement=true})
    content.setAccessibilityActions(entity=cell67,semanticId="xiangqi.cell.67") { onClick(label="选择交点"){game.tap(67)} }

    val cell68=cells[68]
    cell68.components.set(AccessibilityComponent(label="棋盘交点6路8行",value="空位").apply {isAccessibilityElement=true})
    content.setAccessibilityActions(entity=cell68,semanticId="xiangqi.cell.68") { onClick(label="选择交点"){game.tap(68)} }

    val cell69=cells[69]
    cell69.components.set(AccessibilityComponent(label="棋盘交点7路8行",value="空位").apply {isAccessibilityElement=true})
    content.setAccessibilityActions(entity=cell69,semanticId="xiangqi.cell.69") { onClick(label="选择交点"){game.tap(69)} }

    val cell70=cells[70]
    cell70.components.set(AccessibilityComponent(label="棋盘交点8路8行",value="空位").apply {isAccessibilityElement=true})
    content.setAccessibilityActions(entity=cell70,semanticId="xiangqi.cell.70") { onClick(label="选择交点"){game.tap(70)} }

    val cell71=cells[71]
    cell71.components.set(AccessibilityComponent(label="棋盘交点9路8行",value="空位").apply {isAccessibilityElement=true})
    content.setAccessibilityActions(entity=cell71,semanticId="xiangqi.cell.71") { onClick(label="选择交点"){game.tap(71)} }

    val cell72=cells[72]
    cell72.components.set(AccessibilityComponent(label="棋盘交点1路9行",value="空位").apply {isAccessibilityElement=true})
    content.setAccessibilityActions(entity=cell72,semanticId="xiangqi.cell.72") { onClick(label="选择交点"){game.tap(72)} }

    val cell73=cells[73]
    cell73.components.set(AccessibilityComponent(label="棋盘交点2路9行",value="空位").apply {isAccessibilityElement=true})
    content.setAccessibilityActions(entity=cell73,semanticId="xiangqi.cell.73") { onClick(label="选择交点"){game.tap(73)} }

    val cell74=cells[74]
    cell74.components.set(AccessibilityComponent(label="棋盘交点3路9行",value="空位").apply {isAccessibilityElement=true})
    content.setAccessibilityActions(entity=cell74,semanticId="xiangqi.cell.74") { onClick(label="选择交点"){game.tap(74)} }

    val cell75=cells[75]
    cell75.components.set(AccessibilityComponent(label="棋盘交点4路9行",value="空位").apply {isAccessibilityElement=true})
    content.setAccessibilityActions(entity=cell75,semanticId="xiangqi.cell.75") { onClick(label="选择交点"){game.tap(75)} }

    val cell76=cells[76]
    cell76.components.set(AccessibilityComponent(label="棋盘交点5路9行",value="空位").apply {isAccessibilityElement=true})
    content.setAccessibilityActions(entity=cell76,semanticId="xiangqi.cell.76") { onClick(label="选择交点"){game.tap(76)} }

    val cell77=cells[77]
    cell77.components.set(AccessibilityComponent(label="棋盘交点6路9行",value="空位").apply {isAccessibilityElement=true})
    content.setAccessibilityActions(entity=cell77,semanticId="xiangqi.cell.77") { onClick(label="选择交点"){game.tap(77)} }

    val cell78=cells[78]
    cell78.components.set(AccessibilityComponent(label="棋盘交点7路9行",value="空位").apply {isAccessibilityElement=true})
    content.setAccessibilityActions(entity=cell78,semanticId="xiangqi.cell.78") { onClick(label="选择交点"){game.tap(78)} }

    val cell79=cells[79]
    cell79.components.set(AccessibilityComponent(label="棋盘交点8路9行",value="空位").apply {isAccessibilityElement=true})
    content.setAccessibilityActions(entity=cell79,semanticId="xiangqi.cell.79") { onClick(label="选择交点"){game.tap(79)} }

    val cell80=cells[80]
    cell80.components.set(AccessibilityComponent(label="棋盘交点9路9行",value="空位").apply {isAccessibilityElement=true})
    content.setAccessibilityActions(entity=cell80,semanticId="xiangqi.cell.80") { onClick(label="选择交点"){game.tap(80)} }

    val cell81=cells[81]
    cell81.components.set(AccessibilityComponent(label="棋盘交点1路10行",value="空位").apply {isAccessibilityElement=true})
    content.setAccessibilityActions(entity=cell81,semanticId="xiangqi.cell.81") { onClick(label="选择交点"){game.tap(81)} }

    val cell82=cells[82]
    cell82.components.set(AccessibilityComponent(label="棋盘交点2路10行",value="空位").apply {isAccessibilityElement=true})
    content.setAccessibilityActions(entity=cell82,semanticId="xiangqi.cell.82") { onClick(label="选择交点"){game.tap(82)} }

    val cell83=cells[83]
    cell83.components.set(AccessibilityComponent(label="棋盘交点3路10行",value="空位").apply {isAccessibilityElement=true})
    content.setAccessibilityActions(entity=cell83,semanticId="xiangqi.cell.83") { onClick(label="选择交点"){game.tap(83)} }

    val cell84=cells[84]
    cell84.components.set(AccessibilityComponent(label="棋盘交点4路10行",value="空位").apply {isAccessibilityElement=true})
    content.setAccessibilityActions(entity=cell84,semanticId="xiangqi.cell.84") { onClick(label="选择交点"){game.tap(84)} }

    val cell85=cells[85]
    cell85.components.set(AccessibilityComponent(label="棋盘交点5路10行",value="空位").apply {isAccessibilityElement=true})
    content.setAccessibilityActions(entity=cell85,semanticId="xiangqi.cell.85") { onClick(label="选择交点"){game.tap(85)} }

    val cell86=cells[86]
    cell86.components.set(AccessibilityComponent(label="棋盘交点6路10行",value="空位").apply {isAccessibilityElement=true})
    content.setAccessibilityActions(entity=cell86,semanticId="xiangqi.cell.86") { onClick(label="选择交点"){game.tap(86)} }

    val cell87=cells[87]
    cell87.components.set(AccessibilityComponent(label="棋盘交点7路10行",value="空位").apply {isAccessibilityElement=true})
    content.setAccessibilityActions(entity=cell87,semanticId="xiangqi.cell.87") { onClick(label="选择交点"){game.tap(87)} }

    val cell88=cells[88]
    cell88.components.set(AccessibilityComponent(label="棋盘交点8路10行",value="空位").apply {isAccessibilityElement=true})
    content.setAccessibilityActions(entity=cell88,semanticId="xiangqi.cell.88") { onClick(label="选择交点"){game.tap(88)} }

    val cell89=cells[89]
    cell89.components.set(AccessibilityComponent(label="棋盘交点9路10行",value="空位").apply {isAccessibilityElement=true})
    content.setAccessibilityActions(entity=cell89,semanticId="xiangqi.cell.89") { onClick(label="选择交点"){game.tap(89)} }
}
internal fun createChessCell(mesh:MeshResource,material:UnlitMaterial,shape:ShapeResource,physics:PhysicsMaterialResource,i:Int):Entity {
    val cell=ModelEntity(mesh,material).apply {
        components.set(CollisionComponent(collisionShape=listOf(shape),physicsMaterial=physics))
        components.set(InteractableComponent())
        
        components.get<TransformComponent>()?.position=Vector3(-0.48f+(i%9)*0.12f,0.54f-(i/9)*0.12f,0.034f)
    }
    return cell
}



internal fun attachGameStatus(content:SpatialViewContent,attachments:SpatialViewAttachments,owner:Entity):Entity? {
    val panel=attachments.entity("gameStatus") ?: return null
    owner.addChild(panel)
    panel.components.get<TransformComponent>()?.apply {
        position=Vector3(0f,CHESS_BOARD_CENTER_Y+0.26f,-2.05f)
        eulerAngles=EulerAngles(pitch=-15f)
    }
    panel.components.set(AccessibilityComponent(label="棋盘上方对局状态",value="模式、难度、关卡和回合").apply {isAccessibilityElement=true})
    content.setAccessibilityActions(entity=panel,semanticId="xiangqi.status") {}
    return panel
}

internal fun attachGameOutcome(content:SpatialViewContent,attachments:SpatialViewAttachments,owner:Entity):Entity? {
    val panel=attachments.entity("gameOutcome") ?: return null
    owner.addChild(panel)
    panel.components.get<TransformComponent>()?.apply {
        position=Vector3(0f,CHESS_BOARD_CENTER_Y+0.38f,-1.20f)
        eulerAngles=EulerAngles(pitch=-12f)
    }
    panel.components.set(AccessibilityComponent(label="象棋结束提示面板",value="无结束提示").apply {isAccessibilityElement=true})
    content.setAccessibilityActions(entity=panel,semanticId="xiangqi.outcome") {}
    panel.enabled=false
    return panel
}
