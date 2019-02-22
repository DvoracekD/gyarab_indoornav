package cz.gyarab.nav.dijkstra;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    public Vertex getVertex(int x, int y){
        // pokud jde o subVertex
        ArrayList<Vertex> adjVertices = subVertexes.get(x+","+y);
        if (adjVertices != null){
            Vertex newVertex = new Vertex(x, y, "added");
            vertexes.add(newVertex);
            for (Vertex adjVertex : adjVertices)
                edges.add(new Edge(newVertex, adjVertex));
            return newVertex;
        }

        //pokud jde vrchol grafu hlavních cest
        for (Vertex vertex : vertexes){
            if (vertex.getX() == x && vertex.getY() == y)
                return vertex;
        }

        //pokud se nejedná o vrchol uvnitř budovy
        return null;
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
