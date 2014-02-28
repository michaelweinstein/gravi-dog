package miweinst.engine.shape;

import java.awt.Graphics2D;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import miweinst.engine.beziercurve.BezierCurve;
import miweinst.engine.collisiondetection.ShapeCollisionInfo;
import miweinst.engine.collisiondetection.SeparatingAxis;
import cs195n.Vec2f;

/**
 * This class takes in an array of 
 * Vec2f vertices that must be 
 * in counter-clockwise order!
 * @author miweinst
 */

public class PolygonShape extends Shape {
	public static final String string = "PolygonShape";
	
	//Location stored in Shape, reference
	//Stores points of path
	private Vec2f[] _vertices;
	private Vec2f[] _relativeVertices;
	private Vec2f _centroid;
	
//	private float _area;
	
/////  Stores other Shape in collision (K) and MTV (V)
//	private HashDecorator<Shape, Vec2f> _collisionDecorator;
	private ShapeCollisionInfo _collisionInfo;
	
	//Takes in a location for superclass' reference
	//Vals array should already be in counter-clockwise order
	public PolygonShape(Vec2f loc, Vec2f[] ccverts) {
		super(loc, new Vec2f(0, 0));	//dimensions N.A for poly
		super.setLocation(loc);
		_vertices = ccverts;
		_collisionInfo = null;
		
		_relativeVertices = new Vec2f[ccverts.length];
		_centroid = getCentroidOf(Arrays.asList(ccverts));
		for (int i=0; i<ccverts.length; i++) {
			_relativeVertices[i] = ccverts[i].minus(_centroid);
		}
//		_area = getArea();
	}
	//Doesn't take in a location
	public PolygonShape(Vec2f[] ccverts) {
		super(ccverts[0], new Vec2f(0, 0));	//dimensions N.A for poly
		_vertices = ccverts;
		_collisionInfo = null;	
		_relativeVertices = new Vec2f[ccverts.length];
		_centroid = getCentroidOf(Arrays.asList(ccverts));
		for (int i=0; i<ccverts.length; i++) {
			_relativeVertices[i] = ccverts[i].minus(_centroid);
		}
//		_area = getArea();
	}
	
	/*Returns a Path2D object representing this Polygon*/
	public Path2D toPath() {
		Path2D.Float path = new Path2D.Float();
		path.moveTo(_vertices[0].x, _vertices[0].y);
		for (int i=1; i<_vertices.length; i++) {
			Vec2f v = _vertices[i];
			path.lineTo(v.x, v.y);
		}
		path.closePath();
		return path;
	}
	
	/*Returns number of vertices stored in Polygon*/
	public int getSize() {
		return _vertices.length;
	}
	
	/*Returns an array containing vertices.*/
	public Vec2f[] getVertices() {
		return _vertices.clone();
	}
	
	@Override
	public void setAngle(float angle) {
		super.setAngle(angle);
		updateVertices();
	}
	private void updateVertices() {
		List<Vec2f> verticesWithOffset = new ArrayList<Vec2f>();
		for(Vec2f v : _relativeVertices) {
			//rotate the relative vertices
			double newX = v.x * Math.cos((double)getAngle()) - v.y * Math.sin((double)getAngle());
			double newY = v.x * Math.sin((double)getAngle()) + v.y * Math.cos((double)getAngle());
			//add them to the centroid
			verticesWithOffset.add(new Vec2f((float)newX, (float)newY).plus(_centroid));
		}
		_vertices = verticesWithOffset.toArray(new Vec2f[_vertices.length]);
	}
	
