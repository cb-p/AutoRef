package nl.roboteamtwente.autoref.validators;

import nl.roboteamtwente.autoref.RuleValidator;
import nl.roboteamtwente.autoref.RuleViolation;
import nl.roboteamtwente.autoref.model.*;
import org.robocup.ssl.proto.SslGcCommon;
import org.robocup.ssl.proto.SslGcGameEvent;
import org.robocup.ssl.proto.SslGcGeometry;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

public class AttackerTouchedBallInDefenseAreaValidator implements RuleValidator {
    private static final double GRACE_PERIOD = 2.0;

    private final Map<RobotIdentifier, Double> lastViolations = new HashMap<>();

    @Override
    public RuleViolation validate(Game game) {
        for (Robot robot : game.getBall().getRobotsTouching()) {
            if (!game.getField().isInDefenseArea(robot.getTeam().getSide().getOpposite(), robot.getPosition().xy())) {
                continue;
            }

            if (!lastViolations.containsKey(robot.getIdentifier()) || lastViolations.get(robot.getIdentifier()) + GRACE_PERIOD < game.getTime()) {
                lastViolations.put(robot.getIdentifier(), game.getTime());

                // FIXME: properly set the distance.
                return new Violation(robot.getIdentifier(), robot.getPosition().xy(), 0.0f);
            }
        }

        return null;
    }

    @Override
    public EnumSet<GameState> activeStates() {
        return EnumSet.of(GameState.RUNNING);
    }

    @Override
    public void reset() {
        lastViolations.clear();
    }

    record Violation(RobotIdentifier robot, Vector2 location, float distance) implements RuleViolation {
        @Override
        public String toString() {
            return "Attacker touched ball in defense area (by: " + robot.teamColor() + " #" + robot.id() + ", at " + location + ", distance: " + distance + ")";
        }

        @Override
        public SslGcGameEvent.GameEvent toPacket() {
            return SslGcGameEvent.GameEvent.newBuilder()
                    .setType(SslGcGameEvent.GameEvent.Type.ATTACKER_TOUCHED_BALL_IN_DEFENSE_AREA)
                    .setAttackerTouchedBallInDefenseArea(SslGcGameEvent.GameEvent.AttackerTouchedBallInDefenseArea.newBuilder()
                            .setByTeam(robot.teamColor() == TeamColor.BLUE ? SslGcCommon.Team.BLUE : SslGcCommon.Team.YELLOW)
                            .setByBot(robot.id())
                            .setLocation(SslGcGeometry.Vector2.newBuilder().setX(location.getX()).setY(location.getY()))
                            .setDistance(distance))
                    .build();
        }
    }
}
