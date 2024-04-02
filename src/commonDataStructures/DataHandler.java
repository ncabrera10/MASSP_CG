package commonDataStructures;

import java.util.Random;

/**
 * This class holds all the data.
 *  
 * @author nicolas.cabrera-malik
 *
 */
public class DataHandler {

	/**
	 * Number of activities by convention activity 0 represents idle/switching time
	 */
	
	public static int n;
	
	/**
	 * Activity upper time limits vector 
	 */
	
	public static int[] u;
	
	/**
	 * Demand for each activity and time period
	 */
	
	public static int[][] d;
	
	/**
	 * Time horizon
	 */
	
	public static int T;
	
	/**
	 * Maximum schedule length
	 */
	
	public static int L;
	
	/**
	 * This is the constructor method for a data handler
	 * @param numA
	 * @param numT
	 * @param l
	 */
	public DataHandler(int numA, int numT, int l) {
		n = numA+1;
		T = numT;
		u = new int[n];
		d = new int[n][T];
		L = l;
	}

	/**
	 * This method generates a new instance using as input the 
	 * seed selected by the user
	 * 
	 * @param seed
	 */
	public static void genInstance(int seed) {
		
		Random r = new Random(seed);
		
		//Generate upper limits 
		for (int i = 0; i < n; i++) {
			u[i]=2+r.nextInt(4);
		}
		//Generate demands. Demand for activity 0 is always 0
		for (int i = 1; i < n; i++) {
			for (int t = 0; t < T; t++) {
				d[i][t]=10+r.nextInt(11);

			}
		}
		

	
	}

	/**
	 * This method prints the information of the current instance, including:
	 * 
	 * -Number of activities
	 * -Number of time periods
	 * -Staff requirements for each activity
	 */
	public static void printInstance() {

		System.out.println("Number of Activities: "+(n-1));
		System.out.println("T: "+T);
		System.out.println("L: "+L);
		System.out.print("u: ");
		for (int j = 0; j < n; j++) {
			System.out.print(u[j]+" ");
		}
		System.out.println();
		System.out.print("demand: ");
		for (int i = 0; i < n; i++) {
			for (int t = 0; t < T; t++) {
				System.out.print(d[i][t]+" ");
			}
			System.out.println();
		}
		System.out.println();
		
	
	}
	
}

