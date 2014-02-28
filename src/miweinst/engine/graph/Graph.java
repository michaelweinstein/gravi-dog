package miweinst.engine.graph;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Stack;

public class Graph<E> {
			
	//Holds integers for unique node numbers; keeps track of open numbers in O(1)
	private Stack<Integer> _indexStack;
	
	//Holds all of the nodes in the graph
	private ArrayList<GraphNode<E>> _nodes;
	//Holds all of the edges in graph
	private ArrayList<GraphEdge<E>> _edges;
	
	//Counter for number of nodes;
	private int _size;
	
	//This keeps track of what node is stored at each unique node number
	private HashDecorator<Integer, GraphNode<E>> _numDecorator;
	
	//This keeps track of what GraphNode is wrapping the given elt
	private HashDecorator<E, GraphNode<E>> _objDecorator;
	
	//This decorates each node with a list of its incident edges;
		//uses ArrayList so that edge can be removed easily
	private HashDecorator<GraphNode<E>, ArrayList<GraphEdge<E>>> _incEdgeDecorator;
	
	public Graph(int maxNum) {
						
		_nodes = new ArrayList<GraphNode<E>>();
		_edges = new ArrayList<GraphEdge<E>>();
		_size = 0;

		//Initialize stack of possible unique node numbers
		_indexStack = new Stack<Integer>();
		for (int i=maxNum-1; i>=0; i--) {
			_indexStack.add(i);
		}
		
		_numDecorator = new HashDecorator<Integer, GraphNode<E>>(maxNum);
		_objDecorator = new HashDecorator<E, GraphNode<E>>(maxNum);
		_incEdgeDecorator = new HashDecorator<GraphNode<E>, ArrayList<GraphEdge<E>>>(maxNum);
	}	
	public GraphNode<E> insertNode(E elt) {
		//Create new node with data
		GraphNode<E> newNode = new GraphNode<E>(elt);
		//Assign a unique number for the node, removing it from available stack
		int unique = _indexStack.pop();
		//Give the node a reference to the number
		newNode.setNodeNumber(unique);
		//Give the number a reference to the node
		_numDecorator.setDecoration(unique, newNode);
		//Set the reference from the elt
		_objDecorator.setDecoration(elt, newNode);
		//Decorate node with empty list of incident edges
		_incEdgeDecorator.setDecoration(newNode, new ArrayList<GraphEdge<E>>());
		//Add the node to our list of nodes
		_nodes.add(newNode);
		//Increment the node counter
		_size += 1;
		
		return newNode;
	}
	/**
	 * Sets the two nodes as adjacent. Stored in adjacency matrix
	 * for easy lookup of adjacency.
	 * @param v1
	 * @param v2
	 */
	public GraphEdge<E> insertEdge(GraphNode<E> v1, GraphNode<E> v2, int weight) {
		GraphEdge<E> newEdge = new GraphEdge<E>(weight);
		
		//Edge keeps track of its end nodes
		newEdge.setEndNodes(v1, v2);
		
		//Add edge to list of incident edges decoration
		_incEdgeDecorator.getDecoration(v1).add(newEdge);
		_incEdgeDecorator.getDecoration(v2).add(newEdge);
		
		_edges.add(newEdge);
				
		return newEdge;
	}
	
/**	
	public E removeNode(GraphNode<E> node) {
		System.out.println("Not yet implemented for Tac");
		_size -= 1;
		return null;
	}
*/
	
	/**
	 * Removes an edge from the graph by removing
	 * from list of edges, any incident edge decorations,
	 * and returning the weight of the removed edge.
	 * @param v1
	 * @param v2
	 */
	public int removeEdge(GraphEdge<E> e) {		
		_edges.remove(e);
		_incEdgeDecorator.getDecoration(e.getANode()).remove(e);
		_incEdgeDecorator.getDecoration(e.getBNode()).remove(e);				
		return e.getWeight();
	}
	
	/**
	 * This returns the node that is wrapping the
	 * given element.
	 * 
	 * If the same element was inserted into the graph twice,
	 * it will still only exist once from the most recent insert.
	 * @param elt
	 * @return
	 */
	public GraphNode<E> getNode(E elt) {	
		return _objDecorator.getDecoration(elt);
	}
	
	/**
	 * Returns the node with the given unique node number.
	 * @param num
	 * @return
	 */
	public GraphNode<E> getNodeAtNum(int num) {
		for (GraphNode<E> node: _nodes) {
			if (node.getNodeNumber() == num) {
				return node;
			}
		}
		return null;
	}
	
	/**
	 * Returns the edge that connects the specified vertices.
	 * If the vertices are not adjacent and there is no edge
	 * between them, returns null. Runs much faster because
	 * decoration has been updated using hash in O(1) every
	 * time an edge is added.
	 * @param v
	 * @param u
	 * @return
	 */
	public GraphEdge<E> getEdge(GraphNode<E> v, GraphNode<E> u) {
		for (GraphEdge<E> i: _incEdgeDecorator.getDecoration(v)) {
			for (GraphEdge<E> j: _incEdgeDecorator.getDecoration(u)) {
				if (i == j) {
					return i;
				}
			}
		}
		return null;
	}
	
