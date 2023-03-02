package nl.roboteamtwente.autoref.model;

import java.util.*;

public class Team {
    private final TeamColor color;
    private final Map<Integer, Robot> robots;
    private int goalkeeper;
    private Side side;

    private float robotRadius;

    public Team(TeamColor color) {
        this.color = color;
        this.robots = new HashMap<>();
    }

    public TeamColor getColor() {
        return color;
    }

    public Collection<Robot> getRobots() {
        return robots.values();
    }

    public void addRobot(Robot robot) {
        this.robots.put(robot.getId(), robot);
        robot.setTeam(this);
    }

    public Robot getRobotById(int id) {
        return this.robots.get(id);
    }

    public void setGoalkeeperId(int id) {
        this.goalkeeper = id;
    }

    public int getGoalkeeperId() {
        return this.goalkeeper;
    }

    public Robot getGoalkeeper() {
        return getRobotById(this.goalkeeper);
    }

    public float getRobotRadius() {
        return robotRadius;
    }

    public void setRobotRadius(float robotRadius) {
        this.robotRadius = robotRadius;
    }

    public void setSide(Side side) {
        this.side = side;
    }

    public Side getSide() {
        return side;
    }

    @Override
    public String toString() {
        return "Team{" +
                "color=" + color +
                ", robots=" + robots +
                ", goalkeeper=" + goalkeeper +
                '}';
    }
}
