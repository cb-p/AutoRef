package nl.roboteamtwente.autoref.model;

public enum TeamColor {
    BLUE,
    YELLOW;

    public TeamColor getOpponentColor() {
        return this == BLUE ? YELLOW : BLUE;
    }
}
