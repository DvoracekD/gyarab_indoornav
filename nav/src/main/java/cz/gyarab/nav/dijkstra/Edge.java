package cz.gyarab.nav.dijkstra;

import java.io.Serializable;

/**
 * zdroj: http://www.vogella.com/tutorials/JavaAlgorithmsDijkstra/article.html
 */
public class Edge implements Serializable {
    private final Vertex source;
    private final Vertex destination;
    private final double weight;
    //2,5 metru mezi kontrolními body
    private static final double DISTANCE_BETWEEN_POINTS = 2.5;

    public Edge(Vertex source, Vertex destination, double weight) {
        this.source = source;
        this.destination = destination;
        this.weight = weight;
    }

    public Edge(Vertex source, Vertex destination) {
        this.source = source;
        this.destination = destination;
        weight = computeDistance(source, destination);
    }

    /**
     * spočítá skutečnou vzdálenost mezi dvěma uzly
     * @param node1
     * @param node2
     * @return
     */
    private double computeDistance(Vertex node1, Vertex node2){
        return Math.sqrt(Math.pow(node1.getX()-node2.getX(), 2) + Math.pow(node1.getY()-node2.getY(), 2))*DISTANCE_BETWEEN_POINTS;
    }

    public Vertex getDestination() {
        return destination;
    }

    public Vertex getSource() {
        return source;
    }
    public double getWeight() {
        return weight;
    }

    @Override
    public String toString() {
        return source + " -> " + destination;
    }


}