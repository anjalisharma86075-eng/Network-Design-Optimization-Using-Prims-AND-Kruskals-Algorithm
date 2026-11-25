import java.util.*;
 
/**
 * Network Design Optimization
 * Minimum-cost communication network using Prim's and Kruskal's algorithms.
 *
 * Locations = vertices, cable links = edges, cable cost = edge weight.
 * Goal: connect all locations with minimum total cost (Minimum Spanning Tree).
 */
public class NetworkDesign {
 
    // ---------------- Edge (a possible cable link) ----------------
    static class Edge implements Comparable<Edge> {
        int src, dest, cost;
 
        Edge(int src, int dest, int cost) {
            this.src = src;
            this.dest = dest;
            this.cost = cost;
        }
 
        @Override
        public int compareTo(Edge other) {
            return Integer.compare(this.cost, other.cost);
        }
    }
 
    // ---------------- Union-Find (Disjoint Set) for Kruskal ----------------
    static class DisjointSet {
        int[] parent, rank;
 
        DisjointSet(int n) {
            parent = new int[n];
            rank = new int[n];
            for (int i = 0; i < n; i++) parent[i] = i;
        }
 
        int find(int x) {                       // with path compression
            if (parent[x] != x) parent[x] = find(parent[x]);
            return parent[x];
        }
 
        boolean union(int a, int b) {           // union by rank
            int ra = find(a), rb = find(b);
            if (ra == rb) return false;         // same set -> would form a cycle
            if (rank[ra] < rank[rb]) parent[ra] = rb;
            else if (rank[ra] > rank[rb]) parent[rb] = ra;
            else { parent[rb] = ra; rank[ra]++; }
            return true;
        }
    }
 
    // ---------------- Kruskal's Algorithm : O(E log E) ----------------
    static List<Edge> kruskal(int V, List<Edge> edges) {
        List<Edge> sorted = new ArrayList<>(edges);
        Collections.sort(sorted);               // step 1: sort edges by cost
        DisjointSet ds = new DisjointSet(V);
        List<Edge> mst = new ArrayList<>();
 
        for (Edge e : sorted) {                 // step 2: pick cheapest safe edge
            if (ds.union(e.src, e.dest)) {
                mst.add(e);
                if (mst.size() == V - 1) break; // MST complete
            }
        }
        return mst;
    }
 
    // ---------------- Prim's Algorithm : O(E log V) ----------------
    static List<Edge> prim(int V, List<Edge> edges) {
        // build adjacency list
        List<List<Edge>> adj = new ArrayList<>();
        for (int i = 0; i < V; i++) adj.add(new ArrayList<>());
        for (Edge e : edges) {
            adj.get(e.src).add(e);
            adj.get(e.dest).add(new Edge(e.dest, e.src, e.cost));
        }
 
        boolean[] inTree = new boolean[V];
        PriorityQueue<Edge> pq = new PriorityQueue<>();
        List<Edge> mst = new ArrayList<>();
 
        inTree[0] = true;                       // start from location 0
        pq.addAll(adj.get(0));
 
        while (!pq.isEmpty() && mst.size() < V - 1) {
            Edge e = pq.poll();                 // cheapest edge leaving the tree
            if (inTree[e.dest]) continue;       // skip: would form a cycle
            inTree[e.dest] = true;
            mst.add(e);
            for (Edge next : adj.get(e.dest)) {
                if (!inTree[next.dest]) pq.add(next);
            }
        }
        return mst;
    }
 
    // ---------------- Helpers ----------------
    static long totalCost(List<Edge> mst) {
        long sum = 0;
        for (Edge e : mst) sum += e.cost;
        return sum;
    }
 
    static void printNetwork(String title, List<Edge> mst, String[] names, int V) {
        System.out.println("\n--- " + title + " ---");
        if (mst.size() < V - 1) {
            System.out.println("Network cannot connect all locations (graph is disconnected).");
            return;
        }
        for (Edge e : mst) {
            System.out.printf("  %-12s <--> %-12s  cost = %d%n", names[e.src], names[e.dest], e.cost);
        }
        System.out.println("  Total minimum cost = " + totalCost(mst));
    }
 
    // Random connected graph for performance testing
    static List<Edge> randomConnectedGraph(int V, int E, Random rnd) {
        List<Edge> edges = new ArrayList<>();
        Set<Long> used = new HashSet<>();
        for (int i = 1; i < V; i++) {           // guarantee connectivity
            int j = rnd.nextInt(i);
            edges.add(new Edge(i, j, 1 + rnd.nextInt(1000)));
            used.add((long) Math.min(i, j) * V + Math.max(i, j));
        }
        while (edges.size() < E) {
            int a = rnd.nextInt(V), b = rnd.nextInt(V);
            if (a == b) continue;
            long key = (long) Math.min(a, b) * V + Math.max(a, b);
            if (used.add(key)) edges.add(new Edge(a, b, 1 + rnd.nextInt(1000)));
        }
        return edges;
    }
 
