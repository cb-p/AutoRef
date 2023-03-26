package nl.roboteamtwente.autoref.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;


/**
 * A class which is used to create the Field object of the game
 */
public class Field {
    private final Vector2 position;
    private final Vector2 size;

    private float boundaryWidth;

    private final Map<String, FieldLine> lines;
//    private Map<String, FieldArc> arcs;

    /**
     * The constructor of the Field class which is made up of
     */
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

    public boolean isInDefenseArea(Side side, Vector2 location) {
        // FIXME: This doesn't work for non-straight lines

        String sideString = side == Side.LEFT ? "Left" : "Right";

        FieldLine penaltyStretch = getLineByName(sideString + "PenaltyStretch");
        if (location.getX() * side.getCardinality() < penaltyStretch.p1().getX() * side.getCardinality()) {
            return false;
        }

        FieldLine rightPenaltyStretch = getLineByName(sideString + "FieldRightPenaltyStretch");
        FieldLine leftPenaltyStretch = getLineByName(sideString + "FieldLeftPenaltyStretch");

        FieldLine topPenaltyStretch = rightPenaltyStretch.p1().getY() > leftPenaltyStretch.p1().getY() ? rightPenaltyStretch : leftPenaltyStretch;
        FieldLine bottomPenaltyStretch = topPenaltyStretch == rightPenaltyStretch ? leftPenaltyStretch : rightPenaltyStretch;

        return (location.getY() > bottomPenaltyStretch.p1().getY() && location.getY() < topPenaltyStretch.p1().getY());

    }

    public boolean isInOwnHalf(Side side, Vector2 location){
        FieldLine halfway = getLineByName("HalfwayLine");
//        if (location.getX()  * side.getCardinality() > halfway.p1().getX() * side.getCardinality()){
//            return true;
//        }

        return side == Side.LEFT && location.getX() < halfway.p1().getX() || side == Side.RIGHT && location.getX() > halfway.p1().getX();
    }

    public String getDivision(Game game){
        FieldLine left = getLineByName("LeftGoalLine");
        FieldLine right = getLineByName("RightGoalLine");
        if (right.p1().getX() - left.p1().getX() >= 8.1 && right.p1().getX() - left.p1().getX() <= 9.9 && game.getRobots().size() == 12){
            return "B";
        }
        return "A";
    }
}
