package miweinst.engine.contraints;

import miweinst.engine.shape.AARectShape;
import miweinst.engine.shape.PolygonShape;
import miweinst.engine.shape.Shape;
import miweinst.engine.world.GameWorld;
import miweinst.engine.world.PhysicsEntity;
import miweinst.gravidog.Player;
import cs195n.Vec2f;

public class SpringEntity extends PhysicsEntity {

	private Vec2f _pivot;
	private float _springConstant;
	private float _frictionConstant;
	
	public SpringEntity(GameWorld world) {
		super(world);		
		//Default Shape for mandatory constructor
		PolygonShape shape = new AARectShape(new Vec2f(50f,80f), new Vec2f(10f, 10f)).rectToPoly();
		setShape(shape);
		setSpringConstant(600f*Player.GRAVITY.mag());
		setFrictionConstant(30f*Player.GRAVITY.mag());
		this.setDensity(.01f);
		init();
	}
	public SpringEntity(GameWorld world, Shape shape) {
		super(world);
		setShape(shape);
		init();
	}

	@Override
	public void onTick(long nanosSincePreviousTick) {
		super.onTick(nanosSincePreviousTick);
		applyRestorativeForce();
		applyFriction();		
	}
	
	private void applyRestorativeForce() {
		Vec2f displacementFromPivot = _pivot.minus(getLocation());
		float restorativeForce = displacementFromPivot.smult(_springConstant).mag();
		Vec2f dir = displacementFromPivot.normalized();
		applyForce(dir.smult(restorativeForce), this.getCentroid());
	}
	
	private void applyFriction() {
		float force = getVelocity().mag() * _frictionConstant;
		Vec2f dir = getVelocity().invert().normalized();
		applyForce(dir.smult(force), this.getCentroid());
	}
	
	@Override
	public void setShape(Shape s) {
		super.setShape(s);
		setLocation(s.getLocation());
		_pivot = s.getCentroid();
	}
	
	
	/**
	 * requires shape to be set
	 */
	private void init() {
		_pivot = getShape().getCentroid();
		setRotatable(false);
	}
	
	public void setSpringConstant(float springConstant) {
		_springConstant = springConstant;
	}
	
	public void setFrictionConstant(float frictionConstant) {
		_frictionConstant = frictionConstant;
	}
			

}
