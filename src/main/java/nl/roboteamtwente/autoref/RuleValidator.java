package nl.roboteamtwente.autoref;

import nl.roboteamtwente.autoref.model.Game;
import nl.roboteamtwente.autoref.model.GameState;

import java.util.EnumSet;

public interface RuleValidator {
    RuleViolation validate(Game game);

    EnumSet<GameState> activeStates();

    default void reset(Game game) {
    }
}
