package nl.roboteamtwente.autoref.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class Field {
    private final Vector2 position;
    private final Vector2 size;

    private float boundaryWidth;

    private final Map<String, FieldLine> lines;
//    private Map<String, FieldArc> arcs;

    public Field() {
        this.position = new Vector2(0, 0);
        this.size = new Vector2(0, 0);
        this.boundaryWidth = 0.0f;

        this.lines = new HashMap<>();
    }

    public Vector2 getPosition() {
        return position;
    }

    public Vector2 getSize() {
        return size;
    }

    public float getBoundaryWidth() {
        return boundaryWidth;
    }

    public void setBoundaryWidth(float boundaryWidth) {
        this.boundaryWidth = boundaryWidth;
    }

    public Collection<FieldLine> getLines() {
        return lines.values();
    }

    public FieldLine getLineByName(String name) {
        return lines.get(name);
    }

    public void addLine(FieldLine line) {
        this.lines.put(line.name(), line);
    }
}
