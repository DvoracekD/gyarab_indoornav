package cz.gyarab.location.dijkstra;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * zdroj: http://www.vogella.com/tutorials/JavaAlgorithmsDijkstra/article.html
 */
public class Graph implements Serializable {
    private final List<Vertex> vertexes;
    private final List<Edge> edges;
    private final HashMap<String, ArrayList<Vertex>> subVertexes;

    public Graph(List<Vertex> vertexes, List<Edge> edges, HashMap<String, ArrayList<Vertex>> subVertexes) {
        this.vertexes = vertexes;
        this.edges = edges;
        this.subVertexes = subVertexes;
    }

    public Graph() {
        vertexes = new ArrayList<>();
        edges = new ArrayList<>();
        subVertexes = new HashMap<>();
    }

    public List<Vertex> getVertexes() {
        return vertexes;
    }

    public List<Edge> getEdges() {
        return edges;
    }

    public HashMap<String, ArrayList<Vertex>> getSubVertexes() {
        return subVertexes;
    }
}
