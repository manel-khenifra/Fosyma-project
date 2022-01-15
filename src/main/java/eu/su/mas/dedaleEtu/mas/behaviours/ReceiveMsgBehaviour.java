package eu.su.mas.dedaleEtu.mas.behaviours;

import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.agents.dummies.explo.ExploreMultiAgent;
import jade.core.AID;
import jade.core.behaviours.SimpleBehaviour;

import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;


public class ReceiveMsgBehaviour extends SimpleBehaviour {

	private static final long serialVersionUID = 9088209402507795289L;
	private boolean finished=false;
	private boolean result=false;


	public ReceiveMsgBehaviour(final AbstractDedaleAgent myagent) {
		super(myagent);
	}

	public void getPing() {
		String myPosition=((AbstractDedaleAgent)this.myAgent).getCurrentPosition();
		
		final MessageTemplate msgTemplate = MessageTemplate.and(
				MessageTemplate.MatchProtocol("PING"),
				MessageTemplate.MatchPerformative(ACLMessage.INFORM));
									
		final ACLMessage msg = this.myAgent.receive(msgTemplate);
		//System.out.println("<---- test receiveping ");
		if (msg != null) {		
			if (myPosition!=""){
				sendAnswer(msg.getSender().getLocalName());
				this.result=true;
			}
		}else {
			this.result=false;
		}
		this.finished = true;
	}
	
	public void sendAnswer(String receiverName) {
		String myPosition=((AbstractDedaleAgent)this.myAgent).getCurrentPosition();
		
		final ACLMessage msg = new ACLMessage(ACLMessage.CONFIRM);
		msg.setSender(this.myAgent.getAID());
		if (myPosition!=""){
			msg.setContent(myPosition);
			msg.addReceiver(new AID(receiverName, AID.ISLOCALNAME));  
			((AbstractDedaleAgent)this.myAgent).sendMessage(msg);
		}
	}
	
	public void action() {
		getPing();
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


