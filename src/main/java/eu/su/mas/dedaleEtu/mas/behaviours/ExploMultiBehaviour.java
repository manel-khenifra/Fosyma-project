package eu.su.mas.dedaleEtu.mas.behaviours;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import dataStructures.serializableGraph.SerializableSimpleGraph;
import dataStructures.tuple.Couple;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.agents.dummies.ExploreSoloAgent;
import eu.su.mas.dedaleEtu.mas.agents.dummies.explo.ExploreMultiAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation.MapAttribute;
import jade.core.AID;
import jade.core.behaviours.FSMBehaviour;
import jade.core.behaviours.ParallelBehaviour;
import jade.core.behaviours.SimpleBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import javafx.util.Pair;


/**
 * This behaviour allows an agent to explore the environment and learn the associated topological map.
 * The algorithm is a pseudo - DFS computationally consuming because its not optimised at all.</br>
 * 
 * When all the nodes around him are visited, the agent randomly select an open node and go there to restart its dfs.</br> 
 * This (non optimal) behaviour is done until all nodes are explored. </br> 
 * 
 * Warning, this behaviour does not save the content of visited nodes, only the topology.</br> 
 * Warning, this behaviour is a solo exploration and does not take into account the presence of other agents (or well) and indefinitely tries to reach its target node
 * @author hc
 *
 */
public class ExploMultiBehaviour extends OneShotBehaviour {

	private static final long serialVersionUID = -7430143841802500736L;

	/**
	 * Current knowledge of the agent regarding the environment
	 */
	private MapRepresentation myMap;
	
	private int end;
	

	public ExploMultiBehaviour(final AbstractDedaleAgent myagent, MapRepresentation myMap) {
		super(myagent);
		this.myMap=myMap;
	}
	
	@Override
	public void action() {

		if (this.myMap==null)
			this.myMap= new MapRepresentation();
		
		if (((ExploreMultiAgent)this.myAgent).getMyMap() != null)
			this.myMap = ((ExploreMultiAgent)this.myAgent).getMyMap();
		
		List<Couple<String,String>> otherAgentsPos = ((ExploreMultiAgent)this.myAgent).getOtherAgentsPos();
		
		//0) Retrieve the current position
		String myPosition=((AbstractDedaleAgent)this.myAgent).getCurrentPosition();
	
		if (myPosition!=null){
			/**
			 * Just added here to let you see what the agent is doing, otherwise he will be too quick
			 */
			try {
				this.myAgent.doWait(1000);
			} catch (Exception e) {
				e.printStackTrace();
			}	
			
			String nextNode = ((ExploreMultiAgent)this.myAgent).getNextNode();
			
			//3) while openNodes is not empty, continues.
			if (!this.myMap.hasOpenNode()){
				//Explo finished
				System.out.println(this.myAgent.getLocalName()+" - Exploration successufully done, behaviour removed.");
				this.end = 1;
			}else{
				this.end = 0;
				//4) select next move.
				//4.1 If there exist one open node directly reachable, go for it,
				//	 otherwise choose one from the openNode list, compute the shortestPath and go for it
				if (nextNode==null){
					//no directly accessible openNode
					//chose one, compute the path and take the first step.
					List<Couple<String,Integer>> nodeUs = this.myMap.getDistanceOpenNodes(myPosition);
					if (otherAgentsPos.isEmpty() || nodeUs.size()==1) {
						nextNode=this.myMap.getShortestPathToClosestOpenNode(myPosition).get(0);
						//System.out.println(this.myAgent.getLocalName() + " will go to " + nextNode + " destination " + nodeUs.get(0).getLeft());
				/*		if (!((ExploreMultiAgent)this.myAgent).getNoGolem().getLeft()) {
							if (nodeUs.get(0).getLeft().equals(((ExploreMultiAgent)this.myAgent).getNoGolem().getRight())) {
								this.myMap.addNode(nodeUs.get(0).getLeft(), MapAttribute.closed);
								this.end = 1;
							}
						}
				*/
					}
					else {
						if (!((ExploreMultiAgent)this.myAgent).getNoGolem().getLeft()) {
							Couple<String,Integer> remove = null;
							for (Couple<String,Integer> c : nodeUs) {
								if (c.getLeft().equals(((ExploreMultiAgent)this.myAgent).getNoGolem().getRight())) {
									remove = c;
								}
							}
							nodeUs.remove(remove);
						}
						int i = 0;
						while (nextNode == null) {
							int j = 0;
							//we compare our path with the other agents
							for (Couple<String,String> o : otherAgentsPos) {
								List<Couple<String,Integer>> nodeThem = this.myMap.getDistanceOpenNodes(o.getRight());
								if (nodeUs.get(i).getLeft().compareTo(nodeThem.get(0).getLeft())==0) {
									if (nodeUs.get(i).getRight() > nodeThem.get(0).getRight()) {
										i++;
										break;
									}
								}
								j++;
							
							}
							if (j == otherAgentsPos.size()) {
								nextNode=this.myMap.getShortestPath(myPosition,nodeUs.get(i).getLeft()).get(0);//getShortestPath(myPosition,this.openNodes.get(0)).get(0);
								//System.out.println(this.myAgent.getLocalName() + " will go to " + nextNode + " destination " + nodeUs.get(i).getLeft());
							}
							else if (i == nodeUs.size()) {
								if (nodeUs.size() > otherAgentsPos.size()) {
									nextNode=this.myMap.getShortestPath(myPosition,nodeUs.get(i-1).getLeft()).get(0);
									//System.out.println(this.myAgent.getLocalName() + " will go to " + nextNode + " destination " + nodeUs.get(i-1).getLeft());
								}
								else {
									nextNode=this.myMap.getShortestPathToClosestOpenNode(myPosition).get(0);
									//System.out.println(this.myAgent.getLocalName() + " will go to " + nextNode + " destination " + nodeUs.get(0).getLeft());
								}
							}
						}
					}
				}
				
	
				
				((ExploreMultiAgent)this.myAgent).setMyMap(this.myMap);
				((ExploreMultiAgent)this.myAgent).setNextNode(nextNode);
				((ExploreMultiAgent)this.myAgent).setLastPosition(myPosition);
				
				/************************************************
				 * 				END API CALL ILUSTRATION
				 *************************************************/
				((AbstractDedaleAgent)this.myAgent).moveTo(nextNode);
			}

		}
	}

	@Override
	public int onEnd() {
		return this.end;
	}

}
