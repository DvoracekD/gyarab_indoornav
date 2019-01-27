package cz.gyarab.location;

import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;

public class MyPane extends StackPane {
    public int x;
    public int y;
    boolean selected = false;

    public MyPane(int x, int y) {
        super();
        this.x = x;
        this.y = y;
    }

    public MyPane(int x, int y, Node... children) {
        super(children);
        this.x = x;
        this.y = y;
    }
}
