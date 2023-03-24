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

public class BotKickedBallTooFastValidator implements RuleValidator {

    // Hashmap of previous violations
    private final Map<RobotIdentifier, Double> lastViolations = new HashMap<>();

    // Grace period in seconds
    private static final double GRACE_PERIOD = 2.0;

    @Override
    public RuleViolation validate(Game game) {
        TeamColor team;
        RobotIdentifier robotID;
        Vector2 location;
        Ball ball = game.getBall();


//        // Speed in m/s from the previous frame
//        float prevSpeed = game.getPrevious().getBall().getPosition().xy().distance(game.getPrevious().getBall().getPosition().xy()) * 80;

        // Ball speed in m/s
        float speed = ball.getVelocity().xy().magnitude();

        // TODO: Test which implementation works best
        // If speed in one frame is higher than 6.5 m/s, ball was kicked too fast.
        // Due to inconsistent data, the ball may teleport around and this may be detected
        // as the ball being kicked too fast. In that case, use the implementation below, uses 2 consecutive frames
        if (speed > 6.5) {
            team = game.getLastStartedTouch().by().teamColor();
            robotID = game.getLastStartedTouch().by();

//            robot = ball.getLastTouchedBy();


            location = ball.getPosition().xy();

            // Only if this violation has not been sent in the last 2 seconds, raise it
            if (!lastViolations.containsKey(robotID) || lastViolations.get(robotID) + GRACE_PERIOD < game.getTime()) {
                lastViolations.put(robotID, game.getTime());
                return new Violation(team, robotID.id(), location, speed);
            }
        }

        /*


        // If ball speed is higher than 6.5m/s for two consecutive frames, send violation.
        // Checking just a single frame may be inconsistent due to the ball potentially teleporting a little between frames
        if (prevSpeed > 6.5 && speed > 6.5) {
            team = ball.getLastTouchedBy().getTeam().getColor();
            robot = ball.getLastTouchedBy();
            location = ball.getPosition().xy();

            // Only if this violation has not been sent in the last 2 seconds, raise it
            if (!lastViolations.containsKey(robot.getIdentifier()) || lastViolations.get(robot.getIdentifier()) + GRACE_PERIOD < game.getTime()) {
                lastViolations.put(robot.getIdentifier(), game.getTime());
                return new Violation(team, robot.getId(), location, speed);
        }


         */


        return null;
    }

    // Rule should only be checked when the ball is in play. Might include other game states as well?
    @Override
    public EnumSet<GameState> activeStates() {
//        return EnumSet.of(GameState.KICKOFF, GameState.DIRECT_FREE, GameState.INDIRECT_FREE, GameState.RUNNING);
        return EnumSet.of(GameState.DIRECT_FREE, GameState.INDIRECT_FREE, GameState.RUNNING);
    }

    @Override
    public void reset(Game game) {
        lastViolations.clear();
    }

    record Violation(TeamColor byTeam, int byBot, Vector2 location, float speed) implements RuleViolation {
        @Override
        public String toString() {
            return "Bot kicked ball too fast (by: " + byTeam + ", by bot #" + byBot + ", at " + location + ", ball speed: " + speed + ")";
        }

        @Override
        public SslGcGameEvent.GameEvent toPacket() {
            return SslGcGameEvent.GameEvent.newBuilder()
                    .setType(SslGcGameEvent.GameEvent.Type.BOT_KICKED_BALL_TOO_FAST)
                    .setBotKickedBallTooFast(SslGcGameEvent.GameEvent.BotKickedBallTooFast.newBuilder()
                            .setByTeam(byTeam == TeamColor.BLUE ? SslGcCommon.Team.BLUE : SslGcCommon.Team.YELLOW)
                            .setByBot(byBot)
                            .setLocation(SslGcGeometry.Vector2.newBuilder().setX(location.getX()).setY(location.getY()))
                            .setInitialBallSpeed(speed))
                    .build();
        }
    }

}
