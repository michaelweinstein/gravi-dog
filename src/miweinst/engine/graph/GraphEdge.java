package miweinst.engine.graph;

/**
 * The GraphEdge<E> has generics so that it
 * can declare GraphNodes with type checking,
 * ensuring graph uniformity. However it holds
 * integers as weights.
 * 
 * @author miweinst
 *
 * @param <E>
 */

public class GraphEdge<E> {
	
	private int _weight;	
	private GraphNode<E> _aNode;
	private GraphNode<E> _bNode;
	
	public GraphEdge(int elt) {		
		_weight = elt;		
		
	}
	
	public GraphNode<E> getANode() {
		return _aNode;
	}
	public GraphNode<E> getBNode() {
		return _bNode;
	}
	
	//Because can't instantiate parameterized array
	@SuppressWarnings("unchecked")
	public GraphNode<E>[] getEndNodes() {
		GraphNode<E>[] nodes = new GraphNode[2];
		nodes[0] = _aNode;
		nodes[1] = _bNode;
		return nodes;
	}
	
	public int getWeight() {
		return _weight;
	}
		
	public void setEndNodes(GraphNode<E> v, GraphNode<E> u) {
		_aNode = v;
		_bNode = u;
	}
	
	public void setWeight(int elt) {
		_weight = elt;
	}
}
