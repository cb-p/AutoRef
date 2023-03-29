package nl.roboteamtwente.autoref.validators;

import nl.roboteamtwente.autoref.RuleValidator;
import nl.roboteamtwente.autoref.RuleViolation;
import nl.roboteamtwente.autoref.model.*;
import org.robocup.ssl.proto.SslGcCommon;
import org.robocup.ssl.proto.SslGcGameEvent;
import org.robocup.ssl.proto.SslGcGeometry;


public class AimlessKickValidator implements RuleValidator {
    private static final double GRACE_PERIOD = 2.0;
    private double lastViolation = Double.NEGATIVE_INFINITY;

    @Override
    public RuleViolation validate(Game game) {
        FieldLine rightGoalLine = game.getField().getLineByName("RightGoalLine");
        FieldLine leftGoalLine = game.getField().getLineByName("LeftGoalLine");

        Touch touch = game.getLastFinishedTouch();
        if (touch != null) {
            Robot byBot = game.getRobot(touch.by());

            if (game.getField().isInOwnHalf(byBot.getTeam().getSide(), byBot.getPosition().xy())
                    && game.getTime() - lastViolation > GRACE_PERIOD) {

                if (game.getBall().getPosition().getX() > rightGoalLine.p1().getX()
                        && byBot.getTeam().getSide() == Side.LEFT) {
                    lastViolation = game.getTime();
                    return new Violation(byBot.getTeam().getColor(), byBot.getId(), game.getBall().getPosition().xy(), touch.endLocation().xy());
                }

                if (game.getBall().getPosition().getX() < leftGoalLine.p1().getX()
                        && byBot.getTeam().getSide() == Side.RIGHT) {
                    lastViolation = game.getTime();
                    return new Violation(byBot.getTeam().getColor(), byBot.getId(), game.getBall().getPosition().xy(), touch.endLocation().xy());
                }
            }
        }
        return null;
    }

    @Override
    public void reset(Game game) {
        lastViolation = Double.NEGATIVE_INFINITY;
    }

    @Override
    public boolean isActive(Game game) {
        return game.isBallInPlay() && game.getDivision() == Division.B;
    }

    record Violation(TeamColor byTeam, int byBot, Vector2 ballLocation, Vector2 kickLocation) implements RuleViolation {
        @Override
        public String toString() {
            return "Aimless kick (by: " + byTeam + ", by bot #" + byBot + ", at kick location: " + kickLocation + ", ball left at " + ballLocation + " )";
        }

        /**
         * Function that formats the violation into a packet to send to the GameController.
         *
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
