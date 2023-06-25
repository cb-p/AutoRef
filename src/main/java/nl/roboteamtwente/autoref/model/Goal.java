package nl.roboteamtwente.autoref.model;

/**
 * A class which is used to create the Goal object of the game.
 */
public class Goal {

    private float width;
    private float depth;

    public Goal() {
        this.width = 0.0f;
        this.depth = 0.0f;
    }

    public float getWidth() {
        return width;
    }

    public void setWidth(float width) {
        this.width = width;
    }

    public float getDepth() {
        return depth;
    }

    public void setDepth(float depth) {
        this.depth = depth;
    }
}
