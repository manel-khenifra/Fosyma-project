package eu.su.mas.dedaleEtu.mas.behaviours;

import java.util.Iterator;
import java.util.List;
import dataStructures.tuple.Couple;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.agents.dummies.explo.ExploreMultiAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation.MapAttribute;
import jade.core.behaviours.OneShotBehaviour;


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
public class FirstPartExploBehaviour extends OneShotBehaviour {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5541214527651063524L;


	/**
	 * Current knowledge of the agent regarding the environment
	 */
	private MapRepresentation myMap;
	

	public FirstPartExploBehaviour(final AbstractDedaleAgent myagent, MapRepresentation myMap) {
		super(myagent);
		this.myMap=myMap;
	}
	
	@Override
	public void action() {

		if (this.myMap==null) {
			this.myMap= new MapRepresentation();
			((ExploreMultiAgent)this.myAgent).initializeOtherAgentsPos();
		}
		
		if (((ExploreMultiAgent)this.myAgent).getMyMap() != null) {
			this.myMap = ((ExploreMultiAgent)this.myAgent).getMyMap();
		}
		
		//0) Retrieve the current position
		String myPosition=((AbstractDedaleAgent)this.myAgent).getCurrentPosition();
	
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

			//1) remove the current node from openlist and add it to closedNodes.

			this.myMap.addNode(myPosition,MapAttribute.closed);
			boolean noGolem = true;
			
			String nextNode = ((ExploreMultiAgent)this.myAgent).getNextNode();
			
			if (nextNode != null) {
				if (!nextNode.equals(((ExploreMultiAgent)this.myAgent).getLastPosition())) {
					for (Couple<String,List<Couple<Observation,Integer>>> o : lobs) {
						if (o.getLeft().equals(nextNode)){
							if (containsStench(o.getRight())) {
								if (((ExploreMultiAgent)this.myAgent).getOtherAgentsPos().isEmpty()) {
									noGolem = false;
									System.out.println(this.myAgent.getLocalName() + " encountered a golem !");
								}
								else {
									boolean someoneElse = false;
									
									for (Couple<String,String> c : ((ExploreMultiAgent)this.myAgent).getOtherAgentsPos()) {
										if (c.getRight().equals(nextNode)) {
											someoneElse = true;
										}
									}
									
									if (!someoneElse) {
										noGolem = false;
										System.out.println(this.myAgent.getLocalName() + " encountered a golem !");
									}
								}
							}
						}
					}
				}
			}
			
			((ExploreMultiAgent)this.myAgent).setNoGolem(new Couple<Boolean, String>(noGolem, nextNode));

			//2) get the surrounding nodes and, if not in closedNodes, add them to open nodes.
			nextNode=null;
			Iterator<Couple<String, List<Couple<Observation, Integer>>>> iter=lobs.iterator();
			while(iter.hasNext()){
				String nodeId=iter.next().getLeft();
				boolean isNewNode=this.myMap.addNewNode(nodeId);
				//the node may exist, but not necessarily the edge
				if (myPosition!=nodeId) {
					this.myMap.addEdge(myPosition, nodeId);
					if (nextNode==null && isNewNode) {
						nextNode=nodeId;
					}
				}
			}
			
			((ExploreMultiAgent)this.myAgent).setNextNode(nextNode);
			((ExploreMultiAgent)this.myAgent).setMyMap(this.myMap);
		}
			
			
	}

	@Override
	public int onEnd() {
		return 1;
	}
	
	public boolean containsStench(List<Couple<Observation, Integer>> L){
        for (Couple<Observation, Integer> c:L){
            if(c.getLeft().getName().equals("Stench"))
                return true;
        }
        return false;
    }

}
