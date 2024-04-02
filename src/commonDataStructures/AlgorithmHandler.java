package commonDataStructures;

import ilog.concert.*;
import ilog.cplex.*;
import pulseDataStructures.PulseArc;
import pulseDataStructures.PulseGraph;
import pulseDataStructures.PulseHandler;
import pulseDataStructures.PulseNode;
import threads.PulseTask;
import threads.ShortestPathTask;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * This class contains the logic to run the CG procedure.
 * 
 * Depending on the algorithm selected by the user, either a labeling or the pulse will
 * be used to solve the pricing problem.
 * 
 * 
 * @author nicolas.cabrera-malik
 *
 */
public class AlgorithmHandler {

	/**
	 * Optimal value for the linear relaxation after solving CG
	 */
	
	public double LR;		
	
	/**
	 * Stuff used for the CG approach
	 */
	
	boolean stop; 	
	
	/**
	 * Pool of columns 
	 */
	public ArrayList<Column> omegaHat; 
	
	/**
	 * Dual variables
	 */
	public static double pi[][]; 			

	/**
	 * Network representation of the subproblem
	 */
	
	Graph DP;		
	

	/**
	 * Stats for reporting: Max time solving an interation of the auxiliary problem
	 */
	public double maxTime;
	
	/**
	 * Stats for reporting: Avg time solving an interation of the auxiliary problem
	 */
	
	public double avgTime;
	
	/**
	 * Stats for reporting: Min time solving an interation of the auxiliary problem
	 */
	
	public double minTime;
	
	/**
	 * The labeling algorithm identifier
	 */
	public int pricingSolved;
	
	/**
	 * The cplex model 1
	 */
	IloCplex cplex;							

	/**
	 * This method creates an algorithm data handler
	 * @throws InterruptedException
	 * @throws IloException
	 */
	public AlgorithmHandler() throws InterruptedException, IloException {
		LR = (int) Double.POSITIVE_INFINITY;
		cplex = new IloCplex();
		stop = false;
		omegaHat = new ArrayList<Column>();
		
		maxTime = -1;
		avgTime = 0;
		minTime = 999999999;
		
	}

	/**
	 * Runs the CG using the IP as auxiliary problem
	 * @throws IloException
	 */
	public void runCGIP() throws IloException {
		initializeColumns();

		int iter = 0; 
		while(stop==false) {
			System.out.println("******************************************* ITERATION "+iter+" Omega Size: "+omegaHat.size()+" *************************************************************************");
			stop = true;
			solveCGMP();					//Solve master problem
			Column s = solvePricingIP(); 	//Solve pricing problem using IP		
			if(s.RC<-0.0001) {
				stop = false;
				omegaHat.add(s);
				s.print();
			}
			iter++;
		}

	}

	/**
	 * Solves the auxiliary problem using cplex
	 * @return
	 * @throws IloException
	 */
	private Column solvePricingIP() throws IloException {
		cplex = new IloCplex();
		cplex.setOut(null);
		IloNumVar[][] x = new IloNumVar[DataHandler.n][DataHandler.T];
		IloNumVar[] y = new IloNumVar[DataHandler.T];
				
		//Create variables 
		for (int i = 0; i < DataHandler.n; i++) {
			for (int t = 0; t < DataHandler.T; t++) {
				x[i][t] = cplex.numVar(0, 1, IloNumVarType.Bool);
			}
		}		
		for (int t = 0; t < DataHandler.T; t++) {
			y[t] = cplex.numVar(0, 1, IloNumVarType.Bool);
		}
		
		// Add constraints
		// Maximum shift duration
			IloLinearNumExpr expr1 = cplex.linearNumExpr();
			for (int i = 0; i < DataHandler.n; i++) {
				for (int t = 0; t < DataHandler.T; t++) {
					expr1.addTerm(1, x[i][t]);
				}
			}		
			cplex.addLe(expr1, DataHandler.L);
		
		// Max one activity at any given time
		for (int t = 0; t < DataHandler.T; t++) {
			IloLinearNumExpr expr = cplex.linearNumExpr();
			for (int i = 0; i < DataHandler.n; i++) {
					expr.addTerm(1, x[i][t]);
			}		
			cplex.addLe(expr, 1);
		}
		
		//Maximum time per activity
		for (int i = 0; i < DataHandler.n; i++) {
			IloLinearNumExpr expr = cplex.linearNumExpr();
			for (int t = 0; t < DataHandler.T; t++) {
				expr.addTerm(1, x[i][t]);
			}		
			cplex.addLe(expr, DataHandler.u[i]);
		}

		// Shift starts once
		IloLinearNumExpr expr2 = cplex.linearNumExpr();
		for (int t = 0; t < DataHandler.T; t++) {
			expr2.addTerm(1, y[t]);
		}
		cplex.addEq(expr2, 1);
		
		//Ensure continuity
		for (int i = 1; i < DataHandler.n; i++) {
			for (int t = 1; t < DataHandler.T; t++) {
				IloLinearNumExpr expr = cplex.linearNumExpr();
				expr.addTerm(1, x[i][t]);
				expr.addTerm(-1, x[i][t-1]);
				expr.addTerm(-1, x[0][t-1]);
				expr.addTerm(-1, y[t]);

				cplex.addLe(expr, 0);
			}		
		}
		// Activities at time 1 only if shift starts at time 1
		for (int i = 0; i < DataHandler.n; i++) {
				IloLinearNumExpr expr = cplex.linearNumExpr();
				expr.addTerm(1, x[i][0]);
				expr.addTerm(-1, y[0]);
				cplex.addLe(expr, 0);
		}
		// A change of activity can only happen when worker is busy
		for (int t = 1; t < DataHandler.T; t++) {
			IloLinearNumExpr expr = cplex.linearNumExpr();
			expr.addTerm(1, x[0][t]);
			
			for (int i = 1; i < DataHandler.n; i++) {
					expr.addTerm(-1, x[i][t-1]);
			}		
			cplex.addLe(expr, 0);
		}
		// ADD OBJECTIVE FUNCTION	
		IloLinearNumExpr obj = cplex.linearNumExpr();
		for (int i = 0; i < DataHandler.n; i++) {
			for (int t = 0; t < DataHandler.T; t++) {
				obj.addTerm(pi[i][t], x[i][t]);
			}
		}		


		cplex.addMaximize(obj,"REDUCED COST"); // Max because we are considering the negative term only

		// Optimize model
		cplex.solve();

		if(cplex.getStatus() == IloCplex.Status.Infeasible || cplex.getStatus() == IloCplex.Status.InfeasibleOrUnbounded){
			System.out.println("		CG PRICING is infeasible!");
			cplex.clearModel();
			cplex.end();
			return null;
		}
		else{
			// Print solution
			double RC= (1- cplex.getObjValue());
			System.out.println("Min Reduced Cost pricing IP: "+RC);
			// Generate the new column
			Column s = new Column();
			s.RC = RC;
			for (int i = 0; i < DataHandler.n; i++) {
				for (int t = 0; t < DataHandler.T; t++) {
					if(cplex.getValue(x[i][t])>0.1) {s.s[i][t]=1;}
				}
			}		
			//s.print(data);
			cplex.clearModel();
			cplex.end();
			return s;
		}

		
	}

