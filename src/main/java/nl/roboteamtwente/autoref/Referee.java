package nl.roboteamtwente.autoref;

import nl.roboteamtwente.autoref.model.Game;
import nl.roboteamtwente.autoref.validators.*;

import java.util.ArrayList;
import java.util.List;

public class Referee {
    private static final List<RuleValidator> RULE_VALIDATORS = List.of(
            new BotCrashingValidator(),
            new BotInterferedPlacementValidator(),
            new BotTooFastInStopValidator(),
            new AttackerTouchedBallInDefenseAreaValidator(),
            new BallLeftFieldTouchLineValidator(),
            new BallLeftFieldGoalLineValidator(),
            new BotKickedBallTooFastValidator(),
            new DefenderInDefenseAreaValidator(),
            new AttackerDoubleTouchedBallValidator(),
            new AimlessKickValidator()
    );

    private List<RuleValidator> activeValidators;

    private Game game;

    public Game getGame() {
        return game;
    }

    public void setGame(Game game) {
        this.game = game;
    }

    public List<RuleViolation> validate() {
        if (activeValidators == null || game.getState() != game.getPrevious().getState()) {
            List<RuleValidator> validators = RULE_VALIDATORS.stream().filter((validator) -> validator.isActive(game)).toList();

            List<RuleValidator> toReset = new ArrayList<>(validators);
            if (activeValidators != null) {
                toReset.removeAll(activeValidators);
            }

            toReset.forEach(validator -> validator.reset(game));
            activeValidators = validators;
        }

        List<RuleViolation> violations = new ArrayList<>();
        for (RuleValidator validator : activeValidators) {
            try {
                RuleViolation violation = validator.validate(game);

                if (violation != null) {
                    violations.add(violation);
                }
            } catch (Exception e) {
                e.printStackTrace();

                System.err.println("!! " + validator.getClass().getSimpleName() + " will now be deactivated.");
                activeValidators = new ArrayList<>(activeValidators);
                activeValidators.remove(validator);
            }
        }
        return violations;
    }
}
