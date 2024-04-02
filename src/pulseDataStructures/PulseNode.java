package pulseDataStructures;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;

import commonDataStructures.AlgorithmHandler;
import commonDataStructures.DataHandler;

/**
 * This class represents a node in the pulse graph. It contains the pulseF and pulseB methods.
 * 
 * @author nicolas.cabrera-malik
 *
 */
public class PulseNode {

	/**
	 * This array contains the indexes for all the outgoing arcs from this node
	 */
	
	public ArrayList<Integer> magicIndex;
	
	/**
	 * This array contains the indexes for all the incoming arcs to this node
	 */
	
	public ArrayList<Integer> magicIndex2;
	
	/**
	 * Boolean that tells if the node is visited for first time in the forward pulse
	 */
	
	boolean firstTime = true;
	
	/**
	 * Boolean that tells if the node is visited for first time in the backward pulse
	 */
	
	boolean firstTime2 = true;
	
	/**
	 * Bounds to reach the end node
	 */
	
	public double[] minCostPathWeights; 
	
	public Hashtable<Integer,double[]> minResourcePathWeights; 
	
	
	/**
	 * Bounds to reach the source node
	 */
	
	public double[] minCostPathWeightsB; 
	public Hashtable<Integer,double[]> minResourcePathWeightsB; 
	
	/**
	 * SP stuff
	 */
	
	public static final int infinity = (int)Double.POSITIVE_INFINITY;
	
	/**
	 * List of pending pulses in the node
	 */
	
	public ArrayList<PendingPulse> pendF;
	public ArrayList<PendingPulse> pendB;
	
	/**
	 * The list of incoming arcs
	 */
	private ArrayList<PulseArc> incomingArcs;
	
	/**
	 * The list of outgoing arcs
	 */
	private ArrayList<PulseArc> outgoingArcs;
	
	/**
	 * The vertex id
	 */
	private int id;
	
	/**
	 * Time period
	 */
	int t;  
	
	/**
	 * Activity
	 */
	int i;
	
	/**
	 * The key
	 */
	
	String key;
	
	/**
	 * Creates a node
	 * @param i the id
	 */
	public PulseNode(int i) {
		id = i;
		setLabels();
		magicIndex = new ArrayList<Integer>();
		magicIndex2 = new ArrayList<Integer>();
		pendF = new ArrayList<PendingPulse>();
		pendB = new ArrayList<PendingPulse>();
		incomingArcs = new ArrayList<PulseArc>();
		outgoingArcs = new ArrayList<PulseArc>();
	}
	
	
	
	/**
	 * Initializes the SP labels
	 */
	public void setLabels() {
		minCostPathWeights = new double[PulseGraph.R+1]; 
		minCostPathWeights[0] = infinity;
		minResourcePathWeights = new Hashtable<Integer,double[]>(); 
		minCostPathWeightsB = new double[PulseGraph.R+1]; 
		minCostPathWeightsB[0] = infinity;
		minResourcePathWeightsB = new Hashtable<Integer,double[]>(); 
		
		for(int j=1;j<=PulseGraph.R;j++) {
			minResourcePathWeights.put(j, new double[PulseGraph.R+1]);
			minResourcePathWeights.get(j)[0] = 0;
			minCostPathWeights[j] = 0;
			minResourcePathWeightsB.put(j, new double[PulseGraph.R+1]);
			minResourcePathWeightsB.get(j)[0] = 0;
			minCostPathWeightsB[j] = 0;
			
			for(int k = 1;k<=PulseGraph.R;k++) {
				if(k == j) {
					minResourcePathWeights.get(j)[k] = infinity;
					minResourcePathWeightsB.get(j)[k] = infinity;
				}else {
					minResourcePathWeights.get(j)[k] = 0;
					minResourcePathWeightsB.get(j)[k] = 0;
				}
			}
			
		}
	}

