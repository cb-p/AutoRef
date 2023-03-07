package nl.roboteamtwente.autoref.model;

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

    public Side getOpposite() {
        return this == LEFT ? RIGHT : LEFT;
    }
}
