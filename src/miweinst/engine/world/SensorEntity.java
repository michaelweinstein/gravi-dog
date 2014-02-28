package miweinst.engine.world;

import java.util.ArrayList;
import java.util.List;

import miweinst.engine.entityIO.Output;
import miweinst.engine.shape.Shape;
import miweinst.gravidog.Boulder;
 
/* This generic SensorEntity superclass detects specified PhysicsEntities
 * that touch the area specified by a Shape, and checks for detection on
 * every tick. Since it checks on every tick, only has Output that is triggered
 * when the target is detected. 
 * 
 * This runs Output WHILE the Entity is being detected (condition is met)
 * on every tick, until it is no longer detected.*/

public class SensorEntity extends PhysicsEntity {	
	public final String string = "Entity";

	public Output onDetect;
	public Output onNoDetect;
	//Entities to watch whether collides with area
	private ArrayList<PhysicsEntity> _entities;
	
	public SensorEntity(GameWorld world) {
		super(world);
		onDetect = new Output();
		onNoDetect = new Output();
		_entities = new ArrayList<PhysicsEntity>();
		this.setInteractive(false);
		this.setVisible(false);
	}
	
	/* Take in sensor area as Shape object, and an Entity that triggers Output
	 * when enters the area. Basic Sensor functionality, can override condition() 
	 * in subclasses.*/
	public SensorEntity(GameWorld world, Shape area, PhysicsEntity... other) {
		super(world);
		//Set Entity's shape as detection area
		this.setShape(area);
		onDetect = new Output();
		onNoDetect = new Output();
		for (PhysicsEntity e: other) 
			_entities.add(e);
		//Invisible and non-interactive
		this.setInteractive(false);
		this.setVisible(false);
	}
	
	/**/
	public void setEntities(PhysicsEntity... other) {
		for (PhysicsEntity e: other) 
			_entities.add(e);
	}
	
	/* Pass a connection to define response when
	 * Sensor is activated, specifying input. Must connect
	 * Output onDetect to some other Input in order to 
	 * actually pass event.*/
/*	public void connect(Connection c) {
		onDetect.connect(c);
	}*/
		
	/* Generic collision sensor condition; can be overriden
	 * in subclasses. Checks if a player touches Sensor,
	 * which is a non-interactive entity, no collision response. 
	 * Condition checked on every tick in this.onTick(long)*/
	public boolean condition() {
		for (PhysicsEntity ent: _entities) {
			if (ent != null)
				if (this.collidesWithoutCollisionResponse(ent))
					return true;
		}
		return false;
	}
	
	/* Trigger output when condition is met, checks for itself. 
	 * Also triggers an OIutput*/
	@Override
	public void onTick(long nanosSincePreviousTick) {
		super.onTick(nanosSincePreviousTick);
		if (this.condition()) {
			onDetect.run();
		} else {
			onNoDetect.run();
		}
	}
	
	/*Returns Output mapped to String o.*/
	@Override
	public Output getOutput(String o) {
		if (new String("onDetect").equals(o)) {
			return onDetect;
		}
		if (new String("onNoDetect").equals(o)) {
			return onNoDetect;
		}
		return null;
	}
	
	public List<PhysicsEntity> getEntities() {
		return _entities;
	}
}
