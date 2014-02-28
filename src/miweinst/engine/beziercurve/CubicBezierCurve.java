package miweinst.engine.beziercurve;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.CubicCurve2D;
import java.util.ArrayList;
import java.util.List;

import miweinst.engine.collisiondetection.ShapeCollisionInfo;
import miweinst.engine.shape.AARectShape;
import miweinst.engine.shape.CircleShape;
import miweinst.engine.shape.PolygonShape;
import miweinst.engine.shape.Shape;

import org.ddogleg.solver.Polynomial;
import org.ddogleg.solver.PolynomialOps;
import org.ddogleg.solver.PolynomialRoots;
import org.ddogleg.solver.RootFinderType;
import org.ejml.data.Complex64F;

import cs195n.Vec2f;

public class CubicBezierCurve extends BezierCurve {

	public Vec2f start, ctrl_one, ctrl_two, end;
	private Vec2f[] _points;

	private ArrayList<LineSegment> _segs;
	private ArrayList<Vec2f> _pois;

	////Visualization
	private ArrayList<CircleShape> _drawDots;
	private ArrayList<LineSegment> _drawLines;
	
	private boolean _drawBorder;

	/**Default constructor and constructor taking in four _points (2 endpoints, 2 ctrl _points)*/
	public CubicBezierCurve() {
		super(new Vec2f(0, 0), new Vec2f(0, 0));	
		_points = new Vec2f[4];
		//List to be populated by findDrawingPoints
		_segs = new ArrayList<LineSegment>();
		this.init(new Vec2f(35, 40), new Vec2f(45.5f, 63), new Vec2f(40, 22), new Vec2f(70, 40.6f));
	}
	public CubicBezierCurve(Vec2f point1, Vec2f point2, Vec2f point3, Vec2f point4) {
		super(point1, new Vec2f(0, 0));
		_points = new Vec2f[4];
		//List to be populated by findDrawingPoints
		_segs = new ArrayList<LineSegment>();
		this.init(point1, point2, point3, point4);		
	}

	/**Initializes variables and arr to four _points: point1 and point4 are start
	 * and end point, point2 and point3 are ctrl _points. Sets shape location to start.*/
	private void init(Vec2f point1, Vec2f point2, Vec2f point3, Vec2f point4) {
		start = _points[0] = point1;
		ctrl_one = _points[1] = point2;
		ctrl_two = _points[2] = point3;
		end = _points[3] = point4;
		this.setLocation(start);
		this.updateSegs();
		_pois = new ArrayList<Vec2f>();
		_drawBorder = false;
		//////		
		_drawDots = new ArrayList<CircleShape>();
		_drawLines = new ArrayList<LineSegment>();
//		approxLineLength = point1.dist(point2) + point2.dist(point3) + point3.dist(point4);
	}

	/**Updates the array of _points, called if any values of
	 * start, end, ctrl_one or ctrl_two are changed. Should
	 * be called automatically from any methods within BezierCurve classes
	 * that change any of the point values, for encapsulation's sake.*/
	public void updatePointArr() {
		_points[0] = start;
		_points[1] = ctrl_one;
		_points[2] = ctrl_two;
		_points[3] = end;
		updateSegs();
	}
	/**Other direction: Updates vars for each point if pointsArr has been changed.*/
	public void updatePointVars() {
		start = _points[0];
		ctrl_one = _points[1];
		ctrl_two = _points[2];
		end = _points[3];
		updateSegs();
	}
	/**Updates LineSegment references in _segs*/
	private void updateSegs() {
		//Populate list of LineSegments _segs
		ArrayList<Vec2f> pointList = new ArrayList<Vec2f>();
		for (float t=0; t <= 1; t += .01f) 
			pointList.add(getCasteljauPoint(t)); 
		ArrayList<LineSegment> segList = LineSegment.pointsToSegs(pointList);
		_segs = segList;
	}

	/**Location of curve is defined as the starting endpoint. 
	 * When new location is set, points do not move relative to
	 * each other; all points are translated by same amount so
	 * that start point is at the new location.*/
	@Override
	public Vec2f getLocation() {
		return start;
	}
	@Override
	public void setLocation(Vec2f loc) {
		super.setLocation(loc);
		Vec2f d = new Vec2f(loc.x - loc.x, loc.y - start.y);
		this.translate(d);
	}

