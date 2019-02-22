package cz.gyarab.nav.map;

import android.app.Application;
import android.os.AsyncTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Scanner;

import cz.gyarab.nav.dijkstra.Edge;
import cz.gyarab.nav.dijkstra.Graph;
import cz.gyarab.nav.dijkstra.Vertex;

public class GraphLoader extends AsyncTask<String, Void, Graph> {

    private Graph graph;
    private Application app;
    private GraphLoadedListener listener;

    public interface GraphLoadedListener{
        void onGraphLoaded(Graph graph);
    }

    public void setListener(GraphLoadedListener listener) {
        this.listener = listener;
    }

    public GraphLoader(Application app) {
        this.app = app;
        graph = new Graph();
    }

    @Override
    protected Graph doInBackground(String... names) {
        //nahrání json souboru
        String json = "";
        try (InputStream is = app.getAssets().open(names[0])) {
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
                        jsonVertex.getInt("id"),
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

    @Override
    protected void onPostExecute(Graph graph) {
        super.onPostExecute(graph);
        this.graph = graph;
        if (listener != null)
            listener.onGraphLoaded(graph);
    }

    public Graph getGraph() {
        return graph;
    }
}
