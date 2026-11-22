package com.mhmmad.balldropchainreaction

import org.jbox2d.collision.shapes.CircleShape
import org.jbox2d.collision.shapes.PolygonShape
import org.jbox2d.common.Vec2
import org.jbox2d.dynamics.Body
import org.jbox2d.dynamics.BodyDef
import org.jbox2d.dynamics.BodyType
import org.jbox2d.dynamics.FixtureDef
import org.jbox2d.dynamics.World
import org.jbox2d.dynamics.joints.RevoluteJointDef
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.hypot

data class Actor(val body: Body, val image: String, val width: Float, val height: Float)

/** Real rigid-body physics using JBox2D. All coordinates use metres, positive Y up. */
class PhysicsScene(val level: Level, val placed: List<Placed> = emptyList()) {
    var world: World = World(Vec2(0f, -10.4f))
        private set
    val actors = mutableListOf<Actor>()
    private val springs = mutableListOf<Body>()
    private lateinit var ball: Body
    var time = 0f
        private set
    var status = Status.READY
        private set
    var collisions = 0
        private set
    private var lastYVelocity = 0f
    var lastImpactX = -10f
        private set
    var lastImpactY = -10f
        private set
    var lastImpactTime = -10f
        private set
    private val springCooldown = hashMapOf<Body, Float>()

    enum class Status { READY, RUNNING, PAUSED, WON, LOST }

    init { buildWorld() }

    private fun staticRect(x: Float, y: Float, width: Float, height: Float, angle: Float = 0f,
                           friction: Float = .75f, bounce: Float = .15f): Body {
        val def = BodyDef().apply { type = BodyType.STATIC; position.set(x,y); this.angle = angle }
        val body = world.createBody(def)
        val shape = PolygonShape().apply { setAsBox(width/2, height/2) }
        body.createFixture(FixtureDef().apply { this.shape = shape; density = 0f; this.friction=friction; restitution=bounce })
        return body
    }
    private fun dynamicRect(x: Float,y: Float,w: Float,h: Float,angle: Float=0f, massFactor: Float=1f): Body {
        val body = world.createBody(BodyDef().apply { type=BodyType.DYNAMIC; position.set(x,y); this.angle=angle; angularDamping=.6f; linearDamping=.02f })
        val shape = PolygonShape().apply { setAsBox(w/2,h/2) }
        body.createFixture(FixtureDef().apply { this.shape=shape; density=massFactor; friction=.64f; restitution=.12f })
        return body
    }
    private fun buildWorld() {
        // Safety bounds and floor: avoid bodies falling forever or being lost numerically.
        staticRect(8f,.25f,16f,.5f)
        staticRect(-.2f,4.5f,.4f,9f)
        staticRect(16.2f,4.5f,.4f,9f)
        level.ramps.forEach { ramp ->
            val body=staticRect(ramp.x,ramp.y,ramp.length,.22f,radians(ramp.angle))
            actors += Actor(body,"plank_long",ramp.length,.38f)
        }
        for (p in level.props) addProp(p.kind,p.x,p.y,p.angle)
        for (p in placed) addProp(p.kind,p.x,p.y,p.angle)
        val body=world.createBody(BodyDef().apply { type=BodyType.DYNAMIC; position.set(level.spawnX,level.spawnY); bullet=true; linearDamping=.01f; angularDamping=.02f })
        val circle=CircleShape().apply { m_radius=.26f }
        body.createFixture(FixtureDef().apply { shape=circle; density=1.3f; friction=.36f; restitution=.42f })
        ball=body
        actors += Actor(body, when(level.id%5){0->"ball_purple";1->"ball_red";2->"ball_yellow";3->"ball_green";else->"ball_blue"},.60f,.60f)
    }
    private fun addProp(tool: Tool,x: Float,y: Float,angle: Float) {
        when(tool) {
            Tool.PLANK -> actors += Actor(staticRect(x,y,2.6f,.22f,radians(angle)),"plank",2.6f,.42f)
            Tool.DOMINO -> actors += Actor(dynamicRect(x,y,.22f,.86f,radians(angle),.15f),"domino",.31f,.92f)
            Tool.SPRING -> {
                val b=staticRect(x,y,.86f,.40f,0f,bounce=.65f)
                springs+=b; actors+=Actor(b,"spring",.94f,.70f)
            }
            Tool.SEESAW -> {
                val base=staticRect(x,y-.24f,.35f,.42f)
                actors+=Actor(base,"gear",.52f,.52f)
                val board=dynamicRect(x,y+.12f,2.25f,.18f,radians(angle),.65f)
                val joint=RevoluteJointDef()
                joint.initialize(base,board,Vec2(x,y+.10f))
                joint.enableLimit=true; joint.lowerAngle=-.48f; joint.upperAngle=.48f
                world.createJoint(joint)
                actors+=Actor(board,"plank_long",2.4f,.4f)
            }
            Tool.PIPE -> {
                actors+=Actor(staticRect(x,y-0.7f,.24f,1.65f),"tube",.40f,1.8f)
                actors+=Actor(staticRect(x+.95f,y-0.7f,.24f,1.65f),"tube",.40f,1.8f)
            }
        }
    }
    fun run() { if(status==Status.READY||status==Status.PAUSED) status=Status.RUNNING }
    fun pause() { if(status==Status.RUNNING) status=Status.PAUSED }
    /** Fixed 1/60 second step. Call no more than four times per frame (render loop limit). */
    fun step() {
        if(status!=Status.RUNNING) return
        world.step(1f/60f,8,3)
        time += 1f/60f
        springs.forEach { spring ->
            val dy=ball.position.y-spring.position.y
            val dx=abs(ball.position.x-spring.position.x)
            val v=ball.linearVelocity
            if(dx<.62f && dy in .25f..1.0f && v.y<.7f && time-(springCooldown[spring] ?: -9f)>.35f) {
                ball.applyLinearImpulse(Vec2(0f,ball.mass*2.4f),ball.worldCenter)
                springCooldown[spring]=time
            }
        }
        val vy=ball.linearVelocity.y
        if(vy-lastYVelocity > 1.8f) {
            collisions++
            lastImpactX=ball.position.x
            lastImpactY=ball.position.y
            lastImpactTime=time
        }
        lastYVelocity=vy
        // Entering the goal is a real geometric condition on the dynamic ball.
        val distance=hypot(ball.position.x-level.targetX, ball.position.y-level.targetY)
        if(distance < .53f) status=Status.WON
        if(time>23f || (time>2f && abs(ball.linearVelocity.x)<.035f && abs(vy)<.035f && distance>1.0f)) status=Status.LOST
    }
    fun score(): Int = when {
        placed.size <= 1 && time < 12f -> 3
        placed.size <= 2 && time < 18f -> 2
        else -> 1
    }
    fun ballPosition(): Vec2 = Vec2(ball.position.x,ball.position.y)
    fun springCompression(body: Body): Float =
        ((.20f-(time-(springCooldown[body] ?: -10f)))/.20f).coerceIn(0f,1f)
    fun destroy() { actors.clear(); springs.clear(); springCooldown.clear() }
    companion object { fun radians(deg: Float) = (deg*PI/180f).toFloat() }
}