	/**Translate all ctrl _points and endpoints in curve by specified dx, dy. 
	 * Makes changes in array of _points and then udpates the endpoints and
	 * control _points using updatePoints.*/
	public void translate(Vec2f d) {
		for (int i=0; i<_points.length; i++) {
			Vec2f p = _points[i];
			_points[i] = new Vec2f(p.x + d.x, p.y + d.y);
		}
		this.updatePointVars();
		this.updateSegs();
	}
	/* Rotate each point around the given point rotateAround by
	 * an angle theta. Uses LineSegment's static method to rotate
	 * point around another point.*/
	public void rotate(Vec2f rotateAround, float theta) {
		//First get rotated _points in _points arr
		for (int i=0; i<_points.length; i++)
			_points[i] = LineSegment.rotate(rotateAround, _points[i], theta);
		//Update vars to rotated _points
		this.updatePointVars();
		this.updateSegs();
	}

	/**Calculates a point at parameter t along this instance of CubicBezierCurve,
	 * using addition/assignment of each term of the following cubic equation: */
	//Cubic Bezier curve: [x,y] = (1-t)^3*P0 + 3(1-t)^2*tP1 + 3(1-t)ttP2 + tttP3
	public Vec2f calculateBezierPoint(float t) {
		float u = 1-t;
		float uu = u*u;
		float uuu = uu*u;
		float tt = t*t;
		float ttt = tt*t;		
		Vec2f p = start.smult(uuu);
		p = p.plus(ctrl_one.smult(3*uu*t));
		p = p.plus(ctrl_two.smult(3*u*tt));
		p = p.plus(end.smult(ttt));
		return  p;
	}	

	/** Implementation of de Casteljau's algorithm to return point
	 * on curve at specified t=u value. Returns point on the curve
	 * at t=u, more efficient than raw mathematical calculation used in
	 * calculateBezierPoint, and more extensible.
	 * Given a ratio u b/w 0 and 1, returns point C = (1-u)A + uB */
	public Vec2f getCasteljauPoint(float u) {
		Vec2f[] arr = new Vec2f[_points.length];
		//Save input in working arr
		for (int i=0; i<_points.length; i++) 
			arr[i] = _points[i];
		for (int i=1; i<arr.length; i++) {
			for (int j=0; j<arr.length-i; j++) {
				arr[j] = arr[j].smult(1-u).plus(arr[j+1].smult(u));
			}
		}
		return arr[0];
	}

	/** Find derivative of point on curve at specified t value. The 
	 * derivative of an n-order Bezier curve is an (n-1)-order Bezier curve. 
	 * The ctrl _points of derivative curve are: q0 = p1-p0, q1 = p2-p1, q2 = p3-p2, so on...
	 * Note: The derivative and tangent of C(t) are equivalent
	 * Calculated arithmetically using the following polynomial: */ 
	// dC(t)/dt = C'(t) = 
	// (-3*P0*(1-t)^2) + (P1*(3*(1-t)^2 - 6*(1-t)*t)) + (P2*(6*(1-t)*t - 3*tt)) + (3*P3*tt)
	public Vec2f findDerivative(float t) {
		float u = 1-t;
		float uu = u*u;
		float tt = t*t;
		//Each term added after calculated with Vec2f ops
		Vec2f dt = start.smult(-3*uu);
		dt = dt.plus(ctrl_one.smult(3*uu - 6*u*t));
		dt = dt.plus(ctrl_two.smult(6*u*t - 3*tt));
		dt = dt.plus(end.smult(3*tt));
		return dt.normalized();
	}
	//C''(t) = 6(1-t)P0+3(-4+6t)P1+3(2-6t)P2+6tP3
	public Vec2f findSecondDerivative(float t) {
		float u = 1-t;
		Vec2f ddt = start.smult(6*u);
		ddt = ddt.plus(ctrl_one.smult(3*(-4+6*t)));
		ddt = ddt.plus(ctrl_two.smult(3*(2-6*t)));
		ddt = ddt.plus(end.smult(6*t));
		return ddt;
	}
	/** Returns the vector perpendicular to the normalized tangent
	 * at the point f(t) for any t value. */
	public Vec2f findNormal(float t) {
		Vec2f dt = findDerivative(t);
		return new Vec2f(-dt.y, dt.x).normalized();
	}

