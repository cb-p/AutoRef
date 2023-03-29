package nl.roboteamtwente.autoref.validators;

import nl.roboteamtwente.autoref.RuleValidator;
import nl.roboteamtwente.autoref.RuleViolation;
import nl.roboteamtwente.autoref.model.*;
import org.robocup.ssl.proto.SslGcCommon;
import org.robocup.ssl.proto.SslGcGameEvent;
import org.robocup.ssl.proto.SslGcGeometry;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

public class BotInterferedPlacementValidator implements RuleValidator {
    private static final double GRACE_PERIOD = 2.0;

    private static final float MIN_DISTANCE_BETWEEN_ROBOT_AND_PLACEMENT = 0.5f;

    //Map from robotId -> last enter the distance <= MIN_DISTANCE_BETWEEN_ROBOT_AND_PLACEMENT
    private final Map<RobotIdentifier, Double> lastEnterForbiddenArea = new HashMap<>();

    /**
     * Calculate the distance between point 3 to the line defined by point 1 and point 2
     *
     * @param p1 - first point of the line
     * @param p2 - second point of the line
     * @param p3 - the point to calculate distance to the line
     * @return the distance between the point 3 and the line defined by point 1 and 2
     */
    public float calculateDistancePointToLine(Vector2 p1, Vector2 p2, Vector2 p3) {
        double x1 = p1.getX();
        double y1 = p1.getY();
        double x2 = p2.getX();
        double y2 = p2.getY();
        double x3 = p3.getX();
        double y3 = p3.getY();

        // Find the coefficients a, b, and c of the equation ax + by + c = 0 that
        // defines the line passing through points p1 and p2
        double a = y2 - y1;
        double b = x1 - x2;
        double c = x2 * y1 - x1 * y2;

        // Calculate the distance between point p3 and the line defined by the equation ax + by + c = 0 using the formula:
        // |ax3 + by3 + c| / sqrt(a^2 + b^2)
        double numerator = Math.abs(a * x3 + b * y3 + c);
        double denominator = Math.sqrt(a * a + b * b);
        double distance = numerator / denominator;


        return (float) distance;
    }

    /**
     * Round float number to 1 decimal place
     *
     * @param number
     * @return rounded float number
     */
    public float roundFloatTo1DecimalPlace(float number) {
        DecimalFormat df = new DecimalFormat("#.#"); // Creates a decimal format object with one decimal place
        String roundedFloatStr = df.format(number); // Formats the float as a string with one decimal place
        return Float.parseFloat(roundedFloatStr); // Parses the rounded string back into a float
    }

    /**
     * Check if time a robot enter forbidden area is more than 2 seconds
     *
     * @param bot              - identifier of the robot
     * @param currentTimeStamp - the time robot enter forbidden area
     * @return true if robot enter more than 2 seconds else update the lastEnterForbidden Area
     */

    public boolean checkViolation(RobotIdentifier bot, double currentTimeStamp) {
        if (lastEnterForbiddenArea.containsKey(bot)) {
            Double timestampLastViolation = lastEnterForbiddenArea.get(bot);
            // if enter forbidden area more than 2 second => return fault and reset enter forbidden area time
            if (currentTimeStamp > timestampLastViolation + GRACE_PERIOD) {
                lastEnterForbiddenArea.put(bot, currentTimeStamp);
                return true;
            }
        } else {
            // if the first time => add into enter forbidden area
            lastEnterForbiddenArea.put(bot, currentTimeStamp);
        }
        return false;
    }

    @Override
    public void reset(Game game) {
        lastEnterForbiddenArea.clear();
    }

    @Override
    public RuleViolation validate(Game game) {
        if (game.getState() == GameState.BALL_PLACEMENT) {

            Team opponentTeam = game.getTeam(game.getStateForTeam().getOpponentColor());
            for (Robot robot : opponentTeam.getRobots()) {
                Vector2 robotPos = robot.getPosition().xy();
                Vector2 placementPos = game.getDesignatedPosition();
                Vector2 ballPos = game.getBall().getPosition().xy();
                float distance = calculateDistancePointToLine(ballPos, placementPos, robotPos);
                if (distance < MIN_DISTANCE_BETWEEN_ROBOT_AND_PLACEMENT) {
                    if (checkViolation(robot.getIdentifier(), game.getTime())) {
                        Vector2 roundRobotPos = new Vector2(roundFloatTo1DecimalPlace(robot.getPosition().getX()), roundFloatTo1DecimalPlace(robot.getPosition().getY()));
                        return new BotInterferedPlacementValidator.BotInterferedPlacementViolation(robot.getTeam().getColor(), robot.getId(), roundRobotPos, ballPos, placementPos);
                    }
                } else if (lastEnterForbiddenArea.containsKey(robot.getIdentifier())) {
                    lastEnterForbiddenArea.remove(robot.getIdentifier());
                }
            }
        }
        return null;
    }

    @Override
    public boolean isActive(Game game) {
        return game.getState() == GameState.BALL_PLACEMENT;
    }

    record BotInterferedPlacementViolation(TeamColor byTeam, int byBot, Vector2 location, Vector2 ballPos, Vector2 placementPos) implements RuleViolation {
        @Override
        public String toString() {
            return "Bot interfered placement (by: " + byTeam + ", bot #" + byBot + " location: " + location + " ballPos" + ballPos + " placementPos: " + placementPos + " )";
        }

        @Override
        public SslGcGameEvent.GameEvent toPacket() {
            return SslGcGameEvent.GameEvent.newBuilder()
                    .setType(SslGcGameEvent.GameEvent.Type.BOT_TOO_FAST_IN_STOP)
                    .setBotInterferedPlacement(SslGcGameEvent.GameEvent.BotInterferedPlacement.newBuilder()
                            .setByBot(byBot)
                            .setByTeam(byTeam == TeamColor.BLUE ? SslGcCommon.Team.BLUE : SslGcCommon.Team.YELLOW)
                            .setLocation(SslGcGeometry.Vector2.newBuilder().setX(location.getX()).setY(location.getY())))
                    .build();
        }
    }
}