	/**
	 * This method solves the master problem
	 * @throws IloException
	 */
	private void solveCGMP() throws IloException {
		cplex = new IloCplex();
		cplex.setOut(null);
		
		int numS = omegaHat.size();
		
		// Define decision variables
		IloNumVar[] lambda = new IloNumVar[numS];
		
		for (int j = 0; j < numS; j++) {
			lambda[j] = cplex.numVar(0, Double.MAX_VALUE, IloNumVarType.Float);
		}
		
		// Define constraints: satisfy demand
		IloRange[][] c1 = new IloRange[DataHandler.n][DataHandler.T];
		
		for (int i = 0; i < DataHandler.n; i++) {
			for (int t = 0; t < DataHandler.T; t++) {

				IloLinearNumExpr expr = cplex.linearNumExpr();

				for (int j = 0; j < numS; j++) {
					expr.addTerm(omegaHat.get(j).s[i][t], lambda[j]);
				}
				c1[i][t] = cplex.addGe(expr, DataHandler.d[i][t]);
			}
		}
		// Define objective function
		IloLinearNumExpr obj = cplex.linearNumExpr();
		for (int j = 0; j < numS; j++) {
			obj.addTerm(1, lambda[j]);
		} 
		cplex.addMinimize(obj,"OBJ");
		
		cplex.solve();
		
		if(cplex.getStatus() == IloCplex.Status.Infeasible){
			System.out.println("		MP problem is infeasible!");
		}
		else{
			System.out.println("***************************************MP Obj: "+cplex.getObjValue());
			LR = cplex.getObjValue();
		
			// Get dual variables

			pi = new double[DataHandler.n][DataHandler.T];
			for (int i = 0; i < DataHandler.n; i++) {
				for (int t = 0; t < DataHandler.T; t++) {
					pi[i][t] = cplex.getDual(c1[i][t]);
				}
			}

		}
		cplex.clearModel();
		cplex.end();

		
	}

	/**
	 * This method generates the initial feasible columns
	 */
	private void initializeColumns() {
		// Generate n simple schedules
		for (int i = 1; i < DataHandler.n; i++) {
			for (int t = 0; t < DataHandler.T; t++) {
				Column s = new Column();
				s.s[i][t]=1;
				omegaHat.add(s);
			}
		}


	}


	/**
	 * This method runs the CG using the traditional labeling algorithm as the auxiliary problem
	 * @throws IloException
	 */
	public void runCGLABEL() throws IloException {
		initializeColumns();
		genNetwork();
		int iter = 0; 
		double Atime = System.currentTimeMillis();
		while(stop==false && (System.currentTimeMillis()-Atime)/1000 <= 3600) { //We impose a time limit of 1h.
			System.out.println("******************************************* ITERATION "+iter+" Omega Size: "+omegaHat.size()+" *************************************************************************");
			stop = true;
			solveCGMP();						//Solve master problem
			Column s = solvePricingLabeling(); 	//Solve pricing problem using IP		
			if(s.RC<-0.0001) {
				stop = false;
				omegaHat.add(s);
				//s.print(data);
			}
			iter++;
			
		}
		
		avgTime = avgTime/pricingSolved;

	}

