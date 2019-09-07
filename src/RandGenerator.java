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
import java.util.logging.Logger;
import java.util.logging.Level;

import org.graphstream.algorithm.generator.Generator;
import org.graphstream.algorithm.generator.RandomGenerator;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.SingleGraph;

public class RandGenerator {

    private static double percentageOfEdgeChanges;
    private static Map<String, List<Integer>> edges_info = new HashMap<>();
    private static final Logger _logger = Logger.getLogger(RandGenerator.class.getName());

    public static void main(String[] args) throws IOException {

        int size = 0, snaps = 0, avgDegree = 0;

        if (args.length == 4) {

            try {
                size = Integer.parseInt(args[0]);
                snaps = Integer.parseInt(args[1]);
                avgDegree = Integer.parseInt(args[2]);
                percentageOfEdgeChanges = Double.parseDouble(args[3]);
            } catch (NumberFormatException e) {
                _logger.log(Level.SEVERE, "Argument" + args[0] + " must be an integer.");
                _logger.log(Level.SEVERE, "Argument" + args[1] + " must be an integer.");
                _logger.log(Level.SEVERE, "Argument" + args[2] + " must be an integer.");
                _logger.log(Level.SEVERE, "Argument" + args[3] + " must be a double.");
                System.exit(1);
            }
        }

        for (int i = 100000; i <= 500000; i += 100000) {
            generate(size, snaps, avgDegree);
        }
    }

    /**
     * Generate graph
     */
    private static void generate(int size, int snapshots, int avgD) throws IOException {

        _logger.log(Level.INFO, "Graph generation started");
        Graph graph = new SingleGraph("Random");
        Generator gen = new RandomGenerator(avgD);
        gen.addSink(graph);
        gen.begin();

        FileWriter w = new FileWriter("graph_" + (size / 1000) + "s_" + snapshots);
        edges_info = new HashMap<>();

        for (int i = 0; i < size; i++)
            gen.nextEvents();

        gen.end();

        Set<String> edgesToBeChanged, allEdges = new HashSet<>();
        List<Integer> list;

        for (Edge e : graph.getEachEdge()) {
            list = new ArrayList<>();
            list.add(0);
            edges_info.put(e.getId(), list);
            allEdges.add(e.getId());
        }

        for (int t = 1; t < snapshots; t++) {
            edgesToBeChanged = getEdgesToBeChanged(graph);
            addEdges(graph, edgesToBeChanged, allEdges);

            for (String e : edgesToBeChanged) {
                graph.removeEdge(e);
                allEdges.remove(e);
            }

            for (Edge e : graph.getEachEdge())
                edges_info.get(e.getId()).add(t);
        }

        String[] token;
        for (Entry<String, List<Integer>> entry : edges_info.entrySet()) {
            token = entry.getKey().split("_");
            w.write(token[0] + " " + token[1] + "\t" + entry.getValue() + "\n");
            w.flush();
        }

        _logger.log(Level.INFO, "#nodes: " + graph.getNodeCount() + "\t#edges: " + edges_info.size());
        w.close();
    }

    /**
     * Add new edges
     */
    private static void addEdges(Graph graph, Set<String> edgesThatChanged, Set<String> allEdges) {

        Random rand = new Random();
        int u, v;
        String e;
        String[] token;
        Set<String> edges = new HashSet<>();

        for (int i = 0; i < edgesThatChanged.size(); i++) {

            u = rand.nextInt(graph.getNodeCount() - 1) + 1;
            v = rand.nextInt(graph.getNodeCount() - 1) + 1;

            if (u == v) {
                i--;
                continue;
            }

            if (u < v)
                e = u + "_" + v;
            else
                e = v + "_" + u;

            if (edges.contains(e) || edgesThatChanged.contains(e) || allEdges.contains(e))
                i--;
            else
                edges.add(e);
        }

        for (String e_ : edges) {
            token = e_.split("_");
            graph.addEdge(e_, token[0], token[1]);
            edges_info.computeIfAbsent(e_, k -> new ArrayList<>());
            allEdges.add(e_);
        }
    }

    /**
     * Return set of edges u_v existing in the graph
     */
    private static Set<String> getEdgesToBeChanged(Graph graph) {

        Random rand = new Random();
        int numOfChanges = (int) (graph.getEdgeCount() * percentageOfEdgeChanges);
        int num;
        Edge e;
        Set<String> e_ = new HashSet<>();

        for (int i = 0; i < numOfChanges; i++) {
            num = rand.nextInt(graph.getEdgeCount() - 1) + 1;
            e = graph.getEdge(num);

            if (e != null && !e_.contains(e.getId())) {
                e_.add(e.getId());
            } else
                i--;
        }

        return e_;
    }
}
