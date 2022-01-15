package eu.su.mas.dedaleEtu.mas.behaviours;

import java.io.IOException;
import java.io.Serializable;

import dataStructures.serializableGraph.SerializableSimpleGraph;
import dataStructures.tuple.Couple;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.agents.dummies.ExploreSoloAgent;
import eu.su.mas.dedaleEtu.mas.agents.dummies.explo.ExploreMultiAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation.MapAttribute;
//import eu.su.mas.dedale.mas.agents.dedaleDummyAgents.Explo.ExploreSoloAgent;
import jade.core.AID;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;



public class SendMapBehaviour extends SimpleBehaviour implements Serializable{
	
	private static final long serialVersionUID = -4739824503130129123L;

	private boolean finished=false;
	
	private boolean result=false;
	
	/**
	 * Name of the agent that should receive the values
	 */
	private String receiverName;
	
	private MapRepresentation myMap;
	
	
	public SendMapBehaviour(final AbstractDedaleAgent myagent, String receiverName) {
		super(myagent);
		this.receiverName=receiverName;
	}


	public void action() {

		String myPosition=((AbstractDedaleAgent)this.myAgent).getCurrentPosition();

		final ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
		msg.setProtocol("SHARE-TOPO");
		msg.setSender(this.myAgent.getAID());
		
		if (myPosition!=null){
			//System.out.println("<---- test sendmap ");
			this.myMap =((ExploreMultiAgent)this.myAgent).getMyMap();
			if (this.myMap != null) {
				try {
					SerializableSimpleGraph<String, MapAttribute> sg = this.myMap.getSerializableGraph();
					Couple<String, SerializableSimpleGraph<String, MapAttribute>> sgg = new Couple<String, SerializableSimpleGraph<String, MapAttribute>>(myPosition,sg);
					msg.setContentObject(sgg);
					
					msg.addReceiver(new AID(this.receiverName, AID.ISLOCALNAME));  

					//Mandatory to use this method (it takes into account the environment to decide if someone is reachable or not)
					((AbstractDedaleAgent)this.myAgent).sendMessage(msg);						
				} catch (IOException e) {
					e.printStackTrace();
				}
				this.result = true; 
			}
			else {
				this.result = false;
			}
			this.finished = true;
		}
	
		
	}

	public boolean done() {
		return finished;
	}
	
	@Override
	public int onEnd() {
		if (this.result) {
			((ExploreMultiAgent)this.myAgent).setWaiting(true);
			return 1;
		}
		else {
			((ExploreMultiAgent)this.myAgent).setWaiting(false);
			return 0;
		}
	}


}
