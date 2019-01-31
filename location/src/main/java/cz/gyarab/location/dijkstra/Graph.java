package cz.gyarab.location.dijkstra;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * zdroj: http://www.vogella.com/tutorials/JavaAlgorithmsDijkstra/article.html
 */
public class Graph implements Serializable {
    private final List<Vertex> vertexes;
    private final List<Edge> edges;

    public Graph(List<Vertex> vertexes, List<Edge> edges) {
        this.vertexes = vertexes;
        this.edges = edges;
    }

    public Graph() {
        vertexes = new ArrayList<>();
        edges = new ArrayList<>();
    }

    public List<Vertex> getVertexes() {
        return vertexes;
    }

    public List<Edge> getEdges() {
        return edges;
    }
}
