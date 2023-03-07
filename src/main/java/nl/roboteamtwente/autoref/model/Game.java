package nl.roboteamtwente.autoref.model;

import java.util.ArrayList;
import java.util.List;

public class Game {
    private final List<Robot> robots;
    private final Ball ball;

    private final Team blue;
    private final Team yellow;

    private final Field field;

    private GameState state;
    private double time;

    private Game previous;

    public Game() {
        this.robots = new ArrayList<>();
        this.ball = new Ball();
        this.field = new Field();

        this.blue = new Team(TeamColor.BLUE);
        this.yellow = new Team(TeamColor.YELLOW);

        this.state = GameState.HALT;
        this.time = 0.0;
        this.previous = this;
    }

    public Ball getBall() {
        return ball;
    }

    public Team getTeam(TeamColor color) {
        if (color == TeamColor.BLUE) {
            return blue;
        } else {
            return yellow;
        }
    }

    public List<Robot> getRobots() {
        return robots;
    }

    public void addRobot(Robot robot) {
        this.robots.add(robot);
    }

    @Override
    public String toString() {
        return "Game{" +
                "robots=" + robots +
                ", ball=" + ball +
                ", blue=" + blue +
                ", yellow=" + yellow +
                '}';
    }

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
}