	/**
	 * This is the labeling algorithm
	 * @return
	 */
	private Column solvePricingLabeling() {
		double Atime = System.currentTimeMillis();
		
		// Initialize labels		
		@SuppressWarnings("unchecked")
		ArrayList<double[]> [] labels = new ArrayList [DP.nodes.size()];

		// Initialize paths		
		@SuppressWarnings("unchecked")
		ArrayList<String> [] paths = new ArrayList [DP.nodes.size()];

		//Initialize
		for (int i = 0; i < DP.nodes.size(); i++) {
					labels[i] = new ArrayList<double[]>();
					paths[i] = new ArrayList<String>();
		}			
		// Start at the FIRST label with cost 0 and resource consumption 0. Position n+1 captures the cost
		double[] iniLabel = new double[DataHandler.n+1];
		String iniTour = "S";
		labels[0].add(iniLabel);
		paths[0].add(iniTour);
		
		// add label to the queue to start the algorithm
		ArrayList<Integer> queue = new ArrayList<Integer>();
		queue = new ArrayList<Integer>();
		queue.add(0);

		// Update the labels for each arc in a topological order
		int[] nodeInQueue = new int[DP.nodes.size()];
		nodeInQueue[0]++;
		while (queue.size()>0) {
			//System.out.println("QUEUE: "+queue);
			int nodeIndex = queue.get(0);
			nodeInQueue[nodeIndex]--;
			// Expand all the labels
			for (int k = 0; k < labels[nodeIndex].size(); k++) {
				double[] label = labels[nodeIndex].get(k);
				String path = paths[nodeIndex].get(k);
				// expand thru all the out arcs
				for (int i = 0; i < DP.nodes.get(nodeIndex).out.size(); i++) {
					Arc arc = DP.arcs.get(DP.nodes.get(nodeIndex).out.get(i));
					Node headNode = DP.nodes.get(arc.head);
						double[] newLabel = new double[DataHandler.n+1];
						String newPath = path+" "+headNode.key;
						for (int q = 0; q < DataHandler.n; q++) {
							newLabel[q] = label[q];  //Copy old label
						}
						if(arc.i>=0) {
							newLabel[arc.i]++; //Update the resource consumption
							newLabel[DataHandler.n] = round(label[DataHandler.n]+arc.cost - pi[arc.i][arc.t]); //Update the cost
						}
						else {//This is a start of shift arc
							newLabel[DataHandler.n]  = round(label[DataHandler.n]+arc.cost); //Update the cost
						}
						if(checkFeasible(newLabel, newPath)==true && checkDominance(newLabel, labels[arc.head], paths[arc.head], headNode.endNode)==false ) { // checkDominance and feasibility
							labels[arc.head].add(newLabel); //Add non-dominated label
							paths[arc.head].add(newPath); //Add corresponding tour
							
							if(nodeInQueue[arc.head]==0) {
								queue.add(arc.head);	//Add head node to the queue if not added before
								nodeInQueue[arc.head]++;
							}
						}

					
				}

			}
			queue.remove(0);
		}
		
		double minRC = labels[DP.lastNode].get(0)[DataHandler.n];
		System.out.println("PATH REDUCED COST: "+minRC);
		
		//Recover optimal path
		Column s = new Column();
		s.RC = minRC;
		System.out.println("OPT PATH: "+paths[DP.lastNode].get(0)); 
		parseOptPath(paths[DP.lastNode].get(0),s);
		
		// Capture some stats for reporting
		double runtime = (System.currentTimeMillis()-Atime)/1000.0;
		if(runtime>maxTime) {maxTime = runtime;}
		if(runtime<minTime) {minTime = runtime;}
		avgTime+=runtime;
		pricingSolved++;
		
		return s;
	}

	/**
	 * Recovers the optimal path
	 * @param path
	 * @param s
	 */
	private void parseOptPath(String path, Column s) {
		String delims = "[ ]+";
		String[] tokens = path.split(delims);
		for (int i = 1; i < tokens.length-1; i++) {
			int activity = Integer.parseInt(tokens[i].substring(0, 1));
			int time = Integer.parseInt(tokens[i].substring(2));
			s.s[activity][time] = 1;
		}
		
	}

	/**
	 * This method checks dominance relationships
	 * @param newLabel
	 * @param labels
	 * @param paths
	 * @param endNode
	 * @return
	 */
	private boolean checkDominance(double[] newLabel, ArrayList<double[]> labels, ArrayList<String> paths, boolean endNode) {
		int Q = newLabel.length-1;
		if(endNode) { //Just check the cost
			if(labels.size()>0) {
				if(labels.get(0)[Q]<=newLabel[Q]) {return true;}
				//Compare against the list of labels in the node to see if it dominates
				if(labels.get(0)[Q]> newLabel[Q]) {labels.remove(0); paths.remove(0);}
			}
		}
		else {
			//Compare against the list of labels in the node to see if it is dominated
			boolean dominated = true;	
			for (int i = 0; i < labels.size(); i++) {
				dominated = true;
				for (int j = 0; j < newLabel.length; j++) {
					if(newLabel[j] < labels.get(i)[j] ) {
						dominated = false;
						j=newLabel.length+10;
					}
				}
				if(dominated==true) {
					return true;
				}
			}

			//Compare against the list of labels in the node to see if it dominates
			if(dominated==false) {
				for (int i = 0; i < labels.size(); i++) {
					boolean dominates = true;
					for (int j = 0; j < newLabel.length; j++) {
						if(newLabel[j] > labels.get(i)[j] ) {
							dominates = false;
							j=newLabel.length+10;
						}
					}
					if(dominates==true) {
						labels.remove(i); 
						paths.remove(i);
						i--;
					}


				}

			}
		}
		return false;
	}

