package miweinst.gravidog;

import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

import miweinst.engine.App;
import miweinst.engine.beziercurve.BezierCurveEntity;
import miweinst.engine.beziercurve.CurvedPathEntity;
import miweinst.engine.contraints.PinEntity;
import miweinst.engine.contraints.SpringEntity;
import miweinst.engine.entityIO.Connection;
import miweinst.engine.entityIO.Input;
import miweinst.engine.entityIO.Output;
import miweinst.engine.graph.HashDecorator;
import miweinst.engine.screen.Viewport;
import miweinst.engine.shape.AARectShape;
import miweinst.engine.shape.CircleShape;
import miweinst.engine.shape.PolygonShape;
import miweinst.engine.shape.Shape;
import miweinst.engine.world.GameWorld;
import miweinst.engine.world.PhysicsEntity;
import miweinst.engine.world.RelayEntity;
import miweinst.engine.world.SensorEntity;
import cs195n.CS195NLevelReader;
import cs195n.CS195NLevelReader.InvalidLevelException;
import cs195n.LevelData;
import cs195n.LevelData.ConnectionData;
import cs195n.LevelData.EntityData;
import cs195n.LevelData.ShapeData;
import cs195n.LevelData.ShapeData.Type;
import cs195n.Vec2f;

public class GravidogWorld extends GameWorld {    

	private final Vec2f GRAVITY_COEFFICIENT = new Vec2f(0f, -20f);

	//Calls levelWin if Relay is enabled
	public Input doDoorReached = new Input() {
		public void run(Map<String, String> args) {
			//_doorRelay has Connection to doLevelWin
			_doorRelay.doActivate();

		}
	};
	//Enables relay when any star is collected
	public Input doStarCollected = new Input() {
		public void run(Map<String, String> args) {
			_doorRelay.doEnable();
			LevelMenuScreen.addStar();
			_door.setShapeColor(_door.getOpenColor());
		}
	};
	//Level win
	public Input doLevelWin = new Input() {
		public void run(Map<String, String> args) {
			LevelMenuScreen levelMenu = new LevelMenuScreen(_app);
			//Show stars earned for current level
			levelMenu.updateStars();
			//Unlock next level (levelnum+1)
			/*CURRENT_LEVEL index is next level in LevelMenu because of zero-indexing.*/
			levelMenu.openLevel(LevelMenuScreen.CURRENT_LEVEL);
			_app.setScreen(levelMenu);

			///Will save game at the end of every level
			LevelMenuScreen.save();
		}
	};
	//Level lose
	public Input doLevelLose = new Input() {
		public void run(Map<String, String> args) {
			LevelMenuScreen.levelIncomplete();
			LevelMenuScreen levelMenu = new LevelMenuScreen(_app);
			_app.setScreen(levelMenu);
			LevelMenuScreen.save();
		}
	};

	private App _app;
	private Player _player;
	private GoalDoor _door;
	//Player movement using boolean state array
	private boolean[] _arrowKeyStates;

	//Variable name mapped to PhysicsEntity instance
	private HashDecorator<String, PhysicsEntity> _entities;
	private RelayEntity _doorRelay;
	private boolean _jumpboolean = false;

