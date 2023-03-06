package nl.roboteamtwente.autoref.validators;

import nl.roboteamtwente.autoref.RuleValidator;
import nl.roboteamtwente.autoref.RuleViolation;
import nl.roboteamtwente.autoref.model.*;
import org.robocup.ssl.proto.MessagesRobocupSslGameControllerCommon;
import org.robocup.ssl.proto.MessagesRobocupSslGameControllerGeometry;
import org.robocup.ssl.proto.MessagesRobocupSslGameEvent;

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

    record Violation(TeamColor byTeam, int byBot, Vector2 location) implements RuleViolation {
        @Override
        public String toString() {
            return "Ball left the field (by: " + byTeam + ", by bot #" + byBot + ", at " + location + " )";
        }

        @Override
        public MessagesRobocupSslGameEvent.GameEvent toPacket() {
            return MessagesRobocupSslGameEvent.GameEvent.newBuilder()
                    .setType(MessagesRobocupSslGameEvent.GameEvent.Type.BALL_LEFT_FIELD_TOUCH_LINE)
                    .setBallLeftFieldTouchLine(MessagesRobocupSslGameEvent.GameEvent.BallLeftField.newBuilder()
                            .setByTeam(byTeam == TeamColor.BLUE ? MessagesRobocupSslGameControllerCommon.Team.BLUE : MessagesRobocupSslGameControllerCommon.Team.YELLOW)
                            .setByBot(byBot)
                            .setLocation(MessagesRobocupSslGameControllerGeometry.Vector2.newBuilder().setX(location.getX()).setY(location.getY())))
                    .build();

        }
    }
}
