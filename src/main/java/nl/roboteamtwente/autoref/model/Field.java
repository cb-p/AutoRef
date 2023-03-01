package nl.roboteamtwente.autoref.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class Field {
    private final Vector2 position;
    private final Vector2 size;

    private final Map<String, FieldLine> lines;
//    private Map<String, FieldArc> arcs;

    public Field() {
        this.position = new Vector2(0, 0);
        this.size = new Vector2(0, 0);

        this.lines = new HashMap<>();
    }

    public Vector2 getPosition() {
        return position;
    }

    public Vector2 getSize() {
        return size;
    }

    public Collection<FieldLine> getLines() {
        return lines.values();
    }

    public void addLine(FieldLine line) {
        this.lines.put(line.name(), line);
    }
}
