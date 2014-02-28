package miweinst.engine.behaviortree;

import miweinst.engine.graph.Graph;

public class BehaviorTree {
	
	private Graph<BehaviorNode> _graph;
	
	private SelectorNode _root;
	
	public BehaviorTree(int numNodes) {
		_graph = new Graph<BehaviorNode>(numNodes);
	}
	
	/**
	 * Adds the root of the BehaviorTree, which
	 * should be a SelectorNode.
	 */
	public void addRoot() {
		SelectorNode root = new SelectorNode();
		_root = (SelectorNode) _graph.insertNode(root).getElement();
	}
	
	public void update(long nanos) {
		_root.update(nanos);
	}
	
	public SelectorNode root() {
		return _root;
	}
	
	public void addLeftChild(BehaviorNode node) {		
	}
	
	public void addRightChild(BehaviorNode node) {		
	}
}
