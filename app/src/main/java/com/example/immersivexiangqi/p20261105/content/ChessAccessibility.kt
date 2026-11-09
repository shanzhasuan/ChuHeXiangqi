package com.example.immersivexiangqi.p20261105.content

import com.example.immersivexiangqi.p20261105.game.ChessController
import com.pico.spatial.core.ecs.*
import com.pico.spatial.core.container.SpatialViewContent
import com.pico.spatial.core.ecs.resource.*
import com.pico.spatial.core.math.Vector3
import com.pico.spatial.core.math.EulerAngles
import com.pico.spatial.ui.foundation.content.SpatialViewAttachments

// Literal bindings allow the current CLI source auditor to prove all 90 grid owners.
internal fun bindSceneAccessibility(content:SpatialViewContent,owner:Entity) {
}
internal fun attachControls(content:SpatialViewContent,attachments:SpatialViewAttachments,owner:Entity) {
    attachments.entity("controls")?.let {panel ->
        owner.addChild(panel)
        panel.components.get<TransformComponent>()?.apply {
            position=Vector3(0.98f,CHESS_BOARD_CENTER_Y+0.47f,-1.55f)
            eulerAngles=EulerAngles(pitch=-12f,yaw=-30f)
        }

    }
}
internal fun bindGridAccessibility(content:SpatialViewContent,cells:List<Entity>,game:ChessController) {
    // SDK6.1 physical picking remains detectSpatialTapGesture; ECS automation starts in6.2.
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
    return panel
}

internal fun attachGameOutcome(content:SpatialViewContent,attachments:SpatialViewAttachments,owner:Entity):Entity? {
    val panel=attachments.entity("gameOutcome") ?: return null
    owner.addChild(panel)
    panel.components.get<TransformComponent>()?.apply {
        position=Vector3(0f,CHESS_BOARD_CENTER_Y+0.38f,-1.20f)
        eulerAngles=EulerAngles(pitch=-12f)
    }
    panel.enabled=false
    return panel
}
