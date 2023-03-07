package nl.roboteamtwente.autoref.validators;

import nl.roboteamtwente.autoref.RuleValidator;
import nl.roboteamtwente.autoref.RuleViolation;
import nl.roboteamtwente.autoref.model.*;
import org.robocup.ssl.proto.SslGcCommon;
import org.robocup.ssl.proto.SslGcGameEvent;
import org.robocup.ssl.proto.SslGcGeometry;

import java.util.EnumSet;

public class BallLeftFieldTouchLineValidator implements RuleValidator {

    public String leftAt;
    @Override
    public RuleViolation validate(Game game) {
        Vector2 location;
        Robot byBot;
        TeamColor byTeam;
        Vector3 ball = game.getBall().getPosition();
        FieldLine bottomTouchLine = game.getField().getLineByName("BottomTouchLine");
        FieldLine topTouchLine = game.getField().getLineByName("TopTouchLine");
//        FIXME: LINES 21 TO 29 ARE USED ONLY WHEN THE BALL IS MANUALLY RELOCATED, THIS IS TO BE REMOVED LATER
        if (ball.getY() > topTouchLine.p1().getY()) {
            leftAt = topTouchLine.name();
            location = ball.xy();
            return new Violation(null, 0, location);
        }

        if (ball.getY() < bottomTouchLine.p1().getY()){
            leftAt = bottomTouchLine.name();
            location = ball.xy();
            return new Violation(null, 0, location);
        }

        //FIXME: THIS WILL NOT WORK IF A BALL IS LOCATED MANUALLY SINCE byBot.getId() CANNOT BE NULL
        for (TeamColor teamColor : TeamColor.values()) {
            for (Robot robot : game.getTeam(teamColor.getOpponentColor()).getRobots()) {
                if (robot.hasJustTouchedBall() && ball.getY() > topTouchLine.p1().getY()) {
                    leftAt = topTouchLine.name();
                    byBot = robot;
                    byTeam = robot.getTeam().getColor();
                    location = ball.xy();
                    return new Violation(byTeam, byBot.getId(), location);
                }


                if (robot.hasJustTouchedBall() && ball.getY() < bottomTouchLine.p1().getY()){
                    leftAt = bottomTouchLine.name();
                    byBot = robot;
                    byTeam = robot.getTeam().getColor();
                    location = ball.xy();
                    return new Violation(byTeam, byBot.getId(), location);
                }
                }
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
