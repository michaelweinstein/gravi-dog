package miweinst.engine.world;

import java.awt.Graphics2D;
import java.util.Map;

import cs195n.Vec2f;

/**
 * Single logical object within the game. Whoa, abstract.
 * 
 * Everything within Entity and its subclasses 
 * are set in game units. GameWorld will convert units
 * to pixels based on the value passed to it by Viewport
 * indicating game world origin in pixels.
 * 
 * @author miweinst
 */

public abstract class Entity {
	public final String string = "Entity";
	private Vec2f _location;
	private Vec2f _dimensions;	
	private boolean _visible;
	
	public Entity(GameWorld world) {
		_location = new Vec2f(0, 0);
		_dimensions = new Vec2f(1, 1);
		_visible = true;
	}
	
	/*Gets/Sets boolean that specifies
	 * whether Entity's Shape is drawn or not,
	 * in subclass' non-abstract draw definition.*/
	public boolean isVisible() {
		return _visible;
	}
	public void setVisible(boolean visible) {
		_visible = visible;
	}
	
	/*Get/Set Location Vec2f var*/
	public Vec2f getLocation() {
		return _location;
	}
	public void setLocation(Vec2f loc) {
		_location = loc;
	}
	/*Individually gets/sets components of Locatoin.*/
	public float getX() {
		return _location.x;
	}
	public void setX(float x) {
		_location = new Vec2f(x, _location.y);
	}
	public float getY() {
		return _location.y;
	}
	public void setY(float y) {
		_location = new Vec2f(_location.x, y);
	}
	
	/*Get/Set Dimension Vec2f var*/
	public Vec2f getDimensions() {
		return _dimensions;
	}
	public void setDimensions(Vec2f dim) {
		_dimensions = dim;
	}
	/*Individually gets/sets components of Dimension.*/
	public float getWidth() {
		return _dimensions.x;
	}
	public void setWidth(float w) {
		_dimensions = new Vec2f(w, _dimensions.y);
	}
	public float getHeight() {
		return _dimensions.y;
	}
	public void setHeight(float h) {
		_dimensions = new Vec2f(_dimensions.x, h);
	}
	
	public abstract void setProperties(Map<String, String> args);
	
	/*Method called on each tick or fixed timestep iteration.*/
	public abstract void onTick(long nanosSincePreviousTick);

	/*Depends entirely on shape of bounds of entity, 
	 * defined by associated Shape or Sprite in subclasses.*/
	public abstract boolean contains(Vec2f pnt);
	
	/*Draws associated Shape or Sprite in subclasses.*/
	public abstract void draw(Graphics2D g);
}
