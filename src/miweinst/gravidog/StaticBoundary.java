package miweinst.gravidog;

import java.util.Map;

import cs195n.Vec2f;
import miweinst.engine.world.GameWorld;
import miweinst.engine.world.PhysicsEntity;

public class StaticBoundary extends PhysicsEntity {
	public static final String string = "StaticBoundary";

	private float _thickness;
	private GravidogWorld _gworld;
	private Player _player;
	public StaticBoundary(GameWorld world) {
		super(world);	
		this.setStatic(true);
		_gworld = (GravidogWorld) world;
		_player = _gworld.getPlayer();
	}
	
	@Override
	public void onTick(long nanos) {
		super.onTick(nanos);
		//Set border width once when player is not null
		if (_player == null) {
			_player = _gworld.getPlayer();
			//Code gets called once
			if (_player != null)
				this.getShape().setBorderWidth(Constants.borderWidth(_player));
		}
	}
	
	public float getThickness() {
		return _thickness;
	}
	
	//Moved this into onTick because _player was null here
/*	@Override
	public void setProperties(Map<String, String> map) {
		super.setProperties(map);
	}*/	
}