	public boolean isEdge(GraphNode<E> v, GraphNode<E> u) {
		if (_incEdgeDecorator.contains(v) && _incEdgeDecorator.contains(u)) {
			return getEdge(v, u)!=null;
		}
		return false;
	}
		
	/**
	 * Return the weight of the edge between the two specified nodes.
	 * @param v1
	 * @param v2
	 * @return
	 */
	public int getEdgeWeight(GraphNode<E> v1, GraphNode<E> v2) {
		return this.getEdge(v1, v2).getWeight();		
	}
	
	/**
	 * Returns the edges that v is connected to in O(1).
	 * @param v
	 * @return
	 */
	public Iterator<GraphEdge<E>> incidentEdges(GraphNode<E> v) {		
		return _incEdgeDecorator.getDecoration(v).iterator();
	}
	
	/**
	 * Returns the node that is opposite from specified node v
	 * on the same edge e.
	 * Returns null if the edge is not incident on vertex v.
	 * @param v
	 * @param e
	 * @return
	 */
	public GraphNode<E> opposite(GraphNode<E> v, GraphEdge<E> e) {
		GraphNode<E> opposite = null;
		if (e != null && v!= null) {
			if (e.getANode() != e.getBNode()) {
				if (e.getANode() == v) {
					opposite = e.getBNode();
				}
				else if (e.getBNode() == v) {
					opposite = e.getANode();
				}
			}
		}
		return opposite;
	}
	
	/**
	 * Changes the weight of the edge represented in the adjacency
	 * matrix. If the weight is 0, then the edge is removed and the
	 * nodes are no longer adjacent.
	 */
	public void setEdgeWeight(GraphNode<E> v1, GraphNode<E> v2, int weight) {
		this.getEdge(v1, v2).setWeight(weight);
	}
	
	/**
	 * This method sets the edge weight of all incident edges for
	 * a given node. It is useful in map generation, for example,
	 * even though the same edge may be set multiple times, a whole
	 * region often shares the same edge weights.
	 * @param v
	 * @param weight
	 */
	public void setIncidentEdgeWeights(GraphNode<E> v, int weight) {		
		for (GraphEdge<E> edge: _incEdgeDecorator.getDecoration(v)) {
			edge.setWeight(weight);
		}		
/*		for (int i=0; i<_adjacencyMatrix[0].length; i++) {
			if (_adjacencyMatrix[v.getNodeNumber()][i] != 0) {
				_adjacencyMatrix[v.getNodeNumber()][i] = weight;
			}
			if (_adjacencyMatrix[i][v.getNodeNumber()] != 0) {
				_adjacencyMatrix[i][v.getNodeNumber()] = weight;
			}
		}
*/
	}
	
	/**
	 * Returns whether or not two nodes are adjacent.
	 * @param v1
	 * @param v2
	 * @return
	 */
	public boolean areAdjacent(GraphNode<E> v1, GraphNode<E> v2) {
		boolean adjacent = false;		
		for (GraphEdge<E> e: _incEdgeDecorator.getDecoration(v1)) {
			if (this.opposite(v1, e) == v2) {
				adjacent = true;
			}
		}
/*			for (GraphEdge<E> e2: this.getIncidentEdges(v2)) {
				//If there is an edge in both node's incident edges dec
				if (e1 == e2) {
					adjacent = true;
				}
			}
		}*/		
		return adjacent;
	}
	
	/**
	 * Returns an iterator over all the nodes
	 * @return
	 */
	public Iterator<GraphNode<E>> nodes() {
		return _nodes.iterator();
	}
	
	public Iterator<GraphEdge<E>> edges() {
		return _edges.iterator();
	}
	
	/**
	 * Returns the size of the graph.
	 * @return
	 */
	public int size() {
		return _size;
	}
	
	/**
	 * This method returns an iterator over all of the neighbors
	 * of the specified GraphNode.
	 * 
	 * @param curr
	 * @return
	 */
	public Iterator<GraphNode<E>> neighbors(GraphNode<E> v) {
		ArrayList<GraphNode<E>> neighbors = new ArrayList<GraphNode<E>>();
		for (GraphEdge<E> edge: _incEdgeDecorator.getDecoration(v)) {
			GraphNode<E> neighbor = this.opposite(v, edge);
			neighbors.add(neighbor);
		}
		
		return neighbors.iterator();
/*		int num = curr.getNodeNumber();
		for (int i=0; i<_adjacencyMatrix[0].length; i++) {
			//If there is an edge between curr and the node with i number
			if (_adjacencyMatrix[num][i] != 0) {
				//Get the node with unique number i
				GraphNode<E> neighbor = _numDecorator.getDecoration(i);
				//and add it to the list of neighbors
				neighbors.add(neighbor);
			}
		}*/				
	}	
	

	public void clear() {
		System.out.println("Not yet implemented for Tac");
	}
}
