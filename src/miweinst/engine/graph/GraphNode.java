package miweinst.engine.graph;

public class GraphNode<E> {
	
	//The unique number of this node
	private int _number;
	
	private E _data;
	
	private GraphNode<E> _prev;
	
	private float _dist;
	
	//Whether or not Node is "visible" 
	private boolean _visible;
		
	public GraphNode(E element) {
		_data = element;
		_number = 0;
		_dist = 0;
		_visible = true;
	}
	
	//ACCESSORS
	public E getElement() {
		return _data;
	}	
	public int getNodeNumber() {
		return _number;
	}
	
	//Mostly used for pathfinding
	public GraphNode<E> getPrev(){
		return _prev;
	}
	
	public float getDist() {
		return _dist;
	}
	
	public boolean isVisible(){
		return _visible;
	}
	
	//MUTATORS
	public void setNodeNumber(int num) {
		_number = num;
	}
	
	public void setElement(E elt) {
		_data = elt;
	}

	public void setDist(float dist) {
		_dist = dist;
	}
	
	//Mostly used for pathfinding
	public void setPrev(GraphNode<E> prev){
		_prev = prev;
	}
	
	
	public void setVisible(boolean in) {
		_visible = in;
	}
}
