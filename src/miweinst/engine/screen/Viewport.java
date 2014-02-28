package miweinst.engine.screen;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;

import miweinst.engine.shape.AARectShape;
import miweinst.engine.shape.Shape;
import miweinst.engine.world.GameWorld;
import cs195n.Vec2f;

//graphics uses math coordinates
public class Viewport {	

	//Shape of Viewport screen, container with border
//	private Shape _screen;
	private AARectShape _screen;
	
	//World of game space in game units
	private GameWorld _gameWorld;
	
	/**Dimensions of viewport, passed in constructor*/
	private Vec2f _screenDim;	
	
	//Upper left of screen in pixels
	private Vec2f _upperLeftScreenLoc;
	
	//Center of screen in game units
	private Vec2f _centerInGameUnits;
	//Scale in px/unit 
	private float _scale;
	//viewport angle (CCW) with 0 as the normal downward direction
	private float _theta;
	
//	private final float ROTATIONAL_RESOLUTION = (float)Math.PI/1000f;
	
	//Container is the rectangle Viewport screen; (Not drawn, so can be generic superclass)
	//gameDim is the size of the game world, in game units
	public Viewport(Shape container) {	
		_screenDim = container.getDimensions();
		_upperLeftScreenLoc = new Vec2f(container.getX(), container.getY());
		_scale = 5f;
		
		//Default starting game view; upper left flush with world's
		_centerInGameUnits = new Vec2f(0, 0); 
		_screen = new AARectShape(_upperLeftScreenLoc, _screenDim);
		_theta = 0;
	}
	
	public void setWorld(GameWorld world) {
		_gameWorld = world;
	}
	
	
	public float getScale() {
		return _scale;
	}
	
	/**
	 * This method takes in a point in the game world
	 * and translates it into a point on the screen.
	 * 
	 * @param Vec2f gamePoint; a point in the game world, in game units
	 * @return Vec2f; a point on the screen, in pixels
	 */
	public Vec2f gamePointToScreen(Vec2f gamePoint) {
		AffineTransform tx = getTransform();
		Point2D gameP = new Point2D.Float(gamePoint.x, gamePoint.y);
		Point2D screenP = tx.transform(gameP, null);
		return new Vec2f((float)screenP.getX(), (float)screenP.getY());
	}
	
	/**
	 * This method takes in a point on the screen
	 * and translates it into a point in the game.
	 * 
	 * @param Vec2f screenPoint; a point on the screen, in pixels
	 * @return Vec2f; a point in the game, in game units
	 */
	public Vec2f screenPointToGame(Vec2f screenPoint) {		
		AffineTransform tx = null;
		try {
			tx = getTransform().createInverse();
		} catch (NoninvertibleTransformException e) {
			System.err.println("ERROR: transform not invertible");
		}
		Point2D gameP = new Point2D.Float(screenPoint.x, screenPoint.y);
		Point2D screenP = tx.transform(gameP, null);
		return new Vec2f((float)screenP.getX(), (float)screenP.getY());
		
	}
	
	/**
	 * This is a mutator for the size of the Viewport
	 * screen, which will be set depending on the 
	 * application. This value is passed into the
	 * constructor, but can modified later using
	 * this method. 
	 * The size of the screen is the square size 
	 * of the game map that is visible at any given time.
	 * The screen size is in pixels.
	 * 
	 * @param Vec2f newSize; size of visible viewport screen
	 */
	public void setScreenSize(Vec2f newSize) {
		_screenDim = newSize;
	}
	
	public Vec2f getScreenSize() {
		return _screenDim;
	}
	
	
	
	/**
	 * This is a mutator for the location of the Viewport
	 * screen. It is initialized t0 (200,200) by default,
	 * but should be set depending on the application from
	 * any subclass.
	 * The screen location is in pixels.
	 * @param Vec2f newLoc; upper left of viewport screen in pixels
	 */
	public void setScreenLoc(Vec2f newLoc) {
		_upperLeftScreenLoc = newLoc;
	}
	public Vec2f getScreenLoc() {
		return _upperLeftScreenLoc;
	}
	
	/*Background color of Viewport screen*/
	public Color getScreenColor() {
		return _screen.getColor();
	}
	public void setScreenColor(Color col) {
		_screen.setColor(col);
	}
	/*Instead of individual attribute
	 * accessors, allows other classes
	 * to just modify a reference to the screen.*/
	public Shape getScreen() {
		return _screen;
	}
	
	/**
	 * Mutator to set upper left location of Viewport
	 * screen in game units. Defaults in Viewport constructor
	 * to 0,0. But should be able to be set for specific games
	 * outside of Viewport.
	 * @param screenInGameUnits
	 */
	public void setPortCenterInGameUnits(Vec2f screenInGameUnits) {
		_centerInGameUnits = screenInGameUnits;
	}	
	
