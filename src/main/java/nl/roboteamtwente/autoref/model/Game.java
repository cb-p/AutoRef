package nl.roboteamtwente.autoref.model;

import java.util.ArrayList;
import java.util.List;

public class Game {
    private final List<Robot> robots;
    private final Ball ball;

    private final Team blue;
    private final Team yellow;

    private final Field field;

    public Game() {
        this.robots = new ArrayList<>();
        this.ball = new Ball();
        this.field = new Field();

        this.blue = new Team(TeamColor.BLUE);
        this.yellow = new Team(TeamColor.YELLOW);
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
}
