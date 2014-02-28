package miweinst.gravidog;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import javax.imageio.ImageIO;

import miweinst.engine.gfx.sprite.Resources;
import miweinst.engine.gfx.sprite.SpriteLoader;
import cs195n.Vec2i;

public class GravidogResources extends Resources {

	public static void init() {		
		//Stores arrays of sprite frames in animations, accessed by string key
		_cache = new HashMap<String, BufferedImage[]>();
		
		File fLStar = new File("src/miweinst/resources/star_large.png");
		BufferedImage largeStar = null;
		try {
			largeStar = ImageIO.read(fLStar);
		} catch (IOException e) {
			System.out.println("Star file not found.");
			e.printStackTrace();
		}
		
		BufferedImage[] stars = { largeStar };
		
		_cache.put("large_star", stars);

		File f = new File("src/miweinst/resources/blackdog.png");
		SpriteLoader levelLoader = new SpriteLoader(f, new Vec2i(250, 150), 0, new Vec2i(4, 4), 0);
		BufferedImage[][] levelOut = levelLoader.getSprites();
		
		BufferedImage[] walking = new BufferedImage[levelOut[0].length + levelOut[1].length];
		BufferedImage[] running = new BufferedImage[levelOut[2].length + levelOut[3].length];
		BufferedImage[] standing = new BufferedImage[1];
		BufferedImage[] jumping = new BufferedImage[1];
		
		for(int i = 0; i < levelOut[0].length; i++) {
			walking[i] = levelOut[0][i];
		}
		for(int i = 0; i < levelOut[1].length; i++) {
			walking[levelOut[0].length + i] = levelOut[1][i];
		}
		for(int i = 0; i < levelOut[2].length; i++) {
			running[i] = levelOut[2][i];
		}
		for(int i = 0; i < levelOut[3].length; i++) {
			running[levelOut[2].length + i] = levelOut[3][i];
		}
		standing[0] = levelOut[0][0];
		jumping[0] = levelOut[3][0];
		
		_cache.put("walking", walking);
		_cache.put("running", running);
		_cache.put("standing", standing);
		_cache.put("jumping", jumping);
		
		//Sets the cache of Resources superclass so that 
		setCache(_cache);
	}
	
	
	
}
