package commonDataStructures;

/**
 * This class defines an arc in the auxiliary graph
 * 
 * @author nicolas.cabrera-malik
 *
 */
public class Arc {

	/**
	 * The arc's tail id
	 */
	int tail;
	
	/**
	 * The arc's head id
	 */
	int head;
	
	/**
	 * Time period of the arc
	 */
	int t;
	
	/**
	 * The activity of the arc
	 */
	int i;
	
	/**
	 * Cost of using the arc
	 */
	double cost;
	
	/**
	 * The arc's key
	 */
	String key;
	
	/**
	 * This method creates a new empty arc
	 */
	public Arc(){
		key="";
		cost = 0;
	}

	/**
	 * This method recovers the key of the arc
	 * @param bdd
	 * @return
	 */
	public String getKey(Graph bdd) {
		key = bdd.nodes.get(tail).key+" -> "+bdd.nodes.get(head).key;
		return key;
	}
	
	/**
	 * This method prints information of the arc
	 */
	public void print() {
		
		System.out.println(key);
				
	}
	
	
}
