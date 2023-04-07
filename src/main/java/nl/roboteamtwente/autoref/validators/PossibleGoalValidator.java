package nl.roboteamtwente.autoref.validators;

import nl.roboteamtwente.autoref.RuleValidator;
import nl.roboteamtwente.autoref.RuleViolation;
import nl.roboteamtwente.autoref.model.*;
import org.robocup.ssl.proto.SslGcCommon;
import org.robocup.ssl.proto.SslGcGameEvent;
import org.robocup.ssl.proto.SslGcGeometry;

public class PossibleGoalValidator implements RuleValidator {

    private double lastNonStoppingFoulbyYellow;
    private double lastNonStoppingFoulbyBlue;

    // Rule states: The team did not commit any non_stopping foul in the last two seconds before the ball entered the goal.
    private static final double NON_STOPPING_FOUL_TIME_CONSTRAINT = 2;

    public void setLastNonStoppingFoul(double lastNonStoppingFoul, TeamColor byTeam) {
        switch (byTeam){
            case BLUE -> {
                lastNonStoppingFoulbyBlue = lastNonStoppingFoul;
            }
            case YELLOW -> {
                lastNonStoppingFoulbyYellow = lastNonStoppingFoul;
            }
            case BOTH -> {
                lastNonStoppingFoulbyBlue = lastNonStoppingFoul;
                lastNonStoppingFoulbyYellow = lastNonStoppingFoul;
            }
        }
    }
    boolean checkPossibleGoalPosition(Game game, Vector2 ballPos) {
        System.out.println("Here check");

        FieldLine leftGoalLine = game.getField().getLineByName("LeftGoalLine");

        System.out.println(game.getField().lines.keySet());

        TeamColor byTeam;
        FieldLine rightGoalLine = game.getField().getLineByName("RightGoalLine");
        System.out.println("RightGoalLine");
        System.out.println("P1x:" + rightGoalLine.p1().getX());
        System.out.println("P1y:" + rightGoalLine.p1().getY());
        System.out.println("P2x:" + rightGoalLine.p2().getX());
        System.out.println("P2y" + rightGoalLine.p2().getY());
        FieldLine rightPenaltyStretch = game.getField().getLineByName("RightPenaltyStretch");
        System.out.println("RightPenaltyStretch");
        System.out.println("P1x:" + rightPenaltyStretch.p1().getX());
        System.out.println("P1y:" + rightPenaltyStretch.p1().getY());
        System.out.println("P2x:" + rightPenaltyStretch.p2().getX());
        System.out.println("P2y:" + rightPenaltyStretch.p2().getY());
        FieldLine rightFieldRightPenaltyStretch = game.getField().getLineByName("RightFieldRightPenaltyStretch");
        System.out.println("RightFieldRightPenaltyStretch");
        System.out.println("P1x:" + rightFieldRightPenaltyStretch.p1().getX());
        System.out.println("P1y:" + rightFieldRightPenaltyStretch.p1().getY());
        System.out.println("P2x:" + rightFieldRightPenaltyStretch.p2().getX());
        System.out.println("P2y:" + rightFieldRightPenaltyStretch.p2().getY());
        FieldLine rightFieldLeftPenaltyStretch = game.getField().getLineByName("RightFieldLeftPenaltyStretch");
        System.out.println("RightFieldLeftPenaltyStretch");
        System.out.println("P1x:" + rightFieldLeftPenaltyStretch.p1().getX());
        System.out.println("P1y:" + rightFieldLeftPenaltyStretch.p1().getY());
        System.out.println("P2x:" + rightFieldLeftPenaltyStretch.p2().getX());
        System.out.println("P2y:" + rightFieldLeftPenaltyStretch.p2().getY());

        float goalDepthLength = 0.18f;
        float goalWidthLength;
        if (game.getDivision() == Division.A) {
            goalWidthLength = 1.8f;
        } else {
            goalWidthLength = 1f;
        }

        float leftPostP1x = rightFieldLeftPenaltyStretch.p1().getX();
        float leftPostP1y;
        if (leftPostP1y >= 0) {
            leftPostP1y = 0.9;
        } else {
            leftPostP1y = 0.9;
        }
        float leftPostP2x = leftPostP1x + 0.2f;
        float leftPostP2y = leftPostP1y;
        FieldLine rightFieldLeftPost = new FieldLine("RightFieldLeftPost", new Vector2())
        System.out.println("Ball location");
        System.out.println("Ball x:" + game.getBall().getPosition().getX());
        System.out.println("Ball y:" + game.getBall().getPosition().getY());
        System.out.println("Division:" + game.getDivision());


//        FieldLine rightGoalBottomLine = game.getField().getLineByName("RightGoalBottomLine");
//        System.out.println(rightGoalBottomLine.p1().getX());
//        System.out.println(rightGoalBottomLine.p1().getY());
//        System.out.println(rightGoalBottomLine.p2().getX());
//        System.out.println(rightGoalBottomLine.p2().getY());
//        FieldLine rightGoalDepthLine = game.getField().getLineByName("RightGoalDepthLine");
//        System.out.println(rightGoalDepthLine.p1().getX());
//        System.out.println(rightGoalDepthLine.p1().getY());
//        System.out.println(rightGoalDepthLine.p2().getX());
//        System.out.println(rightGoalDepthLine.p2().getY());



        if (ballPos.getX() > rightGoalLine.p1().getX()) {
//            FieldLine rightGoalDepthLine = game.getField().getLineByName("RightGoalDepthLine");
//            System.out.println(rightGoalDepthLine.p2().getY());
//            System.out.println(rightGoalDepthLine.p1().getY());
//            System.out.println(rightGoalDepthLine.p2().getX());
            if (game.getTeam(TeamColor.BLUE).getSide().equals(Side.RIGHT)) {
                byTeam = TeamColor.BLUE;
            } else {
                byTeam = TeamColor.YELLOW;
            }
        } else if (ballPos.getX() < leftGoalLine.p1().getX()) {
            if (game.getTeam(TeamColor.BLUE).getSide().equals(Side.LEFT)) {
                byTeam = TeamColor.BLUE;
            } else {
                byTeam = TeamColor.YELLOW;
            }
        }
        return false;
    }
    @Override
    public RuleViolation validate(Game game) {
//        if (game.getTime() - lastNonStoppingFoul)
        Vector2 location = game.getBall().getPosition().xy();

        checkPossibleGoalPosition(game, game.getBall().getPosition().xy());

//        Vector2 kickingLocation = game.getBall().getLastTouchStarted().endLocation().xy();
//        RobotIdentifier kickBot = game.getBall().getLastTouchStarted().by();
//        double lastTouchTimestampByTeam = game.getBall().getLastTouchStarted().endTime();
//        TeamColor byTeam;
        return null;
    }