	/**
	 * This method checks feasibility
	 * @param data
	 * @param label
	 * @param newPath
	 * @return
	 */
	private boolean checkFeasible(double[] label, String newPath) {
		for (int i = 0; i < DataHandler.n; i++) {
			if(label[i]>DataHandler.u[i]) {return false;}
		}
		
		String delims = "[ ]+";
		String[] tokens = newPath.split(delims);
		if(tokens.length>DataHandler.L+2) {
			return false;
		}
		
		return true;
	}
	
	/**
	 * This method prints the information a label
	 * @param newLabel
	 */
	@SuppressWarnings("unused")
	private void printLabel(double[] newLabel) {
		for (int i = 0; i < newLabel.length; i++) {
			System.out.print(newLabel[i]+" ");
		}
		System.out.println();
	}
	
	/**
	 * This method rounds a double value
	 * @param value
	 * @return
	 */
	public double round(double value) {
		double rounded;
		
		rounded = Math.round(value*1000)/1000.0;
		
		return rounded;
	}
	
	/**
	 * This method creates the network
	 * @param data
	 */
	private void genNetwork() {

		DP = new Graph();
		
		int nodeCount = 0;
		
		// Create initial node
		Node node0 = new Node();
		node0.key = "S";
		DP.nodes.add(node0);
		DP.GraphMap.put(node0.key, nodeCount);
		nodeCount++;

		// Create end node
		Node nodee = new Node();
		nodee.key = "END";
		nodee.endNode = true;
		DP.nodes.add(nodee);
		DP.GraphMap.put(nodee.key, nodeCount);
		DP.lastNode = (nodeCount);
		nodeCount++;
		
		//Create all other nodes
		for (int i = 0; i < DataHandler.n; i++) {
			for (int t = 0; t < DataHandler.T; t++) {
				Node n = new Node();
				n.t=t;
				n.i=i;
				n.genKey();
				DP.nodes.add(n);
				DP.GraphMap.put(n.key, nodeCount);
				nodeCount++;
				
			}
		}
		
		//Create arcs from S to every activity node and from every activity node to the terminal
		for (int i = 1; i < DataHandler.n; i++) {
			for (int t = 0; t < DataHandler.T; t++) {
				
				String nodeKey=i+"-"+t;
				Arc arc = new Arc();
				arc.i = -1;   // Start of shift arcs, not corresponding to any resource
				arc.t = t;   
				arc.cost = 1; //Cost equals 1 because is a start of shift
				arc.tail = DP.GraphMap.get(node0.key);
				arc.head = DP.GraphMap.get(nodeKey);
				DP.arcs.add(arc);

				Arc arc2 = new Arc();
				arc2.tail = DP.GraphMap.get(nodeKey);
				arc2.head = DP.GraphMap.get(nodee.key);
				arc2.t=t;
				arc2.i=i;
				DP.arcs.add(arc2);
				
			}
		}
		
		//Create arcs from every activity node to the same activity or a change of activity node
		for (int i = 1; i < DataHandler.n; i++) {
			for (int t = 0; t < DataHandler.T-1; t++) {
				
				String nodeKey=i+"-"+t;
				String headKey=i+"-"+(t+1);
				Arc arc = new Arc();
				arc.tail = DP.GraphMap.get(nodeKey);
				arc.head = DP.GraphMap.get(headKey);
				arc.t=t;
				arc.i=i;
				DP.arcs.add(arc);

				headKey=0+"-"+(t+1);
				Arc arc2 = new Arc();
				arc2.tail = DP.GraphMap.get(nodeKey);
				arc2.head = DP.GraphMap.get(headKey);
				arc2.t=t;
				arc2.i=i;
				DP.arcs.add(arc2);
				
			}
		}

		//Create arcs from every change of activity node
			for (int t = 0; t < DataHandler.T-1; t++) {

				String nodeKey=0+"-"+t;
				for (int i = 1; i < DataHandler.n; i++) {
					String headKey=i+"-"+(t+1);
					Arc arc = new Arc();
					arc.tail = DP.GraphMap.get(nodeKey);
					arc.head = DP.GraphMap.get(headKey);
					arc.t=t;
					arc.i=0;
					DP.arcs.add(arc);
				}
								
			}
		
		
		// Populate in and out arrays
		for (int i = 0; i < DP.arcs.size(); i++) {
			Arc a = DP.arcs.get(i);
			DP.nodes.get(a.tail).out.add(i);
			DP.nodes.get(a.head).in.add(i);
		}
		
		DP.print3();
	}

	
	/*********************************************************************************************************
	 *********************************** Methods for the Bidirectional P *************************************
	 *********************************************************************************************************
	 */
	