	/**Find the point P (defined by P(t)) on the Bezier curve that is closest to 
	 * the point M, which can be anywhere. The line seg MP (i.e. M-P) is orthogonal
	 * to the tangent/derivative of P (dP/dt), so MP.dot(dP/dt) == 0. 
	 * Therefore M.minus(P).dot(derivative(getT(P))) == 0, then use root finding.*/
	public float nearestTOnCurve(final Vec2f m) {

		//USING DDOGLEG LIBRARY ROOTFINDING
		Polynomial poly = getPCurveDotProduct(m);

		PolynomialRoots finder = PolynomialOps.createRootFinder(6, RootFinderType.EVD);

		if( !finder.process(poly) )
			throw new RuntimeException("Failed to find solution!");

		List<Complex64F> roots = finder.getRoots();

		float closest = -1f;
		float closestDist = Float.POSITIVE_INFINITY;
		
		roots.add(new Complex64F(0d, 0d));
		roots.add(new Complex64F(1d, 0d));
		
		for(Complex64F z : roots) {
			if(z.isReal() && z.getReal() >= 0d && z.getReal() <= 1d) {
				float dist = this.getCasteljauPoint((float)z.getReal()).dist(m);
				if(dist < closestDist) {
					closest = (float) z.getReal();
					closestDist = dist;
				}
			}
		}
		assert(closest != Float.POSITIVE_INFINITY);

		return closest;



		
//		//Minimum distance between M and P, i.e. M.minus(P).dot(derivative(getT(P))) == 0
//		float t_val = 0;		
//		//Dot/Orthogonal check; project orthogonal
//		float minDot = Float.POSITIVE_INFINITY;
//		for (float t=0; t<=1; t=t+1/SAMPLE_RESOLUTION) {
//			Vec2f p = getCasteljauPoint(t);
//			 float dot = m.minus(p).dot(findDerivative(t));			 
//			 //minDot should be trivially close to 0
//			 if (Math.abs(dot) < minDot) {
//				minDot = Math.abs(dot);
//				t_val = t;
//			 }
//		}
//		return t_val;
		 

//		//Simple distance minimizing through sampling curve
//		float t_val = 0;
//				float minDist = Float.POSITIVE_INFINITY;
//		for (float t=0; t<=1; t=t+1/SAMPLE_RESOLUTION) {
//			Vec2f p = getCasteljauPoint(t);
//			float dist = m.dist2(p);
//			if (dist < minDist) {
//				minDist = dist;
//				t_val = t;
//			}
//		}
//		return t_val;

	}
	public Vec2f nearestPointOnCurve(Vec2f m) {
		return getCasteljauPoint(nearestTOnCurve(m));
	}

	/* Returns the convex hull Polygon whose vertices
	 * are four curve points (2 endpoints, 2 ctrl points)*/
	public PolygonShape getWideBounds() {
		return new PolygonShape(_points);
	}

	/**NOTE: Tune MIN_SQR_DISTANCE and threshold to vary depth of
	 * recursion resolution/smoothness of curve drawing.
	 *  */
	//Minimum length of segment, at which recursion on that segment stops
	private final float MIN_SQR_DISTANCE = .0001f;
	//Normalized dot (equivalent to angle) under 
	//which a new point does not need to be added at mid
	private final float THRESHOLD = -1f;

	/* Recursively finds _points along curve to draw; resolution/closeness of
	 * _points depends on THRESHOLD value.
	 * t0 and t1 are first passed 0 and 1, which are the
	 * t values that correspond to the endpoints. Int insertionIndex maintains
	 * the list order of the _points even while recursive calls are not made 
	 * in point-order starting at 0-index, and pointList is the List<Vec2f> 
	 * that is populated by the method.*/	
	private int findDrawingPoints(float t0, float t1, int insertionIndex, ArrayList<Vec2f> pointList) {		
		//Get midpoint parameter between t0 and tw
		float tMid = (t0+t1)/2;
		//Get endpoints of segment from t0 --> t1
		Vec2f p0 = calculateBezierPoint(t0);
		Vec2f p1 = calculateBezierPoint(t1);
		//Minimum distance check to avoid normalization short vectors
		if (p0.minus(p1).mag2() < MIN_SQR_DISTANCE) {
			return 0;
		}		
		//Get point along curve at tMid
		Vec2f pMid = calculateBezierPoint(tMid);
		//Find unit vecs to both sides of pMid: [mid, start], [mid, end]
		Vec2f lDir = p0.minus(pMid).normalized();
		Vec2f rDir = p1.minus(pMid).normalized();		
		//If the angle formed between segments is too large (i.e. dot too small)
		if (lDir.dot(rDir) > THRESHOLD) {
			//Num _points added to pointList
			int pointsAdded = 0; 			
			//Recursive step on first segment
			pointsAdded += findDrawingPoints(t0, tMid, insertionIndex, pointList);
			//Add new point at correct index
			pointList.add(insertionIndex + pointsAdded, pMid);
			pointsAdded++;		
			//Recursive step on second segment; added at index after prior _points 
			pointsAdded += findDrawingPoints(tMid, t1, insertionIndex + pointsAdded, pointList);		
			return pointsAdded;
		}
		//If threshold not reached, no _points added to pointList
		return 0;
	}

