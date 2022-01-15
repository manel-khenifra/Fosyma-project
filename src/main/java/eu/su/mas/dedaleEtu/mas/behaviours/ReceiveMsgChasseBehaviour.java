package eu.su.mas.dedaleEtu.mas.behaviours;

import java.util.List;

import dataStructures.tuple.Couple;
import eu.su.mas.dedaleEtu.mas.agents.dummies.explo.ExploreMultiAgent;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

public class ReceiveMsgChasseBehaviour extends OneShotBehaviour {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5936080618888488533L;

	public ReceiveMsgChasseBehaviour (final Agent myagent) {
		super(myagent);
	}
	
	@Override
	public void action() {
		MessageTemplate msgTemplate = MessageTemplate.and(
				MessageTemplate.MatchProtocol("CHASSE-STENCH"),
				MessageTemplate.MatchPerformative(ACLMessage.INFORM));
											
		ACLMessage msg = this.myAgent.receive(msgTemplate);
				
		if (msg != null) {	
			try {
				Couple<Couple<String,String>, List<String>> o = (Couple<Couple<String,String>, List<String>>) msg.getContentObject();					
				((ExploreMultiAgent)this.myAgent).addOthNodesStench(o);
				
				if (((ExploreMultiAgent)this.myAgent).getCompteur() >= 50) {
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
				}
			} catch (UnreadableException e) {
				e.printStackTrace();
			}
		}
	}

}