	/**
	 * This method runs the CG using the bidirectional pulse algorithm as the auxiliary problem
	 * @param data
	 * @throws IloException
	 */
	public void runCGPulse() throws IloException {
		
		//Initializes the column generation
		
			initializeColumns();
		
		//Creates the graph
			
			genNetworkPulse();
			
		// Starts the clock:
			
			double Atime = System.currentTimeMillis();
			
			int iter = 0;  
			while(stop==false && (System.currentTimeMillis()-Atime)/1000 <= 3600) { //We impose a time limit of 1h.
				System.out.println("******************************************* ITERATION "+iter+" Omega Size: "+omegaHat.size()+" *************************************************************************");
				stop = true;
				solveCGMP();						//Solve master problem
				Column s = solvePrincingPulse(); 	//Solve pricing problem using IP	
				
				if(s.RC<-0.0001) {
					stop = false;
					omegaHat.add(s);
				}
				iter++;
				
			}
		 
		avgTime = avgTime/pricingSolved;
	}
	
	/**
	 * This method creates the network
	 * @param data
	 */
	private void genNetworkPulse() {

		//Creates an empty pulse graph
		
		new PulseGraph();
		
		//Creates the nodes:
		
			//Initializes the nodeCount
		
				int nodeCount = 0;
				
			//Creates the initial node
			
				PulseNode inode = new PulseNode(0);
				inode.setKey("s");
				PulseGraph.addNode(inode);
				PulseGraph.getGraphMap().put("s",0);
				nodeCount++;
			
			//Create all other nodes
					
				for (int i = 0; i < DataHandler.n; i++) {
					for (int t = 0; t < DataHandler.T; t++) {
						PulseNode node = new PulseNode(nodeCount);
						node.setT(t);
						node.setI(i);
						node.setKey(i+"-"+t);
						PulseGraph.addNode(node);
						PulseGraph.getGraphMap().put(i+"-"+t,nodeCount);
						nodeCount++;
					}	
				}
	
			//Creates the end node
			
				PulseNode fnode = new PulseNode(nodeCount);
				fnode.setKey("f");
				PulseGraph.getGraphMap().put("f",nodeCount);
				PulseGraph.addNode(fnode);
				PulseGraph.setLastID(nodeCount);
		
		//Creates the arcs:
			
			//Initializes the arcCount
				
				int arcCount = 0;	
		
			//Create arcs from S to every activity node and from every activity node to the terminal
			for (int i = 1; i < DataHandler.n; i++) {
				for (int t = 0; t < DataHandler.T; t++) {
					
					String nodeKey=i+"-"+t;
					PulseNode head = PulseGraph.getNodes().get(PulseGraph.getGraphMap().get(nodeKey)); 		
					PulseArc arc = new PulseArc(1,inode,head,arcCount);
					arc.setI(-1); // Start of shift arcs, not corresponding to any resource
					arc.setT(t);
					
					arcCount++;
					
					PulseArc arc2 = new PulseArc(0,head,fnode,arcCount);
					arc2.setI(i); 
					arc2.setT(t);
					
					arcCount++;
								
				}
			}
			
			//Create arcs from every activity node to the same activity or a change of activity node
			for (int i = 1; i < DataHandler.n; i++) {
				for (int t = 0; t < DataHandler.T-1; t++) {
					
					String tailKey=i+"-"+t;
					String headKey=i+"-"+(t+1);
					
					PulseNode head = PulseGraph.getNodes().get(PulseGraph.getGraphMap().get(headKey));
					PulseNode tail = PulseGraph.getNodes().get(PulseGraph.getGraphMap().get(tailKey));
					PulseArc arc = new PulseArc(0,tail,head,arcCount);
					arc.setI(i); 
					arc.setT(t);
					arcCount++;
	
					
					headKey=0+"-"+(t+1);
					head = PulseGraph.getNodes().get(PulseGraph.getGraphMap().get(headKey));
					
					arc = new PulseArc(0,tail,head,arcCount);
					arc.setI(i); 
					arc.setT(t);
					arcCount++;
					
		
					
				}
			}
	
			//Create arcs from every change of activity node
				for (int t = 0; t < DataHandler.T-1; t++) {
	
					String tailKey=0+"-"+t;
					PulseNode tail = PulseGraph.getNodes().get(PulseGraph.getGraphMap().get(tailKey));
					for (int i = 1; i < DataHandler.n; i++) {
						String headKey=i+"-"+(t+1);
						PulseNode head = PulseGraph.getNodes().get(PulseGraph.getGraphMap().get(headKey));
						PulseArc arc = new PulseArc(0,tail,head,arcCount);
						arc.setT(t);
						arc.setI(0);
					}
									
				}
			
	}
		
	/**
	 * This method retrieves the optimal path:
	 * @return
	 */
	public ArrayList<Integer> completeThePath() {
		String[] cad = PulseGraph.best.split(";");
		int rec = Integer.parseInt(cad[0]);
		int b = Integer.parseInt(cad[1]);
		if(b == 1) {
			completeThePathMCF();
		}else if(b == 2) {
			completeThePathMTF(rec);
		}else if(b == 3) {
			completeThePathMCB();
		}else if(b == 4) {
			completeThePathMTB(rec);
		}else {
			return(PulseGraph.getPathJ());
		}
		if(b==3 || b==4) { //If the path was found by the Backward pulse, we reverse the path.
			for (int i = 0; i < PulseGraph.getPathB().size() / 2; i++) {
			     Object temp =PulseGraph.getPathB().get(i);
			     PulseGraph.getPathB().set(i, PulseGraph.getPathB().get(PulseGraph.getPathB().size() - i - 1));
			     PulseGraph.getPathB().set(PulseGraph.getPathB().size() - i - 1, (Integer) temp);
			   }
			return(PulseGraph.getPathB());
		}else {
			return(PulseGraph.getPath());
		}
	}
	