	/*public static methods*/
	/**Static calculation that takes in all four _points as arguments.*/
	public static Vec2f calculateBezierPoint(float t, Vec2f p0, Vec2f p1, Vec2f p2, Vec2f p3) {
		float u = 1-t;
		float uu = u*u;
		float uuu = uu*u;
		float tt = t*t;
		float ttt = tt*t;		
		Vec2f p = p0.smult(uuu);
		p = p.plus(p1.smult(3*uu*t));
		p = p.plus(p2.smult(3*u*tt));
		p = p.plus(p3.smult(ttt));
		return  p;
	}
	/*Returns sign of specified float as 1 or -1*/
	public static int sgn(double d) {
		if (d < 0.0) 
			return -1;
		return 1;
	}
	public static float[] bezierCoeffsX(Vec2f p0, Vec2f p1, Vec2f p2, Vec2f p3) {
		float[] x_cof = new float[4];
		x_cof[0] = -p0.x + 3*p1.x - 3*p2.x + p3.x;	//t^3
		x_cof[1] = 3*(p0.x - 2*p1.x + p2.x);		//t^2
		x_cof[2] = 3*(-p0.x + p1.x);				//t
		x_cof[3] = p0.x;							//1 
		return x_cof;
	}
	public static float[] bezierCoeffsY(Vec2f p0, Vec2f p1, Vec2f p2, Vec2f p3) {
		float[] y_cof = new float[4];
		y_cof[0] = -p0.y + 3*p1.y - 3*p2.y + p3.y;	//t^3
		y_cof[1] = 3*(p0.y - 2*p1.y + p2.y);		//t^2
		y_cof[2] = 3*(-p0.y + p1.y);				//t
		y_cof[3] = p0.y;							//1
		return y_cof;
	}

	/////////////////////REWRITE THIS////////////////////////
	/** Takes in four coefficients in c as [0, t, t^2, t^3] and 
	 * an array s. Returns number of real roots and populates s.
	 * c = double[4], s = double[3]*/
	public static int solveCubic(double[] c, double[] s)  {
		int i, num;  
		double sub;  
		double A, B, C;  
		double sq_A, p, q;  
		double cb_p, D;  
		/* turn into normal form: x^3 + Ax^2 + Bx + C = 0 */  
		A = c[ 2 ] / c[ 3 ];  
		B = c[ 1 ] / c[ 3 ];  
		C = c[ 0 ] / c[ 3 ];  

		/*  substitute x = y - A/3 to eliminate quadric term: 
			x^3 +px + q = 0 */  
		sq_A = A * A;  
		p = 1.0/3 * (- 1.0/3 * sq_A + B);  
		q = 1.0/2 * (2.0/27 * A * sq_A - 1.0/3 * A * B + C);  

		/* use Cardano's formula */  
		cb_p = p * p * p;  
		D = q * q + cb_p;  

		if (D == 0)  {  
			/* one triple solution */  
			if (q == 0) {  
				s[ 0 ] = 0;  
				num = 1;  
			}  
			/* one single and one double solution */
			else  {  
				double u = Math.cbrt(-q);  
				s[ 0 ] = 2 * u;  
				s[ 1 ] = - u;  
				num = 2;  
			}  
		}  
		/* three real solutions */
		else if (D < 0) {  
			double phi = 1.0/3 * Math.acos(-q / Math.sqrt(-cb_p));  
			double t = 2 * Math.sqrt(-p);  
			s[ 0 ] =   t * Math.cos(phi);  
			s[ 1 ] = - t * Math.cos(phi + Math.PI / 3);  
			s[ 2 ] = - t * Math.cos(phi - Math.PI / 3);  
			num = 3;  
		}  
		/* one real solution*/
		else {  
			double sqrt_D = Math.sqrt(D);  
			double u = Math.cbrt(sqrt_D - q);  
			double v = - Math.cbrt(sqrt_D + q);  
			s[ 0 ] = u + v;  
			num = 1;  
		}  
		/* resubstitute */  
		sub = 1.0/3 * A;  
		for (i = 0; i < num; ++i)  
			s[ i ] -= sub;  
		return num;  
	}  

