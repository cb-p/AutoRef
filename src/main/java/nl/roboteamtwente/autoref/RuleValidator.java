package nl.roboteamtwente.autoref;

import nl.roboteamtwente.autoref.model.Game;

public interface RuleValidator {
    RuleViolation validate(Game game);

    boolean isActive(Game game);

    default void reset(Game game) {
    }
}
