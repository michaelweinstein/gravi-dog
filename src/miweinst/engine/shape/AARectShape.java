package miweinst.engine.shape;

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.util.Arrays;

import cs195n.Vec2f;
import miweinst.engine.beziercurve.BezierCurve;
import miweinst.engine.collisiondetection.ShapeCollisionInfo;
import miweinst.engine.collisiondetection.SeparatingAxis;

/**
 * Axis-Aligned Rectangle Shape. Ensures that
 * this Rect cannot set rotation in superclass
 * Shape. Its own class so methods involved in 
 * algorithms like collision detection can 'know'
 * for certain that the incoming Rect is Axis-Aligned.
 * @author miweinst
 */

public class AARectShape extends Shape {
	public static final String string = "AARectShape";

	private Rectangle2D _rect;
	private ShapeCollisionInfo _collisionInfo;

	public AARectShape(Vec2f loc, Vec2f dim) {
		super(loc, dim);
		_rect = new Rectangle2D.Float(loc.x, loc.y, dim.x, dim.y);
		super.setShape(_rect);
		
		_collisionInfo = null;
	}
	
	/*Returns Vec2f storage of point at center*/
	public Vec2f getCenter() {
		return new Vec2f((float)_rect.getCenterX(), (float)_rect.getCenterY());
	}
	
	/*Returns the Maximum and Minimum X and Y values on frame. 
	 * Based on location set to upper left of rect.*/
	public float getMaxX() {
		return this.getX()+this.getWidth();
	}
	public float getMaxY() {
		return this.getY()+this.getHeight();
	}
	public float getMinX() {
		return this.getX();
	}
	public float getMinY() {
		return this.getY();
	}
	
	/*Returns a copy of this shape. Useful for repetitive features*/
	public Rectangle2D.Float clone() {
		return (Rectangle2D.Float) _rect.clone();
	}
	
	/* Returns PolygonShape object with same vertices as this rectangle.
	 * Used for many collision methods
	 * in order to reuse code for collidesPolygon. Turning AARect
	 * into Polygon allows Polygon collision algorithm to be recycled;
	 * not as efficient, because uses two extra axes, but more stable for now.*/
	public PolygonShape rectToPoly() {
		return rectToPoly(true);
/*		//Array of vertices in counter-clockwise order
		Vec2f upperRight = new Vec2f(this.getMaxX(), this.getMaxY());
		Vec2f upperLeft = new Vec2f(this.getMinX(), this.getMaxY());
		Vec2f lowerLeft = new Vec2f(this.getMinX(), this.getMinY());
		Vec2f lowerRight = new Vec2f(this.getMaxX(), this.getMinY());
		Vec2f[] verts = new Vec2f[4];
		verts[0] = upperRight;
		verts[1] = upperLeft;
		verts[2] = lowerLeft;
		verts[3] = lowerRight;	
		PolygonShape poly = new PolygonShape(PolygonShape.getCentroidOf(Arrays.asList(verts)), verts);
		return poly;*/
	}
	
	public PolygonShape rectToPoly(boolean mathCoords) {
		Vec2f[] verts = new Vec2f[4];
		if (mathCoords) {
			//Array of vertices in counter-clockwise order
			Vec2f upperRight = new Vec2f(this.getMaxX(), this.getMaxY());
			Vec2f upperLeft = new Vec2f(this.getMinX(), this.getMaxY());
			Vec2f lowerLeft = new Vec2f(this.getMinX(), this.getMinY());
			Vec2f lowerRight = new Vec2f(this.getMaxX(), this.getMinY());
			verts[0] = upperRight;
			verts[1] = upperLeft;
			verts[2] = lowerLeft;
			verts[3] = lowerRight;	
		}
		else {
			//Array of vertices in counter-clockwise order
			Vec2f upperRight = new Vec2f(this.getMaxX(), this.getMinY());
			Vec2f upperLeft = new Vec2f(this.getMinX(), this.getMinY());
			Vec2f lowerLeft = new Vec2f(this.getMinX(), this.getMaxY());
			Vec2f lowerRight = new Vec2f(this.getMaxX(), this.getMaxY());
			verts[0] = upperRight;
			verts[1] = upperLeft;
			verts[2] = lowerLeft;
			verts[3] = lowerRight;	
		}
		PolygonShape poly = new PolygonShape(PolygonShape.getCentroidOf(Arrays.asList(verts)), verts);
		return poly;
	}
	
