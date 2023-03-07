package nl.roboteamtwente.autoref.model;


/**
 * An enumeration of the half that a team is in. It is either left or right and has a
 * cardinality of -1 and +1 respectively.
 */
public enum Side {
    LEFT(-1),
    RIGHT(+1);

    private final int cardinality;

    Side(int cardinality) {
        this.cardinality = cardinality;
    }

    public int getCardinality() {
        return cardinality;
    }
}
