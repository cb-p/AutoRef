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
     * The distance from the defender area lines that the violation will begin to trigger
     */
    private static final double MAX_DISTANCE = 0.2;


    @Override
    public RuleViolation validate(Game game) {
        // Check if team color is not null
        if (game.getState() != GameState.STOP && game.getStateForTeam() == null) {
            return null;
        }

        Field field = game.getField();

        // Loop through all robots and check if they are in the other team's defender area
        for (Robot robot : game.getRobots()) {

            TeamColor teamColor = robot.getTeam().getColor();

            // Opponent's side
            Side opponentSide = game.getTeam(teamColor).getSide().getOpposite();
            String sideString = opponentSide == Side.LEFT ? "Left" : "Right";

            // Get FieldLine for the defending side
            FieldLine penaltyStretch = opponentSide == Side.LEFT ? field.getLineByName(sideString + "FieldLeftPenaltyStretch") : field.getLineByName(sideString + "FieldRightPenaltyStretch");

            // Distance from the defender area (returns 0 if robot is inside)
            float distance = 0;

            // Easier to read
            // TODO: Is the cardinality on the opponent side 1 or -1? Important for the if-checks for X coordinate, might need to be inverted
            float robotX = robot.getPosition().getX() * opponentSide.getCardinality();
            float robotY = robot.getPosition().getY();
            float lineX = penaltyStretch.p1().getX() * opponentSide.getCardinality();
            float lineY = penaltyStretch.p1().getY();

            // Check if robot is within defender area
            if (field.isInDefenseArea(opponentSide, robot.getPosition().xy())) {
                if (!lastViolations.containsKey(robot.getIdentifier()) || lastViolations.get(robot.getIdentifier()) + GRACE_PERIOD < game.getTime()) {
                    lastViolations.put(robot.getIdentifier(), game.getTime());
                    return new Violation(robot.getIdentifier(), robot.getPosition().xy(), distance, game.getBall().getPosition().xy());
                }
            }

            // Check if robot's X is within 0.2 m of the defender area
            if (robotX > lineX - MAX_DISTANCE) {

                // Check if robot's Y is also within 0.2m of the defender area
                // TODO: Check if true: Can use the absolute value of the Y position as the Y coordinate is mirrored from the middle line of the field
                if (abs(robotY) < abs(lineY) + MAX_DISTANCE) {

                    if (abs(robotY) < abs(lineY)) {
                        // Robot is in front of the line
                        distance = lineX - robotX;
                    } else if (robotX > lineX) {
                        // Robot is above or below the defender area, within 0.2m
                        distance = lineY - robotY;
                    }

                    // Check if robot is within one of the corners and calculate distance to the corner
                    // TODO: Check: Can get either p1 or p2, they should have the same coordinates when taken the absolute Y value
                    if (robotX < lineX && abs(robotY) > abs(lineY)) {
                        // Robot is in one of the corners, use pythagorean theorem to get distance to that corner
                        distance = (float) Math.sqrt(Math.pow(lineX - robotX, 2) + Math.pow(abs(lineY) - abs(robotY), 2));
                        if (distance > MAX_DISTANCE) {
                            // Robot is not within 0.2m of the defender area
                            continue;
                        }
                    }

                    // Finally check if the violation has not been triggered for this robot yet in the past 2 seconds
                    if (!lastViolations.containsKey(robot.getIdentifier()) || lastViolations.get(robot.getIdentifier()) + GRACE_PERIOD < game.getTime()) {
                        lastViolations.put(robot.getIdentifier(), game.getTime());
                        // If this returns 0 and the robot is not in the defender area, something's wrong
                        return new Violation(robot.getIdentifier(), robot.getPosition().xy(), distance, game.getBall().getPosition().xy());
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

    record Violation(RobotIdentifier robot, Vector2 location, float distance, Vector2 ballLocation) implements RuleViolation {
        @Override
        public String toString() {
            return "Attacker too close to defense area (by: " + robot.teamColor() + " #" + robot.id() + ", at " + location + ", distance: " + distance + ", ball location: " + ballLocation + ")";
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
                            .setBallLocation(SslGcGeometry.Vector2.newBuilder().setX(ballLocation.getX()).setY(ballLocation.getY()))
                    )
                    .build();
        }
    }
}
