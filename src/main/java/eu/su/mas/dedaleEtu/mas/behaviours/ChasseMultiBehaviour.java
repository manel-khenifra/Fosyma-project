package eu.su.mas.dedaleEtu.mas.behaviours;
import eu.su.mas.dedaleEtu.mas.agents.dummies.explo.ExploreMultiAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import dataStructures.tuple.Couple;

public class ChasseMultiBehaviour extends OneShotBehaviour {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1087655301033262353L;

	private MapRepresentation myMap;
	
	private int end;
	
	public ChasseMultiBehaviour(final Agent myagent) {
		super(myagent);
		this.myMap = ((ExploreMultiAgent) this.myAgent).getMyMap();
	}

	@Override
	public void action() {
		
		String myPosition=((AbstractDedaleAgent)this.myAgent).getCurrentPosition();
		
		
		if (myPosition!=null){
			
			try {
				this.myAgent.doWait(1000);
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			String nextNode = null;
			
			if (((ExploreMultiAgent)this.myAgent).getCompteurPartir().getRight() > 3) {
				((ExploreMultiAgent)this.myAgent).reinitializeCompteurPartir();
				((ExploreMultiAgent)this.myAgent).setPosGolemPartir(null);
			}
			else if (((ExploreMultiAgent)this.myAgent).getCompteurPartir().getLeft() > 20) {
				((ExploreMultiAgent)this.myAgent).reinitializeCompteurPartir();
				((ExploreMultiAgent)this.myAgent).setPosGolemPartir(null);
			}
			
			boolean finished = false;
			boolean givenGolem = false;
			boolean golem = false;
			boolean givenPGolem = false;
			
			for (Couple<Couple<String,String>, List<String>> c : ((ExploreMultiAgent)this.myAgent).getOthNodesStench()) {
				if (c.getLeft().getRight() != null) {
					if (this.myMap.sendNodeEdges(c.getLeft().getRight()).getLeft() > 1)
						givenGolem = true;
				}
				
				if (c.getRight().contains("FINISHED")) {
					if (!this.myMap.sendNodeEdges(c.getLeft().getRight()).getRight().contains(myPosition)) {
						finished = true;
						((ExploreMultiAgent)this.myAgent).setPosGolemPartir(c.getLeft().getRight());
					}
				}
			}
			
			if (((ExploreMultiAgent)this.myAgent).getPositionGolem() != null) {
				for (String node : ((ExploreMultiAgent)this.myAgent).getNodesStench()) {
					if (((ExploreMultiAgent)this.myAgent).getPositionGolem().equals(node))
						golem = true;
				}
				
				for (Couple<Couple<String,String>, List<String>> c : ((ExploreMultiAgent)this.myAgent).getOthNodesStench()) {
					if (c.getLeft().getLeft().equals(((ExploreMultiAgent)this.myAgent).getPositionGolem())) {
						golem = false;
						((ExploreMultiAgent)this.myAgent).setPositionGolem(null);
						((ExploreMultiAgent)this.myAgent).reinitializeCompteur();
						((ExploreMultiAgent)this.myAgent).reinitializeFinish();
					}
				}
			}
			
			if (((ExploreMultiAgent)this.myAgent).getGivenPosGolem() != null) {
				if (this.myMap.sendNodeEdges(((ExploreMultiAgent)this.myAgent).getGivenPosGolem().getRight()).getLeft() > 1) {
					givenPGolem = true;
					
					for (String s : ((ExploreMultiAgent)this.myAgent).getGivenPosGolem().getLeft()) {
						if (((ExploreMultiAgent)this.myAgent).getGivenPosGolem().getRight().equals(s)) {
							givenPGolem = false;
							((ExploreMultiAgent)this.myAgent).setGivenPosGolem(null);
							break;
						}
					}
					
					if (givenPGolem) {
						for (Couple<Couple<String,String>, List<String>> c : ((ExploreMultiAgent)this.myAgent).getOthNodesStench()) {
							if (c.getLeft().getLeft().equals(((ExploreMultiAgent)this.myAgent).getGivenPosGolem().getRight())) {
								givenPGolem = false;
								((ExploreMultiAgent)this.myAgent).setGivenPosGolem(null);
								break;
							}
						}
					}
				}
			}
			
			if (givenGolem) {
				List<Couple<Couple<String,String>, List<String>>> othNodesStench = ((ExploreMultiAgent)this.myAgent).getOthNodesStench();
				List<String> others = new ArrayList<String>();
				List<String> golems = new ArrayList<String>();
				
				for (Couple<Couple<String,String>, List<String>> c : othNodesStench) {
					if (c.getLeft().getRight() != null) {
						others.add(c.getLeft().getLeft());
						if (this.myMap.sendNodeEdges(c.getLeft().getRight()).getLeft() > 1)
							golems.add(c.getLeft().getRight());
					}
				}
				
				givenGolem = false;
				
				for (String golemC : golems) {
					if (((ExploreMultiAgent)this.myAgent).getGivenPosGolem() != null){
						if (((ExploreMultiAgent)this.myAgent).getGivenPosGolem().getRight().equals(golemC)) {
							List<String> path = this.myMap.shortestPathNewMap(myPosition, golemC, others);
							
							if (path!=null)
								if (!path.isEmpty())
									givenGolem = true;
						}
					}
					else {
						List<String> path = this.myMap.shortestPathNewMap(myPosition, golemC, others);
						
						if (path!=null)
							if (!path.isEmpty())
								givenGolem = true;
					}
				}
					
			}
			
			/**
			 * Décision de nextNode.
			 */
			
			if ((((ExploreMultiAgent)this.myAgent).getCompteurPartir().getLeft() >= 1) || finished) {
				if (finished)
					((ExploreMultiAgent)this.myAgent).reinitializeCompteurPartir();
				
				List<String> no = new ArrayList<String>();
				String posGolem = ((ExploreMultiAgent)this.myAgent).getPosGolemPartir();
				
				no = this.myMap.sendNodeEdges(posGolem).getRight();
				
				nextNode = this.myMap.getAway(myPosition, posGolem, no).get(0);
				
				((ExploreMultiAgent)this.myAgent).addLCompteurPartir();
				
				if (((ExploreMultiAgent)this.myAgent).getNodesStench().isEmpty()) {
					((ExploreMultiAgent)this.myAgent).addRCompteurPartir();
				}
				
				System.out.println(this.myAgent.getLocalName() + " -- I'm not needed here");
			}
			
			else if (((ExploreMultiAgent)this.myAgent).getNodesStench().isEmpty() && ((ExploreMultiAgent)this.myAgent).getOthNodesStench().isEmpty()
					&& !givenPGolem) {
				
				List<String> nodes = ((ExploreMultiAgent)this.myAgent).getObsNodes();
				List<String> remNodes = new ArrayList<String>();
				
				for (String n : nodes) {
					Couple<Integer, List<String>> l = this.myMap.sendNodeEdges(n);
					if (l.getLeft() == 1) {
						remNodes.add(n);
					}
				}
				
				for (String n : remNodes) {
					nodes.remove(n);
				}
				
				if (nodes.size() != 1) {
					remNodes = new ArrayList<String>();
					
					for (String n : nodes) {
						if (n.equals(((ExploreMultiAgent)this.myAgent).getLastPosition())) {
							remNodes.add(n);
						}
					}
					
					for (String n : remNodes) {
						nodes.remove(n);
					}
				}

				int size = nodes.size();
				
				Random rand = new Random();
				
				int na = rand.nextInt(size);
				
				nextNode = nodes.get(na);
			}
			
			else if (golem) {
				nextNode = ((ExploreMultiAgent)this.myAgent).getPositionGolem();
				((ExploreMultiAgent)this.myAgent).addCompteur();
			}
			
			else if (givenGolem) {
				
				List<Couple<Couple<String,String>, List<String>>> othNodesStench = ((ExploreMultiAgent)this.myAgent).getOthNodesStench();
				List<String> others = new ArrayList<String>();
				List<String> golems = new ArrayList<String>();
				
				for (Couple<Couple<String,String>, List<String>> c : othNodesStench) {
					if (c.getLeft().getRight() != null) {
						others.add(c.getLeft().getLeft());
						if (this.myMap.sendNodeEdges(c.getLeft().getRight()).getLeft() > 1)
							golems.add(c.getLeft().getRight());
					}
				}
				
				for (String golemC : golems) {
					List<String> path = this.myMap.shortestPathNewMap(myPosition, golemC, others);
					
					if (path!=null) {
						if (!path.isEmpty()) {
							nextNode = path.get(0);
							((ExploreMultiAgent)this.myAgent).setGivenPosGolem(new Couple<List<String>, String>(others, golemC));
						}
					}
				}
				
			}
			
			else if (givenPGolem) {
				Couple<List<String>,String> c = ((ExploreMultiAgent)this.myAgent).getGivenPosGolem();
				
				nextNode = this.myMap.shortestPathNewMap(myPosition, c.getRight(), c.getLeft()).get(0);
			}
			
			else if (((ExploreMultiAgent)this.myAgent).getNodesStench().isEmpty()) {
				
				List<Couple<Couple<String,String>, List<String>>> othNodesStench = ((ExploreMultiAgent)this.myAgent).getOthNodesStench();
				List<Couple<String,Integer>> nodesStench = new ArrayList<Couple<String,Integer>>();
				
				for (Couple<Couple<String,String>, List<String>> l : othNodesStench) {
					for (String s : l.getRight()) {
						List<String> path = this.myMap.getShortestPath(myPosition, s);
						if ((path.size() != 0) && !path.isEmpty()) {
							if (((ExploreMultiAgent)this.myAgent).getObsNodes().contains(path.get(0))) {
								nodesStench.add(new Couple<String, Integer>(s, path.size()));
							}
						}
					}
				}
				
				if (nodesStench.isEmpty()) {
					int size = ((ExploreMultiAgent)this.myAgent).getObsNodes().size();
					
					Random rand = new Random();
					
					int na = rand.nextInt(size);
					
					nextNode = ((ExploreMultiAgent)this.myAgent).getObsNodes().get(na);
				}
				else {
					int smallest = Integer.MAX_VALUE;
					String s = nodesStench.get(0).getLeft();
					
					for (Couple<String, Integer> node : nodesStench) {
						int size = node.getRight();
						
						if (size < smallest) {
							smallest = size;
							s = node.getLeft();
						}
					}
					
					nextNode = this.myMap.getShortestPath(myPosition, s).get(0);
				}
				/*	
					System.out.println(nextNode + " is nextNode, and ");
					for (String s : ((ExploreMultiAgent)this.myAgent).getObsNodes()) {
						System.out.println(s);
					}
				*/
				
			
			/*
				int smallest = Integer.MAX_VALUE;
				
				for (String node : nodesStench) {
					List<String> path = this.myMap.getShortestPath(myPosition, node);
					int size = path.size();
					
					if (size != 0) {
						if (size < smallest) {
							smallest = size;
							nextNode = path.get(0);
						}
					}
				}
			*/	
			}
			
			else {
				
				List<String> obsNodes = ((ExploreMultiAgent)this.myAgent).getObsNodes();
				List<String> nodesStench = ((ExploreMultiAgent)this.myAgent).getNodesStench();
				
				int f = 0;
				
				// on compte le nombre de feuilles observables
				for (String node : obsNodes) {
					Couple<Integer, List<String>> l = this.myMap.sendNodeEdges(node);
					if (l.getLeft() == 1) {
						f += 1;
					}
				}
				
				int fs = 0;
				
				// on compte le nombre de feuilles avec une odeur
				for (String node : nodesStench) {
					Couple<Integer, List<String>> l = this.myMap.sendNodeEdges(node);
					if (l.getLeft() == 1) {
						fs += 1;
					}
				}
				
				// si on n'a qu'une feuille avec une odeur mais plus d'une feuille observables, le golem est peut-être dans la feuille
				// on essaye d'aller dans la feuille
				if (fs == 1 && f > 1) {
					for (String node : nodesStench) {
						Couple<Integer, List<String>> l = this.myMap.sendNodeEdges(node);
						if (l.getLeft() == 1) {
							nextNode = node;
						}
					}
				}
					
				else {
					
					if (nodesStench.size() != 1) {
					 
						
						List<String> remNodes = new ArrayList<String>();
						
						for (String n : nodesStench) {
							if (n.equals(((ExploreMultiAgent)this.myAgent).getLastPosition())) {
								remNodes.add(n);
							}
						}
						
						for (String n : remNodes) {
							nodesStench.remove(n);
						}
					}
					
					int size = nodesStench.size();
					
					Random rand = new Random();
					
					int na = rand.nextInt(size);
					
					nextNode = nodesStench.get(na);
				}
			}
			
			((ExploreMultiAgent)this.myAgent).setLastPosition(myPosition);
			((ExploreMultiAgent)this.myAgent).setNextNode(nextNode);
			((AbstractDedaleAgent)this.myAgent).moveTo(nextNode);
			
		}
		
	}
}
