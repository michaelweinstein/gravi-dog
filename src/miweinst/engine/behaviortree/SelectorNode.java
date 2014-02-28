package miweinst.engine.behaviortree;

import java.util.ArrayList;

public class SelectorNode extends BehaviorNode {
	
	private ArrayList<BehaviorNode> _children;
	private BehaviorNode _lastRunning;
	
	public SelectorNode() {
		_children = new ArrayList<BehaviorNode>();
		_lastRunning = null;
	}

	@Override
	public Status update(long nanos) {	
		//Always start at first child, and updates in order automatically
		for (BehaviorNode child: _children) {
			Status childStatus = child.update(nanos);
			//Return SUCCESS when first child returns SUCCESS
			if (childStatus == Status.SUCCESS) {
//				if (_lastRunning!= null) _lastRunning.reset();
//				_lastRunning = null;
				return Status.SUCCESS;
			}
			//Return RUNNING if child running; 
			if (childStatus == Status.RUNNING) {
				//Will always be before or same as _lastRunning, because loops in order
				if (_lastRunning != null) {
					if (child != _lastRunning) _lastRunning.reset();
				}
				_lastRunning = child;
				return Status.RUNNING;
			}
		}		
		//Return FAILED if all children failed
		return Status.SUCCESS;
	}

	@Override
	public void reset() {
		for (BehaviorNode child: _children) {
			child.reset();
		}
	}
	
	//If RUNNING, stores which node was running; else null
	public BehaviorNode getLastRunning() {
		return _lastRunning;
	}
	
	//Adds new node as first child (highest priority); shifts children towards end
	public void addChildStart(BehaviorNode newChild) {
		_children.add(0, newChild);
	}
	//Adds new node as last child (lowest priority)
	public void addChildEnd(BehaviorNode newChild) {
		_children.add(newChild);
	}
}
