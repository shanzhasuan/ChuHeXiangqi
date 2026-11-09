package com.example.immersivexiangqi.p20261105.content

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.util.Log
import com.pico.spatial.core.ecs.*
import com.pico.spatial.core.ecs.resource.*
import com.pico.spatial.core.math.Color4
import com.pico.spatial.core.math.EulerAngles
import com.pico.spatial.core.math.Vector3
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.yield
import org.json.JSONObject

/** Static room geometry is independent of the Editor-authored chess assembly. */
internal class ChessClubRoom private constructor(
    private val room: Entity,
    private val cube: MeshResource,
    private val wood: PhysicallyBasedMaterial
) {
    private fun box(at: Vector3, size: Vector3, material: Material): Entity {
        return ModelEntity(cube, material).also { entity ->
            room.addChild(entity)
            entity.components.get<TransformComponent>()?.apply {
                this.position = at
                scaleVector = size
            }
        }
    }

    /** top is the measured bottom of the loaded chessboard, so both surfaces touch. */
    fun addTable(x: Float, z: Float, width: Float, depth: Float, top: Float) {
        val thickness = 0.065f
        val bottom = top - thickness
        require(bottom > 0f)
        box(Vector3(x, top - thickness / 2f, z), Vector3(width, thickness, depth), wood)
        for (sx in listOf(-1f, 1f)) {
            for (sz in listOf(-1f, 1f)) {
                box(Vector3(x + sx * (width / 2f - 0.10f), bottom / 2f,
                    z + sz * (depth / 2f - 0.10f)), Vector3(0.075f, bottom, 0.075f), wood)
            }
            box(Vector3(x + sx * (width / 2f - 0.09f), bottom - 0.065f, z),
                Vector3(0.045f, 0.13f, depth - 0.16f), wood)
        }
        Log.i("Xiangqi", "chess_table top=$top width=$width depth=$depth")
    }

    companion object {
        suspend fun create(context: Context, owner: Entity): ChessClubRoom {
            val room = Entity()
            owner.addChild(room)
            val data = withContext(Dispatchers.IO) {
                context.assets.open("chessclub/room.json").bufferedReader().use { JSONObject(it.readText()) }
            }
            val textures = withContext(Dispatchers.IO) {
                listOf("walnut", "floor", "plaster").associateWith { name ->
                    TextureResource.load("chessclub/$name.jpg", option = TextureCreateOption())
                } + ("window" to TextureResource.load("chessclub/window.png", option = TextureCreateOption()))
            }
            fun pbr(color: Color4, roughness: Float, texture: TextureResource? = null) =
                PhysicallyBasedMaterial.create().apply {
                    setBaseColor(color)
                    setMetallic(0f)
                    setRoughness(roughness)
                    if (texture != null) setBaseColorTexture(texture)
                }
            fun unlit(color: Color4) = UnlitMaterial.create().apply {
                setBaseColor(color)
            }
            val wood = pbr(Color4(1f, 1f, 1f, 1f), 0.65f, textures.getValue("walnut"))
            val materials: Map<String, Material> = mapOf(
                "wood" to wood,
                "floor" to pbr(Color4(1f, 1f, 1f, 1f), 0.78f, textures.getValue("floor")),
                "plaster" to pbr(Color4(1f, 1f, 1f, 1f), 0.92f, textures.getValue("plaster")),
                "window" to unlit(Color4(1f, 1f, 1f, 1f)).apply {
                    setBaseColorTexture(textures.getValue("window"))
                },
                "dark" to pbr(Color4(0.035f, 0.029f, 0.023f, 1f), 0.8f),
                "lamp" to unlit(Color4(1f, 0.91f, 0.75f, 1f)),
                "book0" to pbr(Color4(0.18f, 0.09f, 0.055f, 1f), 0.9f),
                "book1" to pbr(Color4(0.08f, 0.13f, 0.12f, 1f), 0.9f),
                "book2" to pbr(Color4(0.32f, 0.25f, 0.14f, 1f), 0.9f)
            )
            val cube = MeshResource.createBox(Vector3(1f, 1f, 1f))
            val assembly = ChessClubRoom(room, cube, wood)
            val boxes = data.getJSONArray("boxes")
            fun vector(o: JSONObject, key: String): Vector3 {
                val a = o.getJSONArray(key)
                return Vector3(a.getDouble(0).toFloat(), a.getDouble(1).toFloat(), a.getDouble(2).toFloat())
            }
            for (i in 0 until boxes.length()) {
                val b = boxes.getJSONObject(i)
                assembly.box(vector(b, "position"), vector(b, "size"), materials.getValue(b.getString("material")))
                if (i % 4 == 3) yield()
            }
            val ibl = withContext(Dispatchers.IO) {
                TextureResource.load("chessclub/ambient_lighting.ktx", option = TextureCreateOption().apply {
                    textureEncoding = TextureEncoding.LINEAR
                })
            }
            val environment = Entity().apply {
                components.set(StageEnvironmentLightingComponent(ImageBasedLightSource.Single(ibl), 9f))
            }
            room.addChild(environment)
            val sun = Entity().apply {
                components.set(DirectionalLightComponent(Color4(1f, 0.95f, 0.84f, 1f), 350f, false))
                components.get<TransformComponent>()?.eulerAngles = EulerAngles(pitch = -65f, yaw = -25f)
            }
            room.addChild(sun)
            val fill = Entity().apply {
                components.set(DirectionalLightComponent(Color4(0.88f, 0.94f, 1f, 1f), 90f))
                components.get<TransformComponent>()?.eulerAngles = EulerAngles(pitch = -50f, yaw = 145f)
            }
            room.addChild(fill)
            // Keep the visible lamp geometry, but avoid two additional real-time
            // light sources. IBL and both directional lights retain board lighting.
            // This is a reversible workload reduction, not a tracking diagnosis.
            val bitmap = Bitmap.createBitmap(1024, 256, Bitmap.Config.ARGB_8888)
            val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = 0xffe6cc8e.toInt()
                textAlign = Paint.Align.CENTER
                textSize = 158f
                typeface = Typeface.create("serif", Typeface.BOLD)
            }
            Canvas(bitmap).drawText("楚 河 棋 社", 512f, 128f - (paint.ascent() + paint.descent()) / 2f, paint)
            val sign = ModelEntity(MeshResource.createPlane(1.64f, 0.41f),
                UnlitMaterial.create(BlendingMode.TRANSPARENT).apply {
                    setBaseColor(Color4(1f, 1f, 1f, 1f))
                    setBaseColorTexture(TextureResource.create(bitmap))
                    })
            room.addChild(sign)
            sign.components.get<TransformComponent>()?.position = Vector3(0f, 2f, -4.374f)
            Log.i("Xiangqi", "chessclub_loaded boxes=${boxes.length()} ibl=true directional_lights=2 point_lights=0 lighting_revision=1.3")
            return assembly
        }
    }
}
