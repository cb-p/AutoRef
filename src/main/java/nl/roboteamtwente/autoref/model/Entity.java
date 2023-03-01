package nl.roboteamtwente.autoref.model;

public abstract class Entity {
    protected Vector3 position = new Vector3(0, 0, 0);
    protected Vector3 velocity = new Vector3(0, 0, 0);

    public Vector3 getPosition() {
        return position;
    }

    public Vector3 getVelocity() {
        return velocity;
    }
}
