
package main;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import commonDataStructures.AlgorithmHandler;
import commonDataStructures.DataHandler;
import ilog.concert.IloException;

/**
 * This is the main class to run the column generation procedure.
 * 
 * The user can select the algorithm, the number of activities, number of time periods, among others.
 * 
 * @author nicolas.cabrera-malik
 *
 */
public class Main_MASP {

	/**
	 * This method runs the column generation procedure
	 * @param args 0: Algorithm 1: Number of activities 2: Seed 3: Number of time periods 4: Maximum schedule length
	 * @throws IOException
	 * @throws InterruptedException
	 * @throws IloException
	 */
	public static void main(String[] args) throws IOException, InterruptedException, IloException {
		
		// Recover the information selected by the user:
		
		int al = Integer.parseInt(args[0]); // Algorithm: 1 - labeling, 2 - pulse
		int n = Integer.parseInt(args[1]); // Number of activities
		int seed = Integer.parseInt(args[2]); //Seed used to generate the instance
		int T = Integer.parseInt(args[3]); // Number of time periods
		int L = Integer.parseInt(args[4]); //Maximum schedule length

		
		// Decides in which folder to store the results:
		
		String algor = "";
		if(al == 1) {
			algor = "Labeling/CG LABEL - "+n+" - "+seed;
		}
		if(al == 2) {
			algor = "Pulse/CG Pulse - "+n+" - "+seed;
		}
		
		// Starts a txt file to print the results:
		
		PrintWriter pw = new PrintWriter( new File("./results/"+algor+".txt"));
		
		//Generate data handler (with empty values)
					
		new DataHandler(n,T,L);
				
		//Generates an instance, defining the specific limits
				
		DataHandler.genInstance(seed);
				
		//Prints relevant info from the instance
					
		DataHandler.printInstance();
					
		// Begin the time count		
					
		double Atime = System.currentTimeMillis();
					
		// Create an AlgorithmHandler
					
		AlgorithmHandler alg = new AlgorithmHandler();

		//Runs the selected algorithm
						
		if(al == 1) {
			alg.runCGLABEL();
		}
		if(al == 2) {
			alg.runCGPulse();
		}
		
		// Prints the key results:
		
		pw.println(algor+" "+DataHandler.T+" "+DataHandler.L+" "+seed+" "+(System.currentTimeMillis()-Atime)/1000+" "+alg.LR+" "+alg.pricingSolved+" "+alg.maxTime+" "+alg.avgTime+" "+alg.minTime);
		System.out.println(algor+" "+DataHandler.T+" "+DataHandler.L+" "+seed+" "+(System.currentTimeMillis()-Atime)/1000+" "+alg.LR+" "+alg.pricingSolved+" "+alg.maxTime+" "+alg.avgTime+" "+alg.minTime);
		
		// Close the print writer:
		
		pw.close();
		
		
		//Remove all columns
					
		alg.omegaHat.clear();
					
		//Runs the garbage collector:
		
		System.gc();


	}
}
