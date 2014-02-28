package miweinst.engine.gfx;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;

import miweinst.engine.shape.AARectShape;
import miweinst.engine.shape.PolygonShape;
import miweinst.engine.shape.Shape;
import cs195n.Vec2f;


public class Text {
	public static final String string = "Text";

	private Shape _container;
	private String _string;

	private Vec2f _location;
	
	private Color _textColor;
	private int _fontSize;
	
	private Font _font;
	private String _fontType;
	private int _fontStyle;
	
	private boolean _isVisible;
	
	//True if a custom font is passed into setCustomFont;
	private boolean _custom;
	
	public Text(Shape container, String text, Vec2f loc) {
		_container = container;
		_string = text;
		_location = loc;
		
		_custom = false;
		
		setFontType(Font.SERIF, Font.BOLD);	//Default font type
		setColor(Color.BLACK); //Default text color
		setFontSize(12);	//Default font size	
	}
	public static PolygonShape getApproximateBounds(Text text) {
		float buttonSize = text.getFontSize();
		Vec2f buttonDim = new Vec2f((text.getString().length()-2)*buttonSize, buttonSize);
		Vec2f buttonLoc = new Vec2f(text.getLocation().x + buttonDim.x/2, text.getLocation().y - buttonSize/2);
		//Pass in Boolean because uses default java coordinates
		PolygonShape bounds = new AARectShape(buttonLoc, buttonDim).rectToPoly(false);
		bounds.setLocation(buttonLoc);
		bounds.setDimensions(buttonDim);
		return bounds;
	}
	
//// NOT WORKING
/*	public Vec2f getBoundingBox() {
		setFontType(_fontType,_fontStyle);	//Update type, style and size
		AffineTransform tx = new AffineTransform();
		FontRenderContext frc = new FontRenderContext(tx, false, false);
		Rectangle2D box = _font.getStringBounds(_string, frc);
		Vec2f dim = new Vec2f((float) box.getWidth(), (float) box.getHeight());
		return dim;
	}*/
	
	
	/*Toggles whether text string is drawn by Graphics*/
	public boolean isVisible() {
		return _isVisible;
	}
	public void setVisible(boolean visible) {
		_isVisible = visible;
	}
	
	/**
	 * This method resizes text based on a boolean parameter.
	 * The limits of resize stops when the text's width exceeds
	 * that of its containing box. I also hardcoded in some edge case
	 * limits for when the text is obnoxiously large or unreadable.
	 * 
	 * @param bigger; tells whether the resize makes window smaller or larger
	 */
	public void resize(boolean bigger) {
		
		FontRenderContext frc = new FontRenderContext(new AffineTransform(), false, false);
		if (_fontSize > 15 && _fontSize < 80) {
			if (bigger) {
				if (_location.x + _font.getStringBounds(_string, frc).getWidth()< _container.getX() + _container.getWidth()) {			
					_fontSize += 1;
				}	
			}	
			else	{
				if (_font.getStringBounds(_string, frc).getWidth() + 50 < _container.getWidth()) {
					_fontSize -= 1;	
				}
			}
		}
		else {
			if (_fontSize == 15) {
				_fontSize = 16;
			}
			if (_fontSize == 80) {
				_fontSize = 79;
			}
		}
	}
	
	public void setFontType(String type, int style) {
		_font = new Font(type, style, _fontSize);
		_custom = false;
	}
	public void setFont(Font newFont, boolean custom) {
		_font = newFont;
		_fontType = newFont.getName();
		_fontStyle = newFont.getStyle();		
		_custom = custom;
	}
	public Font getFont() {
		return _font;
	}
	public void setColor(Color col) {
		_textColor = col;
	}
	public Color getColor() {
		return _textColor;
	}
	public void setFontSize(int size) {
		_fontSize = size;
		_font.deriveFont(size);
	}
	public int getFontSize() {
		return _fontSize;
	}
	
	public void setString(String string) {
		_string = string;
	}
	
	public String getString() {
		return _string;
	}
	public void setLocation(Vec2f newLoc) {
		_location = newLoc;
	}
	public Vec2f getLocation() {
		return _location;
	}
	
	/**
	 * This sets all the font variables to those
	 * of another text objects, making it easy
	 * to visually group text.
	 * @param other, another Text object
	 */
	public void setFontToText(Text other) {
		_font = other.getFont();
		_fontSize = other.getFontSize();
		_textColor = other.getColor();
	}
	
	public void centerTextHorizontal() {
//		updateContainer(_container);
		double w = getApproximateBounds(this).getDimensions().x;		
		float x = (float) (_container.getWidth()/2 - w/2);	
		Vec2f newLoc = new Vec2f(x, _location.y);
		_location = newLoc;
	}
	
	public void centerTextVertical() {
//		updateContainer(_container);
		double h = getApproximateBounds(this).getDimensions().y;
		float y = (float) (_container.getHeight()/2 + h/4);
		Vec2f newLoc = new Vec2f(_location.x, y);
		_location = newLoc;
	}
	
	public void updateContainer(Shape container) {
		_container = container;
	}

	public void draw(Graphics2D g) {
		if (_custom == false) {		
			this.setFontType(_fontType,_fontStyle);
		}
		if (_isVisible) {
			g.setColor(_textColor);
			g.setFont(_font);		
			g.drawString(_string, _location.x, _location.y);
		}
	}
}