	/**
	 *Resets the SP labels 
	 */
	public void resetSPLabels() {
		minCostPathWeights[0] = infinity;
		minCostPathWeightsB[0] = infinity; 
		
		for(int j=1;j<=PulseGraph.R;j++) {
			minResourcePathWeights.get(j)[0] = 0;
			minCostPathWeights[j] = 0;
			minResourcePathWeightsB.get(j)[0] = 0;
			minCostPathWeightsB[j] = 0;
			
			for(int k = 1;k<=PulseGraph.R;k++) {
				if(k == j) {
					minResourcePathWeights.get(j)[k] = infinity;
					minResourcePathWeightsB.get(j)[k] = infinity;
				}else {
					minResourcePathWeights.get(j)[k] = 0;
					minResourcePathWeightsB.get(j)[k] = 0;
				}
			}
			
		}
		
		pendB.clear();
		pendF.clear();
		firstTime = true;
		firstTime2 = true;
		
	}

	/**
	 * @return the t
	 */
	public int getT() {
		return t;
	}



	/**
	 * @param t the t to set
	 */
	public void setT(int t) {
		this.t = t;
	}



	/**
	 * @return the i
	 */
	public int getI() {
		return i;
	}



	/**
	 * @param i the i to set
	 */
	public void setI(int i) {
		this.i = i;
	}



	/**
	 * @return the key
	 */
	public String getKey() {
		return key;
	}



	/**
	 * @param key the key to set
	 */
	public void setKey(String key) {
		this.key = key;
	}



	/**
	 * @return the incomingArcs
	 */
	public ArrayList<PulseArc> getIncomingArcs() {
		return incomingArcs;
	}



	/**
	 * @param incomingArcs the incomingArcs to set
	 */
	public void setIncomingArcs(ArrayList<PulseArc> incomingArcs) {
		this.incomingArcs = incomingArcs;
	}



	/**
	 * @return the outgoingArcs
	 */
	public ArrayList<PulseArc> getOutgoingArcs() {
		return outgoingArcs;
	}



	/**
	 * @param outgoingArcs the outgoingArcs to set
	 */
	public void setOutgoingArcs(ArrayList<PulseArc> outgoingArcs) {
		this.outgoingArcs = outgoingArcs;
	}
	
	
	/**
	 * This is the pulse procedure in the forward direction.
	 * @param pulseWeights: 0: cost 1: Time 2:Next Rec..
	 * @param step number of steps (For queues)
	 * @param pred the predecessor for the current pulse
	 */
	
	public void pulseF(double[] pulseWeights,int step,ArrayList<Integer>path){
		
		//Checks if the other thread is still alive
		if(PulseGraph.termine == false) {
		
			// if a node is visited for first time, sort the arcs
				
			if (this.firstTime) {
				this.firstTime = false;
				this.SortF(this.outgoingArcs);
			}
			
			//Checks if the pulse is dominated
			if(notDominatedF(pulseWeights,this)) {
				
				//Creates a pending pulse
				
				PendingPulse p = new PendingPulse(this.id,pulseWeights,path);
				p.setSortCriteriaF(pulseWeights[0] + this.minCostPathWeights[0]);
				p.setNotTreated(false);
				this.pendF.add(p);
				
				path.add(id);
				// Check for cycles
					
					//Checks if the current path should be completed
					
					if(completePathCheckF(pulseWeights,path)) {
						
						// Pulse all the head nodes for the outgoing arcs
						for (Iterator<PulseArc> iter = outgoingArcs.iterator();iter.hasNext();) { //Recordemos que cojo primero al arco que tiene menor costo
							
							// Update Cost and time
								PulseArc a = iter.next();
								double[] newWeights = new double[PulseHandler.R+1];
								newWeights[0] = (pulseWeights[0] + a.geteDist() - AlgorithmHandler.pi[a.i][a.t]); //Distancia del camino al agregar el nodo
								
								for(int r=1;r<=PulseHandler.R;r++) {
									if(a.i == r-1) {
										newWeights[r] = (pulseWeights[r] + 1);
									}else {
										newWeights[r] = (pulseWeights[r]);
									}
								}
								PulseNode head = a.getTarget();
								
								// Pruning strategies: infeasibility, bounds and labels
							
								boolean factible = true;
								int r = 1;
								while(factible && r<=PulseHandler.R) {
									if(newWeights[r] + head.minResourcePathWeights.get(r)[r] > DataHandler.u[r-1]) {
										factible = false;
									}
									r++;
								}
								
								if (path.size() <= DataHandler.L && factible && ((newWeights[0] + head.minCostPathWeights[0]) < PulseGraph.PrimalBound)){
							
									step++;
									if(step >= PulseGraph.depth) {
										//Checks if the stopped pulse is dominated
										if(notDominatedF(newWeights,head)) {
											p = new PendingPulse(head.id,newWeights,path);
											p.setSortCriteriaF(newWeights[0] + head.minCostPathWeights[0]);
											PulseHandler.addPendingPulse_DOrder(p, PulseHandler.pendingQueueF);
											head.pendF.add(p);
											
										}
									}
									else {
										head.pulseF(newWeights, step,path);
									}
									step--;
								}
								
							}
						}
					path.remove(path.size()-1);
				}
			}
	}
	
