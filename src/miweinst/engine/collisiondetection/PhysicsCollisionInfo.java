package miweinst.engine.collisiondetection;

import miweinst.engine.world.PhysicsEntity;
import cs195n.Vec2f;

public final class PhysicsCollisionInfo {
	public final Vec2f mtv;
	public final PhysicsEntity other;
	
	public PhysicsCollisionInfo(Vec2f mtvec, PhysicsEntity otherEnt) {
		mtv = mtvec;
		other = otherEnt;
	}

	public PhysicsEntity getOther() {
		return other;
	}
	public Vec2f getMTV() {
		return mtv;
	}
}
