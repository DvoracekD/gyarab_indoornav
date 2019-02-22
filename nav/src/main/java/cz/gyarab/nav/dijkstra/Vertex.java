package cz.gyarab.nav.dijkstra;

import java.io.Serializable;

/**
 * zdroj: http://www.vogella.com/tutorials/JavaAlgorithmsDijkstra/article.html
 */
public class Vertex implements Serializable {
    final private int id;
    final private int x;
    final private int y;
    final private String name;

    public Vertex(int x, int y, String name) {
        id = -1;
        this.x = x;
        this.y = y;
        this.name = name;
    }

    public Vertex(int id, int x, int y, String name) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + x + y;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Vertex other = (Vertex) obj;
        if (x == other.x && y == other.y)
            return true;
        else return false;
    }

    @Override
    public String toString() {
        return x + ", " + y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getId() {
        return id;
    }
}
