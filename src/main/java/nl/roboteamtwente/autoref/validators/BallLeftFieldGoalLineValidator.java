package nl.roboteamtwente.autoref.validators;

import nl.roboteamtwente.autoref.RuleValidator;
import nl.roboteamtwente.autoref.RuleViolation;
import nl.roboteamtwente.autoref.model.*;
import org.robocup.ssl.proto.SslGcCommon;
import org.robocup.ssl.proto.SslGcGameEvent;
import org.robocup.ssl.proto.SslGcGeometry;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

public class BallLeftFieldGoalLineValidator implements RuleValidator {

    private static final double GRACE_PERIOD = 2.0;
    //Map from robotId -> last violation time
    private final Map<RobotIdentifier, Double> lastViolations = new HashMap<>();

    /**
     * Check if the violation is still in GRACE_PERIOD
     * @param bot - identifier of the bot
     * @param currentTimeStamp - the current time that detect violation again
     * @return true if bot still under GRACE_PERIOD
     */
    private boolean botStillOnCoolDown(RobotIdentifier bot, double currentTimeStamp)
    {
        if (lastViolations.containsKey(bot))
        {
            Double timestampLastViolation = lastViolations.get(bot);
            if (currentTimeStamp <= timestampLastViolation + GRACE_PERIOD) {
                return true;
            } else {
                lastViolations.remove(bot);
                return false;
            }
        }
        return false;
    }

    @Override
    public RuleViolation validate(Game game) {
        Vector2 location;
        Robot byBot;
        TeamColor byTeam;
        Vector3 ballPosition = game.getBall().getPosition();
        FieldLine rightGoalLine = game.getField().getLineByName("RightGoalLine");
        FieldLine leftGoalLine = game.getField().getLineByName("LeftGoalLine");
        FieldLine rightGoalTopLine = game.getField().getLineByName("RightGoalTopLine");
        FieldLine rightGoalBottomLine = game.getField().getLineByName("RightGoalBottomLine");


        //FIXME: If the ball enters the goal itself then this triggers this violation, to prevent that from happening certain dimensions need to be excluded
//        if (ball.getX() >= rightGoalBottomLine.p1().getX() && ball.getX() <= rightGoalTopLine.p2().getX() &&  ball.getY() <  rightGoalLine.p1().getY() ){
//            return null;
//        }

//        if (ball.getX() > rightGoalLine.p1().getX()){
//            location = ball.xy();
//            return new Violation(null, 0, location);
//        }
//
//        if (ball.getX() < leftGoalLine.p1().getX()){
//            location = ball.xy();
//            return new Violation(null, 0, location);
//        }

//        //FIXME: This will not work with manual ball placement, if you want to test this manually, comment line 41-57 and uncomment 26-38.
//        for (TeamColor teamColor : TeamColor.values()) {
//            for (Robot robot : game.getTeam(teamColor).getRobots()) {
//                if (robot.hasJustTouchedBall() && ball.getX() > rightGoalLine.p1().getX()) {
//                    byBot = robot;
//                    byTeam = robot.getTeam().getColor();
//                    location = ball.xy();
//                    return new Violation(byTeam, byBot.getId(), location);
//                }
//
//                if (robot.hasJustTouchedBall() && ball.getX() < leftGoalLine.p1().getX()){
//                    byBot = robot;
//                    byTeam = robot.getTeam().getColor();
//                    location = ball.xy();
//                    return new Violation(byTeam, byBot.getId(), location);
//                }
//            }
//        }


        for (Robot robot : game.getRobots()) {
//            System.out.println(game.getBall().getRobotsTouching());
//            if (robot.getId() == 0) {
//                System.out.println("Robot 0 check" + robot.isTouchingBall());
//            }
            if (ballPosition.getX() > rightGoalLine.p1().getX() || game.getBall().getLastTouchedBy() == robot) {
                if (!botStillOnCoolDown(robot.getIdentifier(), game.getTime())) {
                    lastViolations.put(robot.getIdentifier(), game.getTime());
                    byBot = robot;
                    byTeam = robot.getTeam().getColor();
                    location = ballPosition.xy();
                    return new Violation(byTeam, byBot.getId(), location);
                }
            }

            if (ballPosition.getX() < leftGoalLine.p1().getX() || game.getBall().getLastTouchedBy() == robot) {
                if (!botStillOnCoolDown(robot.getIdentifier(), game.getTime())) {
                    lastViolations.put(robot.getIdentifier(), game.getTime());
                    byBot = robot;
                    byTeam = robot.getTeam().getColor();
                    location = ballPosition.xy();
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