	/**
	 * This method completes the path, if the optimal path was found by using the forward min cost path completion.
	 * 
	 */
	public void completeThePathMCF() {
		boolean termine = false;
		double costoAcumulado = PulseGraph.finalCostF;
		double[] weightAcumulado = PulseGraph.finalResourceF.clone();
		int nodoInicial = PulseGraph.finalNodeF;
		while(termine == false) {
			int nodoActual = PulseGraph.getLastID();
			for(int i = 0; i < PulseGraph.getNodes().get(nodoInicial).getOutgoingArcs().size(); i++) {
				PulseArc arc = PulseGraph.getNodes().get(nodoInicial).getOutgoingArcs().get(i);
				if(arc.getI() >= 0) {
					if(costoAcumulado + arc.geteDist() + arc.getTarget().minCostPathWeights[0] - pi[arc.getI()][arc.getT()] == PulseGraph.PrimalBound) {
						boolean cond = true;
						int r=0;
						while(cond && r<PulseHandler.R) {
							if(arc.getI() == r) {
								if(weightAcumulado[r] + 1 +arc.getTarget().minCostPathWeights[r+1] != PulseGraph.resourceStars[r]) {
	    							cond = false;	
	    						}
							}else {
								if(weightAcumulado[r] +arc.getTarget().minCostPathWeights[r+1] != PulseGraph.resourceStars[r]) {
	    							cond = false;	
	    						}
							}
							
							r+=1;
						}
						if(cond) {
							costoAcumulado+=arc.geteDist()- pi[arc.getI()][arc.getT()];
							for(int rr=0;rr<PulseHandler.R;rr++) {
								if(arc.getI() == rr) {
									weightAcumulado[rr]+=1;
								}else {
									weightAcumulado[rr]+=0;
								}
							}
							nodoActual = arc.getTarget().getId();
						}
					}
					
				}else {
					if(costoAcumulado + arc.geteDist() + arc.getTarget().minCostPathWeights[0] == PulseGraph.PrimalBound) {
						boolean cond = true;
						int r=0;
						while(cond && r<PulseHandler.R) {
							if(arc.getI() == r) {
								if(weightAcumulado[r] + 1 +arc.getTarget().minCostPathWeights[r+1] != PulseGraph.resourceStars[r]) {
	    							cond = false;	
	    						}
							}else {
								if(weightAcumulado[r] +arc.getTarget().minCostPathWeights[r+1] != PulseGraph.resourceStars[r]) {
	    							cond = false;	
	    						}
							}
							
							r+=1;
						}
						if(cond) {
							costoAcumulado+=arc.geteDist();
							for(int rr=0;rr<PulseHandler.R;rr++) {
								if(arc.getI() == rr) {
									weightAcumulado[rr]+=1;
								}else {
									weightAcumulado[rr]+=0;
								}
							}
							nodoActual = arc.getTarget().getId();
						}
					}
					
				}
				
			}
				

			PulseGraph.getPath().add(nodoActual);
			if(nodoActual == PulseGraph.getLastID()) {
				termine = true;
			}else {
				nodoInicial = nodoActual;
			}
		}
	}
	
	/**
	 * This method completes the path, if the optimal path was found by using the backward min time path completion.
	 * 
	 */
	public void completeThePathMTB(int rec) {
		boolean termine = false;
		double costoAcumulado = PulseGraph.finalCostB;
		double[] weightAcumulado = PulseGraph.finalResourceB.clone();
		int nodoInicial = PulseGraph.finalNodeB;
		while(termine == false) {
			int nodoActual = 0;
			for(int i = 0; i < PulseGraph.getNodes().get(nodoInicial).getIncomingArcs().size(); i++) {
				PulseArc arc = PulseGraph.getNodes().get(nodoInicial).getIncomingArcs().get(i);
				if(arc.getI()>=0) {
					if(costoAcumulado + arc.geteDist() + arc.getSource().minResourcePathWeightsB.get(rec)[0] - pi[arc.getI()][arc.getT()] == PulseGraph.PrimalBound) {
						boolean cond = true;
						int r=0;
						while(cond && r<PulseHandler.R) {
							if(arc.getI() == r) {
								if(weightAcumulado[r] + 1 +arc.getSource().minResourcePathWeightsB.get(rec)[r+1] != PulseGraph.resourceStars[r]) {
	    							cond = false;	
	    						}
							}else {
								if(weightAcumulado[r] +arc.getSource().minResourcePathWeightsB.get(rec)[r+1] != PulseGraph.resourceStars[r]) {
	    							cond = false;	
	    						}
							}
							
							r+=1;
						}
						if(cond) {
							costoAcumulado+=arc.geteDist()- pi[arc.getI()][arc.getT()];
							for(int rr=0;rr<PulseHandler.R;rr++) {
								if(arc.getI() == rr) {
									weightAcumulado[rr]+=1;
								}else {
									weightAcumulado[rr]+=0;
								}
							}
							nodoActual = arc.getSource().getId();
						}
					}
				}else {
					if(costoAcumulado + arc.geteDist() + arc.getSource().minResourcePathWeightsB.get(rec)[0] == PulseGraph.PrimalBound) {
						boolean cond = true;
						int r=0;
						while(cond && r<PulseHandler.R) {
							if(arc.getI() == r) {
								if(weightAcumulado[r] + 1 +arc.getSource().minResourcePathWeightsB.get(rec)[r+1] != PulseGraph.resourceStars[r]) {
	    							cond = false;	
	    						}
							}else {
								if(weightAcumulado[r] +arc.getSource().minResourcePathWeightsB.get(rec)[r+1] != PulseGraph.resourceStars[r]) {
	    							cond = false;	
	    						}
							}
							
							r+=1;
						}
						if(cond) {
							costoAcumulado+=arc.geteDist();
							for(int rr=0;rr<PulseHandler.R;rr++) {
								if(arc.getI() == rr) {
									weightAcumulado[rr]+=1;
								}else {
									weightAcumulado[rr]+=0;
								}
							}
							nodoActual = arc.getSource().getId();
						}
					}
				}
				
				
			}

			PulseGraph.getPathB().add(nodoActual);
			if(nodoActual == 0) {
				termine = true;
			}else {
				nodoInicial = nodoActual;
			}
		}
	}
	
