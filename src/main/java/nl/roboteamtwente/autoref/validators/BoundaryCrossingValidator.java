package nl.roboteamtwente.autoref.validators;

import nl.roboteamtwente.autoref.RuleValidator;
import nl.roboteamtwente.autoref.RuleViolation;
import nl.roboteamtwente.autoref.model.*;
import org.robocup.ssl.proto.SslGcCommon;
import org.robocup.ssl.proto.SslGcGameEvent;
import org.robocup.ssl.proto.SslGcGeometry;

public class BoundaryCrossingValidator implements RuleValidator {
    // We add a margin to correct for the small position error when the ball bounces off the boundary.
    private static final float MARGIN = 0.1f;

    private boolean triggered = false;

    @Override
    public RuleViolation validate(Game game) {
        if (triggered) {
            return null;
        }

        Vector2 location;
        Vector3 ball = game.getBall().getPosition();

        float boundaryWidth = game.getField().getBoundaryWidth() + MARGIN;

        if (ball.getY() > game.getField().getPosition().getY() + game.getField().getSize().getY() + boundaryWidth
                || ball.getY() < game.getField().getPosition().getY() - boundaryWidth
                || ball.getX() > game.getField().getPosition().getX() + game.getField().getSize().getX() + boundaryWidth
                || ball.getX() < game.getField().getPosition().getX() - boundaryWidth) {

            Touch touch = game.getLastFinishedTouch();
            location = ball.xy();

            triggered = true;
            if (touch != null) {
                Robot byBot = game.getRobot(touch.by());
                return new Violation(byBot.getTeam().getColor(), location);
            } else {
                return new Violation(null, location);
            }
        }

        return null;
    }

    @Override
    public boolean isActive(Game game) {
        return game.isBallInPlay();
    }


    @Override
    public void reset(Game game) {
        triggered = false;
    }


    record Violation(TeamColor byTeam, Vector2 location) implements RuleViolation {
        @Override
        public String toString() {
            return "Ball left the Boundary Crossing (by: " + byTeam + ", at " + location + ")";
        }

        @Override
        public SslGcGameEvent.GameEvent toPacket() {
            return SslGcGameEvent.GameEvent.newBuilder()
                    .setType(SslGcGameEvent.GameEvent.Type.BOUNDARY_CROSSING)
                    .setBoundaryCrossing(SslGcGameEvent.GameEvent.BoundaryCrossing.newBuilder()
                            .setByTeam(byTeam == TeamColor.BLUE ? SslGcCommon.Team.BLUE : SslGcCommon.Team.YELLOW)
                            .setLocation(SslGcGeometry.Vector2.newBuilder().setX(location.getX()).setY(location.getY())))
                    .build();

        }
    }
}