	/**Calculates intersection between curve and line.
	 * Useful for POI and collision detection. */
	public ArrayList<Vec2f> collidesLine(LineSegment l) {
		//Express line in form: Ax + By + C = 0
		float A = l.end.y - l.start.y;		//A = y2-y1
		float B = l.start.x - l.end.x;		//B = x1-x2
		float C = l.start.x*(l.start.y-l.end.y) + l.start.y*(l.end.x-l.start.x);	//C = x1*(y1-y2)+y1*(x2-x1)
		//Coefficients of Bezier polynomial
		float[] bx = bezierCoeffsX(start, ctrl_one, ctrl_two, end);
		float[] by = bezierCoeffsY(start, ctrl_one, ctrl_two, end);
		//Plug in Bezier coefficients into line equation to get degree-3 polynomial
		double[] pArr = new double[4];
		pArr[3] = A*bx[0] + B*by[0];		//t^3
		pArr[2] = A*bx[1] + B*by[1];		//t^2
		pArr[1] = A*bx[2] + B*by[2];		//t
		pArr[0] = A*bx[3] + B*by[3] + C;	//1
		//Find roots of the new polynomial, curve plugged into line 
		double[] r = new double[3];
		solveCubic(pArr, r);

		//Three roots; given -1 if invalid or imaginary
		assert r.length == 3;	

		ArrayList<Vec2f> pts = new ArrayList<Vec2f>();
		for (int i=0; i<3; i++) {
			//get t for each root
			float t = (float) r[i];		
			//Plug in t_val for root into polynomial
			Float x0 = bx[0]*t*t*t+bx[1]*t*t+bx[2]*t+bx[3];
			Float y0 = by[0]*t*t*t+by[1]*t*t+by[2]*t+by[3];
			Vec2f p = new Vec2f(x0, y0);

			//Add poi to list, then remove if out of range
			pts.add(p);

			//Check if intersections are in bounds of line seg (so far, intersections with infinite line)
			float check;
			//If not vertical line
			if ((l.end.x - l.start.x) != 0) {	
				check = (x0 - l.start.x)/(l.end.x - l.start.x);	// s=(X[0]-lx[0])/(lx[1]-lx[0]);
			}
			else {
				check = (y0 - l.start.y)/(l.end.y - l.start.y);	//s=(X[1]-ly[0])/(ly[1]-ly[0]);
			}
			//If poi is out of t e[0, 1] or off line seg, remove
			if (t<=0 || t>=1.0 || check<0 || check>1.0) {
				pts.remove(p);
			}
		}
		return pts;
	}


	/*collision detection and POI*/
	/**Double dispatch for collision and poi*/
	@Override
	public boolean collides(Shape s) {
		return s.collidesCurve(this);
	}
	@Override
	public Vec2f poi(Shape s) {
		return s.poiCurve(this);
	}

	/**Collision and point of intersection for circles. POI returns 
	 * nearest point to circle's center on curve. Doesn't really matter,
	 * since circles have arbitrary rotation and curves don't rotate.*/
	@Override
	public boolean collidesCircle(CircleShape c) {	
		//Only collide if circle in convex hull
		if (!getWideBounds().collides(c)) {
			return false;
		}
		this.setCollisionInfo(null);
		c.setCollisionInfo(null);

		Vec2f p = this.nearestPointOnCurve(c.getCentroid());
		float dist = p.dist(c.getCentroid());
		Vec2f mtv = p.minus(c.getCentroid()).normalized().smult(c.getRadius()-dist);
		if (dist <= c.getRadius()) {
			this.setCollisionInfo(new ShapeCollisionInfo(this, c, mtv));
			c.setCollisionInfo(new ShapeCollisionInfo(c, this, mtv.smult(-1)));			
			return true;
		}
		return false;
	}
	@Override
	public Vec2f poiCircle(CircleShape c) {
		return this.nearestPointOnCurve(c.getCentroid());
	}

