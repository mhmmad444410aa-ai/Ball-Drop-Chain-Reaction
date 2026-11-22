package com.mhmmad.balldropchainreaction

import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.roundToInt

/** Uses the same 1/4 metre coordinate snapping as the actual hint button. */
class PhysicsAuditTest {
    private fun snapped(h: Placed): Placed =
        h.copy(x=(h.x*4).roundToInt()/4f, y=(h.y*4).roundToInt()/4f)

    @Test fun allTwentyHintsReachGoalWithRealPhysics() {
        val failures=mutableListOf<String>()
        val results=mutableListOf<String>()
        for(level in LevelCatalog.levels) {
            val scene=PhysicsScene(level,listOf(snapped(level.hint)))
            scene.run()
            var maxX=scene.ballPosition().x
            repeat(60*24) {
                scene.step()
                val p=scene.ballPosition()
                assertTrue("Nonfinite position in level ${level.id}",p.x.isFinite()&&p.y.isFinite())
                maxX=maxOf(maxX,p.x)
            }
            val p=scene.ballPosition()
            val result="L${level.id}=${scene.status} x=${"%.2f".format(p.x)} y=${"%.2f".format(p.y)} maxX=${"%.2f".format(maxX)} t=${"%.1f".format(scene.time)}"
            results+=result
            if(scene.status!=PhysicsScene.Status.WON) failures+=result
            scene.destroy()
        }
        println("PHYSICS_AUDIT ${results.joinToString(" | ")}")
        assertTrue("Unsolved hinted levels: ${failures.joinToString("; ")}",failures.isEmpty())
    }
    @Test fun missingBridgeCannotCompleteStageOne() {
        val level=LevelCatalog.levels.first()
        val scene=PhysicsScene(level)
        scene.run()
        repeat(60*24){scene.step()}
        println("PHYSICS_NO_BRIDGE status=${scene.status} x=${scene.ballPosition().x}")
        assertTrue("Level 1 must require placing the plank",scene.status!=PhysicsScene.Status.WON)
        scene.destroy()
    }
    @Test fun gravityAcceleratesBall() {
        val scene=PhysicsScene(LevelCatalog.levels.first())
        val start=scene.ballPosition().y
        scene.run()
        assertTrue(scene.world.gravity.y<0f)
        repeat(3){scene.step()}
        assertTrue(kotlin.math.abs(scene.ballPosition().y-start)>.00001f)
        scene.destroy()
    }
}
