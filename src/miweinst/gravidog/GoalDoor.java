package miweinst.gravidog;

import java.awt.Color;
import java.util.Map;

import miweinst.engine.entityIO.Connection;
import miweinst.engine.world.GameWorld;
import miweinst.engine.world.SensorEntity;

/*Just a SensorEntity that draws a Rectangle on top of it,
 * so no physical interactions just sensor detection.*/

public class GoalDoor extends SensorEntity {
	
	private GravidogWorld _gworld;
	private Player _player;
	private Color _closedColor;
	private Color _openColor;
	
	public GoalDoor(GameWorld world) {
		super(world);		
		_gworld = (GravidogWorld)world;			
		_player = _gworld.getPlayer();
		super.setEntities(_player);
		this.setVisible(true);
		
		_closedColor = Color.GRAY;
		//light green
		_openColor = new Color(87, 228, 92);
		this.setShapeColor(_closedColor);
				
		this.getShape().setBorderWidth(10f);
		//Connect Sensor.onDetect to World.doDoorReached
		super.onDetect.connect(new Connection(_gworld.doDoorReached));
		setStatic(true);
	}
	/**Color when door is locked.*/
	public Color getClosedColor() {
		return _closedColor;
	}
	/**Color when door is unlocked.*/
	public Color getOpenColor() {
		return _openColor;
	}

	@Override
	public void onTick(long nanos) {
		super.onTick(nanos);
		if (_player == null) {
			_player = _gworld.getPlayer();
			super.setEntities(_player);
		}
///////////
//		System.out.println("GoalDoor Player: " + _player);
//		System.out.println("GoalDoor World: " + _gworld);
////////////////////
	}
	
	@Override
	public void setProperties(Map<String, String> props) {
		super.setProperties(props);
		
		_closedColor = Constants.DOOR_CLOSED_COL;
		_openColor = Constants.DOOR_OPEN_COL;
		
		this.setShapeColor(_closedColor);
		
/////UNNECESSARY CAUSE ALL COLORS SHOULD BE CONSTANT IN CONSTANTS CLASS
/*		//Overrides any curr shape color set in Shape properties
		this.setShapeColor(_closedColor);
		if (props.containsKey("closed_color")) {
			Color cCol = GameWorld.stringToColor(props.get("closed_color"));
			this.setShapeColor(cCol);
			_closedColor = cCol;
		}
		if (props.containsKey("open_color")) {
			_openColor = GameWorld.stringToColor(props.get("open_color"));
		}
		//"color" defaults to _openColor
		if (props.containsKey("color")) {
			_openColor = GameWorld.stringToColor(props.get("color"));
		}*/
	}
}
