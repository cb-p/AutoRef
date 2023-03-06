package nl.roboteamtwente.autoref.model;

import java.util.ArrayList;
import java.util.List;

public class Ball extends Entity {
    private final List<Robot> robotsTouching = new ArrayList<>();
    private Vector3 lastTouchedAt;
    private Robot lastTouchedBy;

    public Vector3 getLastTouchedAt() {
        return lastTouchedAt;
    }

    public Robot getLastTouchedBy() {
        return lastTouchedBy;
    }

    public void setLastTouchedAt(Vector3 lastTouchedAt) {
        this.lastTouchedAt = lastTouchedAt;
    }

    public void setLastTouchedBy(Robot lastTouchedBy) {
        this.lastTouchedBy = lastTouchedBy;
    }

    public List<Robot> getRobotsTouching() {
        return robotsTouching;
    }

    @Override
    public String toString() {
        return "Ball{" +
                "position=" + position +
                ", velocity=" + velocity +
                '}';
    }
}
