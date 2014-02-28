package miweinst.engine.collisiondetection;

import java.util.ArrayList;

import miweinst.engine.world.PhysicsEntity;

/*One class created for each CollisionGroup, and */

public class CollisionGroup {
	//Constant for each instance, so doesn't need accessor/mutator
	public final int group;
	private ArrayList<Integer> _filter;
	private ArrayList<PhysicsEntity> _entities;
	
	public CollisionGroup(int group) {
		this.group = group;
		_filter = new ArrayList<Integer>();
		_entities = new ArrayList<PhysicsEntity>();
	}
	
	/*Accessor/Mutator for list of other collision groups
	 * filtered from this collision group*/
	public Integer[] getFilter() {
		return _filter.toArray(new Integer[_filter.size()]);
	}
	public void setFilter(int... groups) {
		for (int g: groups) {
			_filter.add(g);
		}
	}
	
	/*Accessor/Mutator for the Entity members of this collision group. */
	public PhysicsEntity[] getEntities() {
		return _entities.toArray(new PhysicsEntity[_entities.size()]);
	}
	public void addEntity(PhysicsEntity... ents) {
		for (PhysicsEntity e: ents) {
			_entities.add(e);
		}
	}
	
	public boolean isMember(PhysicsEntity ent) {
		return _entities.contains(ent);
//		for (PhysicsEntity e: _entities) {
//			if (e == ent)
//				return true;
//		}
//		return false;
	}

	/*Returns whether or not the specified group
	 * is allowed to collide with this group, based
	 * on whether it is in the filter.*/
	public boolean inFilter(int group) {
		return _filter.contains(group);
/*		for (Integer g: _filter) {
			if (g == group)
				return false;
		}
		return true;*/
	}
}
