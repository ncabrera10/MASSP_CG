package commonDataStructures;

import java.util.ArrayList;

/**
 * This class represents a node in the DP graph:
 * 
 * @author nicolas.cabrera-malik
 *
 */
public class Node {

	/**
	 * Key of the node
	 */
	String key;
	
	/**
	 * List that contains the pointers to the outgoing arcs
	 */
	ArrayList<Integer> out;
	
	/**
	 * List that contains the points to the incoming arcs
	 */
	ArrayList<Integer> in;
	
	/**
	 * Indicates if the current node is the sink node
	 */
	boolean endNode;

	// State 
	
	/**
	 * Time period
	 */
	int t;
	
	/**
	 * Activity
	 */
	int i;
	
	/**
	 * This method creates a new node
	 */
	public Node(){
		key="";
		out = new ArrayList<Integer>();
		in = new ArrayList<Integer>();
		endNode = false;
	}
	
	/**
	 * This method recovers the key associated with the node
	 */
	public void genKey() {
		key+=i+"-"+t;	
	}



}
