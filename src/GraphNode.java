
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

// *** VT2021, Inlämningsuppgift, del 2
// Grupp 096
// joas47

public class GraphNode extends Circle {

    private static final int NODE_SIZE = 12;
    private final String name;

    public GraphNode(double x, double y, String name) {
        super(x, y, NODE_SIZE);
        this.name = name;
        setFill(Color.BLUE);
    }

    public void toggleMarked() {
        if (getFill().equals(Color.BLUE)) {
            setFill(Color.RED);
        } else {
            setFill(Color.BLUE);
        }
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name;

    }
}
