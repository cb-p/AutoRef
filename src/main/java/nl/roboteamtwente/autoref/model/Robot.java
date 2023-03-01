package nl.roboteamtwente.autoref.model;

public class Robot extends Entity {
    private final int id;
    private float angle;
    private Team team;

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
