
import java.io.Serializable;
import java.util.*;

// *** VT2021, Inlämningsuppgift, del 2
// Grupp 096
// joas47

public class ListGraph<T> implements Graph<T>, Serializable {

    private Map<T, Set<Edge<T>>> nodes = new HashMap<>();

    @Override
    public void add(T t) {
        // Add node to graph.
        // If already there, do nothing.
        nodes.putIfAbsent(t, new HashSet<>());
    }

    @Override
    public void remove(T t) {
        // Remove node from graph.
        // Remove node's edges.
        // Remove edges from connecting nodes.
        // If node is missing, throw NoSuchElementException
        Set<Edge<T>> edges = getEdgesFrom(t);
        ArrayList<Edge<T>> toBeRemoved = new ArrayList<>(edges);
        for (Edge<T> edge : toBeRemoved) {
            disconnect(t, edge.getDestination());
        }
        nodes.remove(t);
    }

    @Override
    public void connect(T t1, T t2, String name, int weight) {
        // Connect two nodes by adding edges on both nodes.
        // If either node is missing, throw NoSuchElementException
        // If weight < 0, throw IllegalArgumentException
        // If edge already exists, throw IllegalStateException.
        // (Högst en förbindelse mellan två noder).
        if (nodes.containsKey(t1) && nodes.containsKey(t2)) {
            if (weight >= 0) {
                if (!t1.equals(t2)) {
                    connectHelper(t2, t1, name, weight);
                }
                connectHelper(t1, t2, name, weight);
            } else {
                throw new IllegalArgumentException("Weight can't be negative");
            }
        } else {
            throw new NoSuchElementException("Nodes doesn't exist");
        }
    }

    private void connectHelper(T t1, T t2, String name, int weight) {
        Set<Edge<T>> set = nodes.get(t2);
        Edge<T> edge = new Edge<>(t1, weight, name);
        boolean found = false;
        for (Edge<T> e : set) {
            if (e.getDestination().equals(edge.getDestination())) {
                found = true;
                break;
            }
        }
        if (!found) {
            set.add(edge);
        } else {
            throw new IllegalStateException("Edge between " + t1 + " and "
                    + t2 + " already exists");
        }
    }

    @Override
    public void disconnect(T from, T to) {
        // Remove edges connecting nodes from both sides.
        // If either node is missing, throw NoSuchElementException
        // If there's no edges connecting nodes, throw IllegalStateException (use helper method getEdgeBetween())
        Edge<T> edge1 = getEdgeBetween(from, to);
        Edge<T> edge2 = getEdgeBetween(to, from);
        if (edge1 != null && edge2 != null) {
            removeEdge(from, edge1);
            removeEdge(to, edge2);
        } else {
            throw new IllegalStateException("No edges between " + from + " and " + to);
        }
    }

    private void removeEdge(T from, Edge<T> edge1) {
        Set<Edge<T>> edgesFrom = nodes.get(from);
        edgesFrom.removeIf(edge -> edge.equals(edge1));
    }

    @Override
    public void setConnectionWeight(T t1, T t2, int weight) {
        // Sets the edges connecting nodes to new weight.
        // If either node is missing, or if there's no edges between nodes, throw NoSuchElementException
        // If weight < 0, throw IllegalArgumentException
        Edge<T> e1 = getEdgeBetween(t1, t2);
        Edge<T> e2 = getEdgeBetween(t2, t1);
        if (e1 != null && e2 != null) {
            e1.setWeight(weight);
            e2.setWeight(weight);
        } else {
            throw new NoSuchElementException("No edges between nodes");
        }
    }

    @Override
    public Set<T> getNodes() {
        // Return a COPY of set of all nodes.
        //return nodes.keySet();
        return Set.copyOf(nodes.keySet());
    }

    @Override
    public Set<Edge<T>> getEdgesFrom(T t) {
        // Return a COPY of collection of all edges from this node.
        // If node is missing, throw NoSuchElementException
        Set<Edge<T>> set = nodes.get(t);
        if (set != null) {
            return Set.copyOf(set);
        } else {
            throw new NoSuchElementException("Node missing: " + t);
        }
    }

