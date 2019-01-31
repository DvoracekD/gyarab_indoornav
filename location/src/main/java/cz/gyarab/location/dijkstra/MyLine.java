package cz.gyarab.location.dijkstra;

import javafx.scene.shape.Line;

public class MyLine extends Line {
    private Edge edge;

    public MyLine(double startX, double startY, double endX, double endY) {
        super(startX, startY, endX, endY);
    }

    public Edge getEdge() {
        return edge;
    }

    public void setEdge(Edge edge) {
        this.edge = edge;
    }
}
