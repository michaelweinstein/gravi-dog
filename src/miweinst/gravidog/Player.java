package miweinst.gravidog;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import miweinst.engine.collisiondetection.PhysicsCollisionInfo;
import miweinst.engine.entityIO.Input;
import miweinst.engine.shape.CircleShape;
import miweinst.engine.shape.Shape;
import miweinst.engine.world.GameWorld;
import miweinst.engine.world.PhysicsEntity;
import cs195n.Vec2f;

public class Player extends PhysicsEntity {

	private static final float MOVEMENT_FORCE_COEFFICIENT = 3.0f;
	private final float JUMP_IMPULSE_COEFFICIENT = 12f;
	private float GOAL_VELOCITY_COEFFICIENT = 12f;
	private final float FRICTION_COEFFICIENT = .5f;
	private final float SPRITE_CYCLE_PERIOD = .8f;
	private final float WALL_IMPULSE_COEFFICIENT = 1f;
	private Shape _shape;
	private boolean _gravitySwitched;
	private float _secondsSinceFirstFrame;
	private boolean isFacingRight;

	
	//Inputs: defined as anonymous classes, only one method to override
	//doSetColor sets Player color; args "color"
	public Input doSetColor = new Input()
	{
		public void run(Map<String, String> args) {
			setColor(args.get("color"));
		}
	};
	//doSetBorder sets border attr.; args "border_width", "border_color"
	public Input doSetBorder = new Input()
	{
		public void run(Map<String, String> args) {
			setBorder(args.get("border_width"), args.get("border_color"));
		}
	};
	//switch Gravity
	public Input doGravitySwitch = new Input()
	{
		public void run(Map<String, String> args) {
			if (!_gravitySwitched) {
				PhysicsEntity.GRAVITY = GRAVITY.smult(-1);
				_gravitySwitched = true;
			}
		}
	};
	//reset gravitySwitched so doGravitySwitch can run
	public Input doResetGravitySwitch = new Input()
	{
		public void run(Map<String, String> args) {
			_gravitySwitched = false;
		}
	};
	//SAVE AND LOAD IO
	//store data regarding player's progress in levell to be written to resources/save_data.txt
	public Input doStore = new Input() {
		/* For checkpoint saving in M4, doWrite actually called at same
		 * time as data storage. So for M4, doWrite not run from connection 
		 * but rather from doStore.run*/
		public void run(Map<String, String> args) {
			/*if (args.containsKey("checkpoint"))
				_saveData.add(new String("checkpoint: " + args.get("checkpoint")));

			//Store Player's color for restoration at save.
			Color curr = getShapeColor();
			String col = new String(Integer.toString(curr.getRed()) + "," +  Integer.toString(curr.getGreen()) + "," + Integer.toString(curr.getBlue()));
			_saveData.add(new String("color: " + col));

			doWrite.run(args);*/
		}
	};
	//write any stored data to resources/save_data.txt, only once
	public Input doWrite = new Input() {
		public void run(Map<String, String> args) {
/*			if (!_dataWritten) {
				FileIO.write(_saveData);
				_dataWritten = true;
			}*/
		}
	};
	//resets all save data to initial values; called on quit
	public Input doResetData = new Input() {
		public void run(Map<String, String> args) {
/*			_saveData.clear();
			setDataWritten(false);
			doWrite.run(args);*/
		}
	};
	//reads whether or not Player has reached checkpoint, and Player's prev color
	public Input doRead = new Input() {
		public void run(Map<String, String> args) {
			//i.e. if save_data loaded successfully
			if (args != null) {
				/* if (args.containsKey("color")) {
					setShapeColor(MWorld.stringToColor(args.get("color")));
				}*/
			}
		}
	};

	
	private boolean _lastCollided;
	
	public Player(GameWorld world) {
		super(world);
		//		_world = world;
		
		
		
		this.setStatic(false);		
		_gravitySwitched = false;
		_secondsSinceFirstFrame = 0f;
		
		_lastCollided = false;
					
//		_saveData = new ArrayList<String>();
//		_dataWritten = false;
	}
	
