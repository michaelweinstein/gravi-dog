package miweinst.engine.behaviortree;

/**
 * This class can be subclassed directly for
 * ActionNodes and ConditionNodes. The action
 * performed and the condition to be checked
 * both override the update(float) method.
 * 
 * SelectorNodes and SequenceNodes have subclasses
 * that keep references to their children and 
 * update them in order, returning SUCCESS/FAILED 
 * based on the type of node.
 * 
 * @author miweinst
 *
 */
public abstract class BehaviorNode {
		
	public BehaviorNode() {
	}	
	public enum Status {
		RUNNING,
		SUCCESS,
		FAILED
	}	
	public abstract Status update(long nanos); 	
	public abstract void reset();
}
