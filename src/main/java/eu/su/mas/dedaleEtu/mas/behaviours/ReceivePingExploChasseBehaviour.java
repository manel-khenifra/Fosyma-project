package eu.su.mas.dedaleEtu.mas.behaviours;

import java.io.IOException;

import dataStructures.serializableGraph.SerializableSimpleGraph;
import dataStructures.tuple.Couple;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.agents.dummies.explo.ExploreMultiAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation.MapAttribute;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class ReceivePingExploChasseBehaviour extends OneShotBehaviour {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7138396337138485640L;
	
	private MapRepresentation myMap;
	
	public ReceivePingExploChasseBehaviour(final Agent myagent) {
		super(myagent);
	}

	public void getPing() {
		MessageTemplate msgTemplate = MessageTemplate.and(
				MessageTemplate.MatchProtocol("PING"),
				MessageTemplate.MatchPerformative(ACLMessage.INFORM));
									
		ACLMessage msg = this.myAgent.receive(msgTemplate);
		//System.out.println("<---- test receiveping ");
		if (msg != null) {
			sendAnswer(msg.getSender().getLocalName());
		}
	}
	
	public void sendAnswer(String receiverName) {
		String myPosition=((AbstractDedaleAgent)this.myAgent).getCurrentPosition();
		
		ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
		msg.setProtocol("SHARE-TOPO");
		msg.setSender(this.myAgent.getAID());
		
		this.myMap =((ExploreMultiAgent)this.myAgent).getMyMap();
			if (this.myMap != null) {
				try {
					SerializableSimpleGraph<String, MapAttribute> sg = this.myMap.getSerializableGraph();
					Couple<String, SerializableSimpleGraph<String, MapAttribute>> sgg = new Couple<String, SerializableSimpleGraph<String, MapAttribute>>(myPosition,sg);
					msg.setContentObject(sgg);
					
					msg.addReceiver(new AID(receiverName, AID.ISLOCALNAME));  

					//Mandatory to use this method (it takes into account the environment to decide if someone is reachable or not)
					((AbstractDedaleAgent)this.myAgent).sendMessage(msg);
						
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
	}
	
	@Override
	public void action() {
		getPing();
	}

}