    @Override
    public boolean isActive(Game game) {
        return game.getState() == GameState.RUNNING;
    }

    record PossibleGoal(TeamColor byTeam, TeamColor kickingTeam, int kickingBot, Vector2 location, Vector2 kickLocation, Float maxBallHeight, int numRobotsByTeam, int lastTouchTimestampByTeam) implements RuleViolation {
        @Override
        public String toString() {
            return "Possible goal (by: " + byTeam + ", kicking team: " + kickingTeam + ", bot #" + kickingBot + ", location:" + location + ", kick location:" + kickLocation
                    + ", num robots: "+ numRobotsByTeam + ")";
        }

        @Override
        public SslGcGameEvent.GameEvent toPacket() {
            return SslGcGameEvent.GameEvent.newBuilder()
                    .setType(SslGcGameEvent.GameEvent.Type.POSSIBLE_GOAL)
                    .setPossibleGoal(SslGcGameEvent.GameEvent.Goal.newBuilder()
                            .setByTeam(byTeam == TeamColor.BLUE ? SslGcCommon.Team.BLUE : SslGcCommon.Team.YELLOW)
                            .setKickingTeam(kickingTeam == TeamColor.BLUE ? SslGcCommon.Team.BLUE : SslGcCommon.Team.YELLOW)
                            .setKickingBot(kickingBot)
                            .setLocation(SslGcGeometry.Vector2.newBuilder().setX(location.getX()).setY(location.getY()))
                            .setMaxBallHeight(maxBallHeight)
                            .setKickLocation(SslGcGeometry.Vector2.newBuilder().setX(kickLocation.getX()).setY(kickLocation.getY()))
                            .setNumRobotsByTeam(numRobotsByTeam)
                    )
                    .build();
        }
    }
}
