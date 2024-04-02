package pulseDataStructures;

import java.util.ArrayList;
import java.util.Hashtable;

import commonDataStructures.DataHandler;

/**
 * This class represents a pulse graph. 
 * 
 * @author nicolas.cabrera-malik
 *
 */
public class PulseGraph {

	/**
	 * List of nodes
	 */
	private static ArrayList<PulseNode> nodes;
	
	/**
	 * Bidirectional important info
	 */
	
	public static int terminePrimero;
	static int contadorTerm;
	
	/**
	 * Map to locate a node easily
	 */
	private static Hashtable<String,Integer> GraphMap;
	
	/**
	 * Boolean that is triggered when a pulse ends
	 */
	
	public static boolean termine = false;
	
	/**
	 * Best solution found so far
	 */
	
	public static double PrimalBound;
	
	/**
	 * Resource consumptions of the best solutions found so far
	 */
	
	public static double[] resourceStars;
	
	/**
	 * Id of the last node
	 */
	
	private static int lastID;
	
	/**
	 * The final path (Or partial path)
	 */
	
	static ArrayList<Integer> Path;
	static ArrayList<Integer> PathB;
	static ArrayList<Integer> PathJ;
	
	/**
	 * Number of resources
	 */
	public static int R;
	
	/**
	 * Final cost (where the path was completed)
	 */
	public static double finalCostF;
	public static double finalCostB;
	public static double[] finalResourceF;
	public static double[] finalResourceB;
	
	/**
	 * Information to retrieve the path
	 */
	public static int finalNodeF;
	public static int finalNodeB;
	
	/**
	 * Keep track of which strategy was used to find the optimal path
	 */
	public static String best;
	
	/**
	 * Depth limit for the pulses
	 */
	public static int depth = 2;
	
	/**
	 * This method creates a new pulse graph
	 */
	public PulseGraph() {
		R = DataHandler.n;
		resourceStars = new double[DataHandler.n];
		nodes = new ArrayList<PulseNode>();
		GraphMap = new Hashtable<String,Integer>();
		finalResourceF = new double[DataHandler.n];
		finalResourceB = new double[DataHandler.n];
		Path = new ArrayList<Integer>();
		PathB = new ArrayList<Integer>();
		finalCostF = 0;
		finalCostB = 0;
		PrimalBound = 999;
		finalNodeF = 0;
		finalNodeB = 0;
		termine = false;
	}

	/**
	 * Resets the pulse graph.
	 */
	public static void resetInfo() {
		Path = new ArrayList<Integer>();
		PathB = new ArrayList<Integer>();
		finalCostF = 0;
		finalCostB = 0;
		PrimalBound = 999;
		finalNodeF = 0;
		finalNodeB = 0;
		termine = false;
		best = "";
	}
	
	/**
	 * Sets the last id
	 */
	
	public static void setLastID(int id) {
		lastID = id;
	}
	
	/**
	 * @return the nodes
	 */
	public static ArrayList<PulseNode> getNodes() {
		return nodes;
	}


	/**
	 * @param nodes the nodes to set
	 */
	public static void setNodes(ArrayList<PulseNode> nodes) {
		PulseGraph.nodes = nodes;
	}

	
	public static void addNode(PulseNode node) {
		PulseGraph.nodes.add(node);
	}

	/**
	 * @return the r
	 */
	public static int getR() {
		return R;
	}



	/**
	 * @param r the r to set
	 */
	public static void setR(int r) {
		R = r;
	}



	/**
	 * @return the primalBound
	 */
	public static double getPrimalBound() {
		return PrimalBound;
	}

	/**
	 * @param primalBound the primalBound to set
	 */
	public static void setPrimalBound(double primalBound) {
		PulseGraph.PrimalBound = primalBound;
	}

	/**
	 * @return the resourceStars
	 */
	public static double[] getResourceStars() {
		return resourceStars;
	}

	/**
	 * @param resourceStars the resourceStars to set
	 */
	public static void setResourceStars(double[] resourceStars) {
		PulseGraph.resourceStars = resourceStars;
	}


	/**
	 * @return the path
	 */
	public static ArrayList<Integer> getPath() {
		return Path;
	}

	/**
	 * @param path the path to set
	 */
	public static void setPath(ArrayList<Integer> path) {
		Path = path;
	}


	/**
	 * @return the graphMap
	 */
	public static Hashtable<String, Integer> getGraphMap() {
		return GraphMap;
	}


	/**
	 * @param graphMap the graphMap to set
	 */
	public static void setGraphMap(Hashtable<String, Integer> graphMap) {
		GraphMap = graphMap;
	}


	/**
	 * @return the lastID
	 */
	public static int getLastID() {
		return lastID;
	}
	
	
	public static void setFirst(int ter) {
		if(contadorTerm == 0) {
			terminePrimero = ter;
			contadorTerm = 1;
		}
	}


	/**
	 * @return the pathB
	 */
	public static ArrayList<Integer> getPathB() {
		return PathB;
	}


	/**
	 * @param pathB the pathB to set
	 */
	public static void setPathB(ArrayList<Integer> pathB) {
		PathB = pathB;
	}

	/**
	 * @return the pathJ
	 */
	public static ArrayList<Integer> getPathJ() {
		return PathJ;
	}

	/**
	 * @param pathJ the pathJ to set
	 */
	public static void setPathJ(ArrayList<Integer> pathJ) {
		PathJ = pathJ;
	}
	
	
	
}
