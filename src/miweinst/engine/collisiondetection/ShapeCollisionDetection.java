package miweinst.engine.collisiondetection;

import miweinst.engine.beziercurve.BezierCurve;
import miweinst.engine.shape.AARectShape;
import miweinst.engine.shape.CircleShape;
import miweinst.engine.shape.CompoundShape;
import miweinst.engine.shape.PolygonShape;
import miweinst.engine.shape.Shape;

public interface ShapeCollisionDetection {	
	boolean collides(Shape s);
	boolean collidesCircle(CircleShape c);
	boolean collidesAAB(AARectShape aab);
	boolean collidesCompound(CompoundShape c);
	boolean collidesPolygon(PolygonShape p);
	boolean collidesCurve(BezierCurve c);
}