	public GravidogWorld(App app, Viewport viewport, File f) {
		super(app, viewport);

		_app = app;
		_arrowKeyStates = new boolean[4];
		for (int i=0; i<_arrowKeyStates.length; i++) 
			_arrowKeyStates[i]=false;

		//Unlocks door when star collected
		_doorRelay = new RelayEntity(this);
		_doorRelay.onActivate.connect(new Connection(doLevelWin));

		////////////// START LEVEL READER /////////////////////

		//Map of Strings to Class<?>, for interpreting level data
		classes = new HashDecorator<String, Class<? extends PhysicsEntity>>();                
		classes.setDecoration("PhysicsEntity", PhysicsEntity.class);
		classes.setDecoration("Player", Player.class);
		classes.setDecoration("StaticBoundary", StaticBoundary.class);
		classes.setDecoration("SensorEntity", SensorEntity.class);
		classes.setDecoration("RelayEntity", RelayEntity.class);
		classes.setDecoration("BezierCurveEntity", BezierCurveEntity.class);
		classes.setDecoration("CurvedPathEntity", CurvedPathEntity.class);
		classes.setDecoration("PinEntity", PinEntity.class);
		classes.setDecoration("SpringEntity", SpringEntity.class);
		classes.setDecoration("GoalDoor", GoalDoor.class);
		classes.setDecoration("Star", Star.class);
		classes.setDecoration("Boulder", Boulder.class);
		classes.setDecoration("LevelBounds", LevelBounds.class);
		classes.setDecoration("Block", Block.class);

		///Decoration set to each Entity read from LevelEditor!
		_entities = new HashDecorator<String, PhysicsEntity>();                                        

		LevelData level = null;
		try {
			level = CS195NLevelReader.readLevel(f);
		}
		catch (InvalidLevelException le) {
			System.err.println("The level you loaded is invalid!! MWorld()");
			le.printStackTrace();
		}
		catch (FileNotFoundException fe) {
			System.err.println("File not found!! MWorld()");
			fe.printStackTrace();
		}

		if (level != null) {
			//Each Entity in Level
			for (EntityData ent: level.getEntities()) {
				//Make instance of PhysicsEntity
				String entityClass = ent.getEntityClass();
				String entityName = ent.getName();

				//Create new Entity instance out of Class 
				PhysicsEntity entity = null;
				try {
					Constructor<?> c = classes.getDecoration(entityClass).getConstructor(GameWorld.class);
					entity = (PhysicsEntity) c.newInstance(this);
				} catch (Exception e) {
					System.err.println("Exception...: " + e.getMessage());
					e.printStackTrace();
				}    

				if (entity != null) {
					//Shapes in Entity
					for (ShapeData s: ent.getShapes()) {
						Type shapeType = s.getType();
						Shape shape = null;
						if (shapeType == Type.CIRCLE){
							float rad = s.getRadius();
							shape = new CircleShape(s.getCenter(), rad);
						} else if (shapeType == Type.BOX) {
							shape = new AARectShape(s.getMin(), new Vec2f(s.getWidth(), s.getHeight())).rectToPoly();
						} else if (shapeType == Type.POLY) {
							shape = new PolygonShape(PolygonShape.getCentroidOf(s.getVerts()), s.getVerts().toArray(new Vec2f[s.getVerts().size()]));
						}
						//Parse Shape properties in Entity
						if (shape != null) {                                                        
							//Set properties of Shape
							shape.setProperties(s.getProperties());                                                        
							//Add Shape to Entity
							entity.setShape(shape);
						}                
					}
					//Set PhysicsEntity properties                                                
					entity.setProperties(ent.getProperties());

					//Add Entity to World Map
					_entities.setDecoration(entityName, entity);

					//Add Entity to GameWorld List
					//LevelBounds must be drawn first
					if (entity instanceof LevelBounds) {
						this.addEntityToFront(entity);
					} else {
						this.addEntity(entity);
					}
				}
			}

			//Each Connection in Level
			for (ConnectionData c: level.getConnections()) {
				String src = c.getSource();
				String srcOut = c.getSourceOutput();
				String dst = c.getTarget();
				String dstIn = c.getTargetInput();

				PhysicsEntity source = null;
				PhysicsEntity target = null;
				if (_entities.contains(src)) 
					source = _entities.getDecoration(src);
				else System.err.println("Connection source " + src + " does not exist!");
				if (_entities.contains(dst)) 
					target = _entities.getDecoration(dst);
				else System.err.println("Connection target " + dst + " does not exist!");

				if (source != null && target != null) {                
					Connection toAdd = null;
					Output onOut = source.getOutput(srcOut);
					Input doIn = target.getInput(dstIn);
					if (doIn != null) {
						//Pass in Input target to constructor
						toAdd = new Connection(doIn);        
						//Connect Output source here
						if (onOut !=  null) 
							onOut.connect(toAdd);
						else System.err.println("Source " + src + " has no output " + srcOut);
					}
					else System.err.println("Target " + dst + " has no input " + dstIn);
					//If valid Connection, parse Connection properties
					if (toAdd != null) {
						//Properties of connection
						toAdd.setProperties(c.getProperties());
					}
				}
			}

			//special cases
			for(PhysicsEntity entity : this.getEntities()) {
				if (entity instanceof Player) {
					_player = (Player) entity;
				}
			}
			for(PhysicsEntity entity : this.getEntities()) {
				if (entity instanceof GoalDoor) {
					_door = (GoalDoor) entity;
				}
				if (entity instanceof SensorEntity) {
					SensorEntity playerSensor = (SensorEntity) entity;
					playerSensor.setEntities(_player);
				}
			}
			assert(_player != null);
			//add player to end of list so it draws last
			this.getEntities().remove(_player);
			this.addEntities(_player);
			/* Properties of entire level
			 * Set after entities because it has 
			 * viewport scale and static GRAVITY*/ 
			this.setProperties(level.getProperties());
			PhysicsEntity.setGravity(GRAVITY_COEFFICIENT.smult((float)Math.sqrt(_player.getMass())));
		} 
		else {
			System.err.println("Level is null! MWorld()");
		}

		/////////////////^^^^^^^^^^                

		//TEST ENTITIES (Directly Instantiated)
		//Constraint Entities to test stuff
		/*		Shape pinEntityShape = new AARectShape(new Vec2f(50f, 60f), new Vec2f(15f, 4f)).rectToPoly();
        PinEntity pin = new PinEntity(this, new Vec2f(50f, 60f), pinEntityShape);
        pin.setMass(1f);
        this.addEntity(pin);*/

		/*		Shape springEntityShape = new AARectShape(new Vec2f(134f,80f), new Vec2f(10f, 10f)).rectToPoly();
		SpringEntity spring = new SpringEntity(this, springEntityShape);
		spring.setMass(1f);
		spring.setSpringConstant(100f);
		spring.setFrictionConstant(1f);
		this.addEntity(spring);*/

		/*		//Square to test stuff with
		PolygonShape entityShape = new AARectShape(new Vec2f(134f,80f), new Vec2f(10f, 10f)).rectToPoly();
        PhysicsEntity test = new PhysicsEntity(this);
        test.setShape(entityShape);
        test.setMass(1f);
        test.setGravitational(false);
        this.addEntity(test);*/

		////////////	
		//_player.doRead.run(FileIO.read());
	}

