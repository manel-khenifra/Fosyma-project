package eu.su.mas.dedaleEtu.mas.behaviours;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;

import dataStructures.serializableGraph.SerializableSimpleGraph;
import dataStructures.tuple.Couple;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation.MapAttribute;
import eu.su.mas.dedaleEtu.mas.agents.dummies.ExploreSoloAgent;
//import eu.su.mas.dedale.mas.agents.dedaleDummyAgents.Explo.ExploreSoloAgent;
import eu.su.mas.dedaleEtu.mas.agents.dummies.explo.ExploreMultiAgent;
import jade.core.behaviours.SimpleBehaviour;

import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;


public class ReceiveMapBehaviour extends SimpleBehaviour implements Serializable{

	private static final long serialVersionUID = -4454538062320130950L;

	private boolean finished=false;
	
	private boolean result=false;

	private MapRepresentation myMap;
	
	public ReceiveMapBehaviour(final AbstractDedaleAgent myagent, MapRepresentation myMap) {
		super(myagent);
		this.myMap = myMap;
	}


	public void action() {
		
		String myPosition=((AbstractDedaleAgent)this.myAgent).getCurrentPosition();
		
		if (myPosition!=null){
			
			final MessageTemplate msgTemplate = MessageTemplate.and(
					MessageTemplate.MatchProtocol("SHARE-TOPO"),
					MessageTemplate.MatchPerformative(ACLMessage.INFORM));
										
			final ACLMessage msg = this.myAgent.receive(msgTemplate);
			//System.out.println("<---- test receivemap ");
			if (msg != null) {								
				try {
					Couple<String, SerializableSimpleGraph<String, MapAttribute>> o = (Couple<String, SerializableSimpleGraph<String, MapAttribute>>) msg.getContentObject();
					if (o != null) {
						((ExploreMultiAgent)this.myAgent).addOtherAgentsPos(msg.getSender().getLocalName(), o.getLeft());
						System.out.println(this.myAgent.getLocalName()+ "<---- Position received from "+msg.getSender().getLocalName() + ": " +o.getLeft());
						this.myMap = ((ExploreMultiAgent)this.myAgent).getMyMap();
						this.myMap.mergeMap(o.getRight());
						((ExploreMultiAgent)this.myAgent).setMyMap(this.myMap);
						System.out.println(this.myAgent.getLocalName()+ "<---- Posittion and Map received from "+msg.getSender().getLocalName());
						//((ExploreMultiAgent)this.myAgent).addOthersMap(new Couple<String, SerializableSimpleGraph<String, MapAttribute>>(msg.getSender().getLocalName(), o.getRight()));
						this.result=true;
					}	
				} catch (UnreadableException e) {
					e.printStackTrace();
				}
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
		if (this.result) return 1;
		else return 0;
	}

}



