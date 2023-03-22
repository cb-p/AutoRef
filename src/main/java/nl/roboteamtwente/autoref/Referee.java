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
//            new BallLeftFieldTouchLineValidator(),
//            new BallLeftFieldGoalLineValidator(),
            new DefenderInDefenseAreaValidator(),
            new AttackerDoubleTouchedBallValidator()
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
            System.out.println("game state: " + game.getPrevious().getState() + " -> " + game.getState());

            game.setTimeLastGameStateChange(game.getTime());

            List<RuleValidator> allValidators = RULE_VALIDATORS.stream().toList();

            List<RuleValidator> toReset = new ArrayList<>(allValidators);
//            if (activeValidators != null) {
//                toReset.removeAll(activeValidators);
//            }

            toReset.forEach(validator -> validator.reset(game));
            activeValidators = RULE_VALIDATORS.stream().filter((validator) -> validator.activeStates().contains(game.getState())).toList();
        } else {
            game.setTimeLastGameStateChange(game.getPrevious().getTimeLastGameStateChange());
        }

        List<RuleViolation> violations = new ArrayList<>();
        for (RuleValidator validator : activeValidators) {
            RuleViolation violation = validator.validate(game);

            if (violation != null) {
                violations.add(violation);
            }
        }
        return violations;
    }
}
