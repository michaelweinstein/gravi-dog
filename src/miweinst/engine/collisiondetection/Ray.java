package miweinst.engine.collisiondetection;

import cs195n.Vec2f;
import miweinst.engine.Tuple;
import miweinst.engine.shape.AARectShape;
import miweinst.engine.shape.CircleShape;
import miweinst.engine.shape.PolygonShape;
import miweinst.engine.shape.Shape;

public class Ray {
	private Vec2f _src;
	private Vec2f _dst;
	private Vec2f _dir;
	public Ray(Vec2f src, Vec2f dst) {
		_src = src;
		_dst = dst;
		_dir =  dst.minus(src).normalized();
	}
	
	public Tuple<Float, Vec2f> rayEdge(Vec2f a, Vec2f b) {
		//Edge dir vec
		Vec2f m = a.minus(b).normalized();
		//Edge normal
		Vec2f n = new Vec2f(-m.y, m.x).normalized();
		//Cross products
		float aCross = (a.minus(_src)).cross(_dir);
		float bCross = (b.minus(_src)).cross(_dir);
		//If ray straddled by edge
		if (aCross*bCross < 0) {
			float t = ((b.minus(_src)).dot(n))/(_dir.dot(n));
			Vec2f q = _src.plus(_dir.smult(t));		
			if (t > 0) {
				return new Tuple<Float, Vec2f>(t, q);
			}
			else return null;
		}	
		return null;
	}
	
//////DON'T USE REFLECTION, FIND ANOTHER WAY.
	public Vec2f cast(Shape s) {
		if (s instanceof CircleShape) {
			return cast((CircleShape) s);
		}
		if (s instanceof AARectShape) {
			return cast((AARectShape) s);
		}
		if (s instanceof PolygonShape) {
			return cast((PolygonShape) s);
		}
		else return null;
	}

	/*Cast this ray on the CircleShape circle,
	 * and return the point of intersection.*/
	public Vec2f cast(CircleShape circle) {
		Vec2f c = circle.getCentroid();
		Vec2f proj = c.projectOntoLine(_src, _dst);
		if (c.dot(_dir) - _src.dot(_dir) > 0 ) {
			if (circle.contains(proj)) {	
				float L = _src.dist(proj);
				float r = circle.getRadius();
				float x = c.dist(proj);
				if (!circle.contains(_src)) {
					return _src.plus(_dir.smult(L - (float)Math.sqrt(r*r - x*x)));
				}
				else return _src.plus(_dir.smult(L + (float)(Math.sqrt(r*r - x*x))));
			}
		}
		return null;
	}
	
	/*Cast this ray on AARectShape r, forward
	 * method to cast(PolygonShape) after converting
	 * AARectShape to an equivalent Polygon.*/
	public Vec2f cast(AARectShape r) {
		return this.cast(r.rectToPoly());
	}
	
	/*Cast this ray on the PolygonShape p,
	 * and return point of intersection Vec2f.*/
	public Vec2f cast(PolygonShape p) {
		Tuple<Float, Vec2f> minT = null;
		Vec2f[] verts = p.getVertices();
		for (int i=0; i < verts.length; i++) {			
			Vec2f start = verts[i];
			Vec2f end;
			//Endpoint is first vertex in last iteration
			if (i < verts.length-1) 
				end = verts[i+1];
			else 
				end = verts[0];
			Tuple<Float, Vec2f> t = this.rayEdge(start, end);
			if (minT == null) 
				minT = t;
			else {
				//Store the closest intersection with edge
				if (t != null) {
					if (t.x < minT.x) {
						minT = t;
					}
				}	
			}
		}
		if (minT != null) 
			return minT.y;
		return null;
	}
}