	/*Returns whether point is within AA rectangle. Algorithm
	 * checks point relative to min/max of axis-aligned rectangle.*/
	public boolean contains(Vec2f v) {
		if (this.getMinX() < v.x && this.getMaxX() > v.x) {
			if (this.getMinY() < v.y && this.getMaxY() > v.y){
				return true;
			}
		}
		return false;
	}
	
	/*Updates values in setFrame in case location/dimensions have changed,
	 * then calls super.draw, since _rect is already set as super's shape.*/
	public void draw(Graphics2D g) {
		_rect.setFrame(this.getX(), this.getY(), this.getWidth(), this.getHeight());
		super.draw(g);
	}
	
	/* Accessor/Mutator for ShapeCollisionInfo object which holds 
	 * this shape, the shape collided with, and 
	 * the minimum translation vector. Accessed by
	 * containing PhysicsEntity for collision response.*/
	@Override
	public ShapeCollisionInfo getCollisionInfo() {
		return _collisionInfo;
	}
	@Override
	public void setCollisionInfo(ShapeCollisionInfo info) {
		_collisionInfo = info;
	}

	//Implementation of Collision Detection shapeCollisionDetection; double dispatch
	
	/*Double motherfuckin Dispatch*/
	@Override
	public boolean collides(Shape s) {
		return s.collidesAAB(this);
	}

	@Override
	public boolean collidesCircle(CircleShape circ) {
		//Forward to where algorithm already defined
		return circ.collidesAAB(this);
	}

	@Override
	public boolean collidesAAB(AARectShape aab) {		
		PolygonShape thisRect = this.rectToPoly();
		PolygonShape otherRect = aab.rectToPoly();
		boolean collision = thisRect.collidesPolygon(otherRect);
		this.setCollisionInfo(thisRect.getCollisionInfo());
		aab.setCollisionInfo(otherRect.getCollisionInfo());
		return collision;
	}

	@Override
	public boolean collidesCompound(CompoundShape c) {
		//Forward to where algorithm already defined
		return c.collidesAAB(this);
	}
	
	/*Turn the Axis-Aligned Rectangle into a PolygonShape with n=4 vertices.
	 * This doesn't just forward to Polygon's aab method because it calls
	 * collidesPolygon in a different order, and this way aab.collidesPolygon
	 * and polygon.collidesAAB produce results in both directions, insurance for
	 * valid MTVs.*/
	@Override
	public boolean collidesPolygon(PolygonShape p) {		
		return p.collidesAAB(this);
	}

	@Override
	public Vec2f projectOnto(SeparatingAxis sep) {
		return sep.project(this);
	}

	@Override
	public boolean collidesCurve(BezierCurve c) {
		return c.collidesAAB(this);
	}

	@Override
	public Vec2f getCentroid() {
		return getLocation().plus(getDimensions().sdiv(2f));
	}

	@Override
	public float getMomentOfInertia(float mass) {
		return this.rectToPoly().getMomentOfInertia(mass);
	}

	@Override
	public float getArea() {
		return this.getDimensions().x*this.getDimensions().y;
	}

	@Override
	public Vec2f poi(Shape s) {
		return s.poiPolygon(rectToPoly());
	}

	@Override
	public Vec2f poiPolygon(PolygonShape p) {
		return rectToPoly().poiPolygon(p);
	}

	@Override
	public Vec2f poiCircle(CircleShape c) {
		return rectToPoly().poiCircle(c);
	}

	@Override
	public Vec2f poiCurve(BezierCurve c) {
		return c.poiPolygon(rectToPoly());
	}
}
