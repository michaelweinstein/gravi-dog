package miweinst.engine.shape;

import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;

import cs195n.Vec2f;
import miweinst.engine.beziercurve.BezierCurve;
import miweinst.engine.collisiondetection.ShapeCollisionInfo;
import miweinst.engine.collisiondetection.SeparatingAxis;

	/**
	 * Extends delete_EllipseShape as a guaranteed
	 * circle. Allows collision detection algorithms
	 * to assume that the shape is a perfect circle.
	 * Overrides methods that would manipulate dimensions
	 * of delete_EllipseShape, and adds a setRadius method.
	 * @author miweinst
	 *
	 */

public class CircleShape extends Shape {
	public static final String string = "CircleShape";

	private Ellipse2D.Float _circle;
	private float _radius;
	private ShapeCollisionInfo _collisionInfo;
	//Takes radius (float) in constructor, not Dimensions
	public CircleShape(Vec2f loc, float radius) {
		super(loc, new Vec2f(2*radius, 2*radius));
		_circle = new Ellipse2D.Float(loc.x, loc.y, 2*radius, 2*radius);
		super.setShape(_circle);
		_radius = radius;
		_collisionInfo = null;
	}
	
	public float getRadius() {
		return _radius;
	}
	/*Only circle-specific method.*/
	public void setRadius(float radius) {
		this.setDimensions(new Vec2f(2*radius, 2*radius));
		_radius = radius;
	}
	
	@Override
	public void setWidth(float w) {
		this.setDimensions(new Vec2f(w, w));
		_radius = w/2;
	}
	@Override
	public void setHeight(float h) {
		this.setDimensions(new Vec2f(h, h));
		_radius = h/2;
	}
	@Override
	public void setDimensions(Vec2f dim) {
		if (dim.x == dim.y) {
			super.setDimensions(dim);
			_radius = dim.x/2;
		}
		else System.out.println(dim + "Method Override: Use Circle.setRadius instead... (Circle.setDimensions)");
	}
	
	/* Clamps point c to the bounds of specified
	 * axis-aligned rectangle. Vec2f c is usually center
	 * of circle. Was used in circle-aab collisions before
	 * switching to using Polygon-Circle algorithm to 
	 * calculate MTV with recycled code. 
	 * 
	 * Currently unused by collision detection methods.*/
	public Vec2f clampToRect(Vec2f c, AARectShape aab) {
		float px = c.x;
		float py = c.y;
		//Clamp point values to min and max
		if (c.x < aab.getMinX()) px = aab.getMinX();
		if (c.x > aab.getMaxX()) px = aab.getMaxX();
		if (c.y < aab.getMinY()) py = aab.getMinY();
		if (c.y > aab.getMaxY()) py = aab.getMaxY();
		Vec2f clampedCenter = new Vec2f(px, py);
		
		return clampedCenter;
	}
	
	@Override
	public Vec2f getCentroid() {
		return new Vec2f(this.getX(), this.getY());
	}
	
	@Override
	public void draw(Graphics2D g) {
		_circle.setFrame(super.getX()-_radius, super.getY()-_radius, super.getWidth(), super.getHeight());
		super.draw(g);
	}
		
	/*Implements algorithm for point-circle collision. 
	 * Returns true if point is contained in circle. */
	@Override
	public boolean contains(Vec2f v) {
		float dist = this.getCentroid().dist(v);
		if (dist < this.getRadius()) {
			return true;
		}
		else return false;
	}
	
	@Override
	public ShapeCollisionInfo getCollisionInfo() {
		return _collisionInfo;
	}
	@Override
	public void setCollisionInfo(ShapeCollisionInfo info) {
		_collisionInfo = info;
	}
	
	//Implementation of Collision Detection shapeCollisionDetection; double dispatch
	
	@Override
	public boolean collides(Shape s) {
		return s.collidesCircle(this);
	}

	/*Mtv is parallel to line between circles' centers*/
	@Override
	public boolean collidesCircle(CircleShape c) {
		this.setCollisionInfo(null);
		c.setCollisionInfo(null);
		
		//Distance between centers
		float dist = this.getCentroid().dist(c.getCentroid());
		float sumRad = this.getRadius() + c.getRadius();
		Vec2f dir = c.getCentroid().minus(this.getCentroid());
		Vec2f mtv = dir.normalized().smult(dist - sumRad);
		//If distance b/w centers is less than sum rads
		if (dist < sumRad) {
			this.setCollisionInfo(new ShapeCollisionInfo(this, c, mtv));
			c.setCollisionInfo(new ShapeCollisionInfo(c, this, mtv.smult(-1)));
			return true;
		}
		else {
			return false;
		}
	}

	@Override
	public boolean collidesAAB(AARectShape aab) {	
		//Convert aab to PolygonShape for collision algorithm
		PolygonShape poly = aab.rectToPoly();	
		boolean collision = poly.collidesCircle(this);
		//Pass ShapeCollisionInfo from equivalent Polygon to original AARect
		aab.setCollisionInfo(poly.getCollisionInfo());
		return collision;			
	}

	/*Reuse Compound-Circle collision code in CompoundShape*/
	@Override
	public boolean collidesCompound(CompoundShape c) {
		return c.collidesCircle(this);
	}

	/*Reuse Polygon-Circle collision code in PolygonShape*/
	@Override
	public boolean collidesPolygon(PolygonShape p) {
		return p.collidesCircle(this);
	}

	@Override
	public Vec2f projectOnto(SeparatingAxis sep) {
		return sep.project(this);
	}
	
	@Override
	public boolean collidesCurve(BezierCurve c) {
		return c.collidesCircle(this);
	}
	

	@Override
	public float getMomentOfInertia(float mass) {
		return .5f * mass*_radius*_radius;
	}

	@Override
	public float getArea() {
		return (float) (Math.PI*_radius*_radius);
	}

	@Override
	public Vec2f poi(Shape s) {
		return s.poiCircle(this);
	}

	@Override
	public Vec2f poiPolygon(PolygonShape p) {
		return p.poiCircle(this);
	}

	@Override
	public Vec2f poiCircle(CircleShape c) {
		Vec2f dir = c.getLocation().minus(getLocation());
		float ratio = c._radius/(c._radius + this._radius);
		return getLocation().plus(dir.smult(ratio));
	}

	@Override
	public Vec2f poiCurve(BezierCurve c) {
		return c.poiCircle(this);
	}
}
