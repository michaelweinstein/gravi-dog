package miweinst.engine.collisiondetection;

import java.util.ArrayList;
import java.util.List;

import miweinst.engine.shape.Shape;
import cs195n.Vec2f;

public class ShapeCollisionInfo {
	private final Vec2f mtv;
	private final Shape curr;
	private final Shape other;
	
	public ShapeCollisionInfo(Shape thisShape, Shape otherShape, Vec2f mtvec) {
		mtv = mtvec;
		curr = thisShape;
		other = otherShape;
	}

	public Shape getShape() {
		return curr;
	}
	public Shape getOther() {
		return other;
	}
	public Vec2f getMTV() {
		return mtv;
	}

// STATIC METHODS
	public static Vec2f getMTV(List<Vec2f> shape1Points, List<Vec2f> shape2Points, List<Vec2f> linesOfSite) {
		float mtvLength = Float.POSITIVE_INFINITY;
		Vec2f mtv = null;
		for(Vec2f line : linesOfSite) {
			Vec2f tv = getTV(shape1Points, shape2Points, line);
			if(tv == null) {
				return null;
			}
			float tvLength = tv.mag();
			if(tvLength < mtvLength) {
				mtvLength = tvLength;
				mtv = tv;
			}
		}
		//How to deal with floating point rounding errors (otherwise shapes may still be in collision)? add a little bit?
		mtv = extend(mtv);
		return mtv;
	}
	public static Vec2f extend(Vec2f vector) {
		if(!vector.isZero())
			return vector.plus(vector.normalized().smult(.001f));
		return vector;
	}
	public static Vec2f getTV(List<Vec2f> shape1Points, List<Vec2f> shape2Points, Vec2f lineOfSight) {
		Vec2f projectionSurface = lineOfSight.getNormal();
		boolean projectionIsVertical = projectionSurface.x == 0f;
		//get either the x values or the y values of each set of points, to better compare them.
		List<Float> shape1ProjectionValues = getProjectionValues(shape1Points, projectionSurface, projectionIsVertical);
		List<Float> shape2ProjectionValues = getProjectionValues(shape2Points, projectionSurface, projectionIsVertical);
		float max1 = getMax(shape1ProjectionValues);
		float min1 = getMin(shape1ProjectionValues);
		float max2 = getMax(shape2ProjectionValues);
		float min2 = getMin(shape2ProjectionValues);
		//if there is a collision
		if(min1 < max2 && min2 < max1) {
			float min = Math.min(max2 - min1, max1 - min2);
			boolean rightUpShift = max2 - min1 < max1 - min2;
			if(projectionIsVertical) {
				return new Vec2f(0, 1).smult(min*(rightUpShift ? 1f : -1f));
			} else {
				//get normalized vector with positive x component
				Vec2f norm = projectionSurface.x > 0 ? projectionSurface.normalized() : projectionSurface.normalized().smult(-1f);
				return norm.smult((1f/norm.x)*min*(rightUpShift ? 1f : -1f));
			}
		}
		return null;
	}
	private static float getMin(List<Float> values) {
		assert(values.size() >= 1);
		float min = values.get(0);
		for(float v : values) {
			if(v < min) {
				min = v;
			}
		}
		return min;
	}
	private static float getMax(List<Float> values) {
		assert(values.size() >= 1);
		float max = values.get(0);
		for(float v : values) {
			if(v > max) {
				max = v;
			}
		}
		return max;
	}
	private static List<Float> getProjectionValues(List<Vec2f> points, Vec2f projectionSurface, boolean projectionIsVertical) {
		List<Float> projectionValues = new ArrayList<Float>();
		for(Vec2f p : points) {
			Vec2f projection = p.projectOnto(projectionSurface);
			projectionValues.add(projectionIsVertical ? projection.y : projection.x);
		}
		return projectionValues;
	}
///////////////////////^^^^^^^^^
}
