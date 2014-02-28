package miweinst.engine.contraints;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.Arrays;

import cs195n.Vec2f;
import miweinst.engine.shape.CircleShape;
import miweinst.engine.shape.PolygonShape;
import miweinst.engine.shape.Shape;
import miweinst.engine.world.*;

public class PinEntity extends PhysicsEntity {

	private Vec2f _pinOffsetFromCentroid;
	private Vec2f _pinLoc;

	

	public PinEntity(GameWorld world, Vec2f pinLoc, Shape shape) {
		super(world);
		setShape(shape);
		_pinLoc = pinLoc;
		init();
	}
	
	public PinEntity(GameWorld world) {
		super(world);
		_pinLoc = null;
		super.setShape(null);
	}

	@Override
	public void onTick(long _nanosSincePreviousTick) {
		super.onTick(_nanosSincePreviousTick);
		translateToPin();
		setVelocity(new Vec2f(0, 0));
	}

	

	@Override
	public Vec2f getCentroid() {
		return _pinLoc;
	}
	
	@Override
	public float getMomentOfInertia(float mass) {
		float pinOffsetMag = _pinOffsetFromCentroid.mag();
		float oldInertia = super.getMomentOfInertia(getMass());
		return oldInertia + mass*pinOffsetMag*pinOffsetMag;
	}
	
	@Override
	public void setShape(Shape s) {
		assert(s instanceof PolygonShape || s instanceof CircleShape);
		if(s instanceof PolygonShape) {
			super.setShape(s);
		} else {
			_pinLoc = s.getLocation();
		}
		if(_pinLoc != null && getShape() != null) {
			init();
		}
	}


	/**
	 * requires pinLoc to be non-null. Assumes shape is in starting position.
	 */
	private void init() {
		assert(_pinLoc != null);

		Vec2f shapeCentroid = PolygonShape.getCentroidOf(Arrays.asList(((PolygonShape)getShape()).getVertices()));

		//init _pinOffsetFromCentroid
		_pinOffsetFromCentroid = _pinLoc.minus(shapeCentroid);
		this.setGravitational(false);
		this.setDensity(.01f);
		
		



	}

	private void translateToPin() {
		Vec2f shapeCentroid = getShape().getCentroid();
		//calculate the position of where the pin should be relative to the shape
		float theta = getAngle();
		float pinOnShapeX = (float)(Math.cos(theta)*_pinOffsetFromCentroid.x - 
				Math.sin(theta)*_pinOffsetFromCentroid.y);
		float pinOnShapeY = (float)(Math.sin(theta)*_pinOffsetFromCentroid.x + 
				Math.cos(theta)*_pinOffsetFromCentroid.y);
		Vec2f pinRelativeToShape = new Vec2f(pinOnShapeX, pinOnShapeY).plus(shapeCentroid);
		
		Vec2f translation = _pinLoc.minus(pinRelativeToShape);
		this.setLocation(this.getLocation().plus(translation));
	}

	@Override
	public void draw(Graphics2D g) {
		super.draw(g);
		CircleShape circle = new CircleShape(_pinLoc, 1);
		circle.setColor(Color.red);
		circle.draw(g);
		
		CircleShape centroid = new CircleShape(super.getCentroid(), 1);
		centroid.setColor(Color.blue);
		//centroid.draw(g);
		
		
		/*PolygonShape thisShape = (PolygonShape)this.getShape();
		for(Vec2f v : thisShape.getVertices()) {
			CircleShape vCircle = new CircleShape(v, 1);
			vCircle.draw(g);
		}
		 */

	}



}
