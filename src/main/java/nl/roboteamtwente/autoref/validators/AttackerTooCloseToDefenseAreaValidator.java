package nl.roboteamtwente.autoref.validators;

import nl.roboteamtwente.autoref.RuleValidator;
import nl.roboteamtwente.autoref.RuleViolation;
import nl.roboteamtwente.autoref.model.*;
import org.robocup.ssl.proto.SslGcCommon;
import org.robocup.ssl.proto.SslGcGameEvent;
import org.robocup.ssl.proto.SslGcGeometry;

import java.util.HashMap;
import java.util.Map;

import static java.lang.Math.abs;

public class AttackerTooCloseToDefenseAreaValidator implements RuleValidator {

    private static final double GRACE_PERIOD = 2.0;

    private final Map<RobotIdentifier, Double> lastViolations = new HashMap<>();

    /**
     * The distance from the field line that the violation will begin to trigger
     */
    private static final double MAX_DISTANCE = 0.2;


    @Override
    public RuleViolation validate(Game game) {
        // Check if team color is not null
        if (game.getStateForTeam() == null) {
            return null;
        }

        // Initialize variables
        float distance = 0; // TODO: Use distance variable
        Field field = game.getField();
        TeamColor attackingTeam = game.getStateForTeam();
        Side side = game.getTeam(attackingTeam).getSide();

        // Should check the defending team's side, so invert the result
        String sideString = side == Side.LEFT ? "Right" : "Left";

        // Get FieldLine for the defending side
        FieldLine PenaltyStretch = side == Side.LEFT ? field.getLineByName(sideString + "FieldRightPenaltyStretch") : field.getLineByName(sideString + "FieldLeftPenaltyStretch");

        for (Robot robot : game.getTeam(attackingTeam).getRobots()) {
            // Check if robot's X is within 0.2 m of the defender area
            if (robot.getPosition().getX() * side.getCardinality() < PenaltyStretch.p1().getX() * side.getCardinality() + MAX_DISTANCE) {

                // Check if robot's Y is also within 0.2m of the defender area
                // TODO: Check if true: Can use the absolute value of the Y position as it is mirrored from the middle line of the field
                if (abs(robot.getPosition().getY()) < abs(PenaltyStretch.p1().getY()) + MAX_DISTANCE) {

                    // If so, check if it is within one of the corners and calculate distance to the corner
                    if (robot.getPosition().getX() * side.getCardinality() > PenaltyStretch.p1().getX() * side.getCardinality()
                            && abs(robot.getPosition().getY()) > abs(PenaltyStretch.p1().getY())) {
                        float robotX = robot.getPosition().getX();
                        float robotY = robot.getPosition().getY();
                        float lineX = PenaltyStretch.p1().getX();
                        float lineY = PenaltyStretch.p1().getY();

                        if (Math.sqrt(Math.pow(lineX - robotX, 2) + Math.pow(abs(lineY) - abs(robotY), 2)) > MAX_DISTANCE) {
                            // Robot is not within 0.2m of the defender area
                            continue;
                        }

                    }

                    // Finally check if the violation has not been triggered for this robot yet in the past 2 seconds
                    if (!lastViolations.containsKey(robot.getIdentifier()) || lastViolations.get(robot.getIdentifier()) + GRACE_PERIOD < game.getTime()) {
                        lastViolations.put(robot.getIdentifier(), game.getTime());
                        return new Violation(robot.getIdentifier(), robot.getPosition().xy(), distance);
                    }
                }
            }
        }
        return null;
    }

    // Validator should be active during stop and free kicks, when the ball has not yet entered play
    @Override
    public boolean isActive(Game game) {
        return game.getState() == GameState.STOP || game.getState() == GameState.DIRECT_FREE || game.getState() == GameState.INDIRECT_FREE;
    }

    @Override
    public void reset(Game game) {
        lastViolations.clear();
    }

    record Violation(RobotIdentifier robot, Vector2 location, float distance) implements RuleViolation {
        @Override
        public String toString() {
            return "Attacker too close to defense area (by: " + robot.teamColor() + " #" + robot.id() + ", at " + location + ", distance: " + distance + ")";
        }


        /**
         * Function that formats the violation into a packet to send to the GameController.
         * @return a GameEvent packet of type AttackerTouchedBallInDefenseArea to be handled by the GameController.
         */
        @Override
        public SslGcGameEvent.GameEvent toPacket() {
            return SslGcGameEvent.GameEvent.newBuilder()
                    .setType(SslGcGameEvent.GameEvent.Type.ATTACKER_TOO_CLOSE_TO_DEFENSE_AREA)
                    .setAttackerTooCloseToDefenseArea(SslGcGameEvent.GameEvent.AttackerTooCloseToDefenseArea.newBuilder()
                            .setByTeam(robot.teamColor() == TeamColor.BLUE ? SslGcCommon.Team.BLUE : SslGcCommon.Team.YELLOW)
                            .setByBot(robot.id())
                            .setLocation(SslGcGeometry.Vector2.newBuilder().setX(location.getX()).setY(location.getY()))
                            .setDistance(distance)
//                            .setBallLocation(ball.getPosition().xy())
                    )
                    .build();
        }
    }
}
