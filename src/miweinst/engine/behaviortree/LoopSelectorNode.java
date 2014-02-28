package miweinst.engine.behaviortree;

public class LoopSelectorNode extends SelectorNode {
	
	/**
	 * If one of the children in the SequenceNode fails,
	 * start back at the first child.
	 */
	@Override
	public Status update(long nanos) {
		if (super.update(nanos) == Status.FAILED) {
//			update(nanos);
			return Status.RUNNING;
		}
		return Status.SUCCESS;
	}
}
