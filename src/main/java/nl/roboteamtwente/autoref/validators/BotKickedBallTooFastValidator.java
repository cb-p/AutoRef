package nl.roboteamtwente.autoref.validators;

import nl.roboteamtwente.autoref.RuleValidator;
import nl.roboteamtwente.autoref.RuleViolation;
import nl.roboteamtwente.autoref.model.*;
import org.robocup.ssl.proto.SslGcCommon;
import org.robocup.ssl.proto.SslGcGameEvent;
import org.robocup.ssl.proto.SslGcGeometry;

import java.util.EnumSet;

public class BotKickedBallTooFastValidator implements RuleValidator{
    @Override
    public RuleViolation validate(Game game) {
        // TODO: Currently not using Chipped variable, only 2d coordinates
        TeamColor team;
        int byBot;
        Vector2 location;
        Ball ball = game.getBall();
        boolean chipped = false;

        // Position between the previous frame and current frame
        float speed = game.getPrevious().getBall().getPosition().xy().distance(ball.getPosition().xy());

        // If speed in one frame is higher than (6.5m / 80fps) = 0.08125 m / frame, bot kicked it too fast
        if (speed > 0.08125) {
            team = ball.getLastTouchedBy().getTeam().getColor();
            byBot = ball.getLastTouchedBy().getId();
            location = ball.getPosition().xy();
            return new Violation(team, byBot, location, speed, chipped);
        }

        return null;
    }

    // Rule should only be checked when the ball is in play. Might include other game states as well?
    @Override
    public EnumSet<GameState> activeStates() {
        return EnumSet.of(GameState.KICKOFF, GameState.DIRECT_FREE, GameState.INDIRECT_FREE, GameState.RUNNING);
    }

    record Violation(TeamColor byTeam, int byBot, Vector2 location, float speed, boolean chipped) implements RuleViolation {
        @Override
        public String toString() {
            return "Bot kicked ball too fast (by: " + byTeam + ", by bot #" + byBot + ", at " + location + ", ball speed: " + speed + ", chipped: " + chipped + ")";
        }

        @Override
        public SslGcGameEvent.GameEvent toPacket() {
            return SslGcGameEvent.GameEvent.newBuilder()
                    .setType(SslGcGameEvent.GameEvent.Type.BOT_KICKED_BALL_TOO_FAST)
                    .setBotKickedBallTooFast(SslGcGameEvent.GameEvent.BotKickedBallTooFast.newBuilder()
                            .setByTeam(byTeam == TeamColor.BLUE ? SslGcCommon.Team.BLUE : SslGcCommon.Team.YELLOW)
                            .setByBot(byBot)
                            .setLocation(SslGcGeometry.Vector2.newBuilder().setX(location.getX()).setY(location.getY()))
                            .setChipped(chipped)
                            .setInitialBallSpeed(speed))
                    .build();
        }
    }

}