	public static Vec2f getCentroidOf(List<Vec2f> vertices) {
		float constant = 1f/(6f*getArea(vertices));
		float xSum = 0, ySum = 0;
		int n = vertices.size();
		for(int i = 0; i < n; i++) {
			Vec2f v1 = vertices.get(i);
			Vec2f v2 = vertices.get((i+1)%n);
			xSum += (v1.x + v2.x)*(v1.cross(v2));
			ySum += (v1.y + v2.y)*(v1.cross(v2));
		}
		return new Vec2f(xSum, ySum).smult(constant);
	}
	public static float getArea(List<Vec2f> vertices) {
		int n = vertices.size();
		float area = 0f;
		for(int i = 0; i < n; i++) {
			Vec2f v1 = vertices.get(i);
			Vec2f v2 = vertices.get((i+1)%n);
			area += (v1.cross(v2));
		}
		assert area != 0;
		return area/2f;
	}
	
	@Override
	public Vec2f getCentroid() {
		return _centroid;
	}
	
	/*Move reference location and maintain vertices
	 * relative positions by moving same amount.*/
	public void setLocation(Vec2f loc) {
		super.setLocation(loc);
		float deltax = loc.x - _centroid.x;
		float deltay = loc.y - _centroid.y;
		//translate: moves vertices, updates arrays
		this.translate(deltax, deltay);	
//		_location = loc;
		_centroid = loc;
	}
	
	/*Move polygon location/vertices by delta values.*/
	public void translate(float dx, float dy) {
		Vec2f[] copy = new Vec2f[_vertices.length];
		for (int i=0; i<_vertices.length; i++) {
			Vec2f v = _vertices[i];
			copy[i] = new Vec2f(v.x+dx, v.y+dy);
		}
		_vertices = copy;	
		Vec2f newLoc = new Vec2f(_centroid.x+dx, _centroid.y+dy);
		_centroid = newLoc;
		super.setLocation(newLoc);
////
		//updateVertices()
	}
	
	/*Sets location without updating vertices. 
	 * Polygon does not actually move, only
	 * changes _locations relative position to vertices.*/
/*	public void setLocationReference(Vec2f ref) {
		super.setLocation(ref);
		_location = ref;
	}*/
	
	/*Returns the vertex Vec2f that has
	 * the minimum X or Y values.*/
	public Vec2f getMinX() {
		Vec2f min = new Vec2f(100, 100);	
		boolean first = true;
		for (Vec2f v: _vertices) {
			if (first) {
				min = v;
				first = false;
			}
			if (v.x < min.x) {
				min = v;
			}
		}
		return min;
	}
	/*Minimum Y value of vertices*/
	public Vec2f getMinY() {
		Vec2f min = new Vec2f(0, 0);
		boolean first = true;
		for (Vec2f v: _vertices) {
			if (first) {
				min = v;
				first = false;
			}
			if (v.y < min.y) {
				min = v;
			}
		}
		return min;
	}	
	/*Returns the vertex Vec2f that has
	 * the Maximum X or Y values.*/
	public Vec2f getMaxX() {
		Vec2f max = new Vec2f(100, 100);		
		boolean first = true;
		for (Vec2f v: _vertices) {
			if (first) {
				max = v;
				first = false;
			}
			if (v.x > max.x) {
				max = v;
			}
		}
		return max;
	}
	/*Maximum Y value of vertices*/
	public Vec2f getMaxY() {
		Vec2f max = new Vec2f(0, 0);
		boolean first = true;
		for (Vec2f v: _vertices) {
			if (first) {
				max = v;
				first = false;
			}
			if (v.y > max.y) {
				max = v;
			}
		}
		return max;
	}
	
	/*Returns smallest rect parallel to
	 * coordinate axes containing polygon.*/
	public Rectangle2D getBounds() {
		return this.toPath().getBounds2D();
	}
	
	/*Finds polygon vertex closest to point pt.*/
	public Vec2f closestVertex(Vec2f pt) {
		Vec2f closestVert = this.getVertices()[0];			
		//Find closest vertex to circle's center
		for (int i=0; i < this.getVertices().length; i++) {
			Vec2f vert = this.getVertices()[i];		
			if (pt.dist2(vert) < pt.dist2(closestVert)) {
				closestVert = vert;
			}
		}	
		return closestVert;
	}

