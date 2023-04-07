package nl.roboteamtwente.autoref.validators;

import nl.roboteamtwente.autoref.RuleValidator;
import nl.roboteamtwente.autoref.RuleViolation;
import nl.roboteamtwente.autoref.model.*;
import org.robocup.ssl.proto.SslGcCommon;
import org.robocup.ssl.proto.SslGcGameEvent;
import org.robocup.ssl.proto.SslGcGeometry;

public class PossibleGoalValidator implements RuleValidator {

//    TODO reset time when kick ball, it should raise 1 before the decision was made

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
        float goalDepthLength = 0.18f;
        float goalWidthLength;
        if (game.getDivision() == Division.A) {
            goalWidthLength = 1.8f;
        } else {
            goalWidthLength = 1f;
        }

//        System.out.println(game.getField().lines.keySet());

        TeamColor byTeam;

        FieldLine rightFieldLeftPenaltyStretch = game.getField().getLineByName("RightFieldLeftPenaltyStretch");

        if (rightFieldLeftPenaltyStretch != null) {
            //        LeftToRightCoefficient if leftPenaltyStretch is positive otherwise negative
            float LeftToRightCoefficient = 1;
            if (rightFieldLeftPenaltyStretch.p1().getY() >= 0) {
                LeftToRightCoefficient = 1;
            } else {
                LeftToRightCoefficient = -1;
            }
            float leftPostP1x = rightFieldLeftPenaltyStretch.p1().getX();
            float leftPostP1y = (goalWidthLength/2) * LeftToRightCoefficient;

            float leftPostP2x = leftPostP1x + goalDepthLength;
            float leftPostP2y = leftPostP1y;
//        FieldLine rightFieldLeftPost = new FieldLine("RightFieldLeftPost", new Vector2(leftPostP1x, leftPostP1y), new Vector2(leftPostP2x, leftPostP2y), 0);

            float rightPostP1x = rightFieldLeftPenaltyStretch.p1().getX();
            float rightPostP1y = (goalWidthLength/2) * LeftToRightCoefficient * -1;

            float rightPostP2x = leftPostP1x + goalDepthLength;
            float rightPostP2y = leftPostP1y;
//        FieldLine rightFieldRightPost = new FieldLine("RightFieldRightPost", new Vector2(rightPostP1x, rightPostP1y), new Vector2(rightPostP2x, rightPostP2y), 0);

            // Check if ball inside right goal
            if ((ballPos.getY() >= Math.min(rightPostP1y, leftPostP1y)) && (ballPos.getY() <= Math.max(rightPostP1y, leftPostP1y))
                    && (ballPos.getX() >= Math.min(leftPostP1x,leftPostP2x)) && (ballPos.getX() <= Math.max(leftPostP1x,leftPostP2x))) {
                System.out.println("Inside right goal");
                if (game.getTeam(TeamColor.BLUE).getSide().equals(Side.RIGHT)) {
                    byTeam = TeamColor.BLUE;
                } else {
                    byTeam = TeamColor.YELLOW;
                }
            }
        }




//        System.out.println("Ball location");
//        System.out.println("Ball x:" + game.getBall().getPosition().getX());
//        System.out.println("Ball y:" + game.getBall().getPosition().getY());
//        System.out.println("Division:" + game.getDivision());


        FieldLine leftFieldLeftPenaltyStretch = game.getField().getLineByName("LeftFieldLeftPenaltyStretch");
        if (leftFieldLeftPenaltyStretch != null) {
            float LeftToRightCoefficient;
            //        LeftToRightCoefficient if leftPenaltyStretch is positive otherwise negative
            if (leftFieldLeftPenaltyStretch.p1().getY() >= 0) {
                LeftToRightCoefficient = 1;
            } else {
                LeftToRightCoefficient = -1;
            }
            float leftPostP1x = leftFieldLeftPenaltyStretch.p1().getX();
            float leftPostP1y = (goalWidthLength/2) * LeftToRightCoefficient;

            float leftPostP2x = leftPostP1x - goalDepthLength;
            float leftPostP2y = leftPostP1y;

            float rightPostP1x = rightFieldLeftPenaltyStretch.p1().getX();
            float rightPostP1y = (goalWidthLength/2) * LeftToRightCoefficient * -1;

            float rightPostP2x = leftPostP1x - goalDepthLength;
            float rightPostP2y = leftPostP1y;


            // Check if ball inside right goal
            if ((ballPos.getY() >= Math.min(rightPostP1y, leftPostP1y)) && (ballPos.getY() <= Math.max(rightPostP1y, leftPostP1y))
                    && (ballPos.getX() >= Math.min(leftPostP1x,leftPostP2x)) && (ballPos.getX() <= Math.max(leftPostP1x,leftPostP2x))) {
                System.out.println("Inside left goal");
                if (game.getTeam(TeamColor.BLUE).getSide().equals(Side.LEFT)) {
                    byTeam = TeamColor.BLUE;
                } else {
                    byTeam = TeamColor.YELLOW;
                }
            }
        }

        return false;
    }
    @Override
    public RuleViolation validate(Game game) {
//        if (game.getTime() - lastNonStoppingFoul)
        Vector2 location = game.getBall().getPosition().xy();

        checkPossibleGoalPosition(game, game.getBall().getPosition().xy());

        Touch touch = game.getLastFinishedTouch();
        if (touch == null) {
            return null;
        }
        System.out.println(touch.by());
        System.out.println(touch.endTime());
        System.out.println(touch.endLocation().xy());
        Vector2 kickingLocation = touch.endLocation().xy();
        RobotIdentifier kickBot = touch.by();
        double lastTouchTimeStampByTeam = touch.endTime();
        TeamColor byTeam;
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
