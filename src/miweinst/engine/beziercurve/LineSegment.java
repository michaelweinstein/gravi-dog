package miweinst.engine.beziercurve;

import java.awt.Graphics2D;
import java.awt.geom.Path2D;
import java.util.ArrayList;
import java.util.List;

import cs195n.Vec2f;

/**
 * Immutable data structure;
 * @author maxfuller
 */
public class LineSegment {
	
	public Vec2f start;
	public Vec2f end;
        Path2D _path;

	public LineSegment() {
		this.start = new Vec2f(0, 0);
		this.end = new Vec2f(0, 10);
	}
	public LineSegment(Vec2f a, Vec2f b) {
		this.start = a;
		this.end = b;
                _path = new Path2D.Float();
                _path.moveTo(start.x, start.y);
		_path.lineTo(end.x, end.y);
	}
	
	private void setStart(Vec2f s) {
		start = s;
	}
	private void setEnd(Vec2f e) {
		end = e;
	}
        /*Translates line segment by delta values.*/
	private void translate(Vec2f delta) {
		this.start = start.plus(delta);
		this.end = end.plus(delta);
	}
	
	/* Takes in list of points as Vec2f, then returns a list of LineSegments
	 * that use points as endpoints.*/
	public static ArrayList<LineSegment> pointsToSegs(List<Vec2f> pointList) {
		ArrayList<LineSegment> segList = new ArrayList<LineSegment>();
		for (int i=0; i < pointList.size()-1; i++) {			
			LineSegment seg = new LineSegment(pointList.get(i), pointList.get(i+1));
			segList.add(seg);
		}	
		return segList;
	}
	
	/* Returns point of intersection between two line segments,
	 * if intersection occurs within both line segments' endpoints. 
	 * Else returns null. Also null if line segments are parallel (determinant == 0).
	 * Converts each line into form Ax + By = C. then solves for intersection.*/
/*	public Vec2f intersectsLine(LineSegment other) {
		Vec2f poi = Vec2f.lineIntersect(this.start, this.end, other.start, other.end);	
		float x = poi.x;
		float y = poi.y;
		boolean onA = false;
		boolean onB = false;
		//Check if intersection point lies within line segment a
		if (x >= Math.min(this.start.x, this.end.x) && x <= Math.max(this.start.x, this.end.x)) 
			if (y >= Math.min(this.start.y, this.end.y)&& y <= Math.max(this.start.y, this.end.y))
				onA = true;
		//Check if intersection point lies within line segment b
		if (x >= Math.min(other.start.x, other.end.x) && x <= Math.max(other.start.x, other.end.x)) 
			if (y >= Math.min(other.start.y, other.end.y)&& y <= Math.max(other.start.y, other.end.y))
				onB = true;
		//If lies on both segments, return intersection
		if (onA && onB)
			return poi;
		//If lies outside of segments, no intersection
		else 
			return null;	
	}*/
	
	
	/*Rotate point*/
	public static Vec2f rotate(Vec2f rotateAround, Vec2f point, float theta) {
		float newX = (float) (Math.cos(theta)*(point.x-rotateAround.x)- Math.sin(theta)*(point.y-rotateAround.y) + rotateAround.x); 
		float newY = (float) (Math.sin(theta)*(point.x-rotateAround.x) + Math.cos(theta)*(point.y-rotateAround.y) + rotateAround.y);
		return new Vec2f(newX, newY);
	}
	/* Moves line segment to x-axis using translation and rotation.
	 * Returns an instance of LineSegment translated/rotated to 
	 * x-axis.*/
	public LineSegment toXAxis() {
		LineSegment line = new LineSegment(start, end);
		//Translate start point to x-axis, straight down
		line.translate(new Vec2f(line.start.x, -line.start.y));
		//Rotate end point so line segment is on x-axis
		float theta = (float) Math.atan2(line.end.y-line.start.y, line.end.x-line.start.x);
		line.end = rotate(line.start, line.end, theta);
		return line;
	}
	
	/* Uses a Path2D in order to maintain floating point 
	 * accuracy. Graphics' drawLine only takes ints.*/
	public void draw(Graphics2D g) {
		g.draw(_path);
		System.out.println("line draw");
	}
}