    // ---------------- Menu options ----------------
    static void runSample() {
        String[] names = {"Delhi", "Jaipur", "Lucknow", "Chandigarh", "Agra", "Dehradun", "Kanpur"};
        int V = names.length;
        List<Edge> edges = new ArrayList<>();
        edges.add(new Edge(0, 1, 28));  // Delhi - Jaipur
        edges.add(new Edge(0, 3, 25));  // Delhi - Chandigarh
        edges.add(new Edge(0, 4, 23));  // Delhi - Agra
        edges.add(new Edge(0, 5, 26));  // Delhi - Dehradun
        edges.add(new Edge(1, 4, 24));  // Jaipur - Agra
        edges.add(new Edge(2, 4, 33));  // Lucknow - Agra
        edges.add(new Edge(2, 6, 9));   // Lucknow - Kanpur
        edges.add(new Edge(3, 5, 17));  // Chandigarh - Dehradun
        edges.add(new Edge(4, 6, 28));  // Agra - Kanpur
        edges.add(new Edge(0, 2, 55));  // Delhi - Lucknow
        edges.add(new Edge(1, 3, 52));  // Jaipur - Chandigarh
 
        System.out.println("\nSample network: " + V + " locations, " + edges.size() + " possible links");
        printNetwork("Kruskal's Algorithm", kruskal(V, edges), names, V);
        printNetwork("Prim's Algorithm", prim(V, edges), names, V);
    }
 
    static void runUserInput(Scanner sc) {
        System.out.print("Number of locations: ");
        int V = sc.nextInt();
        String[] names = new String[V];
        for (int i = 0; i < V; i++) {
            System.out.print("Name of location " + i + ": ");
            names[i] = sc.next();
        }
        System.out.print("Number of possible links: ");
        int E = sc.nextInt();
        List<Edge> edges = new ArrayList<>();
        System.out.println("Enter each link as: from_index to_index cost");
        for (int i = 0; i < E; i++) {
            int a = sc.nextInt(), b = sc.nextInt(), c = sc.nextInt();
            if (a < 0 || a >= V || b < 0 || b >= V || c < 0) {
                System.out.println("Invalid link, skipped.");
                continue;
            }
            edges.add(new Edge(a, b, c));
        }
        printNetwork("Kruskal's Algorithm", kruskal(V, edges), names, V);
        printNetwork("Prim's Algorithm", prim(V, edges), names, V);
    }
 
    static void runPerformance() {
        System.out.println("\nPerformance comparison (time in milliseconds, average of 5 runs)");
        System.out.printf("%-8s %-10s %-9s %-12s %-12s %-10s%n",
                "V", "E", "Type", "Kruskal(ms)", "Prim(ms)", "SameCost?");
        Random rnd = new Random(42);
        int[] sizes = {100, 500, 1000, 2000};
        for (int V : sizes) {
            for (String type : new String[]{"Sparse", "Dense"}) {
                int E = type.equals("Sparse") ? 3 * V : V * (V - 1) / 4;
                List<Edge> edges = randomConnectedGraph(V, E, rnd);
                kruskal(V, edges); prim(V, edges);          // JVM warm-up
 
                long tk = 0, tp = 0, ck = 0, cp = 0;
                for (int r = 0; r < 5; r++) {
                    long s = System.nanoTime();
                    ck = totalCost(kruskal(V, edges));
                    tk += System.nanoTime() - s;
                    s = System.nanoTime();
                    cp = totalCost(prim(V, edges));
                    tp += System.nanoTime() - s;
                }
                System.out.printf("%-8d %-10d %-9s %-12.3f %-12.3f %-10s%n",
                        V, E, type, tk / 5e6, tp / 5e6, ck == cp ? "Yes" : "NO");
            }
        }
    }
 
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        while (true) {
            System.out.println("\n===== Network Design Optimization (MST) =====");
            System.out.println("1. Run sample network");
            System.out.println("2. Enter your own network");
            System.out.println("3. Performance comparison (Prim vs Kruskal)");
            System.out.println("4. Exit");
            System.out.print("Choice: ");
            if (!sc.hasNextInt()) break;
            int ch = sc.nextInt();
            switch (ch) {
                case 1: runSample(); break;
                case 2: runUserInput(sc); break;
                case 3: runPerformance(); break;
                case 4: System.out.println("Goodbye!"); return;
                default: System.out.println("Invalid choice.");
            }
        }
    }
}// 