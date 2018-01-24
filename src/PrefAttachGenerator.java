import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

import org.graphstream.algorithm.generator.BarabasiAlbertGenerator;
import org.graphstream.algorithm.generator.Generator;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;

public class PrefAttachGenerator {

	private static double percentageOfEdgeChanges;
	private static Map<String, List<Integer>> edges_info = new HashMap<>();

	public static void main(String[] args) throws IOException {

		int size = 0, snaps = 0, newLinks = 0;

		if (args.length == 4) {

			try {
				size = Integer.parseInt(args[0]);
				snaps = Integer.parseInt(args[1]);
				newLinks = Integer.parseInt(args[2]);
				percentageOfEdgeChanges = Double.parseDouble(args[3]);
			} catch (NumberFormatException e) {
				System.err.println("Argument" + args[0] + " must be an integer.");
				System.err.println("Argument" + args[1] + " must be an integer.");
				System.err.println("Argument" + args[2] + " must be an integer.");
				System.err.println("Argument" + args[3] + " must be a double.");
				System.exit(1);
			}
		}

		generate(size, snaps, newLinks);

	}

    /**
	 * Generate graph
	 * 
	 * @param size
	 * @param snapshots
	 * @param avgD
	 * @throws IOException
	 */
	private static void generate(int size, int snapshots, int newLinks) throws IOException {

		System.out.println("Graph generation started");

		edges_info = new HashMap<>();

		Graph graph = new SingleGraph("Barabàsi-Albert");
		Generator gen = new BarabasiAlbertGenerator(newLinks, false);

		// Generate nodes size of size:
		gen.addSink(graph);
		gen.begin();

		for (int i = 0; i < size; i++) {
			gen.nextEvents();
		}
		gen.end();
		
		System.out.println("#nodes: " + graph.getNodeCount() + "\t#edges: " + graph.getEdgeCount());


		graph.setStrict(false);
		Set<String> edgesToBeChanged, allEdges = new HashSet<>();
		List<Integer> list;

		List<Node> nodesOrd = new ArrayList<>(graph.getNodeSet());

		for (Edge e : graph.getEachEdge()) {
			list = new ArrayList<>();
			list.add(0);
			edges_info.put(e.getId(), list);
			allEdges.add(e.getId());
		}

		for (int t = 1; t < snapshots; t++) {

		//	nodesOrd.sort((Node n1, Node n2) -> Integer.compare(n1.getDegree(), n2.getDegree()));

			edgesToBeChanged = getEdgesToBeChanged(graph);
			addEdges(graph, edgesToBeChanged, allEdges);
	//		addEdges(graph, edgesToBeChanged, allEdges, nodesOrd);

			for (String e : edgesToBeChanged) {
				graph.removeEdge(e);
				allEdges.remove(e);
			}

			for (Edge e : graph.getEachEdge())
				edges_info.get(e.getId()).add(t);			
		}

		String[] token;
		
		FileWriter w = new FileWriter("graph_" + (size / 1000) + "s_" + snapshots);


		for (Entry<String, List<Integer>> entry : edges_info.entrySet()) {
			token = entry.getKey().split("_");
			w.write(token[0] + " " + token[1] + "\t" + entry.getValue() + "\n");
			w.flush();
		}

		System.out.println("#nodes: " + graph.getNodeCount() + "\t#edges: " + edges_info.size());

		w.close();
	}

	/**
	 * Add new edges
	 * 
	 * @param graph
	 * @param edgesThatChanged
	 * @param allEdges
	 */	
	private static void addEdges(Graph graph, Set<String> edgesThatChanged, Set<String> allEdges) {

		Random rand = new Random();
		int u, v;
		String e, e1;
		String[] token;
		Set<String> edges = new HashSet<>();

		for (int i = 0; i < edgesThatChanged.size(); i++) {

			u = rand.nextInt(graph.getNodeCount() - 1) + 1;
			v = rand.nextInt(graph.getNodeCount() - 1) + 1;

			if (u == v) {
				i--;
				continue;
			}
			
			e = u + "_" + v;
			e1 = v + "_" + u;

			if (edges.contains(e) || edges.contains(e1)
					|| edgesThatChanged.contains(e) || edgesThatChanged.contains(e1) || allEdges.contains(e)
					|| allEdges.contains(e1))
				i--;
			else
				edges.add(e);

//			if (u < v)
//				e = u + "_" + v;
//			else
//				e = v + "_" + u;
//
//			if (edges.contains(e) || edgesThatChanged.contains(e) || allEdges.contains(e))
//				i--;
//			else
//				edges.add(e);
		}

		for (String e_ : edges) {
			token = e_.split("_");
			graph.addEdge(e_, token[0], token[1]);

			if (edges_info.get(e_) == null)
				edges_info.put(e_, new ArrayList<>());

			allEdges.add(e_);
		}
	}

	
	// private static void addEdges1(Graph graph, Set<String> edgesThatChanged, Set<String> allEdges, List<Node> nodesOrd) {

		// Random rand = new Random();
		// int u;
		// String e, e1;
		// String[] token;
		// Set<String> edges = new HashSet<>();

		// int graphEdges = graph.getEdgeCount();
		// double maxProb = (double) nodesOrd.get(nodesOrd.size() - 1).getDegree() / (double) graph.getEdgeCount();

		// for (int i = 0; i < edgesThatChanged.size(); i++) {

			// u = rand.nextInt(graph.getNodeCount() - 1) + 1;
			
			// double prob = ThreadLocalRandom.current().nextDouble(0,maxProb);

			// int deg = (int) (graphEdges * prob);
			// boolean added = false;

			// for (Node n : nodesOrd) {

				// e = u + "_" + n.getId();
				// e1 = n.getId() + "_" + u;

				// if (n.getDegree() >= deg || n.getId().equals("" + u) || edges.contains(e) || edges.contains(e1)
						// || edgesThatChanged.contains(e) || edgesThatChanged.contains(e1) || allEdges.contains(e)
						// || allEdges.contains(e1))
					// continue;

				// edges.add(e);
				// added = true;
				// break;
			// }

			// if (!added)
				// i--;
		// }

		// for (String e_ : edges) {
			// token = e_.split("_");
			// graph.addEdge(e_, token[0], token[1]);

			// if (edges_info.get(e_) == null)
				// edges_info.put(e_, new ArrayList<>());

			// allEdges.add(e_);
		// }
	// }

	/**
	 * Return set of edges u_v existing in the graph
	 * 
	 * @param graph
	 * @return
	 */
	private static Set<String> getEdgesToBeChanged(Graph graph) {

		Random rand = new Random();

		int numOfChanges = (int) (graph.getEdgeCount() * percentEdgeChange);
		int num;
		Edge e;
		Set<String> e_ = new HashSet<>();

		for (int i = 0; i < numOfChanges; i++) {
			num = rand.nextInt(graph.getEdgeCount() - 1) + 1;
			e = graph.getEdge(num);

			if (e != null && !e_.contains(e.getId()))
				e_.add(e.getId());
			else
				i--;
		}

		return e_;
	}
}