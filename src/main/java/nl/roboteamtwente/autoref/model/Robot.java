package nl.roboteamtwente.autoref.model;

public class Robot extends Entity {
    private final int id;
    private float orientation;
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

    public float getOrientation() {
        return orientation;
    }

    public void setOrientation(float orientation) {
        this.orientation = orientation;
    }

    public boolean isGoalkeeper() {
        return this.team.getGoalkeeperId() == this.id;
    }

    @Override
    public String toString() {
        return "Robot{" +
                "id=" + id +
                ", orientation=" + orientation +
                ", position=" + position +
                ", velocity=" + velocity +
                '}';
    }
}