	/*Returns whether specified point is contained in Polygon. 
	 * Uses cross products to check point's orientation to 
	 * the vector constructed along any edge. Points in Polygon
	 * must be stored in counterclockwise order.
	 * Also, this is only for a Polygon path that has
	 * straight edges. No quad curves are accepted,
	 * because currentSegment() would return more than 
	 * 2 values and this method would throw ArryOutofBoundsException*/
	public boolean contains(Vec2f v) {
		PathIterator iter = this.toPath().getPathIterator(null);
		boolean first = true;
		Vec2f src = new Vec2f(0, 0);
		Vec2f dst = new Vec2f(0, 0);
		Vec2f start = new Vec2f(0, 0);
		while (iter.isDone() == false) {
			if (first) {
				float[] vert = new float[2];
				iter.currentSegment(vert);
				src = new Vec2f(vert[0], vert[1]);
				first = false;
				//Store first endpoint for last vector
				start = src;			
			}
			else {
				float[] vert = new float[2];
				iter.currentSegment(vert);
				dst = new Vec2f(vert[0], vert[1]);
				//If at last vertex, get vector back to start vertex
				if (iter.currentSegment(vert) == PathIterator.SEG_CLOSE) {
					dst = start;
				}
				//Get vector from one end point to next
				Vec2f pnt = new Vec2f(dst.y-src.y, dst.x-src.x);
				Vec2f end = new Vec2f(v.y-src.y, v.x-src.x);
				//If ANY cross-product is less than 0, false
				float cross = pnt.cross(end);
				if (cross < 0) {
					return false;
				}
				//Set new start point to end point, for next vector
				src = dst;
			}		
			iter.next();
		}
		return true;
	}
	
	/*Creates a Path2D of the same values/vertices
	 * as this PolygonShape, and passes it to superclass
	 * so it is drawn.*/
	@Override
	public void draw(Graphics2D g) {
		Path2D path = this.toPath();		
		super.setShape(path);
		super.draw(g);		
//////		
/*		if (_testPath != null) {
			g.setColor(Color.RED);
			g.draw(_testPath);
		}*/
///^^^^
	}
	
	/*ShapeCollisionDetection implementation of interface.*/

	@Override
	public boolean collides(Shape s) {
		return s.collidesPolygon(this);
	}

	/* Checks collision between Polygon and Circle. Uses
	 * separating axis from circle center to closest Polygon
	 * vertex, and uses axes of all normals of Polygon edges.
	 * Also checks if Polygon contains the center of the circle,
	 * or if the circle contains the closest vertex on the Polygon,
	 * to save computation if obviously true.*/
	@Override
	public boolean collidesCircle(CircleShape c) {	
		this.setCollisionInfo(null);
		c.setCollisionInfo(null);	
		
		Vec2f[] allAxes = new Vec2f[this.getVertices().length+1];
		Float minMag = Float.POSITIVE_INFINITY;
		Vec2f mtv = null;
		//Populated allAxes with Polygon edge normals
		Vec2f[] verts = this.getVertices();	
		Vec2f start = null;
		Vec2f end = null;		
		for (int i=0; i<verts.length; i++) {	
			if (i < verts.length-1) {
				start = new Vec2f(verts[i].x, verts[i].y);
				end = new Vec2f(verts[i+1].x, verts[i+1].y);
			}
			else {
				start = new Vec2f(verts[i].x, verts[i].y);
				end = new Vec2f(verts[0].x, verts[0].y);
			}			
			Vec2f edge = end.minus(start).normalized();
			Vec2f normal = new Vec2f(-edge.y, edge.x).normalized();	
			allAxes[i] = normal;
		}		
		//Finish allAxes with circle-poly separating axis
		Vec2f closestVert = this.closestVertex(c.getCentroid());				
		allAxes[allAxes.length-1] = closestVert.minus(c.getCentroid()).normalized();	
		//Get minimum MTV and populate ShapeCollisionInfo attributes
		for (int i=0; i<allAxes.length; i++) {
			Vec2f axis = allAxes[i];
			SeparatingAxis sepAxis = new SeparatingAxis(axis);
			Float mtv1d = sepAxis.intervalMTV(sepAxis.project(c), sepAxis.project(this));
			if (mtv1d == null) {
				return false;
			}
			if (Math.abs(mtv1d) < minMag) {
				minMag = Math.abs(mtv1d);
				mtv = axis.smult(mtv1d);
			}
		}
		this.setCollisionInfo(new ShapeCollisionInfo(this, c, mtv.sdiv(-1)));
		c.setCollisionInfo(new ShapeCollisionInfo(c, this, mtv));	
		return true;
	}
	

