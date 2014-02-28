package miweinst.engine.gfx.sprite;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import cs195n.Vec2i;

/**
 * This class is just an action class, a wrapper for the sprite 
 * that loads it in and stores it. It basically creates a 2D
 * array of BufferedImages. This array is taken into the
 * Resources class and combined into Sprites that contain
 * all the frames of a certain animation.
 * 
 * @author miweinst
 *
 */

public class SpriteLoader {
	public static final String string = "SpriteLoader";

	private Vec2i _spriteSize; //width,height of indiv sprite
	private int _padding;	//pixels
	
	private BufferedImage _spriteSheet;
	private BufferedImage[][] _sprites;
	
	/**
	 * 						
	 * @param f
	 * @param spriteSize width x height in pixels
	 * @param padding in pixels
	 * @param sheetSize cols x rows
	 * @param sheetBorder 
	 */
	public SpriteLoader(File f, Vec2i spriteSize, int padding, Vec2i sheetSize, int sheetBorder) {		
		try {
			_spriteSheet = ImageIO.read(f);
		} catch(IOException e) {
			System.out.println("Oh no! IOException in SpriteLoader!");
		}	
		_spriteSize = spriteSize;
		_padding = padding;
				
		_sprites = new BufferedImage[(sheetSize.y-2*sheetBorder)][(sheetSize.x-2*sheetBorder)];
				
		for (int c=0; c < sheetSize.y-2*sheetBorder; c++) {
			for (int r=0; r < sheetSize.x-2*sheetBorder; r++) {
				int y = r+sheetBorder;
				int x = c+sheetBorder;
				_sprites[c][r] = this.loadSprite(new Vec2i(y,x));
			}
		}	
	}
	
	/**
	 * This method gets the subimage from a sprite sheet at the 
	 * @param index
	 * @return
	 */
	private BufferedImage loadSprite(Vec2i index) {		
		return _spriteSheet.getSubimage(index.x*(_spriteSize.x+_padding)+_padding, index.y*(_spriteSize.y+_padding)+_padding, _spriteSize.x, _spriteSize.y);

	}
	
	//Returns 2D array; indexed as [row][col]
	public BufferedImage[][] getSprites() {
		return _sprites;
	}
	
	
}
