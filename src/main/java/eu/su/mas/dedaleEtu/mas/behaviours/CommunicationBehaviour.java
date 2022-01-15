package eu.su.mas.dedaleEtu.mas.behaviours;

import java.util.List;

import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.agents.dummies.ExploreSoloAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import jade.core.AID;
import jade.core.behaviours.FSMBehaviour;
import jade.core.behaviours.ParallelBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class CommunicationBehaviour extends ParallelBehaviour {

	/**
	 * 
	 */
	private static final long serialVersionUID = -9105455214162907106L;
	private int end;
	
	
	public CommunicationBehaviour(final AbstractDedaleAgent myagent, List<String> agentsNames, MapRepresentation myMap) {
		super();
		
		end = 1;

		SendMsgBehaviour sendMsgB = new SendMsgBehaviour(myagent, agentsNames);
		ReceiveMsgBehaviour	receiveMsgB = new ReceiveMsgBehaviour(myagent);
		ReceiveMapBehaviour receiveMapB = new ReceiveMapBehaviour(myagent, myMap);
		LastStateBehaviour lastState = new LastStateBehaviour();
		
		addSubBehaviour(sendMsgB);
		
		FSMBehaviour fsmReceive = new FSMBehaviour(myagent);
		fsmReceive.registerFirstState(receiveMsgB, "getPing");
		fsmReceive.registerLastState(receiveMapB, "receiveMap");
		fsmReceive.registerLastState(lastState, "end");
		fsmReceive.registerTransition("getPing", "receiveMap", 1);
		fsmReceive.registerTransition("getPing", "end", 0);
		
		addSubBehaviour(fsmReceive);
		
		ReceiveMapBehaviour incaseChasse = new ReceiveMapBehaviour(myagent, myMap);
		
		addSubBehaviour(incaseChasse);
	/*	
		if(receiveMsgB.onEnd() == 1) {
			if(receiveMapB.onEnd() == 0) 
				end = 0;
		}
	*/
	}

	@Override
	public int onEnd() {
		this.reset();
		return end;
	}

}
