package nl.roboteamtwente.autoref.model;

import java.util.ArrayList;
import java.util.List;


/**
 * This is the game object which controls different aspects of a RoboCup game.
 */
public class Game {

    /**
     * List of robot objects that are in the game and are part of the playing field.
     */
    private final List<Robot> robots;

    /**
     * The game keeps track of the ball object at all times
     */
    private final Ball ball;

    /**
     * The game consists of 2 teams blue and yellow
     */
    private final Team blue;
    private final Team yellow;


    /**
     * The game consists of 2 teams playing on a field.
     */
    private final Field field;

    public Game() {
        this.robots = new ArrayList<>();
        this.ball = new Ball();
        this.field = new Field();

        this.blue = new Team(TeamColor.BLUE);
        this.yellow = new Team(TeamColor.YELLOW);
    }

    /**
     *
     * @return the ball object of the game
     */
    public Ball getBall() {
        return ball;
    }

    /**
     *
     * @param color is an object TeamColor which we want the Team for
     * @return the Team object (blue || yellow) based on the color given to the method.
     */
    public Team getTeam(TeamColor color) {
        if (color == TeamColor.BLUE) {
            return blue;
        } else {
            return yellow;
        }
    }

    /**
     *
     * @return the list of robots playing the game.
     */
    public List<Robot> getRobots() {
        return robots;
    }

    /**
     *
     * @param robot is added to the list of robots on the playing field.
     */
    public void addRobot(Robot robot) {
        this.robots.add(robot);
    }

    /**
     *
     * @return a string value for the game objects with all robot objects, ball and the teams.
     */
    @Override
    public String toString() {
        return "Game{" +
                "robots=" + robots +
                ", ball=" + ball +
                ", blue=" + blue +
                ", yellow=" + yellow +
                '}';
    }

    /**
     *
     * @return the field the game is played at.
     */
    public Field getField() {
        return field;
    }
}
