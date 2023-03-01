package nl.roboteamtwente.autoref.ui;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import nl.roboteamtwente.autoref.model.Vector2;

public class ScaledDrawer {
    private final GraphicsContext g;
    private final Vector2 center;
    private final float scale;

    public ScaledDrawer(GraphicsContext g, Vector2 center, float scale) {
        this.g = g;
        this.center = center;
        this.scale = scale;
    }

    public void drawLine(Vector2 p1, Vector2 p2, float thickness, Color color) {
        g.setStroke(color);
        g.setLineWidth(thickness);
        g.strokeLine(
                translateX(p1.getX()), translateY(p1.getY()),
                translateX(p2.getX()), translateY(p2.getY())
        );
    }

    public void drawCircle(Vector2 center, float radius, Color color) {
        g.setFill(color);
        g.fillOval(
                translateX(center.getX() - radius / 2), translateY(center.getY() - radius / 2),
                radius * scale, radius * scale
        );
    }

    private float translateX(float x) {
        return (float) ((x - center.getX()) * scale + g.getCanvas().getWidth() / 2);
    }

    private float translateY(float y) {
        return (float) ((y - center.getY()) * scale + g.getCanvas().getHeight() / 2);
    }
}