    @Override
    public Edge<T> getEdgeBetween(T from, T to) {
        // Return edge between nodes.
        // If either node is missing, throw NoSuchElementException
        // If no edge between nodes, RETURN NULL
        Set<Edge<T>> set1 = getEdgesFrom(from);
        // Don't remove. throws necessary exception.
        Set<Edge<T>> set2 = getEdgesFrom(to);
        for (Edge<T> edge : set1) {
            if (edge.getDestination().equals(to)) {
                return edge;
            }
        }
        return null;
    }

    @Override
    public boolean pathExists(T from, T to) {
        // use helper method depthFirstSearch()
        // If path exists between nodes (don't have to be direct), return true
        // else return false
        // if either node is missing, RETURN FALSE
        if (nodes.containsKey(from) && nodes.containsKey(to)) {
            Set<T> visited = new HashSet<>();
            depthFirstSearch(from, visited);
            return visited.contains(to);
        } else {
            return false;
        }
    }

    private void depthFirstSearch(T where, Set<T> visited) {
        visited.add(where);
        for (Edge<T> e : nodes.get(where)) {
            if (!visited.contains(e.getDestination())) {
                depthFirstSearch(e.getDestination(), visited);
            }
        }
    }

    private void depthFirstSearch(T where, T whereFrom, Set<T> visited, Map<T, T> via) {
        visited.add(where);
        via.put(where, whereFrom);
        for (Edge<T> e : nodes.get(where)) {
            if (!visited.contains(e.getDestination())) {
                depthFirstSearch(e.getDestination(), where, visited, via);
            }
        }
    }

    private List<Edge<T>> gatherPath(T from, T to, Map<T, T> via) {
        List<Edge<T>> path = new ArrayList<>();
        T where = to;
        while (!where.equals(from)) {
            T node = via.get(where);
            Edge<T> e = getEdgeBetween(node, where);
            path.add(e);
            where = node;
        }
        Collections.reverse(path);
        return path;
    }

    private List<Edge<T>> getAnyPath(T from, T to) {
        Set<T> visited = new HashSet<>();
        Map<T, T> via = new HashMap<>();
        depthFirstSearch(from, null, visited, via);
        // Could be LinkedList
        if (!visited.contains(to)) {
            return null;
        }
        return gatherPath(from, to, via);
    }

    private List<Edge<T>> getShortestPath(T from, T to) {
        LinkedList<T> queue = new LinkedList<>();
        Set<T> visited = new HashSet<>();
        Map<T, T> via = new HashMap<>();
        visited.add(from);
        queue.addLast(from);
        while (!queue.isEmpty()) {
            T node = queue.pollFirst();
            for (Edge<T> e : nodes.get(node)) {
                T dest = e.getDestination();
                if (!visited.contains(dest)) {
                    visited.add(dest);
                    queue.add(dest);
                    via.put(dest, node);
                }
            }
        }
        if (!visited.contains(to)) {
            return null;
        }
        return gatherPath(from, to, via);
    }

    // dijkstra's algorithm
    // TODO: (Optional) implement this
    private List<Edge<T>> getQuickestPath(T from, T to) {
        return null;
    }

    @Override
    public List<Edge<T>> getPath(T from, T to) {
        // Return list of edges representing the
        // path between these nodes through the graph.
        // EASY: return any path.
        // MEDIUM: return shortest path (number of nodes passed)
        // HARD: return fastest path (least weight)
        // If there's no path between nodes, RETURN NULL.
        return getShortestPath(from, to);
    }

    @Override
    public String toString() {
        // returnerar en lång sträng med strängar tagna från nodernas
        // toString-metoder och kanternas toString-metoder, gärna med
        // radbrytningar så att man får information
        // om en nod per rad för förbättrad läsbarhet.
        StringBuilder stringBuilder = new StringBuilder();
        for (T t : nodes.keySet()) {
            stringBuilder.append(t).append(": ");
            for (Edge<T> e : nodes.get(t)) {
                stringBuilder.append(e.toString()).append(" ");
            }
            stringBuilder.append("\n");
        }
        return stringBuilder.toString();
    }
}
