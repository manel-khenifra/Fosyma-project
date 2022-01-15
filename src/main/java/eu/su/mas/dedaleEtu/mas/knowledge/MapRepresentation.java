package eu.su.mas.dedaleEtu.mas.knowledge;

import java.io.PrintStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.graphstream.algorithm.Dijkstra;
import org.graphstream.graph.Edge;
import org.graphstream.graph.EdgeRejectedException;
import org.graphstream.graph.ElementNotFoundException;
import org.graphstream.graph.Graph;
import org.graphstream.graph.IdAlreadyInUseException;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.ui.fx_viewer.FxViewer;
import org.graphstream.ui.view.Viewer;
import org.graphstream.ui.view.Viewer.CloseFramePolicy;

import dataStructures.serializableGraph.*;
import dataStructures.tuple.Couple;
import eu.su.mas.dedale.mas.agent.knowledge.MapRepresentation.MapAttribute;
import javafx.application.Platform;

/**
 * This simple topology representation only deals with the graph, not its content.</br>
 * The knowledge representation is not well written (at all), it is just given as a minimal example.</br>
 * The viewer methods are not independent of the data structure, and the dijkstra is recomputed every-time.
 * 
 * @author hc
 */
public class MapRepresentation implements Serializable {

	/**
	 * A node is open, closed, or agent
	 * @author hc
	 *
	 */

	public enum MapAttribute {	
		agent,open,closed;

	}

	private static final long serialVersionUID = -1333959882640838272L;

	/*********************************
	 * Parameters for graph rendering
	 ********************************/

	private String defaultNodeStyle= "node {"+"fill-color: black;"+" size-mode:fit;text-alignment:under; text-size:14;text-color:white;text-background-mode:rounded-box;text-background-color:black;}";
	private String nodeStyle_open = "node.agent {"+"fill-color: forestgreen;"+"}";
	private String nodeStyle_agent = "node.open {"+"fill-color: blue;"+"}";
	private String nodeStyle=defaultNodeStyle+nodeStyle_agent+nodeStyle_open;

	private Graph g; //data structure non serializable
	private Viewer viewer; //ref to the display,  non serializable
	private Integer nbEdges;//used to generate the edges ids

	private SerializableSimpleGraph<String, MapAttribute> sg;//used as a temporary dataStructure during migration


	public MapRepresentation() {
		//System.setProperty("org.graphstream.ui.renderer","org.graphstream.ui.j2dviewer.J2DGraphRenderer");
		System.setProperty("org.graphstream.ui", "javafx");
		this.g= new SingleGraph("My world vision");
		this.g.setAttribute("ui.stylesheet",nodeStyle);

		Platform.runLater(() -> {
			openGui();
		});
		//this.viewer = this.g.display();

		this.nbEdges=0;
	}

	/**
	 * Add or replace a node and its attribute 
	 * @param id
	 * @param mapAttribute
	 */
	public synchronized void addNode(String id,MapAttribute mapAttribute){
		Node n;
		if (this.g.getNode(id)==null){
			n=this.g.addNode(id);
		}else{
			n=this.g.getNode(id);
		}
		n.clearAttributes();
		n.setAttribute("ui.class", mapAttribute.toString());
		n.setAttribute("ui.label",id);
	}

	/**
	 * Add a node to the graph. Do nothing if the node already exists.
	 * If new, it is labeled as open (non-visited)
	 * @param id id of the node
	 * @return true if added
	 */
	public synchronized boolean addNewNode(String id) {
		if (this.g.getNode(id)==null){
			addNode(id,MapAttribute.open);
			return true;
		}
		return false;
	}

	/**
	 * Add an undirect edge if not already existing.
	 * @param idNode1
	 * @param idNode2
	 */
	public synchronized void addEdge(String idNode1,String idNode2){
		this.nbEdges++;
		try {
			this.g.addEdge(this.nbEdges.toString(), idNode1, idNode2);
		}catch (IdAlreadyInUseException e1) {
			System.err.println("ID existing");
			System.exit(1);
		}catch (EdgeRejectedException e2) {
			this.nbEdges--;
		} catch(ElementNotFoundException e3){

		}
	}

