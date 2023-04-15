package nl.roboteamtwente.autoref;

import nl.roboteamtwente.autoref.model.Game;
import nl.roboteamtwente.autoref.validators.*;

import java.util.ArrayList;
import java.util.List;

public class Referee {
    private static final List<RuleValidator> RULE_VALIDATORS = List.of(
            new AimlessKickValidator(),
            new AttackerDoubleTouchedBallValidator(),
            new AttackerTooCloseToDefenseAreaValidator(),
            new AttackerTouchedBallInDefenseAreaValidator(),
            new BallLeftFieldGoalLineValidator(),
            new BallLeftFieldTouchLineValidator(),
            new BotCrashingValidator(),
            new BotDribbledBallTooFarValidator(),
            new BotInterferedPlacementValidator(),
            new BotKickedBallTooFastValidator(),
            new BotTooFastInStopValidator(),
            new BoundaryCrossingValidator(),
            new DefenderInDefenseAreaValidator(),
            new DefenderTooCloseToKickPointValidator(),
            new PenaltyKickFailedValidator(),
            new PlacementSucceededValidator(),
            new PossibleGoalValidator()
    );

    private List<RuleValidator> activeValidators = new ArrayList<>();
    private final List<RuleValidator> disabledValidators = new ArrayList<>();

    private Game game;

    public Game getGame() {
        return game;
    }

    public void setGame(Game game) {
        this.game = game;
    }

    public List<RuleViolation> validate() {
        List<RuleValidator> validators = RULE_VALIDATORS.stream().filter((validator) -> validator.isActive(game)).toList();
        disabledValidators.retainAll(validators);

        validators = new ArrayList<>(validators);
        validators.removeAll(disabledValidators);

        List<RuleValidator> toReset = new ArrayList<>(validators);
        toReset.removeAll(activeValidators);

        for (RuleValidator validator : toReset) {
            System.out.println("reset " + validator.getClass().getSimpleName());
            validator.reset(game);
        }

        activeValidators = validators;

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
                disabledValidators.add(validator);
            }
        }
        return violations;
    }
}
