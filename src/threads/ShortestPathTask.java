package threads;

import pulseDataStructures.BellmanFord;

/**
 * This class runs a SP in either the F or the B direction.
 * 
 * @author nicolas.cabrera-malik
 *
 */
public class ShortestPathTask implements Runnable{ 
	/**
	 * The SP type
	 */
	private int type;
	
	/**
	 * Matrix that contains the values of the dual variables
	 */
	private double[][]pi;
	
	/**
	 * This method creates a new shortest path task
	 * @param ty
	 * @param p
	 */
	public ShortestPathTask(int ty,double[][]p){
		type = ty;
		pi = p;
	}
		


	@Override
	public void run() {
		if(type == 1) {
			new BellmanFord(1,pi);
		}else {
			new BellmanFord(2,pi);
		}
	}
}