	/**
	 * This is the pulse procedure in the forward direction. (When the queue is empty)
	 * @param PTime time for the current pulse
	 * @param PDist Cost for the current pulse
	 * @param step number of steps (For queues)
	 * @param pred the predecessor for the current pulse
	 */
	
	public void pulseFWithQueues(double[] pulseWeights,int step,ArrayList<Integer>path){
		//Checks if the other thread is still alive
		if(PulseGraph.termine == false) {
		
			// if a node is visited for first time, sort the arcs
			if (this.firstTime) {
				this.firstTime = false;
				this.SortF(this.outgoingArcs); 
			}
			
				path.add(id);
				
				//Tries to complete the path
				if(completePathCheckF(pulseWeights,path)) {
					
					// Pulse all the head nodes for the outgoing arcs
					for (Iterator<PulseArc> iter = outgoingArcs.iterator();iter.hasNext();) {
						
						// Update Cost and time
							PulseArc a = iter.next();
							double[] newWeights = new double[PulseHandler.R+1];
							if(a.i>=0) {
								newWeights[0] = (pulseWeights[0] + a.geteDist() - AlgorithmHandler.pi[a.i][a.t]); 
								
							}else {
								newWeights[0] = (pulseWeights[0] + a.geteDist()); 
								
							}
							
							for(int r=1;r<=PulseHandler.R;r++) {
								if(a.i == r-1) {
									newWeights[r] = (pulseWeights[r] + 1);
								}else {
									newWeights[r] = (pulseWeights[r]);
								}
							}
							PulseNode head = a.getTarget();
							
						// Pruning strategies: infeasibility, bounds and labels
							
							boolean factible = true;
							int r = 1;
							while(factible && r<=PulseHandler.R) {
								if(newWeights[r] + head.minResourcePathWeights.get(r)[r] > DataHandler.u[r-1]) {
									factible = false;
								}
							r++;
							}
							
							if (path.size() <= DataHandler.L && factible && ((newWeights[0] + head.minCostPathWeights[0]) < PulseGraph.PrimalBound)){
								step++;
								if(step >= PulseGraph.depth) {
									//Checks if the stopped pulse is dominated
									if(notDominatedF(newWeights,head)) {
										PendingPulse p = new PendingPulse(head.id,newWeights,path);
										p.setSortCriteriaF(newWeights[0] + head.minCostPathWeights[0]);
										PulseHandler.addPendingPulse_DOrder(p, PulseHandler.pendingQueueF);
										head.pendF.add(p);
									}
								}
								else {
									head.pulseF(newWeights, step,path);
								}
								step--;
							}
							
					}
				}
		path.remove(path.size()-1);
		}
	}
	