	/*This method reuses the code from collidesPolygon by
	 * creating PolygonShape with same four vertices as
	 * AARect. Not the most computationally efficient, because
	 * all four edges don't have to be iterated over, but the
	 * most efficient in man-hours, and get to reuse code.*/
	@Override
	public boolean collidesAAB(AARectShape aab) {
		PolygonShape p = aab.rectToPoly();	
		//Populate collision info
		boolean collision = p.collidesPolygon(this);		
		//Get the collision info generated from collidesPolygon
		aab.setCollisionInfo(p.getCollisionInfo());
		//Return the boolean collision detection result
		return collision;
	}

	/*Reuses code from its component shapes' methods. Checks instance
	 * of component, and calls corresponding collides procedure. If any component
	 * shape reports collision, collision detected.*/
	@Override
	public boolean collidesCompound(CompoundShape c) {
		for (Shape s: c.getShapes()) {
			if (s instanceof AARectShape) {
				if (this.collidesAAB((AARectShape) s)) {
					return true;
				}
			}
			else if (s instanceof CircleShape) {
				if (this.collidesCircle((CircleShape) s)) {
					return true;
				}
			}
			else if (s instanceof PolygonShape) {
				if (this.collidesPolygon((PolygonShape) s)) {
					return true;
				}
			}
			//if nested CompoundShapes; recursive
			else if (s instanceof CompoundShape) {
				if (this.collidesCompound((CompoundShape) s)) {
					return true;
				}
			}
			else System.out.println("Error: Huh? Shape not recognized! (collidesCompound.PolygonShape)");
		}
		return false;
	}
	
	/*Checks for collisions between two covnex polygons using
	 * the Separating Axis Theorem. Iterates over edges of
	 * both polygons consecutively, projecting both shapes
	 * onto the axis formed by the normal of each edge and
	 * then checking for overlap. If a single axis shows
	 * overlap between the projections, no collision is detected.*/
	@Override
	public boolean collidesPolygon(PolygonShape p) {	
		this.setCollisionInfo(null);
		p.setCollisionInfo(null);	
		
		//Concatenate both lists of vertices
		Vec2f[][] both = new Vec2f[2][Math.max(p.getVertices().length, this.getVertices().length)];	
		both[0] = p.getVertices();
		both[1] = this.getVertices();	
		Vec2f start = null;
		Vec2f end = null;
		Float minMag = Float.POSITIVE_INFINITY;
		Vec2f mtv = null;
		//Loop through the edges of both Polygons
		for (Vec2f[] iter: both) {
			for (int i=0; i<iter.length; i++) {	
				if (i < iter.length-1) {
					start = new Vec2f(iter[i].x, iter[i].y);
					end = new Vec2f(iter[i+1].x, iter[i+1].y);
				}
				else {
					start = new Vec2f(iter[i].x, iter[i].y);
					end = new Vec2f(iter[0].x, iter[0].y);
				}				
				//Construct vector along edge, normalize for length
				Vec2f edge = end.minus(start).normalized();
				
				//Find axis by vector perpendicular to edge vector:
				Vec2f normal = new Vec2f(-edge.y, edge.x);				
				//Make separating axis for each edge
				SeparatingAxis sepAxis = new SeparatingAxis(normal);
				//Get min and max of one Polygon
				Vec2f arange = sepAxis.project(p);
				//Get min and max of other Polygon
				Vec2f brange = sepAxis.project(this);	

				//If ranges don't overlap on every axis, collision NOT detected
				if (sepAxis.isOverlapping(arange, brange)==false) {
					return false;
				}
				
				//Calculate MTV of all axes and store one with min magnitude		
				Float mtv1d = sepAxis.intervalMTV(arange, brange);
				if (mtv1d != null) {				
					if (Math.abs(mtv1d) < minMag) {
						minMag = Math.abs(mtv1d);
						mtv = normal.smult(mtv1d);	
					}
				}
			}
		}	
		
		//Store this Shape, other Shape and mtv in ShapeCollisionInfo cache
		this.setCollisionInfo(new ShapeCollisionInfo(this, p, mtv.sdiv(-1)));
		p.setCollisionInfo(new ShapeCollisionInfo(p, this, mtv));	
		return true;
	}

