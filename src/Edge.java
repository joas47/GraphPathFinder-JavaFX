
import java.io.Serializable;

// *** VT2021, Inlämningsuppgift, del 2
// Grupp 096
// joas47

public class Edge<T> implements Serializable {

    private T destination;
    private int weight;
    private String name;

    public Edge(T destination, int weight, String name) {
        this.destination = destination;
        this.weight = weight;
        this.name = name;
    }

    public T getDestination() {
        return destination;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        if (weight < 0) {
            throw new IllegalArgumentException("Error: Weight can't be negative!");
        }
        this.weight = weight;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "to " + destination + " by " + name + " takes " + weight;
    }
}