	/**
	 * This is the pulse procedure in the forward direction.
	 * @param pulseWeights: 0: cost 1: Time 2:Next Rec..
	 * @param step number of steps (For queues)
	 * @param pred the predecessor for the current pulse
	 */
	
	public void pulseB(double[] pulseWeights,int step,ArrayList<Integer>path){
		
		//Checks if the other thread is still alive
		if(PulseGraph.termine == false) {
		
			// if a node is visited for first time, sort the arcs
				
			if (this.firstTime) {
				this.firstTime = false;
				this.SortB(this.incomingArcs); 
			}
			
			//Checks if the pulse is dominated
			if(notDominatedB(pulseWeights,this)) {
				
				//Creates a pending pulse
				
				PendingPulse p = new PendingPulse(this.id,pulseWeights,path);
				p.setSortCriteriaB(pulseWeights[0] + this.minCostPathWeightsB[0]);
				p.setNotTreated(false);
				this.pendB.add(p);
				
				path.add(id);
				
				// Check for cycles
					
					//Checks if the current path should be completed
					
					if(completePathCheckB(pulseWeights,path)) {
						
						// Pulse all the head nodes for the outgoing arcs
						for (Iterator<PulseArc> iter = incomingArcs.iterator();iter.hasNext();) {
							
							// Update Cost and time
								PulseArc a = iter.next();
								double[] newWeights = new double[PulseHandler.R+1];
								if(a.i>=0) {
									newWeights[0] = (pulseWeights[0] + a.geteDist() - AlgorithmHandler.pi[a.i][a.t]); 
								}else {
									newWeights[0] = (pulseWeights[0] + a.geteDist());
									
								}
								
								for(int r=1;r<=PulseHandler.R;r++) {
									if(a.i == r-1) {
										newWeights[r] = (pulseWeights[r] + 1);
									}else {
										newWeights[r] = (pulseWeights[r]);
									}
								}
								PulseNode tail = a.getSource();
								
								boolean factible = true;
								int r = 1;
								while(factible && r<=PulseHandler.R) {
									if(newWeights[r] + tail.minResourcePathWeightsB.get(r)[r] > DataHandler.u[r-1]) {
										factible = false;
									}
									r++;
								}
								
								if (path.size() <= DataHandler.L && factible && ((newWeights[0] + tail.minCostPathWeightsB[0]) < PulseGraph.PrimalBound)){
							
									step++;
									if(step >= PulseGraph.depth) {
										//Checks if the stopped pulse is dominated
										if(notDominatedB(newWeights,tail)) {
											p = new PendingPulse(tail.id,newWeights,path);
											p.setSortCriteriaB(newWeights[0] + tail.minCostPathWeightsB[0]);
											PulseHandler.addPendingPulse_DOrder(p, PulseHandler.pendingQueueB);
											tail.pendB.add(p);
											
										}
									}
									else {
										tail.pulseB(newWeights, step,path);
									}
									step--;
								}
								
							}
						}
					path.remove(path.size()-1);
				}
			}
	}
	
	/**
	 * This is the pulse procedure in the forward direction. (When the queue is empty)
	 * @param PTime time for the current pulse
	 * @param PDist Cost for the current pulse
	 * @param step number of steps (For queues)
	 * @param pred the predecessor for the current pulse
	 */
	
