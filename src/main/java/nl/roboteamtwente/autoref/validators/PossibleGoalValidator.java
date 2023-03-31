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

    @Override
    public RuleViolation validate(Game game) {
//        if (game.getTime() - lastNonStoppingFoul)
        Vector2 location = game.getBall().getPosition().xy();



        Vector2 kickingLocation = game.getBall().getLastTouchStarted().endLocation().xy();
        RobotIdentifier kickBot = game.getBall().getLastTouchStarted().by();
        double lastTouchTimestampByTeam = game.getBall().getLastTouchStarted().endTime();
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
