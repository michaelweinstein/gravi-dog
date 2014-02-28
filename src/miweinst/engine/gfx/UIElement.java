package miweinst.engine.gfx;

import java.awt.Graphics2D;
import java.util.ArrayList;

import cs195n.Vec2f;
import miweinst.engine.shape.Shape;

public class UIElement {
	public static final String string = "UIElement";
	
	//An Array of the parts of the UIElement
	private ArrayList<Shape> _parts;	
	//Not visible; shape that contains all parts
	private Shape _boundingBox;
	//Upper left location of UIElement bounding box
	private Vec2f _location;
	
	private Vec2f _anchor;

	public UIElement() {
		_parts = new ArrayList<Shape>();
	}
	
	/**
	 * Sets the location of the UIElement according
	 * to upper left of bounding box. Sets location
	 * of each elt of UI relative to new loc.
	 * @param loc
	 */
	public void setLocation(Vec2f loc) {
		_location = loc;
		_boundingBox.setLocation(loc);
		for (Shape part: _parts) {
			float absX = loc.x + part.getX();
			float absY = loc.y + part.getY();
			part.setLocation(new Vec2f(absX, absY));
		}
	}
	
	/**
	 * Returns the upper left location of bounding box.
	 * @return
	 */
	public Vec2f getLocation() {
		return _location;
	}
	
	/**
	 * Sets the location of UIElement relative to another
	 * object at specified anchor. This only needs to be
	 * called once to set the anchor, then updateAnchor
	 * is called when the anchor object moves.
	 * @param anchor
	 * @param dloc
	 */
	public void setAnchor(Vec2f anchor, Vec2f dloc) {
		float dx = anchor.x + dloc.x;
		float dy = anchor.y + dloc.y;
		_anchor = anchor;
		this.setLocation(new Vec2f(dx, dy));
	}
	
	/**
	 * This method updates the _anchor reference, and 
	 * should be called when the anchor point has moved.
	 * It then updates the location based on the distance
	 * from anchor specified in setAnchor.
	 * @param newAnchor
	 */
	public void updateAnchor(Vec2f newAnchor) {
		if (_anchor != null) {
			float dx = _location.x - _anchor.x;
			float dy = _location.y - _anchor.y;
			float newX = newAnchor.x + dx;
			float newY = newAnchor.y + dy;
			this.setLocation(new Vec2f(newX, newY));
			_anchor = newAnchor;
			
		}
		else System.out.println("You haven't called setAnchor yet. (UIElement.updateAnchor");
	}
	
	/**
	 * Returns bounding box, Shape that
	 * is not drawn but contains each elt
	 * of UI. Location and dimensions of
	 * UI corresponds to bounding box.
	 * @return
	 */
	public Shape getBoundingBox() {
		return _boundingBox;
	}
	
	/**
	 * Add a shape to the Array of parts.
	 * Properties like location are
	 * read relative to upper left.
	 * @param newPart
	 */
	public void addShape(Shape newPart) {
		_parts.add(newPart);
	}
	
	/**
	 * Updates the specified instance of Shape
	 * in ArrayList to the one passed in. If there
	 * is no reference to that instance of Shape in
	 * _parts, returns false. If the Shape is 
	 * successfully updated, returns true.
	 * 
	 * @param newPart
	 * @return
	 */
	public boolean updateShape(Shape part) {
		if (_parts.contains(part)) {
			int index = _parts.indexOf(part);
			_parts.set(index, part);
			return true;
		}
		else {
			return false;
		}
	}
	/**
	 * Sets the location of the specified shape relative to the
	 * location of this UIElement. The Vec2f passed in corresponds
	 * to the dx, dy relative to upper left of UIElement bounding
	 * box. First updates the location (dx, dy) of specified 
	 * Shape, then updates in Array and calls setLocation to update.
	 * If the Shape is not contained in _parts, returns false. If
	 * the Shape's relative location is successfully set, returns
	 * true.
	 * 
	 * @param part
	 * @param loc
	 * @return
	 */
	public boolean setRelativeLocation(Shape part, Vec2f dloc) {
		if (_parts.contains(part)) {
			int index = _parts.indexOf(part);
			part.setLocation(dloc);
			_parts.set(index, part);
			this.setLocation(_location);
			return true;
		}
		else {
			return false;
		}
	}
	
	public void draw(Graphics2D g) {
	}
}