	@Override
	public boolean collidesAAB(AARectShape aab) {
		PolygonShape p = aab.rectToPoly();
		boolean collides = this.collidesPolygon(p);
		aab.setCollisionInfo(p.getCollisionInfo());
		return collides;
	}

	/**Collision and point of intersection for Polygons.*/
	@Override
	public boolean collidesPolygon(PolygonShape p) {
		//Only check for collision if polygon in convex hull
		if (!getWideBounds().collides(p)) {
			return false;
		}		
		this.setCollisionInfo(null);
		p.setCollisionInfo(null);

		boolean collision = false;
		Vec2f[] verts = p.getVertices();		
		ArrayList<LineSegment> sides = new ArrayList<LineSegment>();
		ArrayList<LineSegment> mtv_segs = new ArrayList<LineSegment>();
		_pois.clear();
		//For each side of Polygon:
		for (int i=0; i<verts.length; i++) {
			Vec2f src = verts[i];
			//if not last segment, endpoint is next vertex
			Vec2f dst;
			if (i < verts.length-1) 
				dst = verts[i+1];
			else 
				dst = verts[0];
			LineSegment seg = new LineSegment(src, dst);
			ArrayList<Vec2f> pts = this.collidesLine(seg);
			if (!pts.isEmpty()) {
				collision = true;
				for (Vec2f poi: pts) {
					float ldist = dst.dist(poi);
					float rdist = src.dist(poi);
					if (Math.abs(ldist) < Math.abs(rdist)) 
						mtv_segs.add(new LineSegment(poi, dst));
					else 
						mtv_segs.add(new LineSegment(src, poi));					
					_pois.add(poi);
				}	
			}	
			sides.add(seg);
		}
		if (collision) {			
			Vec2f poi = Vec2f.average(_pois);
			float ct = this.nearestTOnCurve(poi);			
			Vec2f unit_norm = this.findNormal(ct).normalized();
			Vec2f closestPoint = getCasteljauPoint(ct);
			Vec2f pointAlongNorm = closestPoint.plus(unit_norm);
			LineSegment closestSide = null;
			Vec2f sidePoint = null;
			float minDist = Float.POSITIVE_INFINITY;

			for (LineSegment side: sides) {			
				Vec2f xpoint = Vec2f.lineIntersect(closestPoint, pointAlongNorm, side.start, side.end);
				if (xpoint != null) {
					float dist = Math.abs(xpoint.dist(closestPoint));					
					if (dist < minDist || closestSide == null) {
						minDist = dist;
						sidePoint = xpoint;
						closestSide = new LineSegment(closestPoint, xpoint);
					}
				}
			}
			//Check if intersection point on shape is below curve
			if ((sidePoint.minus(closestPoint)).dot(unit_norm) < 0) 
				mtv_segs.add(closestSide);

			Vec2f mtv = null;
			float maxMag = 0;
			LineSegment mtvSeg = null;
			for (LineSegment seg: mtv_segs) {
				Vec2f a_proj = seg.start.projectOnto(unit_norm);
				Vec2f b_proj = seg.end.projectOnto(unit_norm);
				float mag = a_proj.dist(b_proj);
				Vec2f mt = unit_norm.smult(mag);
				//Use largest MTV
				if (mag > maxMag) {
					mtv = mt;
					maxMag = mag;
					mtvSeg = seg;
				}			
			}		
			if (mtv != null) {
				CircleShape circle = new CircleShape(closestPoint, .5f);
				circle.setColor(Color.black);
				_drawDots.add(circle);
				_drawLines.add(mtvSeg);
				this.setCollisionInfo(new ShapeCollisionInfo(this, p, mtv));
				p.setCollisionInfo(new ShapeCollisionInfo(p, this, mtv.smult(1)));
			}
		}	
		return collision;
	}

	@Override
	public Vec2f poiPolygon(PolygonShape p) {
		if (_pois.isEmpty())
			return null;
		return Vec2f.average(_pois);
	}

	@Override
	public Vec2f getCentroid() {
		ArrayList<Vec2f> c = new ArrayList<Vec2f>();
		c.add(start);
		c.add(end);
		return Vec2f.average(c);
	}
	
	/**Draw border at getBorderWidth or set border width to 0*/
	public void drawBorder(boolean border) {
		_drawBorder = border;
	}

