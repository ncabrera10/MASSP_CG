package pulseDataStructures;

import java.util.ArrayList;

/**
 * This class represents a pending pulse. A pulse that is currently on hold in the queue.
 * 
 * @author nicolas.cabrera-malik
 *
 */
public class PendingPulse {

	/**
	 * Node id on which the pulse is actually on.
	 */
	private int NodeID;

	/**
	 * Pending pulse weights
	 */
	
	private double[] pendingWeights;
	
	/**
	 * False if the pulse is already treated.
	 */
	private boolean notTreated;

	/**
	 * Partial path
	 */
	
	private ArrayList<Integer> partialPath;
	
	/**
	 * Sort criteria
	 */
	private double sortCriteria;
	
	/**
	 * This class creates a new pending pulse
	 * @param id
	 * @param partialWeights
	 * @param path
	 */
	public PendingPulse(int id, double[] partialWeights,ArrayList<Integer>path ) {
		NodeID = id;
		pendingWeights = new double[PulseGraph.R+1];
		for(int i=0;i<=PulseGraph.R;i++) {
			pendingWeights[i] = partialWeights[i];
		}
		notTreated = true;
		sortCriteria = 0;
		partialPath = new ArrayList<Integer>();
		for(Integer node : path) {
			partialPath.add(node);
		}
	}
	
	// GETTERS AND SETTERS:
	
	public int getNodeID() {
		return NodeID;
	}

	public void setNodeID(int nodeID) {
		NodeID = nodeID;
	}

	public double getDist() {
		return pendingWeights[0];
	}

	public void setDist(int dist) {
		this.pendingWeights[0] = dist;
	}

	public double getResource(int rec) {
		return pendingWeights[rec];
	}

	public void setResource(int weight,int rec) {
		this.pendingWeights[rec] = weight;
	}

	public boolean getNotTreated() {
		return notTreated;
	}


	public void setNotTreated(boolean notTreated) {
		this.notTreated = notTreated;
	}
	
	public void setSortCriteriaF(double sortCriteria) {
		this.sortCriteria = sortCriteria;
	}
	public void setSortCriteriaB(double sortCriteria) {
		this.sortCriteria = sortCriteria;
	}
	public double getSortCriteria() {
		return sortCriteria;
	}
	public ArrayList<Integer> getPartialPath() {
		return partialPath;
	}
}
