package miweinst.gravidog;

import java.awt.Color;
import java.awt.Graphics2D;

import miweinst.engine.Tuple;
import miweinst.engine.collisiondetection.SeparatingAxis;
import miweinst.engine.shape.CircleShape;
import miweinst.engine.world.GameWorld;
import miweinst.engine.world.PhysicsEntity;
import cs195n.Vec2f;

public class Grenade extends PhysicsEntity {
	public static final String string = "Grenade";

	private GameWorld _world;
//	private Player _player;
	private CircleShape _grenade;
	private CircleShape _fire;
	private float _fireRad;
	private boolean _exploded;
	private int _raycastResolution;
	private float _blastRadius;
	private float _strength;
	
	//Shouldn't need world constructor because only instantiated by Player
/*	public Grenade(GameWorld world) {
		super(world);
		_fireRad = .75f;
		_exploded = false;
		this.setMass(.3f);
		_blastRadius = 25;
		_strength = 20;
		_raycastResolution = 10;		
	}*/
	
	public Grenade(GameWorld world, Player player) {
		super(world);		
		_world = world;
//		_player = player;
		Vec2f initialLoc = player.getCenter();
		_grenade = new CircleShape(initialLoc, .75f);
		_grenade.setColor(new Color(26, 143, 26));
		_fireRad = _grenade.getRadius();
		_exploded = false;
		this.setShape(_grenade);
		this.setDensity(.3f);
		//Radius inside which Entities are given impulse
		_blastRadius = 25;
		//i.e. 20 times ray vector; multiplyer keeps impulse constant over any # of rays cast
		_strength = 20;
		//# rays grenade casts on each entity; in case entities overlap
		_raycastResolution = 10;
	}
	
	/*In case must pass in null Player, set valid reference
	 * here after constructor.*/
/*	public void setPlayer(Player player) {
		_player = player;
	}*/
	
	/*Sets/Gets blast radius of grenade impulse*/
	public float getBlastRadius() {
		return _blastRadius;
	}
	public void setBlastRadius(float rad) {
		_blastRadius = rad;
	}
	/*Sets/Gets number of rays cast on each shape*/
	public int getRaycastResolution() {
		return _raycastResolution;
	}
	public void setRaycastResolution(int res) {
		_raycastResolution = res;
	}
	/*Sets/Gets strength of grenade impulse.*/
	public float getStrength() {
		return _strength;
	}
	public void setStength(float strength) {
		_strength = strength;
	}
	
	@Override
	public void onTick(long nanosSincePreviousTick) {
		super.onTick(nanosSincePreviousTick);
		if (_fire != null) 
			_fireRad *= 1.25;
		if (_fireRad >= _blastRadius) {
			_fireRad = 0;
			_fire = null;
			_exploded = true;
		}
	}
	
	@Override
	public boolean collides(PhysicsEntity e) {
		boolean collision = super.collides(e);
		if (collision) {
			this.explode();
		}
		return collision;
	}
	
	/*Returns whether or not grenade is finished exploding*/
	public boolean isDead() {
		return _exploded;
	}
	
	/*Casts ray at shapes in all directions to apply impulse to
	 * Entities within a certain blast radius. Uses Projections on 
	 * axis normal to ray to calculate direction of impulse to apply.
	 * Player must not be null; but that's OK because Grenade is only ever
	 * instantiated by a Player, which passes instance of itself.*/
	public void explode() {
		_fire = new CircleShape(new Vec2f(this.getLocation().x - _fireRad, this.getLocation().y - _fireRad), _fireRad);
		_fire.setColor(Color.ORANGE);		
		//For each entity, raycast multiple times on them in small increments of length
		for (PhysicsEntity e: _world.getEntitiesToArr()) {
			Vec2f src = _grenade.getCentroid();
			Vec2f rayDir = e.getLocation().minus(src).normalized();
			//Axis perpendicular to ray
			Vec2f axis = new Vec2f(-rayDir.y, rayDir.x).normalized();
			SeparatingAxis sep = new SeparatingAxis(axis);
			//Double dispatch projection design
			Vec2f proj = e.getShape().projectOnto(sep);		
			//Project ray src onto axis
			Vec2f dstMid = e.getShape().getLocation();
			float mid = src.dot(axis);
			//center to min
			float offA = proj.x - mid;
			//center to max
			float offB = proj.y - mid;
			float L = Math.abs(proj.y - proj.x);
			float inc = L / _raycastResolution;
			Vec2f[] dsts = new Vec2f[_raycastResolution];
			for (int i=0; i < dsts.length; i++) {
				if (i == 0) 
					dsts[i] = dstMid.plus(axis.smult(offA));
				else if (i == dsts.length-1) 
					dsts[i] = dstMid.plus(axis.smult(offB));
				else {
					dsts[i] = dsts[i-1].plus(axis.smult(inc));
				}
			}
			float multiplyer = _strength / _raycastResolution;
			for (int i=0; i<dsts.length; i++) {
				Vec2f dst = dsts[i];
				Tuple<PhysicsEntity, Vec2f> cast = _world.castRay(src, dst, this);
				if (cast != null) {
					if (this.getLocation().dist(cast.x.getLocation()) < _blastRadius) {
						 cast.x.applyImpulse(cast.y.minus(src).smult(multiplyer), cast.y);
					}
				}
			}
		}
	}
	
	@Override
	public void draw(Graphics2D g) {
		super.draw(g);
		if (_fire != null) {
			_fire.setRadius(_fireRad);
			_fire.draw(g);
		}
	}
}