	/**
	 * Compute the shortest Path from idFrom to IdTo. The computation is currently not very efficient
	 * 
	 * 
	 * @param idFrom id of the origin node
	 * @param idTo id of the destination node
	 * @return the list of nodes to follow, null if the targeted node is not currently reachable
	 */
	public synchronized List<String> getShortestPath(String idFrom,String idTo){
		List<String> shortestPath=new ArrayList<String>();

		Dijkstra dijkstra = new Dijkstra();//number of edge
		dijkstra.init(g);
		dijkstra.setSource(g.getNode(idFrom));
		dijkstra.compute();//compute the distance to all nodes from idFrom
		List<Node> path=dijkstra.getPath(g.getNode(idTo)).getNodePath(); //the shortest path from idFrom to idTo
		Iterator<Node> iter=path.iterator();
		while (iter.hasNext()){
			shortestPath.add(iter.next().getId());
		}
		dijkstra.clear();
		if (shortestPath.isEmpty()) {//The openNode is not currently reachable
			return null;
		}else {
			shortestPath.remove(0);//remove the current position
		}
		return shortestPath;
	}

	public List<String> getShortestPathToClosestOpenNode(String myPosition) {
		//1) Get all openNodes
		List<String> opennodes=getOpenNodes();

		//2) select the closest one
		List<Couple<String,Integer>> lc=
				opennodes.stream()
				.map(on -> (getShortestPath(myPosition,on)!=null)? new Couple<String, Integer>(on,getShortestPath(myPosition,on).size()): new Couple<String, Integer>(on,Integer.MAX_VALUE))//some nodes my be unreachable if the agents do not share at least one common node.
				.collect(Collectors.toList());

		Optional<Couple<String,Integer>> closest=lc.stream().min(Comparator.comparing(Couple::getRight));
		//3) Compute shorterPath

		return getShortestPath(myPosition,closest.get().getLeft());
	}

	
	public List<Couple<String,Integer>> getDistanceOpenNodes(String myPosition){
		//1) Get all openNodes
		List<String> opennodes=getOpenNodes();
		
		List<Couple<String,Integer>> lc=new ArrayList<Couple<String,Integer>>();
		
		for (int i=0; i<opennodes.size(); i++) {
			if (getShortestPath(myPosition,opennodes.get(i))!=null) {
				lc.add(new Couple<String,Integer>(opennodes.get(i),getShortestPath(myPosition,opennodes.get(i)).size()));
			}
			else {
				lc.add(new Couple<String,Integer>(opennodes.get(i),Integer.MAX_VALUE));
			}
		}
		
		//lc.stream().sorted(Comparator.comparing(Couple::getRight));
		lc.sort(Comparator.comparing(Couple::getRight));
		
		return lc;
	}


	public List<String> getOpenNodes(){
		return this.g.nodes()
				.filter(x ->x .getAttribute("ui.class")==MapAttribute.open.toString()) 
				.map(Node::getId)
				.collect(Collectors.toList());
	}


	/**
	 * Before the migration we kill all non serializable components and store their data in a serializable form
	 */
	public void prepareMigration(){
		serializeGraphTopology();

		closeGui();

		this.g=null;
	}

	/**
	 * Before sending the agent knowledge of the map it should be serialized.
	 */
	private void serializeGraphTopology() {
		this.sg= new SerializableSimpleGraph<String,MapAttribute>();
		Iterator<Node> iter=this.g.iterator();
		while(iter.hasNext()){
			Node n=iter.next();
			sg.addNode(n.getId(),MapAttribute.valueOf((String)n.getAttribute("ui.class")));
		}
		Iterator<Edge> iterE=this.g.edges().iterator();
		while (iterE.hasNext()){
			Edge e=iterE.next();
			Node sn=e.getSourceNode();
			Node tn=e.getTargetNode();
			sg.addEdge(e.getId(), sn.getId(), tn.getId());
		}	
	}


