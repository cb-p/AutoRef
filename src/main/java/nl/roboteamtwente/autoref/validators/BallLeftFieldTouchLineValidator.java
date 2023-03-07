package nl.roboteamtwente.autoref.validators;

import nl.roboteamtwente.autoref.RuleValidator;
import nl.roboteamtwente.autoref.RuleViolation;
import nl.roboteamtwente.autoref.model.*;
import org.robocup.ssl.proto.SslGcCommon;
import org.robocup.ssl.proto.SslGcGameEvent;
import org.robocup.ssl.proto.SslGcGeometry;

import java.util.EnumSet;

public class BallLeftFieldTouchLineValidator implements RuleValidator {

    @Override
    public RuleViolation validate(Game game) {
        Vector2 location = null;
        Robot byBot = null;
        TeamColor byTeam = null;
        Vector3 ball = game.getBall().getPosition();
        FieldLine bottomTouchLine = game.getField().getLineByName("BottomTouchLine");
        FieldLine topTouchLine = game.getField().getLineByName("TopTouchLine");
        if (ball.getY() > topTouchLine.p1().getY()){
            location = ball.xy();
            return new Violation(null, 0, location);
        }

        if (ball.getY() < bottomTouchLine.p1().getY()){
            location = ball.xy();
            return new Violation(null, 0, location);
        }

//        for (TeamColor teamColor : TeamColor.values()) {
//            for (Robot robot : game.getTeam(teamColor.getOpponentColor()).getRobots()) {
//                //TODO: ADD FUNCTION TO DETERMINE IF A ROBOT LAST TOUCHED BALL IN ROBOT CLASS
//                if (robot.lastTouchedBall()){
//                    byBot = robot;
//                    byTeam = robot.getTeam().getColor();
//                }
//
//                assert byBot != null;
//                return new Violation(byTeam, byBot.getId(), location);
//
//            }
//
//        }

            return null;
    }

    @Override
    public EnumSet<GameState> activeStates() {
        return EnumSet.of(GameState.RUNNING);
    }

    record Violation(TeamColor byTeam, int byBot, Vector2 location) implements RuleViolation {
        @Override
        public String toString() {
            return "Ball left the field (by: " + byTeam + ", by bot #" + byBot + ", at " + location + " )";
        }

        @Override
        public SslGcGameEvent.GameEvent toPacket() {
            return SslGcGameEvent.GameEvent.newBuilder()
                    .setType(SslGcGameEvent.GameEvent.Type.BALL_LEFT_FIELD_TOUCH_LINE)
                    .setBallLeftFieldTouchLine(SslGcGameEvent.GameEvent.BallLeftField.newBuilder()
                            .setByTeam(byTeam == TeamColor.BLUE ? SslGcCommon.Team.BLUE : SslGcCommon.Team.YELLOW)
                            .setByBot(byBot)
                            .setLocation(SslGcGeometry.Vector2.newBuilder().setX(location.getX()).setY(location.getY())))
                    .build();

        }
    }
}
