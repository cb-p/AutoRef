package nl.roboteamtwente.autoref.model;


/**
 * This class represents the physical objects of the game.
 */
public abstract class Entity {

    /**
     * The position variable stores the position of the Entity object.
     */
    protected Vector3 position = new Vector3(0, 0, 0);

    /**
     * The position variable stores the velocity of the Entity object.
     */
    protected Vector3 velocity = new Vector3(0, 0, 0);

    /**
     *
     * @return the position of the physical Entity
     */
    public Vector3 getPosition() {
        return position;
    }

    /**
     *
     * @return the velocity of the physical Entity
     */
    public Vector3 getVelocity() {
        return velocity;
    }
}
