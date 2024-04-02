package threads;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import pulseDataStructures.PendingPulse;
import pulseDataStructures.PulseGraph;
import pulseDataStructures.PulseHandler;

/**
 * This class runs a pulse task. It triggers the pulse propagation in either the F or the B direction.
 * 
 * @author nicolas.cabrera-malik
 *
 */
public class PulseTask implements Runnable{

	
	/**
	 * The pulse type
	 */
	private int type;
	
	/**
	 * This a flag to stop the task
	 */
	private AtomicBoolean running = new AtomicBoolean(false);
	
	private int source;

	/**
	 * 
	 * @param ty
	 * @param graph
	 * @param ths
	 * @param s
	 * @param t
	 */
	public PulseTask(int ty, int s) {
		type = ty;
		source = s;
	}
	
	@Override
	public void run() {
		running.set(true);
        while (running.get()) {
        	if(type == 1){

    			//Initial pulse weights:
    			
    			double[] pulseWeights = new double[PulseGraph.R+1];
    			for(int i=0;i<=PulseGraph.R;i++) {
    				pulseWeights[i] = 0;
    			}
        		//Create the pulse handler
    			
    			new PulseHandler(PulseGraph.R);
    			
    		//Sends the initial pulse:
        		
    			ArrayList<Integer> path = new ArrayList<Integer>();
    			PulseGraph.getNodes().get(source).pulseFWithQueues(pulseWeights, 0, path);
    			
    			
    		//When the first pulse is stopped the queue is full:
    				
    			int pendingPulses = PulseHandler.pendingQueueF.size();
    		
    		//While the queue has at least one element, the search must go on!
    			
    			
        		while(pendingPulses > 0) {
        			
        			//Recovers the last pulse (and removes it):
        				
        				PendingPulse p = PulseHandler.pendingQueueF.remove(pendingPulses-1);
        				p.setNotTreated(false);
        				
        			//The pendingPulse weights:
        				
        				pulseWeights[0] = p.getDist();
            			for(int i=1;i<=PulseHandler.R;i++) {
            				pulseWeights[i] = p.getResource(i);
            			}
        			
        				
        			 //Begins the search:
        				if(PulseGraph.getNodes().get(p.getNodeID()).minCostPathWeights[0] + pulseWeights[0] < PulseGraph.PrimalBound) {
        					PulseGraph.getNodes().get(p.getNodeID()).pulseFWithQueues(pulseWeights, 0, p.getPartialPath());
        				}
        			//Updates the global queue size (How many are left)
            		
        				pendingPulses = PulseHandler.pendingQueueF.size();
        		}
        		
        		PulseGraph.termine = true;
        		PulseGraph.setFirst(1);
        		this.interrupt();
        	}
        	else {
        		//Initial pulse weights:
    			
    			double[] pulseWeights = new double[PulseGraph.R+1];
    			for(int i=0;i<=PulseGraph.R;i++) {
    				pulseWeights[i] = 0;
    			}
    			
        		//Create the pulse handler
    			
    			new PulseHandler(PulseGraph.R);
    			
    		//Sends the initial pulse:
        		
    			ArrayList<Integer> path = new ArrayList<Integer>();
    			PulseGraph.getNodes().get(source).pulseBWithQueues(pulseWeights, 0, path);
    			
    			
    		//When the first pulse is stopped the queue is full:
    				
    			int pendingPulses = PulseHandler.pendingQueueB.size();
    		
    		//While the queue has at least one element, the search must go on!
    			
    			
        		while(pendingPulses > 0) {
        		
        			//Recovers the last pulse (and removes it):
        				
        				PendingPulse p = PulseHandler.pendingQueueB.remove(pendingPulses-1);
        				p.setNotTreated(false);
        				
        			//The pendingPulse weights:
        				
        				pulseWeights[0] = p.getDist();
            			for(int i=1;i<=PulseHandler.R;i++) {
            				pulseWeights[i] = p.getResource(i);
            			}
        			
        				
        			 //Begins the search:
        				if(PulseGraph.getNodes().get(p.getNodeID()).minCostPathWeightsB[0] + pulseWeights[0] < PulseGraph.PrimalBound) {
        					PulseGraph.getNodes().get(p.getNodeID()).pulseBWithQueues(pulseWeights, 0, p.getPartialPath());
        				}
        			//Updates the global queue size (How many are left)
            		
        				pendingPulses = PulseHandler.pendingQueueB.size();
        		}
        		
        		PulseGraph.termine = true;
        		PulseGraph.setFirst(2);
        		this.interrupt();
        	}
        }
	}
	
	/**
	 * This method interrupts a thread
	 */
	public void interrupt() {
        running.set(false);
    }

	/**
	 * This method checks if the thread is active
	 * @return true if the thread is active
	 */
    boolean isRunning() {
        return running.get();
    }
}
