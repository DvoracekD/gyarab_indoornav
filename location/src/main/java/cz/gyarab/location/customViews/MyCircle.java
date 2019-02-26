package cz.gyarab.location.customViews;

import cz.gyarab.location.dijkstra.Vertex;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;

/**
 * Custom circle dolplněný o referenci na vrchol grafu a dvě poslední barvy pro jejich resetování
 */
public class MyCircle extends Circle {

    private Vertex vertex;
    private Color previousColor;
    private Color currentColor;

    public MyCircle(double radius, Paint fill) {
        super(radius, fill);
    }

    public Vertex getVertex() {
        return vertex;
    }

    public void setVertex(Vertex vertex) {
        this.vertex = vertex;
        setColor(Color.RED);
    }

    public void deleteVertex(){
        vertex = null;
        setColor(Color.TRANSPARENT);
    }

    public void setColor(Color color){
        previousColor = currentColor;
        currentColor = color;
        if (color != null)setFill(color);
    }

    public void setPreviousColor(){
        Color temp = previousColor;
        previousColor = currentColor;
        currentColor = temp;
        if (currentColor != null)setFill(currentColor);
    }
}