	public synchronized SerializableSimpleGraph<String,MapAttribute> getSerializableGraph(){
		serializeGraphTopology();
		return this.sg;
	}

	/**
	 * After migration we load the serialized data and recreate the non serializable components (Gui,..)
	 */
	public synchronized void loadSavedData(){

		this.g= new SingleGraph("My world vision");
		this.g.setAttribute("ui.stylesheet",nodeStyle);

		openGui();

		Integer nbEd=0;
		for (SerializableNode<String, MapAttribute> n: this.sg.getAllNodes()){
			this.g.addNode(n.getNodeId()).setAttribute("ui.class", n.getNodeContent().toString());
			for(String s:this.sg.getEdges(n.getNodeId())){
				this.g.addEdge(nbEd.toString(),n.getNodeId(),s);
				nbEd++;
			}
		}
		System.out.println("Loading done");
	}

	/**
	 * Method called before migration to kill all non serializable graphStream components
	 */
	private synchronized void closeGui() {
		//once the graph is saved, clear non serializable components
		if (this.viewer!=null){
			//Platform.runLater(() -> {
			try{
				this.viewer.close();
			}catch(NullPointerException e){
				System.err.println("Bug graphstream viewer.close() work-around - https://github.com/graphstream/gs-core/issues/150");
			}
			//});
			this.viewer=null;
		}
	}

	/**
	 * Method called after a migration to reopen GUI components
	 */
	private synchronized void openGui() {
		this.viewer =new FxViewer(this.g, FxViewer.ThreadingModel.GRAPH_IN_ANOTHER_THREAD);//GRAPH_IN_GUI_THREAD)
		viewer.enableAutoLayout();
		viewer.setCloseFramePolicy(FxViewer.CloseFramePolicy.CLOSE_VIEWER);
		viewer.addDefaultView(true);

		g.display();
	}

	public void mergeMap(SerializableSimpleGraph<String, MapAttribute> sgreceived) {
		//System.out.println("You should decide what you want to save and how");
		//System.out.println("We currently blindy add the topology");

		for (SerializableNode<String, MapAttribute> n: sgreceived.getAllNodes()){
			//System.out.println(n);
			boolean alreadyIn =false;
			//1 Add the node
			Node newnode=null;
			try {
				newnode=this.g.addNode(n.getNodeId());
			}	catch(IdAlreadyInUseException e) {
				alreadyIn=true;
				//System.out.println("Already in"+n.getNodeId());
			}
			if (!alreadyIn) {
				newnode.setAttribute("ui.label", newnode.getId());
				newnode.setAttribute("ui.class", n.getNodeContent().toString());
			}else{
				newnode=this.g.getNode(n.getNodeId());
				//3 check its attribute. If it is below the one received, update it.
				if (((String) newnode.getAttribute("ui.class"))==MapAttribute.closed.toString() || n.getNodeContent().toString()==MapAttribute.closed.toString()) {
					newnode.setAttribute("ui.class",MapAttribute.closed.toString());
				}
			}
		}

		//4 now that all nodes are added, we can add edges
		for (SerializableNode<String, MapAttribute> n: sgreceived.getAllNodes()){
			for(String s:sgreceived.getEdges(n.getNodeId())){
				addEdge(n.getNodeId(),s);
			}
		}
		//System.out.println("Merge done");
	}

	/**
	 * 
	 * @return true if there exist at least one openNode on the graph 
	 */
	public boolean hasOpenNode() {
		return (this.g.nodes()
				.filter(n -> n.getAttribute("ui.class")==MapAttribute.open.toString())
				.findAny()).isPresent();
	}
	
