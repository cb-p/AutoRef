package nl.roboteamtwente.autoref.validators;

import nl.roboteamtwente.autoref.RuleValidator;
import nl.roboteamtwente.autoref.RuleViolation;
import nl.roboteamtwente.autoref.model.*;
import org.robocup.ssl.proto.SslGcCommon;
import org.robocup.ssl.proto.SslGcGameEvent;
import org.robocup.ssl.proto.SslGcGeometry;

public class PossibleGoalValidator implements RuleValidator {

    // Rule states: The team did not commit any non-stopping foul in the last two seconds before the ball entered the goal.
    private double lastNonStoppingFoulbyYellow = Float.NEGATIVE_INFINITY;
    private double lastNonStoppingFoulbyBlue = Float.NEGATIVE_INFINITY;

    // Check if possible goal is raised
    private boolean isRaised = false;

    // Rule states: The team did not commit any non_stopping foul in the last two seconds before the ball entered the goal.
    private static final double NON_STOPPING_FOUL_TIME_CONSTRAINT = 2;

    /**
     * Rule state: The team did not commit any non-stopping foul in the last two seconds before the ball entered the goal.
     * Set the last non-stopping foul per team
     * @param lastNonStoppingFoul - non stopping foul occurs time stamp
     * @param byTeam - team commits the foul
     */
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

    /**
     * Check if the ball is inside the goal and behind the goal post
     * The size of the goal depends on the division
     * @param game
     * @param ballPos the location of the ball
     * @return
     */
    boolean checkPossibleGoalPosition(Game game, Vector2 ballPos) {
        float goalDepthLength = 0.18f;
        float goalWidthLength;

        // Division A and B have different goal size
        if (game.getDivision() == Division.A) {
            goalWidthLength = 1.8f;
        } else {
            goalWidthLength = 1f;
        }

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

            float rightPostP1y = (goalWidthLength/2) * LeftToRightCoefficient * -1;

            float rightPostP2y = leftPostP1y;

            // Check if ball inside right goal
            if ((ballPos.getY() >= Math.min(rightPostP1y, leftPostP1y)) && (ballPos.getY() <= Math.max(rightPostP1y, leftPostP1y))
                    && (ballPos.getX() >= Math.min(leftPostP1x,leftPostP2x)) && (ballPos.getX() <= Math.max(leftPostP1x,leftPostP2x))) {
                return true;
            }
        }

        FieldLine leftFieldLeftPenaltyStretch = game.getField().getLineByName("LeftFieldLeftPenaltyStretch");
        if (leftFieldLeftPenaltyStretch != null) {
            float LeftToRightCoefficient;

            // LeftToRightCoefficient if leftPenaltyStretch is positive otherwise negative
            if (leftFieldLeftPenaltyStretch.p1().getY() >= 0) {
                LeftToRightCoefficient = 1;
            } else {
                LeftToRightCoefficient = -1;
            }
            float leftPostP1x = leftFieldLeftPenaltyStretch.p1().getX();
            float leftPostP1y = (goalWidthLength/2) * LeftToRightCoefficient;

            float leftPostP2x = leftPostP1x - goalDepthLength;
            float leftPostP2y = leftPostP1y;

            float rightPostP1y = (goalWidthLength/2) * LeftToRightCoefficient * -1;

            // Check if ball inside right goal
            if ((ballPos.getY() >= Math.min(rightPostP1y, leftPostP1y)) && (ballPos.getY() <= Math.max(rightPostP1y, leftPostP1y))
                    && (ballPos.getX() >= Math.min(leftPostP1x,leftPostP2x)) && (ballPos.getX() <= Math.max(leftPostP1x,leftPostP2x))) {
                return true;
            }
        }

        return false;
    }

    //TODO: used to check if the possible goal is detected
    public boolean isPossibleGoalRaised() {
        return this.isRaised;
    }

    @Override
    public RuleViolation validate(Game game) {
        if (isRaised) {
            return null;
        }
        Vector2 ballPos = game.getBall().getPosition().xy();

        Touch touch = game.getLastFinishedTouch();
        if (touch == null) {
            return null;
        }

        if (checkPossibleGoalPosition(game, ballPos)) {
            Vector2 kickLocation = touch.endLocation().xy();
            RobotIdentifier kickBot = touch.by();
            TeamColor kickingTeam = kickBot.teamColor();
            double lastTouchTimeStampByTeam = touch.endTime();
            TeamColor byTeam;
            Side ballSide = ballPos.getX() < 0 ? Side.LEFT : Side.RIGHT;
            // Scoring team is the opposite team of the team owns the side
            if (game.getTeam(TeamColor.BLUE).getSide().equals(ballSide)) {
                byTeam = TeamColor.YELLOW;
            } else {
                byTeam = TeamColor.BLUE;
            }

            // Rule states: The team did not commit any non_stopping foul in the last two seconds before the ball entered the goal.
            if ((byTeam == TeamColor.BLUE) && (game.getTime() - lastNonStoppingFoulbyBlue <= NON_STOPPING_FOUL_TIME_CONSTRAINT)) {
                return null;
            }
            if ((byTeam == TeamColor.YELLOW) && (game.getTime()- lastNonStoppingFoulbyYellow <= NON_STOPPING_FOUL_TIME_CONSTRAINT)) {
                return null;
            }

            int numRobotsByTeam = game.getTeam(byTeam).getRobots().size();
            isRaised = true;
            return new PossibleGoalValidator.PossibleGoal(byTeam, kickingTeam, kickBot.id(), ballPos, kickLocation, 0f, numRobotsByTeam, (int) lastTouchTimeStampByTeam);
        }
        return null;
    }

    @Override
    public void reset(Game game) {
        isRaised = false;
        lastNonStoppingFoulbyBlue = Float.NEGATIVE_INFINITY;
        lastNonStoppingFoulbyYellow = Float.NEGATIVE_INFINITY;
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
