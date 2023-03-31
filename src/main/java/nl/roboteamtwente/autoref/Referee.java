package nl.roboteamtwente.autoref;

import nl.roboteamtwente.autoref.model.Game;
import nl.roboteamtwente.autoref.validators.*;

import java.util.ArrayList;
import java.util.List;

public class Referee {
    private static final List<RuleValidator> NON_STOPPING_RULE_VALIDATORS = List.of(
        // Non-stopping rules
        new BotCrashingValidator(),
        new BotKickedBallTooFastValidator(),
        new AttackerTouchedBallInDefenseAreaValidator()
    );

    private static final PossibleGoalValidator POSSIBLE_GOAL_VALIDATOR = new PossibleGoalValidator();
    private static final List<RuleValidator> OTHER_RULE_VALIDATORS = List.of(
            new BotInterferedPlacementValidator(),
            new BotTooFastInStopValidator(),
//            new PossibleGoalValidator(),
            new BallLeftFieldTouchLineValidator(),
            new BallLeftFieldGoalLineValidator(),
            new DefenderInDefenseAreaValidator(),
            new AttackerDoubleTouchedBallValidator(),
            new AimlessKickValidator(),
            new BotDribbledBallTooFarValidator(),
            new PlacementSucceededValidator(),
            new BoundaryCrossingValidator()
    );

    private List<RuleValidator> activeValidators;

    private List<RuleValidator> activeNonStoppingFouls;
    private Game game;

    public Game getGame() {
        return game;
    }

    public void setGame(Game game) {
        this.game = game;
    }

    public List<RuleViolation> validate() {
        if (activeValidators == null || game.getState() != game.getPrevious().getState()) {
            List<RuleValidator> validators = OTHER_RULE_VALIDATORS.stream().filter((validator) -> validator.isActive(game)).toList();

            List<RuleValidator> toReset = new ArrayList<>(validators);
            if (activeValidators != null) {
                toReset.removeAll(activeValidators);
            }

            toReset.forEach(validator -> validator.reset(game));
            activeValidators = validators;
        }

        if (activeNonStoppingFouls == null || game.getState() != game.getPrevious().getState()) {
            List<RuleValidator> validators = NON_STOPPING_RULE_VALIDATORS.stream().filter((validator) -> validator.isActive(game)).toList();

            List<RuleValidator> toReset = new ArrayList<>(validators);
            if (activeNonStoppingFouls != null) {
                toReset.removeAll(activeNonStoppingFouls);
            }

            toReset.forEach(validator -> validator.reset(game));
            activeNonStoppingFouls = validators;
        }

        List<RuleViolation> violations = new ArrayList<>();

        // First loop through all non-stopping validator
        for (RuleValidator nonStoppingValidator : activeNonStoppingFouls) {
            try {
                RuleViolation violation = nonStoppingValidator.validate(game);
                if (violation != null) {
                    // Check for non-stopping fouls
                    POSSIBLE_GOAL_VALIDATOR.setLastNonStoppingFoul(game.getTime());
                    violations.add(violation);
                }
            } catch (Exception e) {
                e.printStackTrace();
                System.err.println("!! " + nonStoppingValidator.getClass().getSimpleName() + " will now be deactivated.");
                activeNonStoppingFouls = new ArrayList<>(activeNonStoppingFouls);
                activeNonStoppingFouls.remove(nonStoppingValidator);
            }
        }

        // Then check possible goal
        if (POSSIBLE_GOAL_VALIDATOR.isActive(game)) {
            try {

                RuleViolation violation = POSSIBLE_GOAL_VALIDATOR.validate(game);
                if (violation != null) {
                    // Check for non-stopping fouls
                    violations.add(violation);
                }
            } catch (Exception e) {
                e.printStackTrace();
                System.err.println("!! Possible goal check error - Referee.java.");
            }
        }

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
