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
        TeamColor team;
        int byBot;
        Vector2 location;
        Ball ball = game.getBall();

//        // Speed in m/s from the previous frame
//        float prevSpeed = game.getPrevious().getPrevious().getBall().getPosition().xy().distance(game.getPrevious().getBall().getPosition().xy()) * 80;

        // Speed in m/s
        float speed = game.getPrevious().getBall().getPosition().xy().distance(ball.getPosition().xy()) * 80;

        // TODO: Test which implementation works best
        // If speed in one frame is higher than (6.5m / 80fps) = 0.08125 m / frame, bot kicked it too fast
        if (speed > 6.5) {
            team = ball.getLastTouchedBy().getTeam().getColor();
            byBot = ball.getLastTouchedBy().getId();
            location = ball.getPosition().xy();
            return new Violation(team, byBot, location, speed);
        }

        /*
        // If ball speed is higher than 6.5m/s for two consecutive frames, send violation.
        // Checking just a single frame may be inconsistent due to the ball potentially teleporting a little between frames
        if (prevSpeed > 6.5 && speed > 6.5) {
            team = ball.getLastTouchedBy().getTeam().getColor();
            byBot = ball.getLastTouchedBy().getId();
            location = ball.getPosition().xy();
            return new Violation(team, byBot, location, speed, chipped);
        }


         */



        return null;
    }

    // Rule should only be checked when the ball is in play. Might include other game states as well?
    @Override
    public EnumSet<GameState> activeStates() {
        return EnumSet.of(GameState.KICKOFF, GameState.DIRECT_FREE, GameState.INDIRECT_FREE, GameState.RUNNING);
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