	/* Draws a Cubic Bezier Curve (with two control _points) by first
	 * recursively getting a List of _points which should be drawn, 
	 * depending on resolution/smoothness set by THRESHOLD (findDrawingPoints),
	 * then draws a line segment between each of the drawing _points.*/
	@Override
	public void draw(Graphics2D g) {	
		g.setStroke(new BasicStroke(0));
		g.setColor(super.getBorderColor());
		if (_drawBorder) {
			g.setStroke(new BasicStroke(getBorderWidth()));
		}

		/* line segment method */
		/*for (LineSegment seg: _segs) {
			seg.draw(g);
		}
		 */

		g.setColor(super.getColor());

		/*CubicCurve2D method */
		CubicCurve2D curve = new CubicCurve2D.Float(start.x, start.y, ctrl_one.x, ctrl_one.y, ctrl_two.x, ctrl_two.y, end.x, end.y);
		g.draw(curve);


		/////VISUALIZATION FOR DEBUGGING
		/*		for (CircleShape circle: _drawDots) {
=======
/////VISUALIZATION FOR DEBUGGING
/*		for (CircleShape circle: _drawDots) {
>>>>>>> 823cfa1776f6360eda340fd3ee0e2b4c0c232d52
			circle.draw(g);
		}
		g.setColor(Color.WHITE);
		for (LineSegment line: _drawLines) {
			line.draw(g);
		}*/
	}

	
	/**
	 * see http://www.iut-arles.up.univ-mrs.fr/web/romain-raffin/sites/romain-raffin/IMG/pdf/Solving_the_nearest_point_on_curve_problem.pdf
	 * @return
	 */
	private Polynomial getPCurveDotProduct(Vec2f p) {
		int n = 3;
		Polynomial result = Polynomial.wrap(0d);
		for(int i = 0; i <= n; i++) {
			for(int j = 0; j <= n-1; j++) {
				float z = choose(n, i)*choose(n-1, j)/choose(2*n-1, i+j);
				Vec2f c = getPoint(i).minus(p);
				Vec2f d = (getPoint(j+1).minus(getPoint(j))).smult((float)n);
				float w = c.dot(d.smult(z));
				Polynomial toAdd = getBernsteinPolynomial(2*n-1, i+j);
				toAdd = PolynomialOps.multiply(toAdd, Polynomial.wrap(w), null);
				result = PolynomialOps.add(result, toAdd, null);
			}
		}
		return result;
	}

	/**
	 * Gets the desired Bernstein polynomial. See
	 * http://www.iut-arles.up.univ-mrs.fr/web/romain-raffin/sites/romain-raffin/IMG/pdf/Solving_the_nearest_point_on_curve_problem.pdf
	 * @param n
	 * @param i
	 * @return
	 */
	public static Polynomial getBernsteinPolynomial(int n, int i) {
		assert(0 <= i && i <= n);

		Polynomial one = Polynomial.wrap(1d);
		Polynomial t  = Polynomial.wrap(0d, 1d); 
		Polynomial oneMinust = Polynomial.wrap(1d, -1d);
		Polynomial scalar = Polynomial.wrap(choose(n, i));

		Polynomial p1 = one, p2 = one;
		for(int j = 0; j < i; j++) {
			p1 = PolynomialOps.multiply(p1, t, null);
		}
		for(int j = 0; j < n-i; j++) {
			p2 = PolynomialOps.multiply(p2, oneMinust, null);
		}

		Polynomial result = PolynomialOps.multiply(p1, p2, null);
		return PolynomialOps.multiply(result, scalar, null);



	}

	/**
	 * gets the point (control or end point) corresponding to the given index
	 * @param i
	 * @return
	 */
	private Vec2f getPoint(int i) {
		switch(i){
		case 0: return start;
		case 1: return ctrl_one;
		case 2: return ctrl_two;
		case 3: return end;
		}
		assert(false);
		return null;
	}

	/**
	 * Math choose function (i.e. 'n choose k')
	 * @param n
	 * @param k
	 * @return
	 */
	public static float choose(int n, int k) {
		if(n < 0 || k > n || k < 0)  {
			System.err.println("Error: invalid input for choose");
			return 0f;
		}
		int numerator = 1;
		int denominator = 1;
		for(int i = 1; i <=k; i++) {
			numerator*=n-i+1;
			denominator*=i;
		}
		return numerator/denominator;
	}


	
}
