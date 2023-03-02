package nl.roboteamtwente.autoref;

import nl.roboteamtwente.autoref.model.Game;

public interface RuleValidator {
    RuleViolation validate(Game game);
}
