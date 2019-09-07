import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.graphstream.algorithm.generator.BarabasiAlbertGenerator;
import org.graphstream.algorithm.generator.Generator;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;

public class PrefAttachGenerator2 {

    private static Map<String, List<Integer>> edges_info = new HashMap<>();
    private static final Logger _logger = Logger.getLogger(PrefAttachGenerator2.class.getName());

    public static void main(String[] args) throws IOException {

        for (int i = 200000; i <= 500000; i += 100000) {
            generate(i, 100);
        }

        for (int i = 200; i <= 500; i += 100) {
            generate(100000, i);
        }
    }

    private static void generate(int size, int snapshots) throws IOException {

        _logger.log(Level.INFO, "Graph generation started");

        Graph graph = new SingleGraph("Barabàsi-Albert");
        int newLinks = 5;
        Generator gen = new BarabasiAlbertGenerator(newLinks, false);

        // Generate nodes size of size:
        gen.addSink(graph);
        gen.begin();

        for (int i = 0; i < size; i++) {
            gen.nextEvents();
        }
        gen.end();

        _logger.log(Level.INFO, "#nodes: " + graph.getNodeCount() + "\t#edges: " + graph.getEdgeCount());

        graph.setStrict(false);
        Set<String> edgesToBeChanged, edgesToBeAdded, allEdges = new HashSet<>();
        List<Integer> list;
        List<Node> nodesOrd = new ArrayList<>(graph.getNodeSet());

        for (Edge e : graph.getEachEdge()) {
            list = new ArrayList<>();
            list.add(0);
            edges_info.put(e.getId(), list);
            allEdges.add(e.getId());
        }

        nodesOrd.sort(Comparator.comparingInt(Node::getDegree));

        for (int t = 1; t < snapshots; t++) {

            edgesToBeChanged = getEdgesToBeChanged(graph);
            edgesToBeAdded = addEdges(graph, edgesToBeChanged, allEdges, nodesOrd);

            for (String e : edgesToBeChanged) {
                graph.removeEdge(e);
                allEdges.remove(e);
            }

            for (Edge e : graph.getEachEdge()) {

                if (edgesToBeChanged.contains(e.getSourceNode() + "_" + e.getTargetNode()) || edgesToBeChanged.contains(e.getTargetNode() + "_" + e.getSourceNode()))
                    continue;
                edges_info.get(e.getId()).add(t);
            }

            for (String e : edgesToBeAdded) {
                edges_info.get(e).add(t);
            }

            _logger.log(Level.INFO, (t + 1) + "/" + snapshots);
        }

        String[] token;

        FileWriter w = new FileWriter("graph_" + (size / 1000) + "s_" + snapshots);
        for (Entry<String, List<Integer>> entry : edges_info.entrySet()) {
            token = entry.getKey().split("_");
            w.write(token[0] + " " + token[1] + "\t" + entry.getValue() + "\n");
            w.flush();
        }

        _logger.log(Level.INFO, "#nodes: " + graph.getNodeCount() + "\t#edges: " + edges_info.size());
        w.close();
    }

    private static Set<String> addEdges(Graph graph, Set<String> edgesThatChanged, Set<String> allEdges, List<Node> nodesOrd) {

        Random rand = new Random();
        int u;
        String e, e1;
        String[] token;
        Set<String> edges = new HashSet<>();
        int graphEdges = graph.getEdgeCount();
        double maxProb = (double) nodesOrd.get(nodesOrd.size() - 1).getDegree() / (double) graph.getEdgeCount();

        for (int i = 0; i < edgesThatChanged.size(); i++) {

            u = rand.nextInt(graph.getNodeCount() - 1) + 1;

            double prob = ThreadLocalRandom.current().nextDouble(0, maxProb);

            int deg = (int) (graphEdges * prob);
            boolean added = false;

            for (Node n : nodesOrd) {

                e = u + "_" + n.getId();
                e1 = n.getId() + "_" + u;

                if (n.getDegree() >= deg || n.getId().equals("" + u) || edges.contains(e) || edges.contains(e1)
                        || edgesThatChanged.contains(e) || edgesThatChanged.contains(e1) || allEdges.contains(e)
                        || allEdges.contains(e1))
                    continue;

                edges.add(e);
                added = true;
                break;
            }

            if (!added)
                i--;
        }

        for (String e_ : edges) {
            token = e_.split("_");
            graph.addEdge(e_, token[0], token[1]);
            edges_info.computeIfAbsent(e_, k -> new ArrayList<>());
            allEdges.add(e_);
        }

        return edges;
    }

    private static Set<String> getEdgesToBeChanged(Graph graph) {

        Random rand = new Random();

        double percentEdgeChange = 0.10;
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