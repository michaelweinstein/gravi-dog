package miweinst.engine.beziercurve;

import java.util.ArrayList;
import java.util.Map;

import miweinst.engine.shape.Shape;
import miweinst.engine.world.GameWorld;
import miweinst.engine.world.PhysicsEntity;
import cs195n.Vec2f;

/*Used for Curve boundaries or ramps. setClosed method
 * sets whether or not path forms a closed shape. Default
 * is a closed curve.*/

public class CurvedPathEntity extends PhysicsEntity {
	
	private BezierPath _bezierPath;
	private ArrayList<Vec2f> _knots;
	private ArrayList<Vec2f> _actrls;
	private ArrayList<Vec2f> _bctrls;
	private boolean _closed;
	
	public CurvedPathEntity(GameWorld world) {
		super(world);
		_knots = new ArrayList<Vec2f>();
		_bezierPath = new BezierPath();
		_closed = true;
		init();
	}
	public CurvedPathEntity(GameWorld world, ArrayList<Vec2f> points) {
		super(world);
		_knots = points;
		_bezierPath = BezierPath.generateClosedCurve(_knots.toArray(new Vec2f[_knots.size()]), _actrls, _bctrls, _closed);	
		_closed = true;
		init();
	}
	private void init() {
		_actrls = new ArrayList<Vec2f>();
		_bctrls = new ArrayList<Vec2f>();
		super.setShape(_bezierPath);		
		this.setStatic(true);	//curve segments already static by default
		this.setVisible(true);
	}
	
	/*THIS IS AN EMPTY ONTICK METHOD BECAUSE BUGS.*/
	@Override
	public void onTick(long nanos) {
		//WHYYYYYYYY ?????
	}

	/*Sets whether or not this BezierPath is a closed shape.*/
	public void setClosed(boolean closed) {
		_closed = closed;
	}
	
	@Override
	public void setShape(Shape s) {
		_actrls.clear();
		_bctrls.clear();
		_knots.add(s.getLocation());
		_bezierPath = BezierPath.generateClosedCurve(_knots.toArray(new Vec2f[_knots.size()]), _actrls, _bctrls, _closed);		
		super.setShape(_bezierPath);
	}
	
	@Override
	public void setProperties(Map<String, String> props) {
		super.setProperties(props);
		//White border necessary to make distinction between level ground and death void
		if (props.containsKey("gravitational_border")) {
			_bezierPath.setGravitationalBorder(Boolean.parseBoolean(props.get("gravitational_border")));
		}
		if (props.containsKey("fill")) {
			_bezierPath.setFilled(Boolean.parseBoolean(props.get("fill")));
		}
		if (props.containsKey("closed")) {
			_closed = Boolean.parseBoolean(props.get("closed"));
		}
		if (props.containsKey("color")) {
			_bezierPath.setColor(GameWorld.stringToColor(props.get("color")));
		}
	}
}