	/**
	 * This method completes the path, if the optimal path was found by using the backward min cost path completion.
	 * 
	 */
	public void completeThePathMCB() {
		boolean termine = false;
		double costoAcumulado = PulseGraph.finalCostB;
		double[] weightAcumulado = PulseGraph.finalResourceB.clone();
		int nodoInicial = PulseGraph.finalNodeB;
		while(termine == false) {
			int nodoActual = 0;
			for(int i = 0; i < PulseGraph.getNodes().get(nodoInicial).getIncomingArcs().size(); i++) {
				PulseArc arc = PulseGraph.getNodes().get(nodoInicial).getIncomingArcs().get(i);
				if(arc.getI() >= 0) {
					if(costoAcumulado + arc.geteDist() + arc.getSource().minCostPathWeightsB[0] - pi[arc.getI()][arc.getT()] == PulseGraph.PrimalBound) {
						boolean cond = true;
						int r=0;
						while(cond && r<PulseHandler.R) {
							if(arc.getI() == r) {
								if(weightAcumulado[r] + 1 +arc.getSource().minCostPathWeightsB[r+1] != PulseGraph.resourceStars[r]) {
	    							cond = false;	
	    						}
							}else {
								if(weightAcumulado[r] +arc.getSource().minCostPathWeightsB[r+1] != PulseGraph.resourceStars[r]) {
	    							cond = false;	
	    						}
							}
							
							r+=1;
						}
						if(cond) {
							costoAcumulado+=arc.geteDist()- pi[arc.getI()][arc.getT()];
							for(int rr=0;rr<PulseHandler.R;rr++) {
								if(arc.getI() == rr) {
									weightAcumulado[rr]+=1;
								}else {
									weightAcumulado[rr]+=0;
								}
							}
							nodoActual = arc.getSource().getId();
							
						}
					}
				}else {
					if(costoAcumulado + arc.geteDist() + arc.getSource().minCostPathWeightsB[0]  == PulseGraph.PrimalBound) {
						boolean cond = true;
						int r=0;
						while(cond && r<PulseHandler.R) {
							if(arc.getI() == r) {
								if(weightAcumulado[r] + 1 +arc.getSource().minCostPathWeightsB[r+1] != PulseGraph.resourceStars[r]) {
	    							cond = false;	
	    						}
							}else {
								if(weightAcumulado[r] +arc.getSource().minCostPathWeightsB[r+1] != PulseGraph.resourceStars[r]) {
	    							cond = false;	
	    						}
							}
							
							r+=1;
						}
						if(cond) {
							costoAcumulado+=arc.geteDist();
							for(int rr=0;rr<PulseHandler.R;rr++) {
								if(arc.getI() == rr) {
									weightAcumulado[rr]+=1;
								}else {
									weightAcumulado[rr]+=0;
								}
							}
							nodoActual = arc.getSource().getId();
						}
					}
				}
				
				
			}

			PulseGraph.getPathB().add(nodoActual);
			if(nodoActual == 0) {
				termine = true;
			}else {
				nodoInicial = nodoActual;
			}
		}
	}
	
