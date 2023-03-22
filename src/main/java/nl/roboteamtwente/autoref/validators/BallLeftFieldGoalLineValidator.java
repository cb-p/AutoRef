package nl.roboteamtwente.autoref.validators;

import nl.roboteamtwente.autoref.RuleValidator;
import nl.roboteamtwente.autoref.RuleViolation;
import nl.roboteamtwente.autoref.model.*;
import org.robocup.ssl.proto.SslGcCommon;
import org.robocup.ssl.proto.SslGcGameEvent;
import org.robocup.ssl.proto.SslGcGeometry;

import java.util.EnumSet;

public class BallLeftFieldGoalLineValidator implements RuleValidator {

    private static final double GRACE_PERIOD = 2.0;
    private double lastViolations;


    /**
     * The validate method of this class determines whether an any robot has caused the ball
     * to exit the right or left goal lines. The X coordinate of the ball is compared against
     * the X coordinates of the left and/or right goal lines to determine if the ball did indeed cross
     * those lines. The robot that last touched the ball is the one responsible for the exiting of
     * the ball and hence the violation.
     *
     * @param game the game object being validated
     * @return a violation when the ball leaves the goal line.
     */
    @Override
    public RuleViolation validate(Game game) {
        Vector2 location;
        Robot byBot;
        TeamColor byTeam;
        Vector3 ball = game.getBall().getPosition();
        FieldLine rightGoalLine = game.getField().getLineByName("RightGoalLine");
        FieldLine leftGoalLine = game.getField().getLineByName("LeftGoalLine");

        if (ball.getX() > rightGoalLine.p1().getX() || ball.getX() < leftGoalLine.p1().getX()){
            byBot =  game.getRobot(game.getLastStartedTouch().by());
            if (byBot != null && (game.getTime() - lastViolations > GRACE_PERIOD)) {
                lastViolations = game.getTime();
                byTeam = byBot.getTeam().getColor();
                location = ball.xy();
                return new Violation(byTeam, byBot.getId(), location);
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

    record Violation(TeamColor byTeam, int byBot, Vector2 location) implements RuleViolation {
        @Override
        public String toString() {
            return "Ball left the Goal line (by: " + byTeam + ", by bot #" + byBot + ", at " + location + " )";
        }


        /**
         * Function that formats the violation into a packet to send to the GameController.
         * @return a GameEvent packet of type BallLeftFieldGoalLine to be handled by the GameController.
         */
        @Override
        public SslGcGameEvent.GameEvent toPacket() {
            return SslGcGameEvent.GameEvent.newBuilder()
                    .setType(SslGcGameEvent.GameEvent.Type.BALL_LEFT_FIELD_GOAL_LINE)
                    .setBallLeftFieldGoalLine(SslGcGameEvent.GameEvent.BallLeftField.newBuilder()
                            .setByTeam(byTeam == TeamColor.BLUE ? SslGcCommon.Team.BLUE : SslGcCommon.Team.YELLOW)
                            .setByBot(byBot)
                            .setLocation(SslGcGeometry.Vector2.newBuilder().setX(location.getX()).setY(location.getY())))
                    .build();

        }
    }
}