	public Couple<Integer,List<String>> sendNodeEdges(String id) {
		Node n = this.g.getNode(id);
		Stream<Edge> edges = n.edges();
		List<String> nodes = new ArrayList<String>();
		
		int count = 0;
		
		for (Object e : edges.toArray()) {
			if (((Edge) e).getNode0().getId().equals(id))
				nodes.add(((Edge) e).getNode1().getId());
			
			else if (((Edge) e).getNode1().getId().equals(id)){
				nodes.add(((Edge) e).getNode0().getId());
			}
			
			count += 1;
		}
		
		return new Couple<Integer,List<String>>(count, nodes);
	}
	
	
	/**
	 * Return a clone of the map without the nodes listed and their edges.
	 */
	public List<String> shortestPathNewMap(String idFrom,String idTo,List<String> nodesId) {
		
	/*
	 *	MapRepresentation newMap = new MapRepresentation();
		
		for (Object node : this.g.nodes().toArray()) {
			boolean add = true;
			
			for (String id : nodesId) {
				if (((Node) node).getId().equals(id))
					add = false;
			}
			
			if (add)
				newMap.addNode(((Node) node).getId(), MapAttribute.closed);
		}
		
		
		for (Object e : this.g.edges().toArray()) {
			boolean add = true;
				
			for (String id : nodesId) {
				if (((Edge) e).getNode0().getId().equals(id)) {
					add = false;
				}
				else if (((Edge) e).getNode1().getId().equals(id)) {
					add = false;
				}
			}
			
			if (add) {
				newMap.addEdge(((Edge) e).getNode0().getId(), ((Edge) e).getNode1().getId());
			}
		}
		
		List<String> path = newMap.getShortestPath(idFrom, idTo);
	
		return path;
	*/
		
	/*	List<String> path = new ArrayList<String>();
		List<String> from = new ArrayList<String>();
		String fr = idFrom;
		
		from.add(idFrom);
		
		int compteur = 0;
		
		while (compteur < 5) {
			boolean yes = false;
			
			for (String f : from) {
				yes = true;
				path = this.getShortestPath(f, idTo);
				
				for (String id : nodesId) {
					if (path.contains(id))
						yes = false;
				}
				
				if (yes) {
					path = this.getShortestPath(idFrom, f);
					
					for (String s : this.getShortestPath(f, idTo)) 
						path.add(s);
					
					compteur = 5;
					break;
				}
			}
			
			if (compteur == 4) {
				path = null;
			}
			
			if (!yes) {
				List<String> remNodes = new ArrayList<String>();
				for (String s : from)
					remNodes.add(s);
					
				for (String s : remNodes) 
					from.remove(s);
				
				for (String s : remNodes) {
					for (String ss : this.sendNodeEdges(s).getRight()) {
						boolean add = true;
						
						for (String id : nodesId) {
							if (id.equals(ss)) 
								add = false;
							else if (this.getShortestPath(idFrom, ss).contains(id))
								add = false;
						}
						
						if (add)
							from.add(ss);
					}
				}
				
				compteur += 1;
			}
		}
		
		return path;
	*/
		List<String> remNodes = new ArrayList<String>();
		
		for (String id : nodesId) {
			if (idFrom.equals(id))
				remNodes.add(id);
		}
		
		for (String id : remNodes) {
			nodesId.remove(id);
		}
		
		
		Graph ng = new SingleGraph("New graph");
	
	/*
		for (Object node : this.g.nodes().toArray()) {
			boolean add = true;
			
			for (String id : nodesId) {
				if (((Node) node).getId().equals(id)) {
					add = false;
				}
			}
			
			if (add) {
				ng.addNode(((Node) node).getId());
			}
		}
		
		for (Object edge : this.g.edges().toArray()) {
			boolean add = true;
			
			for (String id : nodesId) {
				if (((Edge) edge).getNode0().getId().equals(id)) {
					add = false;
				}
				else if (((Edge) edge).getNode1().getId().equals(id)) {
					add = false;
				}
			}
			
			if (add) {
				ng.addEdge(((Edge) edge).getId(), ng.getNode(((Edge) edge).getNode0().getId()), ng.getNode(((Edge) edge).getNode1().getId()));
			}
		}
	
	*/
		
		for (Object node : this.g.nodes().toArray()) {
			ng.addNode(((Node) node).getId());
		}
		
		for (Object edge : this.g.edges().toArray()) {
			boolean add = true;
			
			for (String id : nodesId) {
				if (((Edge) edge).getNode0().getId().equals(id)) {
					add = false;
				}
				else if (((Edge) edge).getNode1().getId().equals(id)) {
					add = false;
				}
			}
			
			if (add) {
				ng.addEdge(((Edge) edge).getId(), ng.getNode(((Edge) edge).getNode0().getId()), ng.getNode(((Edge) edge).getNode1().getId()))
				.setAttribute("length", 1);
			}
			
			else {
				ng.addEdge(((Edge) edge).getId(), ng.getNode(((Edge) edge).getNode0().getId()), ng.getNode(((Edge) edge).getNode1().getId()))
				.setAttribute("length", 1000);
			}
		}
		
		ng.nodes().forEach(n -> n.setAttribute("label", n.getId()));
		ng.edges().forEach(e -> e.setAttribute("label", "" + (int) e.getNumber("length")));
		
		List<String> shortestPath=new ArrayList<String>();

		Dijkstra dijkstra = new Dijkstra(Dijkstra.Element.EDGE, null, "length");//length of edges
		dijkstra.init(ng);
		dijkstra.setSource(ng.getNode(idFrom));
		dijkstra.compute();//compute the distance to all nodes from idFrom
		List<Node> path;
		try {
			path=dijkstra.getPath(ng.getNode(idTo)).getNodePath(); //the shortest path from idFrom to idTo
			Iterator<Node> iter=path.iterator();
			while (iter.hasNext()){
				shortestPath.add(iter.next().getId());
			}
			dijkstra.clear();
			if (shortestPath.isEmpty()) {//The openNode is not currently reachable
				return null;
			}else {
				shortestPath.remove(0);//remove the current position
			}
		} catch (NullPointerException exc) {
			for (Object n : ng.nodes().toArray()){
				System.out.println(((Node) n).getId());
				for (Object e : ((Node) n).edges().toArray()) {
					System.out.println(((Edge) e).getId() + " " + ((Edge) e).getNode0().getId() + " " + ((Edge) e).getNode1().getId());
				}
			}
		}
		
		return shortestPath;
		
	}

