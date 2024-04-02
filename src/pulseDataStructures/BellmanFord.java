package pulseDataStructures;

import java.util.Iterator;

import commonDataStructures.DataHandler;

/**
 * This class implements the bellman ford algorithm to solve the SPs
 * 
 * 
 * @author nicolas.cabrera-malik
 *
 */
public class BellmanFord {

	/**
	 * SP direction
	 */
	
	private int K;
	
	/**
	 * This method creates a new BF instance
	 * @param dir
	 * @param pi
	 */
	public BellmanFord(int dir,double[][] pi) {
		K = dir;
		if(K == 1) {
			runFSP(pi);
		}else {
			runBSP(pi);
		}
	}
	
	/**
	 * Runs the Forward SP
	 * @param pi
	 */
	public void runFSP(double[][]pi) {
		
		//1. expand the first node
		
			//Recover the node
		
				String nodeKey="s";
				PulseNode tail = PulseGraph.getNodes().get(PulseGraph.getGraphMap().get(nodeKey)); 
				tail.minCostPathWeightsB[0] = 0;
				for(int r = 0;r<PulseGraph.R;r++) {
					tail.minCostPathWeightsB[r+1] = 0;
				}
				for(int r = 0;r<PulseGraph.R;r++) {
					if(tail.minResourcePathWeightsB.get(r+1)[r+1] >= 0) {
						for(int rr = 0;rr<PulseGraph.R;rr++) {
							tail.minResourcePathWeightsB.get(r+1)[rr+1] = 0;
						}
						tail.minResourcePathWeightsB.get(r+1)[0] = 0;
					}
				}
				
			//Explore the next nodes:
				
				for(Iterator<PulseArc> Iterator = tail.getOutgoingArcs().iterator();Iterator.hasNext();) {
					PulseArc arc = Iterator.next();
					PulseNode head = arc.getTarget(); 
					if(head.minCostPathWeightsB[0] > arc.geteDist()) {
						head.minCostPathWeightsB[0] = arc.geteDist();
					}
					for(int r = 0;r<PulseGraph.R;r++) {
						if(head.minResourcePathWeightsB.get(r+1)[r+1] >= 0) {
							for(int rr = 0;rr<PulseGraph.R;rr++) {
								head.minResourcePathWeightsB.get(r+1)[rr+1] = 0;
							}
							head.minResourcePathWeightsB.get(r+1)[0] = arc.geteDist();
						}
					}
				}
				
		//2. Explore the network by layers
				
				for (int t = 0; t < DataHandler.T; t++) {
					for (int i = 0; i < DataHandler.n; i++) {
						nodeKey=i+"-"+t;
						tail = PulseGraph.getNodes().get(PulseGraph.getGraphMap().get(nodeKey)); 
						for(Iterator<PulseArc> Iterator = tail.getOutgoingArcs().iterator();Iterator.hasNext();) {
							PulseArc arc = Iterator.next();
							PulseNode head = arc.getTarget(); 
							if(head.minCostPathWeightsB[0] > tail.minCostPathWeightsB[0] - pi[arc.i][arc.t]) {
								head.minCostPathWeightsB[0] = tail.minCostPathWeightsB[0] - pi[arc.i][arc.t];
								for(int r = 0;r<PulseGraph.R;r++) {
									if(arc.i == r) {
										head.minCostPathWeightsB[(arc.i+1)] = tail.minCostPathWeightsB[(arc.i+1)] + 1;
									}else {
										head.minCostPathWeightsB[(r+1)] = tail.minCostPathWeightsB[(r+1)];
									}
								}
							}
							for(int r = 0;r<PulseGraph.R;r++) {
								if(arc.i == r) {
									if(head.minResourcePathWeightsB.get(r+1)[r+1] > tail.minResourcePathWeightsB.get(r+1)[r+1] + 1) {
										head.minResourcePathWeightsB.get(r+1)[r+1] = tail.minResourcePathWeightsB.get(r+1)[r+1] + 1;
										for(int rr = 0;rr<PulseGraph.R;rr++) {
											if(r!=rr) {
												head.minResourcePathWeightsB.get(r+1)[rr+1] = tail.minResourcePathWeightsB.get(r+1)[rr+1];
											}
										}
										head.minResourcePathWeightsB.get(r+1)[0] = tail.minResourcePathWeightsB.get(r+1)[0] - pi[arc.i][arc.t];
											
									}
								}else {
									if(head.minResourcePathWeightsB.get(r+1)[r+1] > tail.minResourcePathWeightsB.get(r+1)[r+1]) {
										head.minResourcePathWeightsB.get(r+1)[r+1] = tail.minResourcePathWeightsB.get(r+1)[r+1];
										for(int rr = 0;rr<PulseGraph.R;rr++) {
											if(r!=rr) {
												if(arc.i == rr) {
													head.minResourcePathWeightsB.get(r+1)[rr+1] = tail.minResourcePathWeightsB.get(r+1)[rr+1] + 1;
												}else {
													head.minResourcePathWeightsB.get(r+1)[rr+1] = tail.minResourcePathWeightsB.get(r+1)[rr+1];
												}
											}
										}
										head.minResourcePathWeightsB.get(r+1)[0] = tail.minResourcePathWeightsB.get(r+1)[0] - pi[arc.i][arc.t];
									}
									
								}
							}
						}
					}
				}
				
						
	}
	
