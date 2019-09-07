import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Generate labels per time instant for given dataset
 *
 * @author ksemer
 */
public class DynamicLabelGenerator {
    // input graph
    private static String dataset = "graph_400s_100";

    private static Map<Integer, List<Integer>> nodes;
    private static final Logger _logger = Logger.getLogger(DynamicLabelGenerator.class.getName());

    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(dataset));
        String line;
        int n1, n2;
        nodes = new TreeMap<>();

        br.readLine();
        while ((line = br.readLine()) != null) {
            String[] token = line.split("\\s+");
            n1 = Integer.parseInt(token[0]);
            n2 = Integer.parseInt(token[1]);

            if (!nodes.containsKey(n1))
                nodes.put(n1, new ArrayList<>());

            if (!nodes.containsKey(n2))
                nodes.put(n2, new ArrayList<>());
        }
        br.close();
        _logger.log(Level.INFO, "Total nodes: " + nodes.size());

        int sizeOfLabels = 5;
        createDataset(sizeOfLabels);
    }

    /**
     * Create sizeOflabels for the dataset interval
     */
    private static void createDataset(int sizeOflabels) throws IOException {
        _logger.log(Level.INFO, "Running size of labels: " + sizeOflabels);
        String labels_output = "_label_";
        FileWriter w = new FileWriter(dataset + labels_output + sizeOflabels);
        Zipf zipf = new Zipf(sizeOflabels, 1);
        int[] numberOfnodes = new int[sizeOflabels + 1];

        // keeps all nodes
        List<Integer> arr = new ArrayList<>(nodes.keySet());

        // how many nodes should have this attribute
        for (int i = 1; i <= sizeOflabels; i++)
            numberOfnodes[i] = (int) (zipf.getProbability(i) * nodes.size());

        // since we call it many times
        for (int key : nodes.keySet())
            nodes.get(key).clear();

        // times that a label will change
        int times = 1;
        for (int j = 0; j < times; j++) {

            // shuffle nodes
            Collections.shuffle(arr);

            int attr = 1, counter = 0;
            for (int i = 0; i < arr.size(); i++) {

                // get node arr.get(i) and add attribute attr
                nodes.get(arr.get(i)).add(attr);

                if (i == (numberOfnodes[attr] + counter - 1)) {
                    counter += numberOfnodes[attr];
                    attr++;
                }
            }
        }

        for (Entry<Integer, List<Integer>> entry : nodes.entrySet()) {
            w.write(entry.getKey() + "\t");
            for (int k = 0; k < entry.getValue().size() - 1; k++)
                w.write(entry.getValue().get(k) + ",");
            w.write(entry.getValue().get(entry.getValue().size() - 1) + "\n");
        }

        w.close();
    }

    static class Zipf {
        private double skew;
        private double bottom = 0;

        Zipf(int size, double skew) {
            this.skew = skew;

            for (int i = 1; i < size; i++) {
                this.bottom += (1 / Math.pow(i, this.skew));
            }
        }

        // This method returns a probability that the given rank occurs.
        double getProbability(int rank) {
            return (1.0d / Math.pow(rank, this.skew)) / this.bottom;
        }
    }
}