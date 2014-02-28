package miweinst.engine.behaviortree;

import java.util.ArrayList;

public class SequenceNode extends BehaviorNode {
	
	private ArrayList<BehaviorNode> _children;
	private BehaviorNode _lastRunning;
	
	public SequenceNode() {
		_children = new ArrayList<BehaviorNode>();
		_lastRunning = null;
	}

	@Override
	public Status update(long nanos) {
		//Start at last running child
		int start = 0;
		if (_lastRunning != null) {
			start = _children.indexOf(_lastRunning);
		}		
		for (int i=start; i<_children.size(); i++) {
			BehaviorNode child = _children.get(i);
			Status childStatus = child.update(nanos);
			//Return RUNNING if one child running
			if (childStatus == Status.RUNNING) {
				_lastRunning = child;
				return Status.RUNNING;
			}
			//Return FAIL when one child does fails
			if (childStatus == Status.FAILED) {
				_lastRunning = null;
				return Status.FAILED;
			}
		}
		//Return SUCCESS if all children were successful
		return Status.SUCCESS;
	}

	@Override
	public void reset() {
		for (BehaviorNode child: _children) {
			child.reset();
		}
	}
	//Adds child to start of sequence
	public void addChildStart(BehaviorNode newChild) {
		_children.add(0, newChild);
	}
	//Adds child to end of sequence
	public void addChildEnd(BehaviorNode newChild) {
		_children.add(newChild);
	}
	
	public BehaviorNode getRunningChild() {
		return _lastRunning;
	}
}