	public void pulseBWithQueues(double[] pulseWeights,int step,ArrayList<Integer>path){
		//Checks if the other thread is still alive
		if(PulseGraph.termine == false) {
		
			// if a node is visited for first time, sort the arcs
			if (this.firstTime) {
				this.firstTime = false;
				this.SortB(this.incomingArcs); 
			}
			
				path.add(id);
				//Tries to complete the path
				if(completePathCheckB(pulseWeights,path)) {
					
					// Pulse all the head nodes for the outgoing arcs
					for (Iterator<PulseArc> iter = incomingArcs.iterator();iter.hasNext();) {
						
						// Update Cost and time
							PulseArc a = iter.next();
							double[] newWeights = new double[PulseHandler.R+1];
							if(a.i>=0) {
								newWeights[0] = (pulseWeights[0] + a.geteDist() - AlgorithmHandler.pi[a.i][a.t]); 
								
							}else {
								newWeights[0] = (pulseWeights[0] + a.geteDist()); 
								
							}
							
							for(int r=1;r<=PulseHandler.R;r++) {
								if(a.i == r-1) {
									newWeights[r] = (pulseWeights[r] + 1);
								}else {
									newWeights[r] = (pulseWeights[r]);
								}
							}
							PulseNode tail = a.getSource();
							
						// Pruning strategies: infeasibility, bounds and labels
							
							boolean factible = true;
							int r = 1;
							while(factible && r<=PulseHandler.R) {
								if(newWeights[r] + tail.minResourcePathWeightsB.get(r)[r] > DataHandler.u[r-1]) {
									factible = false;
								}
							r++;
							}
							
							if (path.size() <= DataHandler.L && factible && ((newWeights[0] + tail.minCostPathWeightsB[0]) < PulseGraph.PrimalBound)){
								step++;
								if(step >= PulseGraph.depth) {
									//Checks if the stopped pulse is dominated
									if(notDominatedB(newWeights,tail)) {
										PendingPulse p = new PendingPulse(tail.id,newWeights,path);
										p.setSortCriteriaB(newWeights[0] + tail.minCostPathWeightsB[0]);
										PulseHandler.addPendingPulse_DOrder(p, PulseHandler.pendingQueueB);
										tail.pendB.add(p);
									}
								}
								else {
									tail.pulseB(newWeights, step,path);
								}
								step--;
							}
							
					}
				}
		path.remove(path.size()-1);
		}
	}
	
	/**
	 * This method checks if the current pulse is dominated. Also, it eliminates dominated pulses.
	 * @param pulseWeights
	 * @param nodeID
	 * @return
	 */
	public boolean notDominatedF(double[] pulseWeights,PulseNode nodo) {
		boolean notdominated = true;
		PendingPulse n;
		int rIndex;

		for(int i = nodo.pendF.size()-1; i >=0 ;i--){
			n = nodo.pendF.get(i);
			boolean dominado = true;
			int r = 0;
			while(dominado && r<=PulseGraph.R) {
				if(n.getResource(r) > pulseWeights[r]) {
					dominado = false;
				}
				r++;
			}
			
			if(dominado) {
				notdominated = false;
			}
			else {
				dominado = true;
				r = 0;
				while(dominado && r<=PulseGraph.R) {
					if(n.getResource(r) < pulseWeights[r]) {
						dominado = false;
					}
					r++;
				}
				if(dominado && PulseHandler.pendingQueueF.size() > 0) {
					if(n.getNotTreated()){
						rIndex = PulseHandler.binarySearch(n,PulseHandler.pendingQueueF);
						if(rIndex < PulseHandler.pendingQueueF.size() && rIndex != -1) {
							PulseHandler.pendingQueueF.remove(rIndex);
						}
						
					}
					nodo.pendF.remove(i);
					n = null;
				}
			}
		}
	
		return notdominated;
	}
	
