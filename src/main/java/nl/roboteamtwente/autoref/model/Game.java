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
     * The ball placement position when game state is PLACEMENT
     */
    private final Vector2 designated_position;

    /**
     * The game consists of 2 teams blue and yellow
     */
    private final Team blue;
    private final Team yellow;


    /**
     * The game consists of 2 teams playing on a field.
     */
    private final Field field;

    private GameState state;
    private double time;

    private Game previous;

    private List<Kick> kicks;

    public Game() {
        this.robots = new ArrayList<>();
        this.ball = new Ball();
        this.field = new Field();

        this.designated_position = new Vector2(0,0);

        this.blue = new Team(TeamColor.BLUE);
        this.yellow = new Team(TeamColor.YELLOW);

        this.state = GameState.HALT;
        this.time = 0.0;
        this.previous = this;

        this.kicks = new ArrayList<>();
    }

    /**
     * @return the ball object of the game
     */
    public Ball getBall() {
        return ball;
    }

    /**
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
     * @return the ball placement position
     */
    public Vector2 getDesignated_position() {
        return designated_position;
    }

    /**
     * @return the list of robots playing the game.
     */
    public List<Robot> getRobots() {
        return robots;
    }

    /**
     * @param robot is added to the list of robots on the playing field.
     */
    public void addRobot(Robot robot) {
        this.robots.add(robot);
    }

    /**
     * Get the robot corresponding to the identifier.
     *
     * @param identifier the identifier to search.
     * @return the matching robot.
     */
    public Robot getRobot(RobotIdentifier identifier) {
        return this.robots.stream().filter((robot) -> robot.getIdentifier().equals(identifier)).findAny().orElse(null);
    }

    /**
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
     * @return the field the game is played at.
     */
    public Field getField() {
        return field;
    }

    public double getTime() {
        return time;
    }

    public void setTime(double time) {
        this.time = time;
    }

    public GameState getState() {
        return state;
    }

    public void setState(GameState state) {
        this.state = state;
    }

    public void setPrevious(Game previous) {
        this.previous = previous;
    }

    public Game getPrevious() {
        return previous;
    }

    public List<Kick> getKicks() {
        return kicks;
    }

    public Kick getLastKick() {
        return kicks.isEmpty() ? null : kicks.get(kicks.size() - 1);
    }
}
