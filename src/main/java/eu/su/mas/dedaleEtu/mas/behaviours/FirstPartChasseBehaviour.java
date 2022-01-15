package eu.su.mas.dedaleEtu.mas.behaviours;

import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.agents.dummies.explo.ExploreMultiAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import dataStructures.tuple.Couple;
import eu.su.mas.dedale.env.Observation;

public class FirstPartChasseBehaviour extends OneShotBehaviour {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8120296833499796359L;

	private MapRepresentation myMap;
	
	private int end;
	
	public FirstPartChasseBehaviour(final Agent agent) {
		super(agent);
		this.myMap = ((ExploreMultiAgent) this.myAgent).getMyMap();
	}
	
	@Override
	public void action() {
		//0) Retrieve the current position
		String myPosition=((AbstractDedaleAgent)this.myAgent).getCurrentPosition();
		
		end = 0;
		
		if (myPosition!=null){
			//List of observable from the agent's current position
			List<Couple<String,List<Couple<Observation,Integer>>>> lobs=((AbstractDedaleAgent)this.myAgent).observe();//myPosition
					
			/**
			 * Just added here to let you see what the agent is doing, otherwise he will be too quick
			 */
			try {
				this.myAgent.doWait(500);
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			List<String> nodesStench = new ArrayList<String>();
			List<String> obsNodes = new ArrayList<String>();
			
			Iterator<Couple<String, List<Couple<Observation, Integer>>>> iter=lobs.iterator();
			while(iter.hasNext()){
				Couple<String, List<Couple<Observation, Integer>>> obs_node = iter.next();
				obsNodes.add(obs_node.getLeft());
				if (containsStench(obs_node.getRight())){
					nodesStench.add(obs_node.getLeft());
					end = 1;
				}
			}
			
			// si l'agent est à la position où il pensait que le golem serait, alors le golem n'y est pas
			if (((ExploreMultiAgent)this.myAgent).getPositionGolem() != null) {
				if (((ExploreMultiAgent)this.myAgent).getPositionGolem().equals(myPosition)) {
					((ExploreMultiAgent)this.myAgent).setPositionGolem(null);
					((ExploreMultiAgent)this.myAgent).reinitializeCompteur();
					((ExploreMultiAgent)this.myAgent).reinitializeFinish();
				}
			}
			
			if (((ExploreMultiAgent)this.myAgent).getGivenPosGolem() != null) {
				if (((ExploreMultiAgent)this.myAgent).getGivenPosGolem().getRight().equals(myPosition)) {
					((ExploreMultiAgent)this.myAgent).setGivenPosGolem(null);
				}
				
				else if (obsNodes.contains(((ExploreMultiAgent)this.myAgent).getGivenPosGolem().getRight())) {
					if (nodesStench.isEmpty()) {
						((ExploreMultiAgent)this.myAgent).setGivenPosGolem(null);
					}
					else {
						if (!nodesStench.contains(((ExploreMultiAgent)this.myAgent).getGivenPosGolem().getRight())) {
							((ExploreMultiAgent)this.myAgent).setGivenPosGolem(null);
						}
					}
				}
				
				if (((ExploreMultiAgent)this.myAgent).getGivenPosGolem() != null) {
					if (!((ExploreMultiAgent)this.myAgent).getOthNodesStench().isEmpty()) {
						for (Couple<Couple<String,String>, List<String>> c : ((ExploreMultiAgent)this.myAgent).getOthNodesStench()) {
							if (c.getLeft().getLeft().equals(((ExploreMultiAgent)this.myAgent).getGivenPosGolem().getRight())) {
								((ExploreMultiAgent)this.myAgent).setGivenPosGolem(null);
								break;
							}
						}
					}
				}
			}
			
			// si l'agent n'est pas à la position vers laquelle il se dirigeait à l'itération précédente
			if (((ExploreMultiAgent)this.myAgent).getNextNode()!=null && !((ExploreMultiAgent)this.myAgent).getNextNode().equals(myPosition)) {
				if (!nodesStench.isEmpty()) {
					if (nodesStench.contains(((ExploreMultiAgent)this.myAgent).getNextNode())) {	
						if (!((ExploreMultiAgent)this.myAgent).getOthNodesStench().isEmpty()) {
							boolean same = false;
							
							// vérifier si on n'a pas pu atteindre la position parce qu'un autre agent y était
							for (Couple<Couple<String,String>, List<String>> c : ((ExploreMultiAgent)this.myAgent).getOthNodesStench()) {
								if (c.getLeft().getLeft().equals(((ExploreMultiAgent)this.myAgent).getNextNode()))
									same = true;
							}
							
							// sinon, un golem y est
							if (!same) {
								((ExploreMultiAgent)this.myAgent).setPositionGolem(((ExploreMultiAgent)this.myAgent).getNextNode());
								((ExploreMultiAgent)this.myAgent).setGivenPosGolem(null);
							}
						}
						
						else {
							((ExploreMultiAgent)this.myAgent).setPositionGolem(((ExploreMultiAgent)this.myAgent).getNextNode());
							((ExploreMultiAgent)this.myAgent).setGivenPosGolem(null);
						}
					}
				}
			}
			
			if (((ExploreMultiAgent)this.myAgent).getCompteur() >= 50) {
				if (this.myMap.sendNodeEdges(((ExploreMultiAgent)this.myAgent).getPositionGolem()).getLeft() == 1) {
					end = 2;
					System.out.println(this.myAgent.getLocalName() + " -- I think I blocked a golem ! I'm so smart !");
				}
				else {
					if (didIFinish(myPosition)) {
						end = 2;
						System.out.println(this.myAgent.getLocalName() + " -- I think I blocked a golem with my friends ! We're so smart !");
					}
				}
			}
			
			((ExploreMultiAgent)this.myAgent).setNodesStench(nodesStench);
			((ExploreMultiAgent)this.myAgent).setObsNodes(obsNodes);
			
			List<Couple<Couple<String,String>, List<String>>> n = new ArrayList<Couple<Couple<String,String>, List<String>>>();
			((ExploreMultiAgent)this.myAgent).setOthNodesStench(n);
		}
	}
	
	public boolean didIFinish(String myPosition) {
		boolean f = true;
		
		List<String> temp = new ArrayList<String>();
		
		temp.add(myPosition);
		
		for (Couple<String,String> c : ((ExploreMultiAgent)this.myAgent).getFinish()) {
			temp.add(c.getRight());
		}
		
		Couple<Integer, List<String>> c = this.myMap.sendNodeEdges(((ExploreMultiAgent)this.myAgent).getPositionGolem());
		
		for (String s : c.getRight()) {
			if (!temp.contains(s))
				f = false;
		}
		
		return f;
	}

	public boolean containsStench(List<Couple<Observation, Integer>> L){
        for (Couple<Observation, Integer> c:L){
            if(c.getLeft().getName().equals("Stench"))
                return true;
        }
        return false;
    }
	
	public int onEnd() {
		return end;
	}
}
