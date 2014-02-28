package miweinst.engine.graph;

import java.util.ArrayDeque;
import java.util.Comparator;
import java.util.Iterator;
import java.util.PriorityQueue;

public abstract class GraphPath<E> {
	
	private Graph<E> _graph;
	private int _size;
	
	public GraphPath(Graph<E> graph) {
		_graph = graph;
		_size = _graph.size();
	}

	/**
	 * This method calculates the heuristic for the specified node to
	 * the destination node to be used in the A* algorithm.
	 * 
	 * This method is to be defined in subclasses because the heuristic
	 * should be specific to whatever game is implementing this pathfinding.
	 * 
	 * @param node
	 * @param dest
	 * @return
	 */
	public abstract float heuristic(GraphNode<E> node, GraphNode<E> dest);
	
	/**
	 * This is a pathfinding method that implements the A* algorithm.
	 * 
	 * It uses a Priority Queue to set each node's prev pointer to the
	 * neighbor node that took the least cost, and then creates the path
	 * by starting at the dest and retracing each prev pointer until the
	 * source is reached, adding each node to an ArrayDeque (used solely as
	 * a non-synchronized version of a Stack), which is then returned.
	 * 
	 * @param src
	 * @param dest
	 * @return
	 */
	public ArrayDeque<GraphNode<E>> findPath(GraphNode<E> src, GraphNode<E> dest) {			
		//Non-synchronized implementation of LIFO stack operations 
		ArrayDeque<GraphNode<E>> path = new ArrayDeque<GraphNode<E>>();		
		//Custom comparator class compares the distances of two GraphNodes
		Comparator<GraphNode<E>> comparator = new DecorationComparator<E>();
		//PQ keeps order of nodes based on their getDist attributes
		PriorityQueue<GraphNode<E>> pq = new PriorityQueue<GraphNode<E>>(_size, comparator); 				
		if (_size > 1) {
			Iterator<GraphNode<E>> nodes = _graph.nodes();
			while (nodes.hasNext()) {
				GraphNode<E> n = nodes.next();
				if (n == src) {
					n.setDist(0f);									
				}
				else {
					n.setDist(Float.POSITIVE_INFINITY);
				}
				n.setPrev(null);
				pq.add(n);
			}								
			while (pq.peek() != dest) {
				GraphNode<E> curr = pq.remove();	//Get the min elt from PQ
				//For each neighbor of the current node
				Iterator<GraphNode<E>> neighbors = _graph.neighbors(curr);
				while (neighbors.hasNext()) {	
					//For each neighbor of current node
					GraphNode<E> n = neighbors.next();	
					//Only check neighbors visible to traversal
					if (n.isVisible()) {
						//Get cost of edge between nodes; sum of edge weight, metric distance heuristic
						float cost = _graph.getEdgeWeight(curr, n) + this.heuristic(n, dest);							
						//If the new dist of neighbor is less than old dist
						if (n.getDist() > curr.getDist() + cost) {													
							//Change dist and order in pq
							//Remove node
							pq.remove(n);
							//Update it
							n.setDist(curr.getDist()+cost);
							n.setPrev(curr);
							//And re-insert to PQ
							pq.add(n);
						}
					}
				}
			}
			//Retrace path back from dest to src using prev pointers
			//Start at the destination node, LIFO order
			GraphNode<E> n = dest;
			//Until the src node is reached
			while (n.getPrev() != null) {
				path.push(n);	
				n = n.getPrev();
			}		
		}	
		return path;
	}
}
