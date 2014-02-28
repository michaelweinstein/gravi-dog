package miweinst.engine.gfx.sprite;

import java.awt.image.BufferedImage;
import java.util.HashMap;


public abstract class Resources {
	protected static HashMap<String, BufferedImage[]> _cache;
		
	public static void init() {
		_cache = new HashMap<String, BufferedImage[]>();
	}	 
	
	/**
	 * This class allows a subclass with a specific cache
	 * of game data to set the cache in the Superclass,
	 * so generic classes with reference to this abstract 
	 * class will still have access to the game data.
	 * @param cache
	 */
	protected static void setCache(HashMap<String, BufferedImage[]> cache) {
		_cache = cache;
	}
	
	/**
	 * 
	 * @param key
	 * @return
	 */
	public static BufferedImage[] getValue(String key) {
		if (_cache.containsKey(key)) {
			return _cache.get(key);
		} else {
			System.out.println("Resources cache does not contain images at that key!");
			return null;
		}
	}
}
