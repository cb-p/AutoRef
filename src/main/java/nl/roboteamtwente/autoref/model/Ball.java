package nl.roboteamtwente.autoref.model;

import java.util.ArrayList;
import java.util.List;

/**
 * The ball class represents a ball object in a RoboCup game. It extends
 * the Entity class, which the common properties of a physical object in
 * this game.
 */
public class Ball extends Entity {

    /**
     * List or robots currently touching the ball
     */
    private final List<Robot> robotsTouching = new ArrayList<>();

    /**
     * A Vector3 variable to store the position of where
     * the ball was last touched.
     */
    private Vector3 lastTouchedAt;

    /**
     * Variable that stores the robot which last touched the ball
     */
    private Robot lastTouchedBy;


    /**
     *
     * @return lastTouchedAt which is the position of where the ball was last touched at.
     */
    public Vector3 getLastTouchedAt() {
        return lastTouchedAt;
    }

    /**
     *
     * @return lastTouchedby which is the robot who last touched the ball.
     */
    public Robot getLastTouchedBy() {
        return lastTouchedBy;
    }

    /**
     * @param lastTouchedAt is the variable to set where the ball was last touched at.
     * Returns nothing since this method only sets the position of where
     * the ball was last touched at.
     */
    public void setLastTouchedAt(Vector3 lastTouchedAt) {
        this.lastTouchedAt = lastTouchedAt;
    }

    /**
     * @param lastTouchedBy is the Robot to set as the new robot who last touched the ball.
     * Returns nothing since this method only sets the robot that
     * the touched the ball last.
     */
    public void setLastTouchedBy(Robot lastTouchedBy) {
        this.lastTouchedBy = lastTouchedBy;
    }

    /**
     *
     * @return a list of robots which are currently touching the ball.
     */
    public List<Robot> getRobotsTouching() {
        return robotsTouching;
    }


    /**
     *
     * @return string value of the ball object.
     */
    @Override
    public String toString() {
        return "Ball{" +
                "position=" + position +
                ", velocity=" + velocity +
                '}';
    }
}
