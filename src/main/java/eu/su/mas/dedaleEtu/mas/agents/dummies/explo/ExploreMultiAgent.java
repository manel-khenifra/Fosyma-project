package eu.su.mas.dedaleEtu.mas.agents.dummies.explo;

import java.util.ArrayList;
import java.util.List;

import dataStructures.serializableGraph.SerializableSimpleGraph;
import dataStructures.tuple.Couple;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedale.mas.agent.behaviours.startMyBehaviours;
import eu.su.mas.dedaleEtu.mas.behaviours.CommunicationBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.ExploMultiBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.FirstPartExploBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.TransitionBehaviour;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation.MapAttribute;
import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.FSMBehaviour;
import jade.domain.AMSService;
import jade.domain.FIPAAgentManagement.AMSAgentDescription;
import jade.domain.FIPAAgentManagement.SearchConstraints;

/**
 * <pre>
 * ExploreSolo agent. 
 * It explore the map using a DFS algorithm.
 * It stops when all nodes have been visited.
 *  </pre>
 *  
 * @author hc
 *
 */

public class ExploreMultiAgent extends AbstractDedaleAgent {

	private static final long serialVersionUID = -6431752665590433727L;
	private MapRepresentation myMap;
	private boolean waiting = false;
	private List<Couple<String, String>> otherAgentsPos;
	private String nextNode;
	private List<String> nodesStench;
	private List<String> obsNodes;
	private List<Couple<Couple<String,String>, List<String>>> othNodesStench;
	private String lastPosition;
	private String positionGolem;
	private Couple<List<String>, String> givenPosGolem;
	private int compteurGolem;
	private List<Couple<String,String>> finish;
	private Couple<Integer,Integer> compteurPartir;
	private String posGolemPartir;
	private List<Couple<String, SerializableSimpleGraph<String, MapAttribute>>> othersMaps;
	private Couple<Boolean, String> noGolem;
	

	/**
	 * This method is automatically called when "agent".start() is executed.
	 * Consider that Agent is launched for the first time. 
	 * 			1) set the agent attributes 
	 *	 		2) add the behaviours
	 *          
	 */
	protected void setup(){

		super.setup();
		
		this.initializeOtherAgentsPos();
		
		nextNode = null;
		lastPosition = null;
		compteurGolem = 0;
		this.reinitializeFinish();
		this.reinitializeCompteurPartir();
		othersMaps = new ArrayList<Couple<String, SerializableSimpleGraph<String, MapAttribute>>>();
		
		List<Behaviour> lb=new ArrayList<Behaviour>();
	
		List <String> agentsNames = new ArrayList<String>();
		
		List <String> agents_ams = this.getAgentsList();
		for (int i=0; i<agents_ams.size(); i++) {
			String agentName = agents_ams.get(i);
			if (!agentName.equals(this.getLocalName()) && agentName.contains("Explo")) {
				agentsNames.add(agentName);
			}
		}
		
		FirstPartExploBehaviour firstExploSB = new FirstPartExploBehaviour(this,this.myMap);
		ExploMultiBehaviour exploSB = new ExploMultiBehaviour(this,this.myMap);
		TransitionBehaviour lastState = new TransitionBehaviour(this);
		
		FSMBehaviour fsmCallB = new FSMBehaviour(this);				
		CommunicationBehaviour com = new CommunicationBehaviour(this, agentsNames, this.myMap);
		
		fsmCallB.registerFirstState(firstExploSB, "firstExplo");
		fsmCallB.registerState(com, "communication");
		fsmCallB.registerState(exploSB, "explo");
		fsmCallB.registerLastState(lastState, "end");
		
		fsmCallB.registerDefaultTransition("firstExplo", "communication");
		fsmCallB.registerTransition("communication", "communication", 0);
		fsmCallB.registerTransition("communication", "explo", 1);
		fsmCallB.registerTransition("explo", "firstExplo", 0);
		fsmCallB.registerTransition("explo", "end", 1);
		
		lb.add(fsmCallB);
		
		/***
		 * MANDATORY TO ALLOW YOUR AGENT TO BE DEPLOYED CORRECTLY
		 */
		addBehaviour(new startMyBehaviours(this,lb));
		
		System.out.println("the  agent "+this.getLocalName()+ " is started");

	}
	
	public MapRepresentation getMyMap() {
		return myMap;
	}
	
	public void setMyMap(MapRepresentation myMap) {
		this.myMap = myMap;
	}
	
	public boolean isWaiting() {
		return waiting;
	}

	public void setWaiting(boolean waiting) {
		this.waiting = waiting;
	}
	
	public void initializeOtherAgentsPos() {
		otherAgentsPos = new ArrayList<Couple<String, String>>();
	}
	
	public void addOtherAgentsPos(String name, String pos) {
		for (Couple<String,String> o : otherAgentsPos) {
			if (o.getLeft().equals(name)) {
				otherAgentsPos.remove(o);
				break;
			}
		}
		otherAgentsPos.add(new Couple<String, String>(name, pos));
	}
	
