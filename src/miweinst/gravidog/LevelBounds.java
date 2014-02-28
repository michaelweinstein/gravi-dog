package miweinst.gravidog;

import java.awt.Color;
import java.util.Map;

import miweinst.engine.entityIO.Connection;
import miweinst.engine.world.GameWorld;
import miweinst.engine.world.SensorEntity;

public class LevelBounds extends SensorEntity {
	
	private final static Color BGCOLOR = Constants.MAIN_BG_COL;

	private GravidogWorld _gworld;
	private boolean _saved;
	
	public LevelBounds(GameWorld world) {
		super(world);
		this.setInteractive(false);
		this.setStatic(true);	
		this.setVisible(true);
		this.setGravitational(false);	
		
		_saved = false;		
		_gworld = (GravidogWorld) world;

		Connection c = new Connection(_gworld.doLevelLose);
		this.onNoDetect.connect(c);
	}
	
	@Override
	public boolean condition() {
		//Only save on first onNoDetect
		if (_saved) {
			return true;
		}
		return super.condition();
	}
	
	@Override
	public void onTick(long nanos) {
		super.onTick(nanos);
		if (!condition()) {
			_saved = true;
		}
		
	}
	
	@Override
	public void setProperties(Map<String, String> props) {
		super.setProperties(props);
		//Override color from Level Editor
		this.setShapeColor(BGCOLOR);		
		setEntities(_gworld.getPlayer());
	}
}
