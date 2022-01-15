package eu.su.mas.dedaleEtu.mas.behaviours;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import dataStructures.tuple.Couple;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.agents.dummies.explo.ExploreMultiAgent;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

public class VictoryChasseBehaviour extends SimpleBehaviour {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7600758478489295687L;
	
	private boolean finished;
	
	public VictoryChasseBehaviour (final Agent myagent) {
		super(myagent);
	}

	@Override
	public void action() {
		finished = false;
		
		String myPosition=((AbstractDedaleAgent)this.myAgent).getCurrentPosition();
		
		MessageTemplate msgTemplate = MessageTemplate.and(
				MessageTemplate.MatchProtocol("CHASSE-STENCH"),
				MessageTemplate.MatchPerformative(ACLMessage.INFORM));
											
		ACLMessage msg = this.myAgent.receive(msgTemplate);
				
		if (msg != null) {	
			try {
				Couple<Couple<String,String>, List<String>> o = (Couple<Couple<String,String>, List<String>>) msg.getContentObject();					
				
				Couple<String,String> remCouple = null;
				for (Couple<String,String> c : ((ExploreMultiAgent)this.myAgent).getFinish()) {
					if (c.getLeft().equals(msg.getSender().getLocalName())) {
						if (!o.getLeft().getLeft().equals(c.getRight())) {
							remCouple = c;
						}
					}
				}
					
				((ExploreMultiAgent)this.myAgent).removeFinish(remCouple);
				String posGolem = ((ExploreMultiAgent)this.myAgent).getPositionGolem();
				if (o.getLeft().getRight() != null) {
					if (o.getLeft().getRight().equals(posGolem)) {
						Couple<Integer, List<String>> c = ((ExploreMultiAgent)this.myAgent).getMyMap().sendNodeEdges(posGolem);
						if (c.getRight().contains(o.getLeft().getLeft())) {
							((ExploreMultiAgent)this.myAgent).addFinish(new Couple<String,String>(msg.getSender().getLocalName(), o.getLeft().getLeft()));
						}
					}
				}
				
				if (!didIFinish(myPosition)) {
					System.out.println(this.myAgent.getLocalName() + " -- Actually no I didn't capture the golem I'm dumb.");
					finished = true;
				}
				else if (o.getLeft().getLeft().equals(((ExploreMultiAgent)this.myAgent).getPositionGolem())) {
					((ExploreMultiAgent)this.myAgent).addOthNodesStench(o);
					((ExploreMultiAgent)this.myAgent).setPositionGolem(null);
					((ExploreMultiAgent)this.myAgent).reinitializeCompteur();
					((ExploreMultiAgent)this.myAgent).reinitializeFinish();
					System.out.println(this.myAgent.getLocalName() + " -- Actually no I didn't capture the golem I'm dumb.");
					finished = true;
				}
				else {
					sendAnswer(msg.getSender().getLocalName());
				}
			} catch (UnreadableException e) {
			e.printStackTrace();
			}
		} 
		else {
			block();
		}
	}
	
	private void sendAnswer(String receiverName) {
		String myPosition=((AbstractDedaleAgent)this.myAgent).getCurrentPosition();
		String positionGolem = ((ExploreMultiAgent)this.myAgent).getPositionGolem();
		
		List<String> eh = new ArrayList<String>();
		eh.add("FINISHED");
		
		ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
		msg.setProtocol("CHASSE-STENCH");
		msg.setSender(this.myAgent.getAID());
		
		try {
			Couple<String,String> couple = new Couple<String,String>(myPosition, positionGolem);
			msg.setContentObject(new Couple<Couple<String,String>,List<String>>(couple, eh));
			msg.addReceiver(new AID(receiverName, AID.ISLOCALNAME));  
			((AbstractDedaleAgent)this.myAgent).sendMessage(msg);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean done() {
		return finished;
	}
	
	public boolean didIFinish(String myPosition) {
		boolean f = true;
		
		List<String> temp = new ArrayList<String>();
		
		temp.add(myPosition);
		
		for (Couple<String,String> c : ((ExploreMultiAgent)this.myAgent).getFinish()) {
			temp.add(c.getRight());
		}
		
		Couple<Integer, List<String>> c = ((ExploreMultiAgent)this.myAgent).getMyMap().sendNodeEdges(((ExploreMultiAgent)this.myAgent).getPositionGolem());
		
		for (String s : c.getRight()) {
			if (!temp.contains(s))
				f = false;
		}
		
		return f;
	}

}
