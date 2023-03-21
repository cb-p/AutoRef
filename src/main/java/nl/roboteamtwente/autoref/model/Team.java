package nl.roboteamtwente.autoref.model;

import java.util.*;

/**
 * This class represents the Team object a robot can belong to.
 */
public class Team {
    /**
     * A team has a TeamColor object which is an enumeration that has 2 values: YELLOW and BLUE.
     */
    private final TeamColor color;

    /**
     * A map that keeps track of which robots belong to a tean by mapping robot ID to a robot object.
     */
    private final Map<Integer, Robot> robots;

    /**
     * A team must have a distinct goalkeeper value, this is represented by the goalkeeper variable.
     */
    private int goalkeeper;

    /**
     * The variable side keeps track of which side a certain team is on.
     */
    private Side side;


    /**
     * Variable to keep track of the radius of the robots.
     */
    private float robotRadius;

    private float robotHeight;

    /**
     * This is the constructor of the Team, a team is constructed with a color and also
     * a map with all robots in that are part of this team object.
     * @param color is the color that the team is defined with.
     */
    public Team(TeamColor color) {
        this.color = color;
        this.robots = new HashMap<>();
    }

    /**
     *
     * @return the TeamColor object of the team.
     */

    public TeamColor getColor() {
        return color;
    }


    /**
     *
     * @return a collection of all robots that are part of this team object.
     */
    public Collection<Robot> getRobots() {
        return robots.values();
    }

    /**
     * A function that adds a robot to the collection of robots in this team object.
     * @param robot is the robot to add.
     */

    public void addRobot(Robot robot) {
        this.robots.put(robot.getId(), robot);
        robot.setTeam(this);
    }

    /**
     * This method returns a Robot object by passing the robot ID to get as parameter.
     * @param id, the id of the robot that is to be retrieved.
     * @return the robot object
     */
    public Robot getRobotById(int id) {
        return this.robots.get(id);
    }

    /**
     * Sets the goalkeeper ID.
     * @param id the id to set to the goalkeeper.
     */
    public void setGoalkeeperId(int id) {
        this.goalkeeper = id;
    }

    /**
     *
     * @return the ID of the goalkeeper.
     */
    public int getGoalkeeperId() {
        return this.goalkeeper;
    }

    /**
     * Function to get the goalkeeper which is a Robot object.
     * @return the robot object of the goalkeeper.
     */
    public Robot getGoalkeeper() {
        return getRobotById(this.goalkeeper);
    }

    /**
     *
     * @return the robot radius
     */
    public float getRobotRadius() {
        return robotRadius;
    }

    /**
     *Sets the robot radius
     * @param robotRadius to set.
     */
    public void setRobotRadius(float robotRadius) {
        this.robotRadius = robotRadius;
    }


    public float getRobotHeight() {
        return robotHeight;
    }

    public void setRobotHeight(float robotHeight) {
        this.robotHeight = robotHeight;
    }

    /**
     * A setter to set the side a Team is on.
     * @param side, the side that will be set to the team.
     */
    public void setSide(Side side) {
        this.side = side;
    }

    /**
     *
     * @return the side of the team.
     */
    public Side getSide() {
        return side;
    }

    /**
     *
     * @return a string value of the team object
     */
    @Override
    public String toString() {
        return "Team{" +
                "color=" + color +
                ", robots=" + robots +
                ", goalkeeper=" + goalkeeper +
                '}';
    }
}
