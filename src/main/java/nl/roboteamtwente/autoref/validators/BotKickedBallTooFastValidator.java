package nl.roboteamtwente.autoref.validators;

import nl.roboteamtwente.autoref.RuleValidator;
import nl.roboteamtwente.autoref.RuleViolation;
import nl.roboteamtwente.autoref.model.*;
import org.robocup.ssl.proto.SslGcCommon;
import org.robocup.ssl.proto.SslGcGameEvent;
import org.robocup.ssl.proto.SslGcGeometry;

import java.util.EnumSet;

public class BotKickedBallTooFastValidator implements RuleValidator {

    // Grace period in seconds
    private static final double GRACE_PERIOD = 2.0;

    // The violation counts for the whole team, so keep track of when the last violation was raised
    private double lastViolation = Double.NEGATIVE_INFINITY;

    @Override
    public RuleViolation validate(Game game) {
        // Variables we need
        TeamColor team;
        RobotIdentifier robotID;
        Vector2 location;
        Ball ball = game.getBall();

        // Ball speed in m/s
        float speed = ball.getVelocity().xy().magnitude();

        // If speed in one frame is higher than 6.5 m/s, ball was kicked too fast.
        // Due to inconsistent data, the ball may teleport around and this may be detected
        // as the ball being kicked too fast. In that case, use the implementation below, uses 2 consecutive frames
        if (speed > 6.5) {
            team = game.getLastStartedTouch().by().teamColor();
            robotID = game.getLastStartedTouch().by();
            location = ball.getPosition().xy();

            // Only if this violation has not been sent in the last 2 seconds, raise it
            if (lastViolation + GRACE_PERIOD < game.getTime()) {
                lastViolation = game.getTime();
                return new Violation(team, robotID.id(), location, speed);
            }
        }


        return null;
    }

    // Rule should only be checked when the ball is in play.
    @Override
    public EnumSet<GameState> activeStates() {
        return EnumSet.of(GameState.DIRECT_FREE, GameState.INDIRECT_FREE, GameState.RUNNING);
    }

    @Override
    public void reset(Game game) {
        lastViolation = Double.NEGATIVE_INFINITY;
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