	/**
	 * This method checks if the current pulse is dominated. Also, it eliminates dominated pulses.
	 * @param pulseWeights
	 * @param nodeID
	 * @return
	 */
	public boolean notDominatedB(double[] pulseWeights,PulseNode nodo) {
		boolean notdominated = true;
		PendingPulse n;
		int rIndex;

		for(int i = nodo.pendB.size()-1; i >=0 ;i--){
			n = nodo.pendB.get(i);
			boolean dominado = true;
			int r = 0;
			while(dominado && r<=PulseGraph.R) {
				if(n.getResource(r) > pulseWeights[r]) {
					dominado = false;
				}
				r++;
			}
			
			if(dominado) {
				notdominated = false;
			}
			else {
				dominado = true;
				r = 0;
				while(dominado && r<=PulseGraph.R) {
					if(n.getResource(r) < pulseWeights[r]) {
						dominado = false;
					}
					r++;
				}
				if(dominado && PulseHandler.pendingQueueB.size() > 0) {
					if(n.getNotTreated()){
						rIndex = PulseHandler.binarySearch(n,PulseHandler.pendingQueueB);
						if(rIndex < PulseHandler.pendingQueueB.size()&& rIndex != -1) {
							PulseHandler.pendingQueueB.remove(rIndex);
						}
					}
					nodo.pendB.remove(i);
					n = null;
				}
			}
		}
	
		return notdominated;
	}
	
	/**
	 * This method checks path completion for time and cost minimum paths
	 * @param PTime
	 * @param PDist
	 * @return true if the search must go on
	 */
	public boolean completePathCheckF(double[] pulseWeights,ArrayList<Integer>partialPath){
		boolean factible = true;
		int r = 0;
		while(factible && r<PulseHandler.R) {
			if(pulseWeights[r+1] + this.minCostPathWeights[r+1] > DataHandler.u[r]) {
				factible = false;
			}
			r++;
		}
		if(factible && pulseWeights[0] + this.minCostPathWeights[0] < PulseGraph.PrimalBound) {
			
			PulseGraph.PrimalBound = pulseWeights[0] + this.minCostPathWeights[0];
			PulseGraph.Path.clear();
			PulseGraph.Path = new ArrayList<Integer>();
			for(Integer node : partialPath) {
				PulseGraph.Path.add(node);
			}
			PulseGraph.finalCostF = pulseWeights[0];
			PulseGraph.finalNodeF = id;
			
			for(int i=0;i<PulseHandler.R;i++) {
				PulseGraph.getResourceStars()[i] = pulseWeights[i+1] + this.minCostPathWeights[i+1];
				PulseGraph.finalResourceF[i] = pulseWeights[i+1];
			}
			
			PulseGraph.best = 0+";"+1;
			return false;
		}
		else{
			
			for(int recu = 0;recu<PulseHandler.R;recu++) {
				factible = true;
				r = 0;
				while(factible && r<PulseHandler.R) {
					if(pulseWeights[r+1] + this.minResourcePathWeights.get(recu+1)[r+1] > DataHandler.u[r]) {
						factible = false;
					}
					r++;
				}
				if(factible && pulseWeights[0] + this.minResourcePathWeights.get(recu+1)[0] < PulseGraph.PrimalBound){
					PulseGraph.PrimalBound = pulseWeights[0] + this.minResourcePathWeights.get(recu+1)[0];
					PulseGraph.finalCostF = pulseWeights[0];
					PulseGraph.finalNodeF = id;
					PulseGraph.Path.clear();
					PulseGraph.Path = new ArrayList<Integer>();
					for(Integer node : partialPath) {
						PulseGraph.Path.add(node);
					}
					for(int i=0;i<PulseHandler.R;i++) {
						PulseGraph.getResourceStars()[i] = pulseWeights[i+1] + this.minResourcePathWeights.get(recu+1)[i+1];
						PulseGraph.finalResourceF[i] = pulseWeights[i+1];
					}
					PulseGraph.best = (recu+1)+";"+2;
				}
			}
			return true;
		}	
	}
	
