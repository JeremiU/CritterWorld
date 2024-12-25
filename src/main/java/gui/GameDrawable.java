package gui;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public interface GameDrawable {
    void draw(GraphicsContext g);

    Color getColor();
}