	/**
	 * This method completes the path, if the optimal path was found by using the forward min time path completion.
	 * 
	 */
	public void completeThePathMTF(int rec) {
		boolean termine = false;
		double costoAcumulado = PulseGraph.finalCostF;
		double[] weightAcumulado = PulseGraph.finalResourceF.clone();
		int nodoInicial = PulseGraph.finalNodeF;
		while(termine == false) {
			int nodoActual = PulseGraph.getLastID();
			for(int i = 0; i < PulseGraph.getNodes().get(nodoInicial).getOutgoingArcs().size(); i++) {
				PulseArc arc = PulseGraph.getNodes().get(nodoInicial).getOutgoingArcs().get(i);
				if(arc.getI()>=0) {
					if(costoAcumulado + arc.geteDist() + arc.getTarget().minResourcePathWeights.get(rec)[0] - pi[arc.getI()][arc.getT()] == PulseGraph.PrimalBound) {
						boolean cond = true;
						int r=0;
						while(cond && r<PulseHandler.R) {
							if(arc.getI() == r) {
								if(weightAcumulado[r] + 1 +arc.getTarget().minResourcePathWeights.get(rec)[r+1] != PulseGraph.resourceStars[r]) {
	    							cond = false;	
	    						}
							}else {
								if(weightAcumulado[r] +arc.getTarget().minResourcePathWeights.get(rec)[r+1] != PulseGraph.resourceStars[r]) {
	    							cond = false;	
	    						}
							}
							
							r+=1;
						}
						if(cond) {
							costoAcumulado+=arc.geteDist()- pi[arc.getI()][arc.getT()];
							for(int rr=0;rr<PulseHandler.R;rr++) {
								if(arc.getI() == rr) {
									weightAcumulado[rr]+=1;
								}else {
									weightAcumulado[rr]+=0;
								}
							}
							nodoActual = arc.getTarget().getId();
						}
					}
				}else {
					if(costoAcumulado + arc.geteDist() + arc.getTarget().minResourcePathWeights.get(rec)[0] == PulseGraph.PrimalBound) {
						boolean cond = true;
						int r=0;
						while(cond && r<PulseHandler.R) {
							if(arc.getI() == r) {
								if(weightAcumulado[r] + 1 +arc.getTarget().minResourcePathWeights.get(rec)[r+1] != PulseGraph.resourceStars[r]) {
	    							cond = false;	
	    						}
							}else {
								if(weightAcumulado[r] +arc.getTarget().minResourcePathWeights.get(rec)[r+1] != PulseGraph.resourceStars[r]) {
	    							cond = false;	
	    						}
							}
							
							r+=1;
						}
						if(cond) {
							costoAcumulado+=arc.geteDist();
							for(int rr=0;rr<PulseHandler.R;rr++) {
								if(arc.getI() == rr) {
									weightAcumulado[rr]+=1;
								}else {
									weightAcumulado[rr]+=0;
								}
							}
							nodoActual = arc.getTarget().getId();
						}
					}
				}
				
			}

			PulseGraph.getPath().add(nodoActual);
			if(nodoActual == PulseGraph.getLastID()) {
				termine = true;
			}else {
				nodoInicial = nodoActual;
			}
		}
	}
		
	/**
	 * This method triggers the pulse propagation in both directions
	 * @throws InterruptedException
	 */
	public static void runPulses() throws InterruptedException {
		Thread tpulse1 = new Thread();
		Thread tpulse2 = new Thread();
		PulseTask task1 = new PulseTask(1,0);
		PulseTask task2 = new PulseTask(2,PulseGraph.getLastID());
		tpulse1 = new Thread(task1);
		tpulse2 = new Thread(task2);
		tpulse1.start();
		tpulse2.start();
		tpulse1.join();
		tpulse2.join();
	}
	
	/**
	 * This method runs the shortest path to find the initial bounds:
	 * @param pi
	 * @throws InterruptedException
	 */
	public static void runShortestPaths(double[][]pi) throws InterruptedException {
		Thread bf1 = new Thread();
		Thread bf2 = new Thread();

		ShortestPathTask task1 = new ShortestPathTask(1,pi);
		ShortestPathTask task2 = new ShortestPathTask(2,pi);

		bf1 = new Thread(task1);
		bf2 = new Thread(task2);
		bf1.start();
		bf2.start();
		bf1.join();
		bf2.join();
	}
	
	/**
	 * This method solves the pricing problem to optimality by calling the pulse algorithm
	 * @return
	 */
	public Column solvePrincingPulse() {
		
		double Atime = System.currentTimeMillis();
		
		//Run the BP
			Column s = new Column();
			try {
				runShortestPaths(pi);
				runPulses();
				s.RC = PulseGraph.PrimalBound;
				ArrayList<Integer>pathFinal = completeThePath();
				System.out.println("PATH REDUCED COST: "+PulseGraph.PrimalBound);
				System.out.println("OPT PATH: "+pathFinal.toString());
				parseOptPathPulse(pathFinal,s);
				
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			double runtime = (System.currentTimeMillis()-Atime)/1000.0;
			if(runtime>maxTime) {maxTime = runtime;}
			if(runtime<minTime) {minTime = runtime;}
			avgTime+=runtime;
			pricingSolved++;
			
			//Reset the SP labels for every node
			
			for(Iterator<PulseNode> iterator = PulseGraph.getNodes().iterator();iterator.hasNext();) {
				iterator.next().resetSPLabels();
			}
	
			PulseGraph.resetInfo();
			
			
		
		return(s);
	}
	
	
	/**
	 * Translates the path we found using the pulse to a column
	 * @param path
	 * @param s
	 */
	private void parseOptPathPulse(ArrayList<Integer> path, Column s) {
		
		path.remove(0);
		path.remove(path.size()-1);
		for(Iterator<Integer> iter=path.iterator();iter.hasNext();) {
			int nodo = iter.next();
			int activity = PulseGraph.getNodes().get(nodo).getI();
			int time = PulseGraph.getNodes().get(nodo).getT();
			s.s[activity][time] = 1;
			
		}

	}
	
}
