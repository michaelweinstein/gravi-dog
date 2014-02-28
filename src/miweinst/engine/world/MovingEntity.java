package miweinst.engine.world;

import java.awt.Color;
import java.awt.Graphics2D;

import miweinst.engine.shape.CircleShape;
import miweinst.engine.shape.Shape;
import cs195n.Vec2f;

/**
 * This subclass defines a game object that
 * moves. That means that this Entity has a 
 * Shape, set with methods that mutate properties.
 * 
 * Also has methods that define an Entity with
 * health that can be damaged. The corresponding
 * mutator methods execute if boolean _isDamagable 
 * is set to true (it's initialized to false).
 * @author miweinst
 */

public abstract class MovingEntity extends Entity {

	/* Entity visualization, or if Shape is
	 * not visible and Sprite is specified in 
	 * subclass, this _shape can act as Bounds 
	 * of Entity.*/
	private Shape _shape;
	
	//Movement vars
	private float _dx;
	private float _dy;
	private boolean _freeMoving;
	
	//Damagable vars
	private boolean _isDamagable;
	private float _health;
	private float _initialHealth;
	private boolean _hostile;
	private boolean _showHealth;
	
	public MovingEntity(GameWorld world) {
		super(world);	
		
		//Start null
		_shape = null;
		
		//Initialize movement to 0
		_dx = 0;
		_dy = 0;
		_freeMoving = false;
		
		//Initialize to not damagable
		_isDamagable = false;
		_initialHealth = 100;
		_health = 0;
		_hostile = false;
		_showHealth = false;
	}
	
	/* This method is mostly used by subclasses
	 * to set the shape that is mutated by
	 * the methods in this class. Also matches
	 * the location and dimensions references in
	 * Entity to the Shape's location.*/
	protected void setShape(Shape shape) {
		if (shape != null) {
			_shape = shape;
			this.setLocation(shape.getLocation());
			this.setDimensions(shape.getDimensions());
		}
	}
	/* If Shape is null, Entity has no shape; returns a CircleShape with
	 * no dimensions, to avoid unnecessary null pointers.*/
	public Shape getShape() {
		return _shape!=null? _shape: new CircleShape(new Vec2f(0,0), 0);
	}
	public void voidShape() {
		_shape = null;
	}
	
	/* Partial override of super methods
	 *  in order to set Location/Dimension 
	 *  attributes of shape.*/
	public void setLocation(Vec2f loc) {
		super.setLocation(loc);
		if (_shape != null) _shape.setLocation(loc);
	}
	public void setX(float x) {
		super.setX(x);
		if (_shape != null)_shape.setX(x);
	}
	public void setY(float y) {
		super.setY(y);
		if (_shape != null)_shape.setY(y);
	}
	public void setDimensions(Vec2f dim) {
		super.setDimensions(dim);
		if (_shape != null)_shape.setDimensions(dim);
	}
	public void setWidth(float w) {
		super.setWidth(w);
		if (_shape != null)_shape.setWidth(w);
	}
	public void setHeight(float h) {
		super.setHeight(h);
		if (_shape != null)_shape.setHeight(h);
	}
	/*If MovingEntity is using Shape, color accessor/mutator.*/
	public Color getShapeColor() {
		if (_shape != null) return _shape.getColor();
		else return null;
	}
	public void setShapeColor(Color color) {
		if (_shape != null) _shape.setColor(color);
	}
	
	/*Gets/Sets free move; i.e. move by dx/dy vars on tick.
	 * If false, MovingEntity moves each time something
	 * calls move(). If true, Entity moves by delta _dx, _dy
	 * on each onTick.*/
	public boolean isFreeMoving() {
		return _freeMoving;
	}
	public void setFreeMoving(boolean moving) {
		_freeMoving = moving;
	}
	/*Accessors and Mutators for speed increments,
	 * used if _freeMoving is true.*/
	public Vec2f getDelta() {
		return new Vec2f(_dx, _dy);
	}
	public void setDelta(Vec2f dxdy) {
		_dx = dxdy.x;
		_dy = dxdy.y;
	}
	/*Individual accessors/mutators for free moving.
	 * Obviously not necessary, but for happy coder.*/
	public float getDx() {
		return _dx;
	}
	public void setDx(float dx) {
		_dx = dx;
	}
	public float getDy() {
		return _dy;
	}
	public void setDy(float dy) {
		_dy = dy;
	}
	
	/*When _freeMoving, moves Entity by delta x,y on each tick.*/
	public void onTick(long nanosSincePreviousTick) {
		if (_freeMoving) 
			this.move(_dx, _dy);
	}
	
	/* Takes in a dx and dy corresponding
	 * to the change in location on each tick.*/
	public void move(float dx, float dy) {
		this.setX(this.getX()+dx);
		this.setY(this.getY()+dy);
	}
	
	/* Whether specified point is inside the
	 * Shape in this Entity. Game Units. */
	@Override
	public boolean contains(Vec2f pnt) {
		boolean isIn = false;
		if (_shape != null) {
			isIn = _shape.contains(pnt);
		}
		return isIn;
	}
	
	/* Forwards collision detection query to associated shape. 
	 * If one the shapes of one of the MovingEntity 
	 * instances is null, returns false and prints error. Because
	 * of double dispatch pattern, Shape of the MovingEntity s 
	 * passed in is the Shape who's collides algorithm is executed.
	 * Note: this is only collision detection, because any response
	 * is not generic to MovingEntity. After collision detection
	 * is forwarded to the Shape, an MTV is stored in that
	 * Shape's ShapeCollisionInfo object, available by accessor.*/
	public boolean collides(MovingEntity s) {
		if (_shape != null && s.getShape() != null) 
			return _shape.collides(s.getShape());
		return false;
	}
	
	//Methods of Damagable Entity
	
	/*Returns whether this Entity can be damaged, has health.*/
	public boolean isDamagable() {
		return _isDamagable;
	}
	public void setDamagable(boolean dam) {
		_isDamagable = dam;
	}	
	/*Methods to change and get entity health*/
	public float getHealth() {
		return _health;
	}
	public void setHealth(float health) {
		if (_isDamagable) _health = health;
		else System.err.println("Entity not damagable! (MovingEntity.setHealth)");
	}
	/*Methods to change health by specified amount.*/
	public void damage(float dhealth) {
		if (_isDamagable) _health -= dhealth;
		else System.err.println("Entity not damagable! (MovingEntity.damage)");
	}
	public void undamage(float dhealth) {
		if (_isDamagable) _health += dhealth;
		else System.err.println("Entity not damagable! (MovingEntity.undamage)");
	}
	public void resetHealth() {
		_health = _initialHealth;
	}
	
	/*Methods to get or set the Entity's 
	 * starting health; does not correspond
	 * with current health, only default health.
	 * resetHealth resets curr health to initial health.*/
	public float getInitialHealth() {
		return _initialHealth;
	}
	public void setInitialHealth(float init) {
		_initialHealth = init;
	}
	
	/*Indicates membership to team, which
	 * is used if members of same team
	 * should not be able to injure teammates.*/
	public boolean getHostile() {
		return _hostile;
	}
	public void setHostile(boolean hostile) {
		_hostile = hostile;
	}
	
	/*If a health bar or visualizer is used,
	 * this boolean will store visibility*/
	public boolean isHealthVisible() {
		return _showHealth;
	}
	public void showHealth() {
		_showHealth = true;
	}
	public void hideHealth() {
		_showHealth = false;
	}

	@Override
	public void draw(Graphics2D g) {
		if (this.isVisible())
			if (_shape != null) 
				_shape.draw(g);
	}
}
