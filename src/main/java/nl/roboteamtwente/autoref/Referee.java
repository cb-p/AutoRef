package nl.roboteamtwente.autoref;

import nl.roboteamtwente.autoref.model.Game;
import nl.roboteamtwente.autoref.validators.AttackerTouchedBallInDefenseAreaValidator;
import nl.roboteamtwente.autoref.validators.BallLeftFieldTouchLineValidator;

import java.util.ArrayList;
import java.util.List;

public class Referee {
    private static final List<RuleValidator> RULE_VALIDATORS = List.of(
            new AttackerTouchedBallInDefenseAreaValidator(),
            new BallLeftFieldTouchLineValidator()
    );

    private final Game game;

    public Referee() {
        this.game = new Game();
    }

    public Game getGame() {
        return game;
    }

    public List<RuleViolation> validate() {
        List<RuleViolation> violations = new ArrayList<>();

        for (RuleValidator validator : RULE_VALIDATORS) {
            RuleViolation violation = validator.validate(game);
            if (violation != null) {
                violations.add(violation);
            }
        }

        return violations;
    }
}
