package cz.gyarab.location;

import cz.gyarab.location.dijkstra.Vertex;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;

public class MyCircle extends Circle {

    private Vertex vertex;

    public MyCircle(double radius, Paint fill) {
        super(radius, fill);
    }

    public Vertex getVertex() {
        return vertex;
    }

    public void setVertex(Vertex vertex) {
        this.vertex = vertex;
        setFill(Color.RED);
    }

    public void deleteVertex(){
        vertex = null;
        setFill(Color.TRANSPARENT);
    }
}
