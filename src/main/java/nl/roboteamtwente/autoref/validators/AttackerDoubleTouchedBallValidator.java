package nl.roboteamtwente.autoref.validators;

import nl.roboteamtwente.autoref.RuleValidator;
import nl.roboteamtwente.autoref.RuleViolation;
import nl.roboteamtwente.autoref.model.*;
import org.robocup.ssl.proto.SslGcCommon;
import org.robocup.ssl.proto.SslGcGameEvent;
import org.robocup.ssl.proto.SslGcGeometry;

import java.util.EnumSet;

public class AttackerDoubleTouchedBallValidator implements RuleValidator {
    private boolean triggered = false;

    @Override
    public RuleViolation validate(Game game) {
        Touch kickIntoPlay = game.getKickIntoPlay();
        if (kickIntoPlay == null) {
            return null;
        }

        if (!kickIntoPlay.equals(game.getLastStartedTouch())) {
            return null;
        }

        Robot robot = game.getRobot(kickIntoPlay.by());
        Touch currentTouch = robot.getTouch();

        // Ball should move 0.05 meters before "in play", then another 0.05 meters before it's a violation.
        float distance = kickIntoPlay.isFinished() ? 0.05f : 0.10f;

        if (!triggered && currentTouch != null && game.getBall().getPosition().distance(kickIntoPlay.startLocation()) >= distance) {
            triggered = true;
            return new Violation(robot.getIdentifier(), game.getKickIntoPlay().startLocation().xy());
        }

        return null;
    }

    @Override
    public EnumSet<GameState> activeStates() {
        return EnumSet.of(GameState.INDIRECT_FREE, GameState.DIRECT_FREE, GameState.RUNNING);
    }

    @Override
    public void reset(Game game) {
        triggered = false;
    }

    record Violation(RobotIdentifier by, Vector2 location) implements RuleViolation {
        @Override
        public String toString() {
            return "Attacker double touched ball (by: " + by.teamColor() + " #" + by.id() + ", at " + location + ")";
        }

        @Override
        public SslGcGameEvent.GameEvent toPacket() {
            return SslGcGameEvent.GameEvent.newBuilder()
                    .setType(SslGcGameEvent.GameEvent.Type.ATTACKER_DOUBLE_TOUCHED_BALL)
                    .setAttackerDoubleTouchedBall(SslGcGameEvent.GameEvent.AttackerDoubleTouchedBall.newBuilder()
                            .setByTeam(by.teamColor() == TeamColor.BLUE ? SslGcCommon.Team.BLUE : SslGcCommon.Team.YELLOW)
                            .setByBot(by.id())
                            .setLocation(SslGcGeometry.Vector2.newBuilder().setX(location.getX()).setY(location.getY())))
                    .build();
        }
    }
}
