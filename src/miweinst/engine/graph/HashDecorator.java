package miweinst.engine.graph;

import java.util.HashMap;
import java.util.Set;

/**
 * Class uses a java.util.HashMap to decorate each node of a 
 * graph (K key) with a value (V val). 
 * 
 * @author miweinst
 *
 * @param <K>
 * @param <V>
 */

public class HashDecorator<K,V>{
	
	private HashMap<K,V> _hash;
	//Constructor I
	public HashDecorator() {
		_hash = new HashMap<K,V>();
	}
	//Constructor II
	/* If number of elements is known, this will make computation
	 * more efficient.*/
	public HashDecorator(int numVerts) {		
		_hash = new HashMap<K,V>(numVerts);
	}

	/**
	 * Returns the value V to which the specified key K is mapped.
	 * @param key
	 * @return value V
	 */
	public V getDecoration(K key) {
		return _hash.get(key);
	}
	
	/**
	 * Maps the specified key to the specified value.
	 * 
	 * @param key; K to decorate
	 * @param value; V value of decoration
	 */
	public void setDecoration(K key, V value) {
		_hash.put(key, value);
	}

	/**
	 * Returns a Set of all the keys contained in the map
	 * @return Set<K>
	 */
	public Set<K> getKeys() {
		return _hash.keySet();
	}

	/**
	 * Returns true if the hash table has a mapping
	 * for the specified key.
	 * @param key
	 * @return boolean, whether this key has a corresponding value
	 */
	public boolean contains(K key) {
		return _hash.containsKey(key);
	}

	/**
	 * Removes the value of the mapping for the specified key, if present, and
	 * returns the removed value.
	 * @param key K
	 * @return value V that was removed
	 */
	public V removeDecoration(K key) {
		return _hash.remove(key);
	}
}