	public List<String> getAway(String me, String posGolem, List<String> no){
		no.add(posGolem);
		boolean found = false;
		int size = g.nodes().toArray().length;
		List<String> path = new ArrayList<String>();
		
		while (!found) {
			found = true;
			
			Random rand = new Random();
			
			int na = rand.nextInt(size);
			
			path = getShortestPath(me, g.getNode(na).getId());
			
			for (String s : no) {
				if (path.contains(s)) {
					found = false;
				}
				else if (path.size() == 0) {
					found = false;
				}
			}
		}
		
		return path;
	}

	public SerializableSimpleGraph<String, MapAttribute> mergeMaps(SerializableSimpleGraph<String, MapAttribute> sg1, SerializableSimpleGraph<String, MapAttribute> sg2){
		for (SerializableNode<String, MapAttribute> n: sg2.getAllNodes()) {
			if (sg1.getNode(n.getNodeId()) == null) {
				sg1.addNode(n.getNodeId(), n.getNodeContent());
			}
			
			else if ((n.getNodeContent()==MapAttribute.closed) && (sg1.getNode(n.getNodeId()).getNodeContent()==MapAttribute.open)) {
				sg1.getNode(n.getNodeId()).setContent(n.getNodeContent());
			}
		}
		
		// AJOUTER DANS SG1 LES EDGES DE SG2 QUI NE SONT PAS DANS SG1.
		
		return sg1;
	}
	
	public SerializableSimpleGraph<String, MapAttribute> myMapMinusTheirs(Couple<String,SerializableSimpleGraph<String, MapAttribute>> other){
		Graph ng = new SingleGraph("New graph");
		
		// METTRE LES NOEUDS ET EDGES DE NOTRE MAP QUI NE SONT PAS DANS OTHER, DANS NG, PUIS SERIALISER NG ET LE METTRE EN RETOUR.
		
		return other.getRight();
	}


}
