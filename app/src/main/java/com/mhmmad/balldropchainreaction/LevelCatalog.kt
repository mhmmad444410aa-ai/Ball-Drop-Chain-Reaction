package com.mhmmad.balldropchainreaction

import kotlin.math.abs

enum class Tool(val asset: String) {
    PLANK("plank"), DOMINO("domino"), SPRING("spring"), SEESAW("seesaw"), PIPE("tube")
}

data class Segment(val x: Float, val y: Float, val length: Float, val angle: Float)
data class Prop(val kind: Tool, val x: Float, val y: Float, val angle: Float = 0f)
data class Placed(val kind: Tool, val x: Float, val y: Float, val angle: Float = 0f)
data class Level(
    val id: Int, val world: Int, val name: String, val spawnX: Float, val spawnY: Float,
    val targetX: Float, val targetY: Float, val ramps: List<Segment>,
    val props: List<Prop>, val inventory: List<Tool>, val hint: Placed
)

/** Hand-authored puzzle descriptions. Solutions are suggestions, NOT yet physics-verified. */
object LevelCatalog {
    val worldNames = listOf("Gravity Basics", "Domino Reactions", "Spring Mechanics", "Moving Machines", "Chain Master")
    private fun s(x: Number,y: Number,len: Number,a: Number) = Segment(x.toFloat(),y.toFloat(),len.toFloat(),a.toFloat())
    private fun p(kind: Tool,x: Number,y: Number,a: Number=0) = Prop(kind,x.toFloat(),y.toFloat(),a.toFloat())
    private fun h(kind: Tool,x: Number,y: Number,a: Number=0) = Placed(kind,x.toFloat(),y.toFloat(),a.toFloat())
    val levels: List<Level> = listOf(
        Level(1,0,"First Fall", 1.1f,7.2f, 14.1f,2.2f,
            listOf(s(2.4,6.8,3.2,-12),s(8.6,4.4,3.6,-15),s(13.1,2.7,3.8,-12)), emptyList(), listOf(Tool.PLANK),h(Tool.PLANK,5.5,5.55,-19)),
        Level(2,0,"Bridge the Gap", 1.1f,7.4f, 14.0f,2.4f,
            listOf(s(2.9,7.0,4.1,-10),s(8.6,4.85,2.7,-18),s(12.6,3.1,4.4,-13)), emptyList(),listOf(Tool.PLANK,Tool.PLANK),h(Tool.PLANK,6.0,5.9,-14)),
        Level(3,0,"Corner Drop", 1.5f,7.4f, 13.8f,1.9f,
            listOf(s(2.7,6.7,2.6,-22),s(9.5,4.15,4.1,-10),s(13.4,2.1,3,-9)),listOf(p(Tool.PIPE,6.0,5.3)),listOf(Tool.PLANK),h(Tool.PLANK,7.25,5.1,-17)),
        Level(4,0,"Double Slope", 1.2f,7.5f, 14.3f,2.1f,
            listOf(s(3.0,7.0,4,-15),s(8.9,4.5,3,-22),s(12.7,2.75,4,-7)), emptyList(),listOf(Tool.PLANK,Tool.PLANK),h(Tool.PLANK,6.1,5.7,-10)),
        Level(5,1,"Domino Arrival", 1.2f,7.3f, 14.2f,2.2f,
            listOf(s(2.7,6.7,3.8,-12),s(9.0,4.7,3.7,-16),s(13.2,2.65,3.5,-12)),listOf(p(Tool.DOMINO,7.4,5.15),p(Tool.DOMINO,8.1,5.15)), listOf(Tool.PLANK,Tool.DOMINO),h(Tool.PLANK,5.6,5.55,-20)),
        Level(6,1,"Topple Together", 1.2f,7.25f,14.0f,1.9f,
            listOf(s(2.5,6.8,3.4,-16),s(8.5,4.7,4,-8),s(13.0,2.5,4.1,-10)),listOf(p(Tool.DOMINO,9.3,5.3),p(Tool.DOMINO,10.0,5.3)),listOf(Tool.PLANK,Tool.DOMINO),h(Tool.PLANK,5.35,5.55,-12)),
        Level(7,1,"Domino Descent", 1.2f,7.3f,14.2f,2.0f,
            listOf(s(3,6.9,4,-14),s(8.8,4.65,3.2,-19),s(13.3,2.6,3.7,-12)),listOf(p(Tool.DOMINO,8.2,5.2),p(Tool.DOMINO,9.3,5.25)),listOf(Tool.PLANK,Tool.DOMINO,Tool.DOMINO),h(Tool.PLANK,5.8,5.5,-14)),
        Level(8,1,"Tipping Point", 1.2f,7.4f,14.0f,2.1f,
            listOf(s(2.9,6.75,3.6,-17),s(8.6,4.6,4.2,-17),s(13.1,2.65,4,-12)),listOf(p(Tool.DOMINO,8.1,5.12),p(Tool.DOMINO,8.8,5.02),p(Tool.DOMINO,9.5,4.95)),listOf(Tool.PLANK,Tool.SEESAW),h(Tool.PLANK,5.5,5.5,-16)),
        Level(9,2,"Bounce One", 1.1f,7.1f,14.2f,2.3f,
            listOf(s(2.7,6.6,3.8,-16),s(8.6,4.65,3.2,-15),s(13.2,2.65,3.8,-12)), listOf(p(Tool.SPRING,6.9,4.8)),listOf(Tool.PLANK,Tool.SPRING),h(Tool.PLANK,5.4,5.65,-12)),
        Level(10,2,"Spring Valley",1.3f,7.6f,14.0f,2.15f,
            listOf(s(2.7,6.85,3.9,-18),s(9.0,4.45,3.7,-15),s(13.15,2.7,4,-12)), listOf(p(Tool.SPRING,10.7,3.3)),listOf(Tool.PLANK,Tool.SPRING),h(Tool.PLANK,5.6,5.45,-13)),
        Level(11,2,"High Bounce",1.0f,7.25f,14.2f,2.4f,
            listOf(s(2.8,6.7,3.6,-14),s(8.8,4.65,3.5,-12),s(13.2,2.85,3.7,-10)),listOf(p(Tool.SPRING,7.4,4.75)),listOf(Tool.PLANK,Tool.SPRING),h(Tool.PLANK,5.6,5.55,-11)),
        Level(12,2,"Spring Timing",1.2f,7.5f,14.3f,2.0f,
            listOf(s(2.9,6.85,4.0,-17),s(9.2,4.5,3.8,-18),s(13.25,2.55,3.8,-10)),listOf(p(Tool.SPRING,10.3,3.7)),listOf(Tool.PLANK,Tool.SPRING,Tool.DOMINO),h(Tool.PLANK,5.7,5.45,-14)),
        Level(13,3,"Lever Lift",1.2f,7.3f,14.1f,2.2f,
            listOf(s(2.75,6.7,3.6,-14),s(9,4.7,3.5,-13),s(13,2.8,4,-10)),listOf(p(Tool.SEESAW,8.0,4.4)),listOf(Tool.PLANK,Tool.SEESAW),h(Tool.PLANK,5.55,5.55,-14)),
        Level(14,3,"Pendulum Push",1.2f,7.3f,14.1f,1.9f,
            listOf(s(2.7,6.65,3.8,-16),s(8.7,4.65,3.6,-16),s(13,2.4,4,-12)),listOf(p(Tool.SEESAW,11.0,3.3)),listOf(Tool.PLANK,Tool.DOMINO),h(Tool.PLANK,5.55,5.5,-16)),
        Level(15,3,"Machine Crossing",1.4f,7.3f,14.0f,2.1f,
            listOf(s(2.9,6.7,3.6,-18),s(8.6,4.5,3.9,-10),s(13,2.6,4,-10)),listOf(p(Tool.PIPE,9.1,5.6),p(Tool.SEESAW,11.0,3.8)),listOf(Tool.PLANK,Tool.SPRING),h(Tool.PLANK,5.7,5.4,-13)),
        Level(16,3,"Mechanical Loop",1.25f,7.4f,14.1f,1.8f,
            listOf(s(2.7,6.75,3.9,-16),s(8.7,4.55,3.7,-15),s(13,2.4,4,-10)),listOf(p(Tool.SEESAW,9.6,4.2),p(Tool.DOMINO,10.9,3.5)),listOf(Tool.PLANK,Tool.SPRING,Tool.DOMINO),h(Tool.PLANK,5.65,5.5,-14)),
        Level(17,4,"Grand Cascade",1.25f,7.45f,14.2f,1.9f,
            listOf(s(2.9,6.8,3.7,-16),s(8.8,4.55,3.9,-13),s(13.1,2.45,3.8,-11)),listOf(p(Tool.SPRING,6.8,5.0),p(Tool.DOMINO,8.2,5.2),p(Tool.SEESAW,11.2,3.6)),listOf(Tool.PLANK,Tool.DOMINO,Tool.SPRING),h(Tool.PLANK,5.65,5.5,-14)),
        Level(18,4,"Chain Timing",1.3f,7.4f,14.1f,2.0f,
            listOf(s(2.8,6.75,3.8,-17),s(8.8,4.6,3.9,-14),s(13.2,2.5,3.7,-14)),listOf(p(Tool.DOMINO,9.2,5.1),p(Tool.SPRING,10.5,3.7),p(Tool.SEESAW,11.6,3.35)),listOf(Tool.PLANK,Tool.DOMINO,Tool.SEESAW),h(Tool.PLANK,5.7,5.45,-13)),
        Level(19,4,"Triple Mechanism",1.2f,7.4f,14.2f,1.95f,
            listOf(s(2.9,6.75,4,-13),s(9.0,4.55,3.8,-18),s(13.3,2.45,3.7,-11)),listOf(p(Tool.DOMINO,8.1,5.2),p(Tool.SPRING,10.6,3.6),p(Tool.SEESAW,12,2.75)),listOf(Tool.PLANK,Tool.SPRING,Tool.DOMINO),h(Tool.PLANK,5.8,5.5,-15)),
        Level(20,4,"Chain Master",1.1f,7.45f,14.3f,2.0f,
            listOf(s(2.85,6.75,3.9,-17),s(8.9,4.6,3.8,-17),s(13.15,2.5,4,-13)),listOf(p(Tool.SPRING,6.6,4.85),p(Tool.DOMINO,8.1,5.15),p(Tool.SEESAW,10.8,3.65),p(Tool.PIPE,12.1,3.8)),listOf(Tool.PLANK,Tool.SEESAW,Tool.SPRING),h(Tool.PLANK,5.65,5.45,-14))
    )
    init {
        check(levels.size == 20 && levels.map { it.id }.distinct().size == 20)
        levels.forEach { level ->
            check(level.id in 1..20 && level.world in 0..4)
            check(level.inventory.isNotEmpty() && level.hint.kind in level.inventory)
            check(level.ramps.isNotEmpty() && level.spawnX < level.targetX)
            check(level.ramps.all { it.length > 0 && abs(it.angle) < 60 })
        }
    }
}