	/*Allows method to override the built in check
	 * on writing save data to file, specifically
	 * used to reset data.*/
/*	private void setDataWritten(boolean b) {
//		_dataWritten = b;
	}*/
	
	@Override
	public void onTick(long nanosSincePreviousTick) {
		super.onTick(nanosSincePreviousTick);
		setGravity();
		
		//Store for use later in draw
		_lastCollided = didCollide();		
		if(_lastCollided) {
			applyfriction();
		}
				
		_secondsSinceFirstFrame += nanosSincePreviousTick/1000000000f;
		_secondsSinceFirstFrame%=SPRITE_CYCLE_PERIOD;
	}	
	
	private void setGravity() {
		List<Vec2f> otherMTVs = new ArrayList<Vec2f>();
		List<PhysicsCollisionInfo> infos = getCollisionInfo();
		PhysicsCollisionInfo infoToBeUsed = null;
		float max = 0;
		//Choose mtv with largest cross product with gravity
		for (PhysicsCollisionInfo i: infos) {
			if (i != null && i.other.isGravitational()) {
				float diffValue = Math.abs(i.mtv.normalized().cross(GRAVITY.normalized()));
				if(diffValue >= max) {
					infoToBeUsed = i;
					max = diffValue;
				}
				otherMTVs.add(i.mtv);
			}
		}
		if (infoToBeUsed != null) {
			Vec2f mtv = infoToBeUsed.mtv;
			float mag = GRAVITY.mag();
						
			Vec2f mtv_norm = mtv.normalized();
			GRAVITY = mtv_norm.smult(-mag);

			//If gravity change between entities
			if(otherMTVs.size() >= 2) {
				/*Give player a small nudge perpindicular to mtv 
				* to avoid switching gravity on every tick
				* when the player is wedged in a corner. */
				Vec2f newPlatformMTV = infoToBeUsed.mtv;
				Vec2f oldPlatformMTV = otherMTVs.get(0) == newPlatformMTV ? otherMTVs.get(1) : otherMTVs.get(0);
				Vec2f dir = newPlatformMTV.getNormal().normalized();
				if(dir.dot(oldPlatformMTV) < 0) {
					dir = dir.invert();
				}
				this.applyImpulse(dir.smult((float) (Math.sqrt(getMass())*getMass()*WALL_IMPULSE_COEFFICIENT)), getCentroid());

			}			
		}
	}

	private void applyfriction() {
		//Opposes movement perpindicular to gravity
		Vec2f gravityNormal = GRAVITY.isZero() ? new Vec2f(1f, 0) : GRAVITY.getNormal().normalized();
		Vec2f gravityNormalInPlayerDirection = gravityNormal.dot(getVelocity()) > 0 ? gravityNormal : gravityNormal.invert();
		Vec2f force =gravityNormalInPlayerDirection.smult(-FRICTION_COEFFICIENT*GRAVITY.mag()*getMass());
		this.applyForce(force, getCentroid());
	}

	/* Applies upward impulse if colliding with something by set _jumpImpulse
	 * value.*/
	public void jump() {
		//If there was a valid collision
		List<PhysicsCollisionInfo> infos = getCollisionInfo();
		PhysicsCollisionInfo info = null;
		for (PhysicsCollisionInfo i: infos) {
			if (i != null) {
				info = i;
			}
		}
		if (info != null) {
			Vec2f mtv = info.mtv;
			this.applyImpulse(mtv.normalized().smult((float) (JUMP_IMPULSE_COEFFICIENT*(float)Math.sqrt(getMass())*getMass())), getCentroid());
		}
	}
	
	public void moveRight() {
		Vec2f dir = GRAVITY.getNormal().normalized();
		goalVelocity(dir.smult(getGoalVelocity()));
		isFacingRight = true;
	}
	
	public void moveLeft() {
		Vec2f dir = GRAVITY.getNormal().normalized().invert();
		goalVelocity(dir.smult(getGoalVelocity()));
		isFacingRight = false;
	}
	
	public void moveDown() {
		Vec2f dir = GRAVITY.normalized();
		goalVelocity(dir.smult(getGoalVelocity()));
	}

	/*Center of Player's body.*/
	public Vec2f getCenter() {
		return _shape.getCentroid();
	}
	
