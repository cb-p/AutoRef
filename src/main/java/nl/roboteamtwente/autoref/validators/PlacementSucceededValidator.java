package nl.roboteamtwente.autoref.validators;

import nl.roboteamtwente.autoref.RuleValidator;
import nl.roboteamtwente.autoref.RuleViolation;
import nl.roboteamtwente.autoref.model.*;
import org.robocup.ssl.proto.SslGcCommon;
import org.robocup.ssl.proto.SslGcGameEvent;
import org.robocup.ssl.proto.SslGcGeometry;
import org.robocup.ssl.proto.SslGcRefereeMessage;

public class PlacementSucceededValidator implements RuleValidator {

    private static final float FREE_KICK_PLACEMENT_DISTANCE = 0.05f;

    private static final float FORCE_START_PLACEMENT_DISTANCE = 0.05f;

    private static final float MAXIMUM_PLACEMENT_DISTANCE_BETWEEN_BALL_AND_DESIGNATED_POS = 0.05f;

    private static final float MIN_PLACEMENT_TIME = 2;

    private static double startPlacement = Float.POSITIVE_INFINITY;

    private static Vector3 initialBallPosition;

    private static boolean issueValidator = false;

    public boolean isConsideredPlacedSuccessfully(Game game) {


//        All robot must keep distance to ball during the placement
        double minDistance = isNextCommandForPlacingTeam(game) ? FREE_KICK_PLACEMENT_DISTANCE : FORCE_START_PLACEMENT_DISTANCE;

        for (Robot robot : game.getRobots()) {
            if (robot.getPosition().xy().distance(game.getBall().getPosition().xy()) < minDistance) {
                return false;
            }
        }

        return true;
    }


    private boolean isNextCommandForPlacingTeam(Game game)
    {
        if (game.getCommand() == SslGcRefereeMessage.SSL_Referee.Command.BALL_PLACEMENT_BLUE)
        {
            return game.getNextCommand() == SslGcRefereeMessage.SSL_Referee.Command.DIRECT_FREE_BLUE
                    || game.getNextCommand() == SslGcRefereeMessage.SSL_Referee.Command.INDIRECT_FREE_BLUE;
        } else if (game.getCommand() == SslGcRefereeMessage.SSL_Referee.Command.BALL_PLACEMENT_YELLOW)
        {
            return game.getNextCommand() == SslGcRefereeMessage.SSL_Referee.Command.DIRECT_FREE_YELLOW
                    || game.getNextCommand() == SslGcRefereeMessage.SSL_Referee.Command.INDIRECT_FREE_YELLOW;
        }
        return false;
    }
    @Override
    public RuleViolation validate(Game game) {

        if (issueValidator || game.getDesignatedPosition() == null) {
            return null;
        }

        Vector3 currentBallPos = game.getBall().getPosition();
        TeamColor forTeam = game.getStateForTeam();
        float precision = game.getDesignatedPosition().distance(currentBallPos.xy());
        double timeTaken = game.getTime() - startPlacement;

        if (precision <= MAXIMUM_PLACEMENT_DISTANCE_BETWEEN_BALL_AND_DESIGNATED_POS && timeTaken >= MIN_PLACEMENT_TIME) {
            issueValidator = true;
            float distance = initialBallPosition.xy().distance(currentBallPos.xy());

            return new PlacementSucceededValidator.PlacementSucceededViolation(forTeam, (float) timeTaken, precision,distance);
        }

        return null;
    }

    @Override
    public void reset(Game game) {
        startPlacement = game.getTime();
        initialBallPosition = game.getBall().getPosition();
        issueValidator = false;
    }

    record PlacementSucceededViolation(TeamColor byTeam, float time_taken, float precision, float distance) implements RuleViolation {
        @Override
        public String toString() {
            return "Placement succeeded (by: "+ byTeam + " time taken: " + time_taken + ", precision: " + precision + ", distance: "+ distance   + " )";
        }

        @Override
        public SslGcGameEvent.GameEvent toPacket() {
            return SslGcGameEvent.GameEvent.newBuilder()
                    .setType(SslGcGameEvent.GameEvent.Type.PLACEMENT_SUCCEEDED)
                    .setPlacementSucceeded(SslGcGameEvent.GameEvent.PlacementSucceeded.newBuilder()
                            .setByTeam(byTeam == TeamColor.BLUE ? SslGcCommon.Team.BLUE : SslGcCommon.Team.YELLOW)
                            .setTimeTaken(time_taken)
                            .setPrecision(precision)
                            .setDistance(distance))
                    .build();

        }
    }


    record PlacementFailedViolation(TeamColor byTeam, float distance) implements RuleViolation {
        @Override
        public String toString() {
            return "Placement failed (by: "+ byTeam +", remaining distance: "+ distance + " )";
        }

        @Override
        public SslGcGameEvent.GameEvent toPacket() {
            return SslGcGameEvent.GameEvent.newBuilder()
                    .setType(SslGcGameEvent.GameEvent.Type.PLACEMENT_FAILED)
                    .setPlacementFailed(SslGcGameEvent.GameEvent.PlacementFailed.newBuilder()
                            .setByTeam(byTeam == TeamColor.BLUE ? SslGcCommon.Team.BLUE : SslGcCommon.Team.YELLOW)
                            .setRemainingDistance(distance))
                    .build();

        }
    }

    @Override
    public boolean isActive(Game game) {
        return game.getState() == GameState.BALL_PLACEMENT;
    }
}
