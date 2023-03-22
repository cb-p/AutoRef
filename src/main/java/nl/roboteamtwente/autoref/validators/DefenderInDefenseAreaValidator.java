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

public class DefenderInDefenseAreaValidator implements RuleValidator {

    private final Map<RobotIdentifier, Double> lastViolations = new HashMap<>();

    private static final double GRACE_PERIOD = 2.0;

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

    //FIXME: The logic here is implemented correctly and works well, however what should happen if there are 2 robots from each team both in the defense area?
    @Override
    public RuleViolation validate(Game game) {
        for (TeamColor teamColor : TeamColor.values()) {
            Side side = game.getTeam(teamColor).getSide();
            String sideString = side == Side.LEFT ? "Left" : "Right";
            for (Robot robot : game.getTeam(teamColor).getRobots()) {
                if (robot.isGoalkeeper()) {
                    continue;
                }

                if (!game.getField().isInDefenseArea(robot.getTeam().getSide(), robot.getPosition().xy())) {
                    continue;
                }

                FieldLine penaltyStretch = game.getField().getLineByName(sideString + "PenaltyStretch");
                FieldLine rightPenaltyStretch = game.getField().getLineByName(sideString + "FieldRightPenaltyStretch");
                FieldLine leftPenaltyStretch = game.getField().getLineByName(sideString + "FieldLeftPenaltyStretch");
                float dist = Math.min(Math.abs(robot.getPosition().getX() - penaltyStretch.p1().getX()), Math.min(Math.abs(robot.getPosition().getY() - rightPenaltyStretch.p1().getY()), Math.abs(robot.getPosition().getY() - leftPenaltyStretch.p1().getY())));

                if (!botStillOnCoolDown(robot.getIdentifier(), game.getTime())) {
                    lastViolations.put(robot.getIdentifier(), game.getTime());
                    return new Violation(teamColor, robot.getId(), robot.getPosition().xy(), dist);
                }
            }
        }
        return null;
    }

    @Override
    public EnumSet<GameState> activeStates() {
        return EnumSet.of(GameState.RUNNING);
    }

    @Override
    public void reset(Game game) {
    }

    record Violation(TeamColor byTeam, int byBot, Vector2 location, float distance) implements RuleViolation {
        @Override
        public String toString() {
            return "Defender in defense area (by: " + byTeam + ", by bot #" + byBot + ", at " + location + ", distance: " + distance + ")";
        }

        @Override
        public SslGcGameEvent.GameEvent toPacket() {
            return SslGcGameEvent.GameEvent.newBuilder()
                    .setType(SslGcGameEvent.GameEvent.Type.DEFENDER_IN_DEFENSE_AREA)
                    .setDefenderInDefenseArea(SslGcGameEvent.GameEvent.DefenderInDefenseArea.newBuilder()
                            .setByTeam(byTeam == TeamColor.BLUE ? SslGcCommon.Team.BLUE : SslGcCommon.Team.YELLOW)
                            .setByBot(byBot)
                            .setLocation(SslGcGeometry.Vector2.newBuilder().setX(location.getX()).setY(location.getY()))
                            .setDistance(distance))
                    .build();
        }
    }
}