	@Override
	public ShapeCollisionInfo getCollisionInfo() {
		return _collisionInfo;
	}
	@Override
	public void setCollisionInfo(ShapeCollisionInfo info) {
		_collisionInfo = info;
	}

	@Override
	public Vec2f projectOnto(SeparatingAxis sep) {
		return sep.project(this);
	}

	@Override
	public boolean collidesCurve(BezierCurve c) {
		return c.collidesPolygon(this);
	}

	@Override
	public float getMomentOfInertia(float mass) {
		int n = _relativeVertices.length;
		float denominator = 0f;
		float numerator = 0f;
		for(int i = 0; i < n; i++) {
			Vec2f v1 = _relativeVertices[i];
			Vec2f v2 = _relativeVertices[(i+1)%n];
			float v1Mag = v1.mag();
			float v2Mag = v2.mag();
			numerator += (v1Mag * v1Mag + v1.dot(v2) + v2Mag * v2Mag) * (v1.cross(v2));
			denominator += (v1.cross(v2));
		}
		return (mass/6f) * numerator/denominator;
	}
	@Override
	public float getArea() {
		return getArea(Arrays.asList(_relativeVertices));
	}

	@Override
	public Vec2f poi(Shape s) {
		return s.poiPolygon(this);
	}

	@Override
	public Vec2f poiPolygon(PolygonShape p) {	
		List<Vec2f> verticesInsideOtherShape = new ArrayList<Vec2f>();
		for(Vec2f vertex : this.getVertices()) 
			if(p.collidePoint(vertex) != null) 
				verticesInsideOtherShape.add(vertex);
		for(Vec2f vertex : p.getVertices()) 
			if(this.collidePoint(vertex) != null) 
				verticesInsideOtherShape.add(vertex);
		if(verticesInsideOtherShape.isEmpty()) 
			return null;
		return Vec2f.average(verticesInsideOtherShape);
	}
	
	@Override
	public Vec2f poiCircle(CircleShape c) {
		this.collidesCircle(c);
		ShapeCollisionInfo info = this.getCollisionInfo();
		if(info == null) {
			return null;
		} else {
			return c.getLocation().plus(info.getMTV().normalized().smult(c.getRadius()));
		}
	}
	
	@Override
	public Vec2f poiCurve(BezierCurve c) {
		return c.poiPolygon(this);
	}
	
////////////////////
	public Vec2f collidePoint(Vec2f point) {
		List<Vec2f> pointList = new ArrayList<Vec2f>();
		pointList.add(point);
		Vec2f mtv = ShapeCollisionInfo.getMTV(Arrays.asList(this.getVertices()), pointList, this.getEdges());
		if(mtv == null) {
			return null;
		}
		return mtv; 
	}	
	public List<Vec2f> getEdges() {
		List<Vec2f> edges = new ArrayList<Vec2f>();
		List<Vec2f> vertices = Arrays.asList(getVertices());
		int n = vertices.size();
		for(int i = 0; i < n; i++) {
			edges.add(vertices.get((i+1)%n).minus(vertices.get(i)));
		}
		return edges;
	}
///////////////^^^^^^^^^^^^

}
