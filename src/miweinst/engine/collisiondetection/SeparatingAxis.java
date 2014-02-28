package miweinst.engine.collisiondetection;

import java.awt.geom.PathIterator;

import cs195n.Vec2f;
import miweinst.engine.shape.AARectShape;
import miweinst.engine.shape.CircleShape;
import miweinst.engine.shape.CompoundShape;
import miweinst.engine.shape.PolygonShape;

public class SeparatingAxis {

	//Axis to project onto
	private Vec2f _axis;
	
	/* Takes in vector representing an axis by
	 * subtracting coordinate vectors p2 - p1. 
	 * Project methods returns the Vec2f 
	 * range(min, max) of specified Shape.  
	 * Only projects to one axis per instantiation, 
	 * so instantiated repetitively from collides()
	 * for consecutive axes.*/
	public SeparatingAxis(Vec2f axis) {
		//Normalize axis for distance operations, i.e. projectCircle +- radius
		_axis = axis.normalized();
	}
	
	/*Takes in two two Vec2f storing min and max of each shape's
	 * range projected onto _axis. Returns a boolean for whether
	 * those ranges' min/max overlap.*/
	public boolean isOverlapping(Vec2f rangeA, Vec2f rangeB) {
		if (rangeA.x < rangeB.y && rangeB.x < rangeA.y) {
			return true;
		}		
		return false;
	}
	
	/*Returns the Minimum Translation Vector of
	 * the overlap between two ranges. Two 
	 * ranges are stored in Vec2f as Vec2f(min, max). 
	 * Uses class Float so it can return null. */
	public Float intervalMTV(Vec2f rangeA, Vec2f rangeB) {
		if (this.isOverlapping(rangeA, rangeB)) {
			Float aRight = rangeB.y - rangeA.x;
			Float aLeft = rangeA.y - rangeB.x;
			//Shouldn't occur because only called on collision
			if (aLeft < 0 || aRight < 0) {
				System.err.println("Error: No actual overlap? (SeparatingAxis.intervalMTV)");
				return null;
			}
			if (aRight < aLeft) 
				return aRight;
			else 
				return -aLeft;
		}
		else return null;
	}
	
	/* Projects the center of the circle
	 * onto the axis, then finds range min/max
	 * by adding and subtracting the 
	 * radius from that projection.*/
	public Vec2f project(CircleShape c) {
		float proj = c.getCentroid().dot(_axis);			
		return new Vec2f(proj - c.getRadius(), proj + c.getRadius());
	}
	
///////
	/*Not necessary yet! Because each Rect
	 * is turned into a Polygon and passed
	 * into project(PolygonShape) method. */
	public Vec2f project(AARectShape r) {
		return this.project(r.rectToPoly());
	}
	/*Not necessary yet! Because each component
	 * of CompoundShape is called sent to
	 * a project method individually.*/
	public Vec2f project(CompoundShape c) {
		System.out.println("Not implemented");
		return null;
	}
/////////^^^^
	
	/*This method projects each vertex of
	 * the Polygon p onto the axis passed
	 * into the constructor. Projects each
	 * vertex of Polygon onto the axis, and
	 * stores reference to minimum and maximum
	 * projection of vertex to return in Vec2f.*/
	public Vec2f project(PolygonShape p) {		
		Float min = null;
		Float max = null;
		//Project each vertex of each shape
		PathIterator iter = p.toPath().getPathIterator(null);
		while (iter.isDone() == false) {
			float[] arr = new float[2];
			iter.currentSegment(arr);			
			if (iter.currentSegment(arr) == PathIterator.SEG_CLOSE) {
				break;
			}		
			Vec2f vert = new Vec2f(arr[0], arr[1]);
			float proj = vert.dot(_axis);

			//First iteration, initialize min/max at vertex[0]
			if (min == null && max == null) {
				min = max = proj;
			}
			if (proj < min) {
				min = proj;
			}
			if (proj > max) {
				max = proj;
			}
			iter.next();
			}				
		Vec2f range = new Vec2f(min, max);
		return range;
	}
	
	public float projectPoint(Vec2f p) {
		return p.dot(_axis);
	}
}