	/**
	 * This method checks path completion for time and cost minimum paths
	 * @param PTime
	 * @param PDist
	 * @return true if the search must go on
	 */
	public boolean completePathCheckB(double[] pulseWeights,ArrayList<Integer>partialPath){
		boolean factible = true;
		int r = 0;
		while(factible && r<PulseHandler.R) {
			if(pulseWeights[r+1] + this.minCostPathWeightsB[r+1] > DataHandler.u[r]) {
				factible = false;
			}
			r++;
		}
		if(factible && pulseWeights[0] + this.minCostPathWeightsB[0] < PulseGraph.PrimalBound) {
			
			PulseGraph.PrimalBound = pulseWeights[0] + this.minCostPathWeightsB[0];
			PulseGraph.PathB.clear();
			PulseGraph.PathB = new ArrayList<Integer>();
			for(Integer node : partialPath) {
				PulseGraph.PathB.add(node);
			}
			PulseGraph.finalCostB = pulseWeights[0];
			PulseGraph.finalNodeB = id;
			
			for(int i=0;i<PulseHandler.R;i++) {
				PulseGraph.getResourceStars()[i] = pulseWeights[i+1] + this.minCostPathWeightsB[i+1];
				PulseGraph.finalResourceB[i] = pulseWeights[i+1];
			}
			
			PulseGraph.best = 0+";"+3;
			return false;
		}
		else{
			
			for(int recu = 0;recu<PulseHandler.R;recu++) {
				factible = true;
				r = 0;
				while(factible && r<PulseHandler.R) {
					if(pulseWeights[r+1] + this.minResourcePathWeightsB.get(recu+1)[r+1] > DataHandler.u[r]) {
						factible = false;
					}
					r++;
				}
				if(factible && pulseWeights[0] + this.minResourcePathWeightsB.get(recu+1)[0] < PulseGraph.PrimalBound){
					PulseGraph.PrimalBound = pulseWeights[0] + this.minResourcePathWeightsB.get(recu+1)[0];
					PulseGraph.finalCostB = pulseWeights[0];
					PulseGraph.finalNodeB = id;
					PulseGraph.PathB.clear();
					PulseGraph.PathB = new ArrayList<Integer>();
					for(Integer node : partialPath) {
						PulseGraph.PathB.add(node);
					}
					for(int i=0;i<PulseHandler.R;i++) {
						PulseGraph.getResourceStars()[i] = pulseWeights[i+1] + this.minResourcePathWeightsB.get(recu+1)[i+1];
						PulseGraph.finalResourceB[i] = pulseWeights[i+1];
					}
					PulseGraph.best = (recu+1)+";"+4;
				}
			}
			return true;
		}	
	}
	
	/**
	 * This method sorts the outgoing arcs
	 * @param set
	 */
	private void SortF(ArrayList<PulseArc> set) 
	{
		QS(outgoingArcs, 0, outgoingArcs.size()-1);
	}
	
	/**
	 * 
	 * @param e
	 * @param b
	 * @param t
	 */
	public void QS(ArrayList<PulseArc> e, int b, int t)
	{
		 int pivote;
	     if(b < t){
	        pivote=colocar(e,b,t);
	        QS(e,b,pivote-1);
	        QS(e,pivote+1,t);
	     }  
	}
	
	
	/**
	 * 
	 * @param e
	 * @param b
	 * @param t
	 * @return
	 */
	public int colocar(ArrayList<PulseArc> e, int b, int t)
	{
	    int i;
	    int pivote;
	    double valor_pivote;
	    PulseArc temp;
	    pivote = b;
	    valor_pivote = e.get(pivote).getTarget().minCostPathWeights[0];
	    for (i=b+1; i<=t; i++){
	        if (e.get(i).getTarget().minCostPathWeights[0] < valor_pivote){
	                pivote++;    
	                temp= e.get(i);
	                e.set(i, e.get(pivote));
	                e.set(pivote, temp);
	        }
	    }
	    temp=e.get(b);
	    e.set(b, e.get(pivote));
        e.set(pivote, temp);
	    return pivote;
	    
	}

	/**
	 * This method sorts the outgoing arcs
	 * @param set
	 */
	private void SortB(ArrayList<PulseArc> set) 
	{
		QS(incomingArcs, 0, incomingArcs.size()-1);
	}
	