	public Vec2f getCenterOfScreen() {
		return _upperLeftScreenLoc.plus(_screenDim.sdiv(2f));
				
	}
	
	public Vec2f getCenterOfPortInGameUnits() {
		return _centerInGameUnits;
	}

	public Vec2f getPixelGameLocation() {
		return gamePointToScreen(new Vec2f(0f, 0f));
	}
	
	/**
	 * This method takes in the change in x and y
	 * between prev and curr (prev-curr) from mouseDragged 
	 * in GameScreen. It converts the values to their
	 * equivalent in game units, and then adds them
	 * to the current x and y location of the game world,
	 * which is the upper left of the screen mapped to game units.
	 * 
	 * @param float dx, change in x between prev and curr mouse position, pxls
	 * @param float dy, change in y between prev and curr mouse position, pxls
	 */
	public void panInPixels(Vec2f pixelDiff) {
		//Vec2f gameDiff = diff.sdiv(_scale);
		//panInGameUnits(new Vec2f(gameDiff.x, -gameDiff.y));
		Vec2f goalLocInPixels = getCenterOfScreen().plus(pixelDiff);
		Vec2f goalLocInUnits = screenPointToGame(goalLocInPixels);
		Vec2f unitDiff = goalLocInUnits.minus(getCenterOfPortInGameUnits());
		panInGameUnits(unitDiff);
	}
	
	public void panInGameUnits(Vec2f gameUnitDiff) {
		_centerInGameUnits = _centerInGameUnits.plus(gameUnitDiff);
	}
	
	/**
	 * This is called to zoom in and out on the game world from the 
	 * Viewport's center. Uses the conversion of scale (pxls/game unit) to
	 * change location of _topLeftInGameUnits, which is the upper corner
	 * of Viewport in game world. This zooms from the center of
	 * the screen.
	 * @param newScale
	 */
	public void zoom(float newScale) {
		//Vec2f _viewPortSizeInGameUnits = _screenDim.smult(1f/_scale);
		 //Vec2f newSize = _screenDim.smult(1f/newScale);
         //Vec2f offset = newSize.minus(_viewPortSizeInGameUnits).sdiv(2);
         //_centerInGameUnits = new Vec2f(_centerInGameUnits.x - offset.x, _centerInGameUnits.y + offset.y);
        _scale = newScale;
	}
	
	/**
	 * get the CCW offset of the viewport from initial 'down'position (negative y axis)
	 * @return
	 */
	public float getTheta() {
		return _theta;
	}
	
	/**
	 * set the rotation orientation of the viewport
	 * @param theta
	 */
	public void setTheta(float theta) {
		_theta = theta % ((float)Math.PI*2f);
	}
	
	/**
	 * change the rotation orientation by thetaDiff
	 * @param thetaDiff
	 */
	public void rotate(float thetaDiff) {
		setTheta(_theta + thetaDiff);
	}

	
	
	private AffineTransform getTransform() {
		AffineTransform tx = new AffineTransform();
		tx.translate(_upperLeftScreenLoc.x, _upperLeftScreenLoc.y);
		tx.scale(_scale, _scale * (-1f));
		Vec2f topLeftOffset = _screenDim.sdiv(_scale).sdiv(2f);
		Vec2f topLeft = new Vec2f(_centerInGameUnits.x - topLeftOffset.x, 
				_centerInGameUnits.y + topLeftOffset.y);
		tx.translate((-1f)*topLeft.x, (-1f)*topLeft.y);
		float resTheta = _theta;
		//resTheta = _theta-_theta%ROTATIONAL_RESOLUTION
		tx.rotate(-(resTheta), _centerInGameUnits.x, _centerInGameUnits.y);
		return tx;	
	}
		
	/** Clips the Graphics2D to only the Viewport window, based on vars
	 * location _upperLeftScreenLoc with size _screenDim. Applies an AffineTransform
	 * to scale according to _scale and translate according to the screen's 
	 * location of game origin (in pxls).*/
	public void draw(Graphics2D g) {

		if (_gameWorld != null) {		
			_screen.setDimensions(_screenDim);
			_screen.draw(g);	
	
			java.awt.Shape clip = g.getClip();	
			g.clipRect((int) _upperLeftScreenLoc.x, (int) _upperLeftScreenLoc.y, (int) _screenDim.x, (int) _screenDim.y);
			AffineTransform tsave = g.getTransform();					
			g.transform(getTransform());			
			
			_gameWorld.draw(g);

			g.setTransform(tsave);
			g.setClip(clip);	
		}
	}

	
}