	public List<Couple<String, String>> getOtherAgentsPos() {
		return otherAgentsPos;
	}
	
	public void setNextNode(String node) {
		nextNode = node;
	}
	
	public String getNextNode() {
		return nextNode;
	}
	
	public void setNodesStench(List<String> nodes) {
		nodesStench = nodes;
	}
	
	public List<String> getNodesStench() {
		return nodesStench;
	}
	
	public void setObsNodes(List<String> nodes) {
		obsNodes = nodes;
	}
	
	public List<String> getObsNodes() {
		return obsNodes;
	}
	
	public void setOthNodesStench(List<Couple<Couple<String,String>, List<String>>> nodes) {
		othNodesStench = nodes;
	}
	
	public void addOthNodesStench(Couple<Couple<String,String>, List<String>> nodes) {
		othNodesStench.add(nodes);
	}
	
	public List<Couple<Couple<String,String>, List<String>>> getOthNodesStench() {
		return othNodesStench;
	}
	
	public void setLastPosition(String p) {
		lastPosition = p;
	}
	
	public String getLastPosition() {
		return lastPosition;
	}
	
	public void setPositionGolem(String p) {
		positionGolem = p;
	}
	
	public String getPositionGolem() {
		return positionGolem;
	}
	
	public void setGivenPosGolem(Couple<List<String>, String> p) {
		givenPosGolem = p;
	}
	
	public Couple<List<String>, String> getGivenPosGolem() {
		return givenPosGolem;
	}
	
	public void addCompteur() {
		compteurGolem += 1;
	}
	
	public void reinitializeCompteur() {
		compteurGolem = 0;
	}
	
	public int getCompteur() {
		return compteurGolem;
	}
	
	public void addFinish(Couple<String,String> s) {
		finish.add(s);
	}
	
	public void removeFinish(Couple<String,String> s) {
		finish.remove(s);
	}
	
	public void reinitializeFinish() {
		finish = new ArrayList<Couple<String,String>>();
	}
	
	public List<Couple<String,String>> getFinish() {
		return finish;
	}
	
	public void addLCompteurPartir() {
		int one = compteurPartir.getLeft() + 1;
		int two = compteurPartir.getRight();
		
		compteurPartir = new Couple<Integer,Integer>(one,two);
	}
	
	public void addRCompteurPartir() {
		int one = compteurPartir.getLeft();
		int two = compteurPartir.getRight() + 1;
		
		compteurPartir = new Couple<Integer,Integer>(one,two);
	}
	
	public void reinitializeCompteurPartir() {
		compteurPartir = new Couple<Integer,Integer>(0,0);
	}
	
	public Couple<Integer,Integer> getCompteurPartir() {
		return compteurPartir;
	}
	
	public void setPosGolemPartir(String p) {
		posGolemPartir = p;
	}
	
	public String getPosGolemPartir() {
		return posGolemPartir;
	}
	
	public void addOthersMap(Couple<String,SerializableSimpleGraph<String, MapAttribute>> other) {
		Couple<String,SerializableSimpleGraph<String, MapAttribute>> alreadyCouple = null;
		
		for (Couple<String,SerializableSimpleGraph<String, MapAttribute>> c : othersMaps) {
			if (c.getLeft().equals(other.getLeft())) {
				alreadyCouple = c;
			}
		}
		
		if (alreadyCouple != null) {
			SerializableSimpleGraph<String, MapAttribute> map = getMyMap().mergeMaps(alreadyCouple.getRight(), other.getRight());
			other = new Couple<String,SerializableSimpleGraph<String, MapAttribute>>(other.getLeft(), map);
		}
		
		othersMaps.add(other);
	}
	
	public boolean doIHaveOtherMap(String name) {
		boolean doI = false;
		
		for (Couple<String,SerializableSimpleGraph<String, MapAttribute>> c : othersMaps) {
			if (c.getLeft().equals(name)) {
				doI = true;
			}
		}
		
		return doI;
	}
	
	public Couple<String,SerializableSimpleGraph<String, MapAttribute>> getOtherMap(String name){
		Couple<String,SerializableSimpleGraph<String, MapAttribute>> other = null;
		
		for (Couple<String,SerializableSimpleGraph<String, MapAttribute>> c : othersMaps) {
			if (c.getLeft().equals(name)) {
				other = c;
			}
		}
		
		return other;
	}
	
	public void setNoGolem(Couple<Boolean, String> b) {
		noGolem = b;
	}
	
	public Couple<Boolean, String> getNoGolem() {
		return noGolem;
	}
	
	/**
	 * @return The list of the ( local )names of the agents currently within the platform
	 *          
	 */
	public List <String> getAgentsList(){
		AMSAgentDescription [] agentsDescriptionCatalog = null ;
		List <String> agentsNames= new ArrayList<String>();
		try {
			SearchConstraints c = new SearchConstraints();
			c.setMaxResults ( Long.valueOf(-1) );
			agentsDescriptionCatalog = AMSService.search(this, new AMSAgentDescription (), c );
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