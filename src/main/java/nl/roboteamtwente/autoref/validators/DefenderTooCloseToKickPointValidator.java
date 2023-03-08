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
     * @return the robot that is closest to the ball
     */
    private Robot closestRobotToBall(Game game) {
        // TODO: Move this function somewhere else to use for other rules
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
        // Height is not important in this case:
        // ball will always remain on the ground during this game state
        Vector2 ball = game.getBall().getPosition().xy();

        TeamColor defendingTeamColor = null;

        // If its kickoff state, the closest team to the ball (should be?) the attacking team
        // Same can be the case for the other states, depending on the timing of when the state is changed; needs experimenting
        // The if-check is currently redundant (due to the active states) but ill keep it in here as it might need tweaking later
        if (game.getState() == GameState.KICKOFF || game.getState() == GameState.DIRECT_FREE || game.getState() == GameState.INDIRECT_FREE){
            if (closestRobotToBall(game).getTeam().getColor() == TeamColor.YELLOW) {
                defendingTeamColor = TeamColor.BLUE;
            } else {
                defendingTeamColor = TeamColor.YELLOW;
            }
        }

        // Check if defender robots are too close to the ball (within 0.5m)
        for (Robot robot : game.getTeam(defendingTeamColor).getRobots()) {
            Vector2 robotPos = robot.getPosition().xy();

            // Calculate distance to ball
            float distanceToBall = ball.distance(robotPos);
            if (distanceToBall < 500){
                if (!lastViolations.containsKey(robot.getIdentifier()) || lastViolations.get(robot.getIdentifier()) + GRACE_PERIOD < game.getTime()) {
                    lastViolations.put(robot.getIdentifier(), game.getTime());
                    // Validator does not work correctly yet, so it's commented out to prevent spamming
//                        return new Violation(teamColor, robot.getId(), robotPos, distanceToBall);
                }
            }

        }

        return null;
    }

    @Override
    public EnumSet<GameState> activeStates() {
        return EnumSet.of(GameState.KICKOFF, GameState.DIRECT_FREE, GameState.INDIRECT_FREE);
    }

    @Override
    public void reset() {
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
