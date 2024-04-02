package pulseDataStructures;

import java.util.ArrayList;


/**
 * This class contains methods to find and sort pending pulses.
 * 
 * @author nicolas.cabrera-malik
 *
 */
public class PulseHandler {

	/**
	 * Pulse queue for pulses on the F direction
	 */
	public static ArrayList<PendingPulse> pendingQueueF;
	
	/**
	 * Pulse queue for pulses on the B direction
	 */
	public static ArrayList<PendingPulse> pendingQueueB;
	
	/**
	 * Number of resources
	 */
	public static int R;
	

	/**
	 * Read data from an instance
	 * @param Instance
	 */
	public PulseHandler(int numRec) {
		R = numRec;
		pendingQueueF = new ArrayList<PendingPulse>();
		pendingQueueB = new ArrayList<PendingPulse>();
	}
	
	public void addPendingPulseF(PendingPulse p) {
		pendingQueueF.add(p);
	}
	
	public void addPendingPulseB(PendingPulse p) {
		pendingQueueB.add(p);
	}
	
	public static int binarySearch(PendingPulse p, ArrayList<PendingPulse> labels) {
		double cScore = p.getSortCriteria();
		boolean cond = true;
		int l = 0; //left
		int r = labels.size()-1; //right
		int m = (int) ((l + r) / 2); //half
		double mVal = 0;
		if(labels.size() == 1){
			return 0;
		}else{
			mVal = labels.get(m).getSortCriteria();
		}
		while (cond) {
			if (r - l > 1) {
				if (cScore > mVal) {
					r = m;
					m = (int) ((l + r) / 2);
				} else if (cScore < mVal) {
					l = m;
					m = (int) ((l + r) / 2);
				} else if (p.getNodeID()>labels.get(m).getNodeID()){
					r = m;
					m = (int) ((l + r) / 2);
				} else if (p.getNodeID()<labels.get(m).getNodeID()){
					l = m;
					m = (int) ((l + r) / 2);
				} else {
					boolean cond2 = true;
					int rec = 0;
					while(cond2 && rec < PulseGraph.R){
						if(p.getResource(rec) > labels.get(m).getResource(rec)) {
							r = m;
							m = (int) ((l + r) / 2);
							cond2 = false;
						}else if(p.getResource(rec) < labels.get(m).getResource(rec)) {
							l = m;
							m = (int) ((l + r) / 2);
							cond2 = false;
						}
						rec++;
					}
					if(cond2) {
						return m;
					}
				} mVal = labels.get(m).getSortCriteria();
			} else {
				cond = false;
				if (p.equals(labels.get(r))){
					return r;
				}else if (p.equals(labels.get(l))){
					return l;
				}

			}
		}
		return -1;

	}
	
	public static void addPendingPulse_DOrder(PendingPulse p, ArrayList<PendingPulse>labels){

		double cScore = p.getSortCriteria();
		boolean cond = true;
		int l = 0; //left
		int r = labels.size(); //Right
		int m = (int) ((l + r) / 2); //Half
		double mVal = 0;
		if(labels.size() == 0) {
			labels.add(p);
			return;
		}
		else if(labels.size()  == 1) {
			mVal = labels.get(m).getSortCriteria();
			if(cScore == mVal) {
				if(p.getNodeID() == labels.get(m).getNodeID()) {
					labels.add(p.getResource(1)>labels.get(m).getResource(1)?0:1,p);
				}
				else {
					labels.add(p.getNodeID()>labels.get(m).getNodeID()?0:1,p);
				}
			}else {
				labels.add(cScore>mVal?0:1,p);
				return;
			}
		}
		else {
			mVal = labels.get(m).getSortCriteria();
		}
		while(cond) {
			if (r - l > 1) {
				if (cScore > mVal) {
					r = m;
					m = (int) ((l + r) / 2);
				} else if (cScore < mVal) {
					l = m;
					m = (int) ((l + r) / 2);
				} else if (p.getNodeID()>labels.get(m).getNodeID()){
					r = m;
					m = (int) ((l + r) / 2);
				} else if (p.getNodeID()<labels.get(m).getNodeID()){
					l = m;
					m = (int) ((l + r) / 2);
				}  else {
					boolean cond2 = true;
					int rec = 0;
					while(cond2 && rec < PulseGraph.R){
						if(p.getResource(rec) > labels.get(m).getResource(rec)) {
							r = m;
							m = (int) ((l + r) / 2);
							cond2 = false;
						}else if(p.getResource(rec) < labels.get(m).getResource(rec)) {
							l = m;
							m = (int) ((l + r) / 2);
							cond2 = false;
						}
						rec++;
					}
					if(cond2){
						labels.add(m, p);
						return;
					}
				}
			
				mVal = labels.get(m).getSortCriteria();
			} else {
				cond = false;
				if(l == m ){
					if (cScore == mVal){
						if(p.getNodeID()==labels.get(m).getNodeID()){
							boolean cond3 = true;
							int recA = 0;
							while(cond3 && recA<PulseHandler.R) {
								if(p.getResource(recA) > labels.get(m).getResource(recA)) {
									labels.add(l,p);
									cond3 = false;
								}
								if(p.getResource(recA) < labels.get(m).getResource(recA)) {
									labels.add(l+1,p);
									cond3 = false;
								}
								recA++;
							}
						}else{
							labels.add(p.getNodeID()>labels.get(m).getNodeID()?l:l+1,p);
						}						
					}else{
						labels.add(cScore>mVal?l:l+1,p);
					}
				}else if (r == m){
					if (cScore == mVal){
						if(p.getNodeID()==labels.get(m).getNodeID()){
							boolean cond3 = true;
							int recA = 0;
							while(cond3 && recA<PulseHandler.R) {
								if(p.getResource(recA) > labels.get(m).getResource(recA)) {
									labels.add(r,p);
									cond3 = false;
								}
								if(p.getResource(recA) < labels.get(m).getResource(recA)) {
									labels.add(Math.min(r+1, labels.size()),p);
									cond3 = false;
								}
								recA++;
							}
						}else{
							labels.add(p.getNodeID()>labels.get(m).getNodeID()?r:Math.min(r+1, labels.size()),p);
						}
					}else{
						labels.add(cScore>mVal?r:Math.min(r+1, labels.size()),p);
					}
				}else
				{
					System.err.println("LABEL, addLabel ");
				}
				return;
			}
			
			
		}
		
	}
	
	public static int normalSearch(PendingPulse p, ArrayList<PendingPulse> labels) {
		int rta = -1;
		for(int i = 0;i < labels.size(); i++) {
			PendingPulse current = labels.get(i);
			if(current.equals(p)) {
				return i;
			}
		}
		
		return rta;
	}
	
}