	/**
	 * Runs the Backward SP
	 * @param pi
	 */
	public void runBSP(double[][]pi) {

		//1. expand the first node
		
		//Recover the node
	
			String nodeKey="f";
			PulseNode head = PulseGraph.getNodes().get(PulseGraph.getGraphMap().get(nodeKey)); 
			head.minCostPathWeights[0] = 0;
			for(int r = 0;r<PulseGraph.R;r++) {
				head.minCostPathWeights[r+1] = 0;
			}
			for(int r = 0;r<PulseGraph.R;r++) {
				if(head.minResourcePathWeights.get(r+1)[r+1] >= 0) {
					for(int rr = 0;rr<PulseGraph.R;rr++) {
						head.minResourcePathWeights.get(r+1)[rr+1] = 0;
					}
					head.minResourcePathWeights.get(r+1)[0] = 0;
				}
			}
			
		//Explore the next nodes:
			
			for(Iterator<PulseArc> Iterator = head.getIncomingArcs().iterator();Iterator.hasNext();) {
				PulseArc arc = Iterator.next();
				PulseNode tail = arc.getSource(); 
				if(tail.minCostPathWeights[0] > arc.geteDist() - pi[arc.i][arc.t]) {
					tail.minCostPathWeights[0] = arc.geteDist() - pi[arc.i][arc.t];
					for(int r=0;r<PulseGraph.R;r++) {
						if(arc.i == r) {
							tail.minCostPathWeights[r+1] = 1;
						}
					}
				}
				for(int r = 0;r<PulseGraph.R;r++) {
					if(arc.i == r) {
						if(tail.minResourcePathWeights.get(r+1)[r+1] >= 1) {
							
							for(int rr = 0;rr<PulseGraph.R;rr++) {
								if(r!=rr) {
									tail.minResourcePathWeights.get(r+1)[rr+1] = 0;
								}else {
									tail.minResourcePathWeights.get(r+1)[rr+1] = 1;
								}
							}
							tail.minResourcePathWeights.get(r+1)[0] = arc.geteDist() - pi[arc.i][arc.t];
						}
					}else {
						if(tail.minResourcePathWeights.get(r+1)[r+1] >= 0) {
							for(int rr = 0;rr<PulseGraph.R;rr++) {
								if(rr == arc.i) {
									tail.minResourcePathWeights.get(r+1)[rr+1] = 1;
								}else {
									tail.minResourcePathWeights.get(r+1)[rr+1] = 0;
								}
							}
							tail.minResourcePathWeights.get(r+1)[0] = arc.geteDist() - pi[arc.i][arc.t];
						}
					}
							
				}
			}
		
	//2. Explore the network by layers
			
			for (int t = DataHandler.T-1; t >= 0; t--) {
				for (int i = 0; i < DataHandler.n; i++) {
					nodeKey=i+"-"+t;
					head = PulseGraph.getNodes().get(PulseGraph.getGraphMap().get(nodeKey)); 
					for(Iterator<PulseArc> Iterator = head.getIncomingArcs().iterator();Iterator.hasNext();) {
						PulseArc arc = Iterator.next();
						PulseNode tail = arc.getSource(); 
						if(arc.i>=0) {
							if(tail.minCostPathWeights[0] > head.minCostPathWeights[0] - pi[arc.i][arc.t]) {
								tail.minCostPathWeights[0] = head.minCostPathWeights[0] - pi[arc.i][arc.t];
								for(int r = 0;r<PulseGraph.R;r++) {
									if(arc.i == r) {
										tail.minCostPathWeights[(arc.i+1)] = head.minCostPathWeights[(arc.i+1)] + 1;
									}else {
										tail.minCostPathWeights[(r+1)] = head.minCostPathWeights[(r+1)];
									}
								}
							}
							for(int r = 0;r<PulseGraph.R;r++) {
								if(arc.i == r) {
									if(tail.minResourcePathWeights.get(r+1)[r+1] > head.minResourcePathWeights.get(r+1)[r+1] + 1) {
										tail.minResourcePathWeights.get(r+1)[r+1] = head.minResourcePathWeights.get(r+1)[r+1] + 1;
										for(int rr = 0;rr<PulseGraph.R;rr++) {
											if(r!=rr) {
												tail.minResourcePathWeights.get(r+1)[rr+1] = head.minResourcePathWeights.get(r+1)[rr+1];
											}
										}
										tail.minResourcePathWeights.get(r+1)[0] = head.minResourcePathWeights.get(r+1)[0] - pi[arc.i][arc.t];
										
									}
								}else {
									if(tail.minResourcePathWeights.get(r+1)[r+1] > head.minResourcePathWeights.get(r+1)[r+1]) {
										tail.minResourcePathWeights.get(r+1)[r+1] = head.minResourcePathWeights.get(r+1)[r+1];
										for(int rr = 0;rr<PulseGraph.R;rr++) {
											if(r!=rr) {
												if(arc.i == rr) {
													tail.minResourcePathWeights.get(r+1)[rr+1] = head.minResourcePathWeights.get(r+1)[rr+1] + 1;
												}else {
													tail.minResourcePathWeights.get(r+1)[rr+1] = head.minResourcePathWeights.get(r+1)[rr+1];
												}
											}
										}
										tail.minResourcePathWeights.get(r+1)[0] = head.minResourcePathWeights.get(r+1)[0] - pi[arc.i][arc.t];
									}
										
								}
							}
						}else {
							
							if(tail.minCostPathWeights[0] > head.minCostPathWeights[0]+arc.geteDist()) {
								tail.minCostPathWeights[0] = head.minCostPathWeights[0] + arc.geteDist();
								for(int r = 0;r<PulseGraph.R;r++) {
									if(arc.i == r) {
										tail.minCostPathWeights[(arc.i+1)] = head.minCostPathWeights[(arc.i+1)] + 1;
									}else {
										tail.minCostPathWeights[(r+1)] = head.minCostPathWeights[(r+1)];
									}
								}
							}
							for(int r = 0;r<PulseGraph.R;r++) {
								if(arc.i == r) {
									if(tail.minResourcePathWeights.get(r+1)[r+1] > head.minResourcePathWeights.get(r+1)[r+1] + 1) {
										tail.minResourcePathWeights.get(r+1)[r+1] = head.minResourcePathWeights.get(r+1)[r+1] + 1;
										for(int rr = 0;rr<PulseGraph.R;rr++) {
											if(r!=rr) {
												tail.minResourcePathWeights.get(r+1)[rr+1] = head.minResourcePathWeights.get(r+1)[rr+1];
											}
										}
										tail.minResourcePathWeights.get(r+1)[0] = head.minResourcePathWeights.get(r+1)[0]+arc.geteDist();
									}
								}else {
									if(tail.minResourcePathWeights.get(r+1)[r+1] > head.minResourcePathWeights.get(r+1)[r+1]) {
										tail.minResourcePathWeights.get(r+1)[r+1] = head.minResourcePathWeights.get(r+1)[r+1];
										for(int rr = 0;rr<PulseGraph.R;rr++) {
											if(r!=rr) {
												if(arc.i == rr) {
													tail.minResourcePathWeights.get(r+1)[rr+1] = head.minResourcePathWeights.get(r+1)[rr+1] + 1;
												}else {
													tail.minResourcePathWeights.get(r+1)[rr+1] = head.minResourcePathWeights.get(r+1)[rr+1];
												}
											}
										}
										tail.minResourcePathWeights.get(r+1)[0] = head.minResourcePathWeights.get(r+1)[0]+arc.geteDist();
									}
								
								}
							}
						}
						
					}
				}
			}
	}	
	
}