	/**
	 * 
	 * @param e
	 * @param b
	 * @param t
	 */
	public void QS2(ArrayList<PulseArc> e, int b, int t)
	{
		 int pivote;
	     if(b < t){
	        pivote=colocar(e,b,t);
	        QS2(e,b,pivote-1);
	        QS2(e,pivote+1,t);
	     }  
	}
	
	
	/**
	 * 
	 * @param e
	 * @param b
	 * @param t
	 * @return
	 */
	public int colocar2(ArrayList<PulseArc> e, int b, int t)
	{
	    int i;
	    int pivote;
	    double valor_pivote;
	    PulseArc temp;
	    pivote = b;
	    valor_pivote = e.get(pivote).getSource().minCostPathWeightsB[0];
	    for (i=b+1; i<=t; i++){
	        if (e.get(i).getSource().minCostPathWeightsB[0] < valor_pivote){
	                pivote++;    
	                temp= e.get(i);
	                e.set(i, e.get(pivote));
	                e.set(pivote, temp);
	        }
	    }
	    temp=e.get(b);
	    e.set(b, e.get(pivote));
        e.set(pivote, temp);
	    return pivote;
	    
	}

	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}



	/**
	 * @param id the id to set
	 */
	public void setId(int id) {
		this.id = id;
	}
	
	/**
	 * This method tries to join paths for a pulse in the forward direction, using information from the backward direction
	 * @param pulseWeights
	 */
	public void checksPathJoinF(double[] pulseWeights,ArrayList<Integer>path) {
		for(int i=0;i<pendB.size();i++) {
			PendingPulse p = pendB.get(i);
			if(p!=null) {
				boolean factible = true;
				int r = 0;
				while(factible && r<PulseHandler.R) {
					if(pulseWeights[r+1] + p.getResource(r+1) > DataHandler.u[r]) {
						factible = false;
					}
					r++;
				}
				if(factible && p.getDist() + pulseWeights[0] < PulseGraph.PrimalBound) {
					PulseGraph.PrimalBound = p.getDist() + pulseWeights[0];
					PulseGraph.PathJ = new ArrayList<Integer>();
					for(Integer node : path) {
						PulseGraph.PathJ.add(node);
					}
					for(int k=p.getPartialPath().size()-1;k>=0;k--) {
						PulseGraph.PathJ.add(p.getPartialPath().get(k));
					}
					for(int j=0;j<PulseHandler.R;j++) {
						PulseGraph.resourceStars[j] = pulseWeights[j+1] + p.getResource(j+1);
					}
					
					PulseGraph.best = 0+";"+5;
				}
			}
			p = null;
		}
	}
	
	/**
	 * This method tries to join paths for a pulse in the forward direction, using information from the backward direction
	 * @param pulseWeights
	 */
	public void checksPathJoinB(double[] pulseWeights,ArrayList<Integer>path) {
		for(int i=0;i<pendF.size();i++) {
			PendingPulse p = pendF.get(i);
			if(p!=null) {
				boolean factible = true;
				int r = 0;
				while(factible && r<PulseHandler.R) {
					if(pulseWeights[r+1] + p.getResource(r+1) > DataHandler.u[r]) {
						factible = false;
					}
					r++;
				}
				if(factible && p.getDist() + pulseWeights[0] < PulseGraph.PrimalBound) {
					PulseGraph.PrimalBound = p.getDist() + pulseWeights[0];
					PulseGraph.PathJ = new ArrayList<Integer>();
					for(Integer node : p.getPartialPath()) {
						PulseGraph.PathJ.add(node);
					}
					for(int k=path.size()-1;k>=0;k--) {
						PulseGraph.PathJ.add(path.get(k));
					}
					for(int j=0;j<PulseHandler.R;j++) {
						PulseGraph.resourceStars[j] = pulseWeights[j+1] + p.getResource(j+1);
					}
					
					PulseGraph.best = 0+";"+5;
				}
			}
			p = null;
		}
	}
	
}
