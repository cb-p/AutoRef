package nl.roboteamtwente.autoref.validators;

import nl.roboteamtwente.autoref.RuleValidator;
import nl.roboteamtwente.autoref.RuleViolation;
import nl.roboteamtwente.autoref.model.*;
import org.robocup.ssl.proto.SslGcCommon;
import org.robocup.ssl.proto.SslGcGameEvent;
import org.robocup.ssl.proto.SslGcGeometry;

public class BallLeftFieldGoalLineValidator implements RuleValidator {
    @Override
    public RuleViolation validate(Game game) {
        Vector2 location = null;
        Robot byBot = null;
        TeamColor byTeam = null;
        Vector3 ball = game.getBall().getPosition();
        FieldLine rightGoalLine = game.getField().getLineByName("RightGoalLine");
        FieldLine leftGoalLine = game.getField().getLineByName("LeftGoalLine");
        FieldLine rightGoalTopLine = game.getField().getLineByName("RightGoalTopLine");
        FieldLine rightGoalBottomLine = game.getField().getLineByName("RightGoalBottomLine");


        //FIXME: If the ball enters the goal itself then this triggers this violation, to prevent that from happening certain dimensions need to be excluded
//        if (ball.getX() >= rightGoalBottomLine.p1().getX() && ball.getX() <= rightGoalTopLine.p2().getX() &&  ball.getY() <  rightGoalLine.p1().getY() ){
//            return null;
//        }

        if (ball.getX() > rightGoalLine.p1().getX()){
            location = ball.xy();
            return new Violation(null, 0, location);
        }

        if (ball.getX() < leftGoalLine.p1().getX()){
            location = ball.xy();
            return new Violation(null, 0, location);
        }

        for (TeamColor teamColor : TeamColor.values()) {
            for (Robot robot : game.getTeam(teamColor.getOpponentColor()).getRobots()) {
                if (robot.hasJustTouchedBall() && ball.getX() > rightGoalLine.p1().getX()) {
                    byBot = robot;
                    byTeam = robot.getTeam().getColor();
                    location = ball.xy();
                    return new Violation(byTeam, byBot.getId(), location);
                }


                if (robot.hasJustTouchedBall() && ball.getX() < leftGoalLine.p1().getX()){
                    byBot = robot;
                    byTeam = robot.getTeam().getColor();
                    location = ball.xy();
                    return new Violation(byTeam, byBot.getId(), location);
                }
            }
        }

        return null;
    }

    record Violation(TeamColor byTeam, int byBot, Vector2 location) implements RuleViolation {
        @Override
        public String toString() {
            return "Ball left the Goal line (by: " + byTeam + ", by bot #" + byBot + ", at " + location + " )";
        }

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
