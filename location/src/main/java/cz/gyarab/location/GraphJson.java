package cz.gyarab.location;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Scanner;

import cz.gyarab.location.dijkstra.Edge;
import cz.gyarab.location.dijkstra.Graph;
import cz.gyarab.location.dijkstra.Vertex;

public class GraphJson {

    private static final String FILE_NAME = "map_graph.json";

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

        //slovník jmen
        JSONObject jsonNames = new JSONObject();
        for (int i = 0; i < vertices.length; i++){
            if (!vertices[i].getName().equals("")){
                jsonNames.put(vertices[i].getName(), i);
            }
        }

        jsonGraph.put("vertices", jsonVertexes);
        jsonGraph.put("edges", jsonEdges);
        jsonGraph.put("subVertexes", jsonSubVertexes);
        jsonGraph.put("names", jsonNames);

        System.out.println(jsonGraph.toString());
        saveToFile(jsonGraph);
    }

    private static void saveToFile(JSONObject graph){
        try (PrintWriter out = new PrintWriter(FILE_NAME)) {
            out.println(graph);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static Graph readJson(){
        Graph graph = new Graph();
        //nahrání json souboru
        String json = "";
        try (InputStream is = new FileInputStream(FILE_NAME)) {
            json = new Scanner(is).useDelimiter("\\A").next();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //vytvoření objektu
        try {
            JSONObject jsonGraph = new JSONObject(json);
            JSONArray jsonVertices = jsonGraph.getJSONArray("vertices");
            Vertex[] vertices = new Vertex[jsonVertices.length()];
            for (int i = 0; i < jsonVertices.length(); i++) {
                JSONObject jsonVertex = jsonVertices.getJSONObject(i);
                vertices[i] = new Vertex(
                        jsonVertex.getInt("x"),
                        jsonVertex.getInt("y"),
                        jsonVertex.getString("name"));
                graph.getVertexes().add(vertices[i]);
            }
            //nahrání hran
            JSONArray jsonEdges = jsonGraph.getJSONArray("edges");
            for (int i = 0; i < jsonEdges.length(); i++) {
                JSONObject jsonEdge = jsonEdges.getJSONObject(i);
                graph.getEdges().add(new Edge(
                        vertices[jsonEdge.getInt("source")],
                        vertices[jsonEdge.getInt("destination")],
                        jsonEdge.getDouble("weight")));
            }
            //subVertexes
            JSONObject jsonSubVertexes = jsonGraph.getJSONObject("subVertexes");
            Iterator<String> iterator = jsonSubVertexes.keys();
            while (iterator.hasNext()){
                String key = iterator.next();
                ArrayList<Vertex> adjVert = new ArrayList<>();
                for (int i = 0; i < jsonSubVertexes.getJSONArray(key).length(); i++) {
                    adjVert.add(vertices[jsonSubVertexes.getJSONArray(key).getInt(i)]);
                }
                graph.getSubVertexes().put(key, adjVert);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return graph;
    }

}
