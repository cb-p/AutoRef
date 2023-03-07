package nl.roboteamtwente.autoref.model;

public class Robot extends Entity {
    private final int id;
    private float angle;
    private Team team;

    private boolean touchingBall = false;
    private boolean justTouchedBall = false;

    public Robot(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public Team getTeam() {
        return team;
    }

    public void setTeam(Team team) {
        this.team = team;
    }

    public float getAngle() {
        return angle;
    }

    public void setAngle(float angle) {
        this.angle = angle;
    }

    public boolean isGoalkeeper() {
        return this.team.getGoalkeeperId() == this.id;
    }

    public boolean isTouchingBall() {
        return touchingBall;
    }

    public void setTouchingBall(boolean touchingBall) {
        this.touchingBall = touchingBall;
    }

    public boolean hasJustTouchedBall() {
        return justTouchedBall;
    }

    public void setJustTouchedBall(boolean justTouchedBall) {
        this.justTouchedBall = justTouchedBall;
    }

    public RobotIdentifier getIdentifier() {
        return new RobotIdentifier(team.getColor(), id);
    }

    @Override
    public String toString() {
        return "Robot{" +
                "id=" + id +
                ", orientation=" + angle +
                ", position=" + position +
                ", velocity=" + velocity +
                '}';
    }
}
