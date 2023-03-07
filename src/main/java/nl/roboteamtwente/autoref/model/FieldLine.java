package nl.roboteamtwente.autoref.model;


/**
 *
 * @param name is the name of the FieldLine on the field
 * @param p1 is the starting coordinate of a FieldLine
 * @param p2 is the ending coordinate of a FieldLine
 * @param thickness is the thickness of a FieldLine
 */
public record FieldLine(String name, Vector2 p1, Vector2 p2, float thickness) {
}
