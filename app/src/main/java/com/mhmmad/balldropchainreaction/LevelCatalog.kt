package com.mhmmad.balldropchainreaction

import kotlin.math.abs
import kotlin.math.atan
import kotlin.math.cos

enum class Tool(val asset: String) { PLANK("plank"), DOMINO("domino"), SPRING("spring"), SEESAW("seesaw"), PIPE("tube") }
data class Segment(val x: Float, val y: Float, val length: Float, val angle: Float)
data class Prop(val kind: Tool, val x: Float, val y: Float, val angle: Float = 0f)
data class Placed(val kind: Tool, val x: Float, val y: Float, val angle: Float = 0f)
data class Level(val id: Int, val world: Int, val name: String, val spawnX: Float, val spawnY: Float,
                 val targetX: Float, val targetY: Float, val ramps: List<Segment>,
                 val props: List<Prop>, val inventory: List<Tool>, val hint: Placed)

/** Physically continuous sloped tracks with a player-supplied bridge.
 * Outcomes must be verified in PhysicsAuditTest before declaring levels solved.
 */
object LevelCatalog {
    val worldNames = listOf("Gravity Basics", "Domino Reactions", "Spring Mechanics", "Moving Machines", "Chain Master")
    private val names = listOf(
        "First Fall", "Bridge the Gap", "Steeper Slide", "Long Crossing",
        "Domino Arrival", "Topple Together", "Domino Descent", "Tipping Point",
        "Bounce One", "Spring Valley", "High Bounce", "Spring Timing",
        "Lever Lift", "Pendulum Push", "Machine Crossing", "Mechanical Loop",
        "Grand Cascade", "Chain Timing", "Triple Mechanism", "Chain Master"
    )
    val levels: List<Level> = (1..20).map { id ->
        val world = (id-1) / 4
        val grade = .135f + (id % 5)*.015f
        val startY = 7.15f + (id % 4)*.12f
        val gapMid = 6.5f + (id % 4)*.25f
        val gapLeft = gapMid - 1.26f
        val gapRight = gapMid + 1.26f
        val angle = Math.toDegrees(-atan(grade.toDouble())).toFloat()
        fun railY(x: Float) = startY - grade*(x-.75f)
        fun ramp(x0: Float,x1: Float): Segment {
            val xc=(x0+x1)/2f
            val length=(x1-x0)/cos(Math.toRadians(angle.toDouble())).toFloat()
            return Segment(xc,railY(xc),length,angle)
        }
        val targetX = 14.15f
        val ramps = listOf(ramp(.55f,gapLeft+.10f),ramp(gapRight-.10f,15.5f))
        val props = when(world) {
            0 -> emptyList()
            1 -> listOf(Prop(Tool.DOMINO,11.45f,railY(11.45f)+.60f))
            2 -> listOf(Prop(Tool.SPRING,11.5f,railY(11.5f)-.55f))
            3 -> listOf(Prop(Tool.SEESAW,10.7f,railY(10.7f)-1.5f))
            else -> listOf(Prop(Tool.DOMINO,12.4f,railY(12.4f)+.60f),Prop(Tool.SPRING,9.4f,railY(9.4f)-.60f))
        }
        val inventory = when(world) {
            0 -> listOf(Tool.PLANK)
            1 -> listOf(Tool.PLANK,Tool.DOMINO)
            2 -> listOf(Tool.PLANK,Tool.SPRING)
            3 -> listOf(Tool.PLANK,Tool.SEESAW)
            else -> listOf(Tool.PLANK,Tool.DOMINO,Tool.SPRING)
        }
        val hint=Placed(Tool.PLANK,gapMid,railY(gapMid),angle)
        Level(id,world,names[id-1],.95f,railY(.95f)+.54f,
            targetX,railY(targetX)+.39f,ramps,props,inventory,hint)
    }
    init {
        check(levels.size==20 && levels.map{it.id}.distinct().size==20)
        levels.forEach { level ->
            check(level.hint.kind in level.inventory)
            check(level.spawnY in 1f..8.5f && level.targetY in 1f..8.5f)
            check(level.ramps.all { it.length>0 && abs(it.angle)<45 })
        }
    }
}