	private float getGoalVelocity() {
		return (float)(GOAL_VELOCITY_COEFFICIENT*Math.sqrt(getMass()));
	}

	@Override
	public void draw(Graphics2D g) {
		
		BufferedImage[] walking = GravidogResources.getValue("walking");
		BufferedImage[] running = GravidogResources.getValue("running");
		BufferedImage[] standing = GravidogResources.getValue("standing");
		BufferedImage[] jumping = GravidogResources.getValue("jumping");
		
		BufferedImage[] currentImageSequence;
		
		float dir = isFacingRight ? 1f : -1f;
		float currRelativeVel = Math.abs(getVelocity().dot(GRAVITY.getNormal().normalized()));
		
		
		if (currRelativeVel < getGoalVelocity()/50) {
			currentImageSequence = standing;
		} else if (currRelativeVel < getGoalVelocity()/2) {
			currentImageSequence = walking;
		} else {
			currentImageSequence = running;
		}
		
		if(!_lastCollided) {
			currentImageSequence = jumping;
		}
		
		int i = (int)((_secondsSinceFirstFrame/SPRITE_CYCLE_PERIOD)*currentImageSequence.length);
		BufferedImage currentImage = currentImageSequence[i];
		
		AffineTransform at = new AffineTransform();
		float playerWidth = 2*((CircleShape)getShape()).getRadius();
		float scale = playerWidth/currentImage.getWidth();
		scale*= 1.5f; //make the image a little larger to accommodate image margins
		
		Vec2f gravDir = GRAVITY.normalized();
		
		float theta = 0f;
		if(gravDir.x != 0f) {
			theta = (float) Math.atan(gravDir.y/gravDir.x); //offset from positive x axis
		} else {
			theta = (float) (gravDir.y < 0 ? -Math.PI/2 : Math.PI/2);
		}
		if(gravDir.x < 0) {
			theta += Math.PI; //since the range of atan is -pi/2 to pi/2 exclusive, here we get the full range
		}
		theta += Math.PI/2; //convert to offset from negative y axis
		
		at.translate(getLocation().x, getLocation().y);
		at.scale(scale, -1f*scale);
		at.rotate(-theta);
		at.scale(dir, 1f);
		at.translate(-125, -65); //pixel offset to center the image
		
		
		g.drawImage(currentImage, at, null);
		
	}

	@Override
	public Input getInput(String s) {
		if (new String("doGravitySwitch").equals(s)) {
			return doGravitySwitch;
		}
		if (new String("doResetGravitySwitch").equals(s)) {
			return doResetGravitySwitch;
		}
		if (new String("doSetColor").equals(s)) {
			return doSetColor;
		}
		if (new String("doSetBorder").equals(s)) {
			return doSetBorder;
		}
		if (new String("doStore").equals(s)) {
			return doStore;
		}
		if (new String("doWrite").equals(s)) {
			return doWrite;
		}
		if (new String("doRead").equals(s)) {
			return doRead;
		}
		System.err.println("No input found (Player.getInputOf)");
		return null;
	}

	/* Takes in a String of RGB components of a color, and changes
	 * color of the Player's shape according to corresponding integer.*/
	public void setColor(String rgb) {	
		this.setShapeColor(GameWorld.stringToColor(rgb));
	}
	public void setBorder(String width, String color) {
		getShape().setBorderWidth(Float.parseFloat(width));
		this.getShape().setBorderColor(GameWorld.stringToColor(color));
	}
	

	/*Applies force until goal velocity is reached.
	 * Force is proportional to difference between 
	 * current x-vel and goal x-vel. So force decreases
	 * as PhysicsEntity gains velocity.
	 * Separate mutators for X- and Y-component of velocity
	 * instead of wrapping in a Vec2f object because
	 * x, y are almost never set together.*/
	
	private void goalVelocity(Vec2f gv) {
		Vec2f dV = gv.minus(getVelocity().projectOnto(gv));
		Vec2f force = dV.smult(getMass()*MOVEMENT_FORCE_COEFFICIENT);
		this.applyForce(force, getCentroid());
	}
}
