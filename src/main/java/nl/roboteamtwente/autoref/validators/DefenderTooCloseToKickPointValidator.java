package nl.roboteamtwente.autoref.validators;

import nl.roboteamtwente.autoref.RuleValidator;
import nl.roboteamtwente.autoref.RuleViolation;
import nl.roboteamtwente.autoref.model.*;
import org.robocup.ssl.proto.SslGcCommon;
import org.robocup.ssl.proto.SslGcGameEvent;
import org.robocup.ssl.proto.SslGcGeometry;

public class DefenderTooCloseToKickPointValidator implements RuleValidator {
    @Override
    public RuleViolation validate(Game game) {
        // TODO: Should have grace period of 2 seconds before another violation is sent to GC

        // if (game.state == KICK_OFF || game.state == FREE_KICK) {     // TODO: implement states :)

        // Height is not important in this case:
        // ball will always remain on the ground during this game state
        Vector2 ball = game.getBall().getPosition().xy();

        for (TeamColor teamColor : TeamColor.values()) { // TODO: only check defending team

            // Check if defender robots are too close to the ball (within 0.5m)
            for (Robot robot : game.getTeam(teamColor.getOpponentColor()).getRobots()) {
                Vector2 robotPos = robot.getPosition().xy();

                // Calculate distance to ball
                float distanceToBall = ball.distance(robotPos);
                if (distanceToBall < 500){

                    // Validator does not work correctly yet, so it's commented out to prevent spamming
//                    return new Violation(teamColor, robot.getId(), robotPos, distanceToBall);

                    return null;
                }

            }

        }
        return null;
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
