package com.mhmmad.balldropchainreaction

import org.junit.Assert.assertTrue
import org.junit.Test

/** Numeric stability smoke test; NOT proof that all puzzles have valid solutions. */
class PhysicsAuditTest {
    @Test fun allTwentyScenesStayFinite() {
        var successes = 0
        val terminal = mutableMapOf<Int, PhysicsScene.Status>()
        for (level in LevelCatalog.levels) {
            val scene = PhysicsScene(level, listOf(level.hint))
            scene.run()
            repeat(60 * 24) {
                scene.step()
                val position = scene.ballPosition()
                assertTrue("Non-finite physics X in level ${level.id}", position.x.isFinite())
                assertTrue("Non-finite physics Y in level ${level.id}", position.y.isFinite())
            }
            if (scene.status == PhysicsScene.Status.WON) successes++
            terminal[level.id] = scene.status
            scene.destroy()
        }
        println("PHYSICS_AUDIT simulated=20, solved_with_single_hint=$successes, terminal=$terminal")
        assertTrue("Unexpected missing test catalog", terminal.size == 20)
    }
    @Test fun gravityAcceleratesBall() {
        val scene = PhysicsScene(LevelCatalog.levels.first())
        val start = scene.ballPosition().y
        scene.run()
        repeat(15) { scene.step() }
        assertTrue("Gravity must act on the ball", scene.ballPosition().y < start)
        scene.destroy()
    }
}
