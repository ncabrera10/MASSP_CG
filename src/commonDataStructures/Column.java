package commonDataStructures;

/**
 * This class defines a column of the CG procedure.
 * 
 * @author nicolas.cabrera-malik
 *
 */
public class Column {

	/**
	 * Matrix that contains the schedule of the column
	 */
	int[][] s;
	
	/**
	 * Matrix that contains the reduced cost:
	 * 
	 */
	double RC;  // Reduced cost
	
	/**
	 * This method creates a new column:
	 */
	public Column(){
		s = new int[DataHandler.n][DataHandler.T];
		RC = 0; 
	}

	/**
	 * This method prints information of the column:
	 */
	public void print() {
		System.out.println("COLUMN SCHEDULE");
		for (int i = 0; i < DataHandler.n; i++) {
			for (int t = 0; t < DataHandler.T; t++) {
				System.out.print(s[i][t]+" ");
			}
			System.out.println();
		}
		System.out.println("REDUCED COST "+RC);
	}



}
