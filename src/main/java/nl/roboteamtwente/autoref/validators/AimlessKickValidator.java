package nl.roboteamtwente.autoref.validators;

import nl.roboteamtwente.autoref.RuleValidator;
import nl.roboteamtwente.autoref.RuleViolation;
import nl.roboteamtwente.autoref.model.*;
import org.robocup.ssl.proto.SslGcCommon;
import org.robocup.ssl.proto.SslGcGameEvent;
import org.robocup.ssl.proto.SslGcGeometry;

import java.util.EnumSet;


public class AimlessKickValidator implements RuleValidator {

    private static final double GRACE_PERIOD = 2.0;
    private double lastViolations;


    @Override
    public RuleViolation validate(Game game) {
        Robot byBot;
        TeamColor byTeam;
        Vector2 location;
        FieldLine rightGoalLine = game.getField().getLineByName("RightGoalLine");
        FieldLine leftGoalLine = game.getField().getLineByName("LeftGoalLine");

        if (game.getBall().getPosition().getX() > rightGoalLine.p1().getX() || game.getBall().getPosition().getX() < leftGoalLine.p1().getX()){
            byBot =  game.getRobot(game.getLastStartedTouch().by());

            if (byBot != null && game.getField().isInOwnHalf(byBot.getTeam().getSide(), byBot.getPosition().xy()) && (game.getTime() - lastViolations > GRACE_PERIOD)) {
                lastViolations = game.getTime();
                byTeam = byBot.getTeam().getColor();
                location = game.getBall().getPosition().xy();
                return new Violation(byTeam, byBot.getId(), location, byBot.getPosition().xy());
            }
        } else {
            lastViolations = Double.NEGATIVE_INFINITY;
        }
        return null;
    }

    @Override
    public EnumSet<GameState> activeStates() {
        return EnumSet.of(GameState.RUNNING);
    }


    record Violation(TeamColor byTeam, int byBot, Vector2 ballLocation, Vector2 kickLocation) implements RuleViolation {
        @Override
        public String toString() {
            return "Aimless kick (by: " + byTeam + ", by bot #" + byBot + ", at kick location: " + kickLocation +  ", ball left at " + ballLocation + " )";
        }


        /**
         * Function that formats the violation into a packet to send to the GameController.
         * @return a GameEvent packet of type BallLeftFieldGoalLine to be handled by the GameController.
         */
        @Override
        public SslGcGameEvent.GameEvent toPacket() {
            return SslGcGameEvent.GameEvent.newBuilder()
                    .setType(SslGcGameEvent.GameEvent.Type.AIMLESS_KICK)
                    .setAimlessKick(SslGcGameEvent.GameEvent.AimlessKick.newBuilder()
                            .setByTeam(byTeam == TeamColor.BLUE ? SslGcCommon.Team.BLUE : SslGcCommon.Team.YELLOW)
                            .setByBot(byBot)
                            .setLocation(SslGcGeometry.Vector2.newBuilder().setX(ballLocation.getX()).setY(ballLocation.getY()))
                            .setKickLocation(SslGcGeometry.Vector2.newBuilder().setX(kickLocation.getX()).setY(kickLocation.getY()))
                    )
                    .build();

        }
    }
}
