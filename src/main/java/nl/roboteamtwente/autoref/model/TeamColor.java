package nl.roboteamtwente.autoref.model;


/**
 * An enumeration to define the color of the teams of the robots.
 */
public enum TeamColor {
    BLUE,
    YELLOW;

    /**
     * @return the opponents color based on the color of the current object.
     */
    public TeamColor getOpponentColor() {
        return this == BLUE ? YELLOW : BLUE;
    }
}
