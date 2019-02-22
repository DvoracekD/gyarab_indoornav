package cz.gyarab.location;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import cz.gyarab.location.dijkstra.Edge;
import cz.gyarab.location.dijkstra.Graph;
import cz.gyarab.location.dijkstra.Vertex;

public class GraphJson {

    public static void saveJson(Graph graph){

        //objekt grafu
        JSONObject jsonGraph = new JSONObject();

        //pole vrcholů
        JSONArray jsonVertexes = new JSONArray();
        Vertex[] vertices = new Vertex[graph.getVertexes().size()];

        for (int i = 0; i < graph.getVertexes().size(); i++){
            Vertex vertex = graph.getVertexes().get(i);

            //vrchol grafu
            JSONObject jsonVertex = new JSONObject();
            jsonVertex.put("x", vertex.getX());
            jsonVertex.put("y", vertex.getY());
            jsonVertex.put("name", vertex.getName());
            jsonVertex.put("id", i);

            vertices[i] = vertex;
            jsonVertexes.put(jsonVertex);
        }

        //pole hran s referencí na vrcholy
        JSONArray jsonEdges = new JSONArray();

        for (Edge edge : graph.getEdges()){

            JSONObject jsonEdge = new JSONObject();

            for (int i = 0; i < vertices.length; i++){
                Vertex vertex = vertices[i];

                //1. vrchol hrany
                if (vertex.getX() == edge.getSource().getX()
                    && vertex.getY() == edge.getSource().getY()){
                    jsonEdge.put("source", i);
                } else
                //2.vrchol hrany
                if (vertex.getX() == edge.getDestination().getX()
                        && vertex.getY() == edge.getDestination().getY()){
                    jsonEdge.put("destination", i);
                }

            }
            jsonEdge.put("weight", edge.getWeight());

            jsonEdges.put(jsonEdge);
        }

        //subVertexes
        JSONObject jsonSubVertexes = new JSONObject();

        for (HashMap.Entry<String, ArrayList<Vertex>> sub : graph.getSubVertexes().entrySet()){

            //pole přidružených vrcholů
            JSONArray jsonAdjVertices = new JSONArray();
            for (Vertex adjVertex : sub.getValue()){

                //zjištění id daného přidruženého vrcholu
                for (int i = 0; i < vertices.length; i++) {
                    Vertex v = vertices[i];

                    if (v.getX() == adjVertex.getX()
                            && v.getY() == adjVertex.getY()){
                        jsonAdjVertices.put(i);
                    }
                }

            }

            jsonSubVertexes.put(sub.getKey(), jsonAdjVertices);

        }

        jsonGraph.put("vertices", jsonVertexes);
        jsonGraph.put("edges", jsonEdges);
        jsonGraph.put("subVertexes", jsonSubVertexes);

        System.out.println(jsonGraph.toString());
    }

}
