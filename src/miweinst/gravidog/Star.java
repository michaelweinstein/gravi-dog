package miweinst.gravidog;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.Map;

import miweinst.engine.entityIO.Connection;
import miweinst.engine.entityIO.Input;
import miweinst.engine.gfx.sprite.Sprite;
import miweinst.engine.world.GameWorld;
import miweinst.engine.world.SensorEntity;
import cs195n.Vec2f;

public class Star extends SensorEntity {

	private GravidogWorld _gworld;
	private Player _player;
	private boolean _collected;
	private Sprite _sprite;
	public Star(GameWorld world) {
		super(world);
		
		_gworld = (GravidogWorld)world;
		_player = _gworld.getPlayer();
		super.setEntities(_player);
		
		BufferedImage[] sprite = GravidogResources.getValue("large_star");
//		_sprite = sprite[0];
		//Need to chance Sprite so pxlLoc isn't used
		_sprite = new Sprite(getDimensions(), sprite);
			
		this.setVisible(true);
		//Star can only be collected once
		_collected = false;
						
		//Connect Sensor.onDetect to World.doDoorReached
		super.onDetect.connect(new Connection(_gworld.doStarCollected));
		super.onDetect.connect(new Connection(new Input() 
		{
			public void run(Map<String, String> args) {
				setVisible(false);
				_collected = true;
				_sprite.setVisible(false);
			}
		}));
		setStatic(true);
	}
	
	/**Partial override condition() to return
	 * false if star has already been
	 * collected (i.e. _collected == true). Otherwise
	 * returns super.condition()*/
	@Override
	public boolean condition() {
		boolean condition = super.condition();
		if (_collected) {
			return false;
		}
		return condition;
	}

	/**Partial override onTick() in order to set
	 * reference to Player if null
	 * at constructor.*/
	@Override
	public void onTick(long nanos) {
		super.onTick(nanos);
		//In case Star is instantiated before Player
		if (_player == null) {
			_player = _gworld.getPlayer();
			super.setEntities(_player);
		}
	}
	
	@Override
	public void draw(Graphics2D g) {
		//Don't draw the circle
//		super.draw(g);
		float scale = _gworld.getScale();
		Vec2f dim = new Vec2f(_sprite.getCurrFrame().getWidth(), _sprite.getCurrFrame().getHeight()).sdiv(scale);
		
		Vec2f loc = getShape().getLocation();
		loc = new Vec2f(loc.x - dim.x/2, loc.y - dim.y/2);
		_sprite.draw(g, getShape().getLocation().minus(getDimensions().sdiv(2)), getShape().getWidth());
	}
}
