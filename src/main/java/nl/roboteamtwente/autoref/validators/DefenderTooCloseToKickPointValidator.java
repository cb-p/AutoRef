package nl.roboteamtwente.autoref.validators;

import nl.roboteamtwente.autoref.RuleValidator;
import nl.roboteamtwente.autoref.RuleViolation;
import nl.roboteamtwente.autoref.model.*;
import org.robocup.ssl.proto.SslGcCommon;
import org.robocup.ssl.proto.SslGcGameEvent;
import org.robocup.ssl.proto.SslGcGeometry;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DefenderTooCloseToKickPointValidator implements RuleValidator {

    private static final double GRACE_PERIOD = 2.0;

    private final Map<RobotIdentifier, Double> lastViolations = new HashMap<>();

    /**
     * @return closest robot to the ball
     */
    private Robot closestRobotToBall(Game game) {
        // TODO: Perhaps move this function somewhere else to use for other rules, if needed
        List<Robot> robots = game.getRobots();
        Robot closestRobot = null;
        float dist = 99999;
        for (Robot robot : robots) {
            // Calculate distance to the ball for each robot
            float distToBall = game.getBall().getPosition().xy().distance(robot.getPosition().xy());
            if (distToBall < dist) {
                dist = distToBall;
                closestRobot = robot;
            }
        }
        return closestRobot;
    }

    @Override
    public RuleViolation validate(Game game) {

        // Check if team color is not null
        if (game.getStateForTeam() == null) {
            return null;
        }

        // Height is not important in this case:
        // ball will always remain on the ground during these game states
        Vector2 ball = game.getBall().getPosition().xy();

        // Get defending teamcolor from the game state
        TeamColor defendingTeamColor = game.getStateForTeam() == TeamColor.YELLOW ? TeamColor.BLUE : TeamColor.YELLOW;

        // Check if defender robots are too close to the ball (within 0.5m)
        for (Robot robot : game.getTeam(defendingTeamColor).getRobots()) {
            Vector2 robotPos = robot.getPosition().xy();

            // Calculate distance to ball
            float distanceToBall = ball.distance(robotPos);
            // If robot is within 0.5m of the ball, it is too close
            if (distanceToBall < 0.5) {
                if (!lastViolations.containsKey(robot.getIdentifier()) || lastViolations.get(robot.getIdentifier()) + GRACE_PERIOD < game.getTime()) {
                    lastViolations.put(robot.getIdentifier(), game.getTime());
                    //TODO: Validator does not work correctly yet, so it's commented out to prevent spamming

//                        return new Violation(teamColor, robot.getId(), robotPos, distanceToBall);
                }
            }

        }

        return null;
    }

    @Override
    public EnumSet<GameState> activeStates() {
        // TODO: Also include KICKOFF
        return EnumSet.of(GameState.DIRECT_FREE, GameState.INDIRECT_FREE);
    }

    @Override
    public void reset(Game game) {
        lastViolations.clear();
    }

    record Violation(TeamColor byTeam, int byBot, Vector2 location, float distance) implements RuleViolation {
        @Override
        public String toString() {
            return "Defender too close to kick point (by: " + byTeam + ", by bot #" + byBot + ", at " + location + ", distance: " + distance + ")";
        }

        @Override
        public SslGcGameEvent.GameEvent toPacket() {
            return SslGcGameEvent.GameEvent.newBuilder()
                    .setType(SslGcGameEvent.GameEvent.Type.DEFENDER_TOO_CLOSE_TO_KICK_POINT)
                    .setDefenderTooCloseToKickPoint(SslGcGameEvent.GameEvent.DefenderTooCloseToKickPoint.newBuilder()
                            .setByTeam(byTeam == TeamColor.BLUE ? SslGcCommon.Team.BLUE : SslGcCommon.Team.YELLOW)
                            .setByBot(byBot)
                            .setLocation(SslGcGeometry.Vector2.newBuilder().setX(location.getX()).setY(location.getY()))
                            .setDistance(distance))
                    .build();
        }
    }
}
