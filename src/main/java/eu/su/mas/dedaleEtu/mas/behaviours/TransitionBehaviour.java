package eu.su.mas.dedaleEtu.mas.behaviours;

import java.util.ArrayList;
import java.util.List;

import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.agents.dummies.explo.ExploreMultiAgent;
import jade.core.AID;
import jade.core.behaviours.FSMBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.AMSService;
import jade.domain.FIPAAgentManagement.AMSAgentDescription;
import jade.domain.FIPAAgentManagement.SearchConstraints;

public class TransitionBehaviour extends OneShotBehaviour {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2419642717395909667L;
	
	public TransitionBehaviour(final AbstractDedaleAgent myagent) {
		super(myagent);
	}
	
	@Override
	public void action() {
		
		List <String> agentsNames = new ArrayList<String>();
		
		List <String> agents_ams = this.getAgentsList();
		for (int i=0; i<agents_ams.size(); i++) {
			String agentName = agents_ams.get(i);
			if (!agentName.equals(this.myAgent.getLocalName()) && agentName.contains("Explo")) {
				agentsNames.add(agentName);
			}
		}
		
		FSMBehaviour fsmChasse = new FSMBehaviour(this.myAgent);
		FirstPartChasseBehaviour fpCB = new FirstPartChasseBehaviour(this.myAgent);
		ReceivePingExploChasseBehaviour rpeCB = new ReceivePingExploChasseBehaviour(this.myAgent);
		ReceiveMsgChasseBehaviour rmCB = new ReceiveMsgChasseBehaviour(this.myAgent);
		CommunicationChasseBehaviour cCB = new CommunicationChasseBehaviour(this.myAgent, agentsNames);
		ChasseMultiBehaviour cmCB = new ChasseMultiBehaviour(this.myAgent);
		VictoryChasseBehaviour vCB = new VictoryChasseBehaviour(this.myAgent);
		LastStateBehaviour lsB = new LastStateBehaviour();
		
		fsmChasse.registerFirstState(fpCB, "fpCB");
		fsmChasse.registerState(rpeCB, "rpeCB");
		fsmChasse.registerState(rmCB, "rmCB");
		fsmChasse.registerState(cCB, "cCB");
		fsmChasse.registerState(cmCB, "cmCB");
		fsmChasse.registerState(vCB, "vCB");
		fsmChasse.registerLastState(lsB, "lsB");
		
		fsmChasse.registerDefaultTransition("fpCB", "cmCB");
		fsmChasse.registerTransition("fpCB", "rpeCB", 0);
		fsmChasse.registerTransition("fpCB", "cCB", 1);
		fsmChasse.registerTransition("fpCB", "vCB", 2);
		fsmChasse.registerDefaultTransition("rpeCB", "rmCB");
		fsmChasse.registerDefaultTransition("rmCB", "cmCB");
		fsmChasse.registerDefaultTransition("cCB", "rpeCB");
		fsmChasse.registerDefaultTransition("vCB", "fpCB");
		fsmChasse.registerDefaultTransition("cmCB", "fpCB");
		fsmChasse.registerTransition("cmCB", "lsB", 1);
		
		((ExploreMultiAgent)this.myAgent).setNextNode(null);
		
		/***
		 * ADD BEHAVIOUR TO THE AGENT
		 */
		this.myAgent.addBehaviour(fsmChasse);
		
	}
	
	public List <String> getAgentsList(){
		AMSAgentDescription [] agentsDescriptionCatalog = null ;
		List <String> agentsNames= new ArrayList<String>();
		try {
			SearchConstraints c = new SearchConstraints();
			c.setMaxResults ( Long.valueOf(-1) );
			agentsDescriptionCatalog = AMSService.search(this.myAgent, new AMSAgentDescription (), c );
		}
		catch (Exception e) {
			System.out. println ( "Problem searching AMS: " + e );
			e . printStackTrace () ;
		}
	
		for ( int i =0; i<agentsDescriptionCatalog. length ; i ++){
			AID agentID = agentsDescriptionCatalog[i ]. getName();
			agentsNames.add(agentID.getLocalName());
		}
		return agentsNames;
	
	}

}
