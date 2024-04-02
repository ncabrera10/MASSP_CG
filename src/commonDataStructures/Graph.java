package commonDataStructures;

import java.util.ArrayList;
import java.util.Hashtable;

/**
 * This class represents a DP graph
 * @author nicolas.cabrera-malik
 *
 */
public class Graph {

	/**
	 * List of nodes
	 */
	ArrayList<Node> nodes;
	
	/**
	 * List of arcs
	 */
	ArrayList<Arc> arcs;
	
	/**
	 * Points to the index of the last node in the nodes array
	 */
	int lastNode;
	
	/**
	 *  The quad constraint it belongs to!
	 */
	int id;
	
	/**
	 * Map containing the nodes already created
	 */
	
	Hashtable<String, Integer> GraphMap;
	
	/**
	 * Arcs with capacities
	 */
	ArrayList<Integer> relevantArcs; 
	
	/**
	 * This method creates a new DP graph:
	 */
	public Graph() {
		nodes = new ArrayList<Node>();
		arcs = new ArrayList<Arc>();
		GraphMap = new Hashtable<String, Integer>();
	}
	
	/**
	 * This method prints information of the DP graph
	 */
	public void print() {
		
		System.out.println("***************************DP GRAPH*****************************************************************");
		
		for (int i = 0; i < nodes.size(); i++) {
			
			System.out.print("Node "+i+": "+nodes.get(i).key+" connects with ");
			for (int j = 0; j < nodes.get(i).out.size(); j++) {
				String head = nodes.get(arcs.get(nodes.get(i).out.get(j)).head).key;
				System.out.print(" "+head);
			}
			System.out.println();
			
		}

	}
	
	
	/**
	 * This method prints the number of nodes and arcs in the graph:
	 */
	public void print3() {
		System.out.println(" NODES: "+nodes.size()+" arcs: "+arcs.size());
	}


	/**
	 * This method clears the arcs into and out of each node
	 */
	public void clearInOut() {
		for (int i = 0; i < nodes.size(); i++) {
			nodes.get(i).in.clear();
			nodes.get(i).out.clear();
		}
		
	}	

	
	
}