	/*Called when game is quit.*/
	public void quitReset() {
		_player.doResetData.run(new HashMap<String, String>());
	}

	/* Calls tick based on fixed timestep. Passes in an
	 * adjusted nanosSincePreviousTick so speed of entity
	 * movement remains relatively constant regardless of timestep.
	 * Collision response of Entities handled. And movement 
	 * based on boolean state array mutated in onKeyPressed.*/
	@Override
	public void onTick(long nanosSincePreviousTick) {
		super.onTick(nanosSincePreviousTick);
		//        	long nanos = nanosSincePreviousTick/super.getIterations();
		for (int i=1; i<=super.getIterations(); i++) {
			//Left key down
			if (_arrowKeyStates[0]) {
				_player.moveLeft();
			}
			//Right key down
			if (_arrowKeyStates[2]) {
				_player.moveRight();
			}
			//Down key down
			if (_arrowKeyStates[3]) {
				_player.moveDown();           
			}
			/////
			if (_arrowKeyStates[1]) {
				if (!_jumpboolean) {
					_player.jump();
					_jumpboolean = true;
				}
			}
			/////^^^
		}
	}

	/*Remove Entity from GameWorld*/
	@Override
	public void removeEntity(PhysicsEntity toRem) {                
		super.removeEntity(toRem);
	}

	/*Updates AffineTransform of Viewport's draw() method, in case panning or zooming
	 * has changed scale or pxlgameloc. Draws Path2D lazor just to visualize raycasting.*/
	@Override
	public void draw(Graphics2D g) {                
		super.draw(g);                        
	}

	//USER INPUT
	/* Arrow key sets state boolean which opens
	 * calling of goalVelocity in onTick.*/
	public void onKeyPressed(KeyEvent e) {
		int arrow = e.getKeyCode()-37;

		if(e.getKeyCode() == KeyEvent.VK_UP && !_arrowKeyStates[1]) {
			//			_player.jump();
			_jumpboolean = false;
			_arrowKeyStates[arrow] = true;
		}
		if (arrow >= 0 && arrow < _arrowKeyStates.length) {
			_arrowKeyStates[arrow] = true;
		}
	}
	/* Releasing arrow key sets state boolean back to false.*/
	public void onKeyReleased(KeyEvent e) {
		int arrow = e.getKeyCode()-37;
		if (arrow >= 0 && arrow < _arrowKeyStates.length) {
			_arrowKeyStates[arrow] = false;
		}          
	}

	public void onMousePressed(MouseEvent e) {
		//convert loc to Game Units, switch y if math coordinates
	}
	public void onMouseDragged(MouseEvent e) {
	}
	public void onMouseMoved(MouseEvent e) {
	}

	public Player getPlayer() {
		return _player;
	}
}