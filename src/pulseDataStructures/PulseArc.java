package pulseDataStructures;

/**
 * Thiss class represents a pulse arc:
 * 
 * @author nicolas.cabrera-malik
 *
 */
public class PulseArc {

	/**
	 * The arc distance
	 */
	private double eDist;

	/**
	 * The arc id
	 */
	private int id;
	/**
	 * The source node
	 */
	private PulseNode source;
	/**
	 * The target node
	 */
	private PulseNode target;
	
	/**
	 * Time period
	 */
	int t; 
	
	/**
	 * Activity
	 */
	int i; 
	
	/**
	 * This method creates an arc
	 * @param d
	 * @param weights
	 * @param nT
	 * @param nH
	 * @param nid
	 */
	public PulseArc(double d, PulseNode nT, PulseNode nH, int nid) {
		eDist = d;
		this.source = nT;
		this.target = nH;
		this.id = nid;
		nT.magicIndex.add(nid);
		nT.getOutgoingArcs().add(this);
		nH.magicIndex2.add(nid);
		nH.getIncomingArcs().add(this);
	}

	/**
	 * @return the eDist
	 */
	public double geteDist() {
		return eDist;
	}

	/**
	 * @param eDist the eDist to set
	 */
	public void seteDist(double eDist) {
		this.eDist = eDist;
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
	 * @return the source
	 */
	public PulseNode getSource() {
		return source;
	}

	/**
	 * @param source the source to set
	 */
	public void setSource(PulseNode source) {
		this.source = source;
	}

	/**
	 * @return the target
	 */
	public PulseNode getTarget() {
		return target;
	}

	/**
	 * @param target the target to set
	 */
	public void setTarget(PulseNode target) {
		this.target = target;
	}
	
	/**
	 * This method is the criteria to sort arcs
	 * @return the minimum distance to reach the end node
	 */
	public double getCompareCriteriaF() {
		return target.minCostPathWeights[0] ;
	}
	
	/**
	 * This method is the criteria to sort arcs
	 * @return the minimum distance to reach the end node
	 */
	public double getCompareCriteriaB() {
		return target.minCostPathWeightsB[0] ;
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
	
	
	
}
