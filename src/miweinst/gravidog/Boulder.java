package miweinst.gravidog;

import java.awt.Color;
import java.util.Map;

import miweinst.engine.collisiondetection.PhysicsCollisionInfo;
import miweinst.engine.entityIO.Connection;
import miweinst.engine.world.GameWorld;
import miweinst.engine.world.PhysicsEntity;
import miweinst.engine.world.SensorEntity;

public class Boulder extends SensorEntity {

	GravidogWorld _world;
	public Boulder(GameWorld world) {
		super(world);
		_world = (GravidogWorld) world;

		this.setDensity(.2f);
		this.setRestitution(.6f);
		super.onDetect.connect(new Connection(_world.doLevelLose));
		
		this.setGravitational(false);
		this.setVisible(true);
		this.setInteractive(true);
		this.setStatic(false);
	}
	
	@Override
	public boolean condition() {
		for(PhysicsEntity e : getEntities()) {
			for(PhysicsCollisionInfo i : getCollisionInfo()) {
				if(i != null && i.getOther() == e) {
					return true;
				}
			}
		}
		return false;
	}

	@Override 
	public void setProperties(Map<String, String> props) {
		super.setProperties(props);
		this.getShape().setColor(Constants.BOULDER_COL);
	}
}
