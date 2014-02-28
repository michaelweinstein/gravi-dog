package miweinst.gravidog;

import java.util.Map;

import miweinst.engine.world.GameWorld;
import miweinst.engine.world.PhysicsEntity;

/* This class is a non-rotatable block. It pretty much is just
 * a block that you can either push around or use gravity to push
 * around. If it's not pushable, then you need to use gravity to move it.
 * That's great for blocking doors and things like that. If it is pushable, than 
 * gravity OR the player can push it around.*/

public class Block extends PhysicsEntity {
	
	private boolean _isPushable;

	public Block(GameWorld world) {
		super(world);
		
		//Use constants
		setShapeColor(Constants.BLOCK_COL);
		
		//False by default
		_isPushable = false;
		
		//Set's density based on whether Player can push block
		setDensity(_isPushable? 1: 100);		//whether or not you want player to be able to push block
		
		setRestitution(.5f);
		setGravitational(false);
		setRotatable(false);
		
		setStatic(false);
		
		/*
		 * mass: 1000
		 * rotatable: false
		 * gravitational: false
		 * restitution: 4*/
	}	
	
	//Uses density to set whether pushable or not
	public boolean isPushable() {
		return _isPushable;
	}
	public void setPushable(boolean pushable) {
		_isPushable = pushable;
		setDensity(_isPushable? 1: 100);
	}
	
	@Override
	public void setProperties(Map<String, String> props) {
		super.setProperties(props);
		setShapeColor(Constants.BLOCK_COL);
	}
}
