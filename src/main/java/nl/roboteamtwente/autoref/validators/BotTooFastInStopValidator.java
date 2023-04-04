package nl.roboteamtwente.autoref.validators;

import nl.roboteamtwente.autoref.RuleValidator;
import nl.roboteamtwente.autoref.RuleViolation;
import nl.roboteamtwente.autoref.model.*;
import org.robocup.ssl.proto.SslGcCommon;
import org.robocup.ssl.proto.SslGcGameEvent;
import org.robocup.ssl.proto.SslGcGeometry;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class BotTooFastInStopValidator implements RuleValidator {
    private static final float MAX_SPEED_ALLOWED = 1.5f;

    //GRACE PERIOD is 2 second after STOP start for robot to slow down
    private static final double GRACE_PERIOD = 2.0;

    private static double startStop = Float.POSITIVE_INFINITY;

    //Set of violators in STOP state
    private final Set<RobotIdentifier> violatorsSet = new HashSet<>();

    private final HashMap<TeamColor, Double> teamLastViolation = new HashMap<>();

    @Override
    public RuleViolation validate(Game game) {
        if (game.getTime() - startStop <= GRACE_PERIOD) {
            return null;
        }

        for (TeamColor team : TeamColor.values()) {
            if (teamLastViolation.containsKey(team) && teamLastViolation.get(team) + GRACE_PERIOD > game.getTime()) {
                for (Robot robot : game.getTeam(team).getRobots()) {
                    RuleViolation violation = validateRobot(robot);
                    if (violation != null) {
                        teamLastViolation.put(team, game.getTime());
                        return violation;
                    }
                }
            }
        }

        return null;
    }

    /**
     * Check if the robot is too fast
     * @param robot robot
     * @return violation record || null
     */
    public RuleViolation validateRobot(Robot robot) {
        float robotSpeed = robot.getVelocity().xy().magnitude();
        //Rule state: A robot must not move faster than 1.5 meters per second during stop. A violation of this rule is only counted once per robot and stoppage.
        if (robotSpeed > MAX_SPEED_ALLOWED && !violatorsSet.contains(robot.getIdentifier())) {
            violatorsSet.add(robot.getIdentifier());
            return new BotTooFastInStopValidator.BotTooFastInStopViolation(robot.getId(), robot.getTeam().getColor(), robot.getPosition().xy(), robotSpeed);
        }
        return null;
    }

    @Override
    public void reset(Game game) {
        violatorsSet.clear();
        teamLastViolation.clear();
        startStop = game.getTime();
    }

    @Override
    public boolean isActive(Game game) {
        return game.getState() == GameState.STOP;
    }

    record BotTooFastInStopViolation(int byBot, TeamColor byTeam, Vector2 location,
                                     Float speed) implements RuleViolation {
        @Override
        public String toString() {
            return "Bot too fast in stop (by: " + byTeam + ", bot #" + byBot + ", speed: " + speed + ")";
        }

        @Override
        public SslGcGameEvent.GameEvent toPacket() {
            return SslGcGameEvent.GameEvent.newBuilder()
                    .setType(SslGcGameEvent.GameEvent.Type.BOT_TOO_FAST_IN_STOP)
                    .setBotTooFastInStop(SslGcGameEvent.GameEvent.BotTooFastInStop.newBuilder()
                            .setByBot(byBot)
                            .setByTeam(byTeam == TeamColor.BLUE ? SslGcCommon.Team.BLUE : SslGcCommon.Team.YELLOW)
                            .setSpeed(speed)
                            .setLocation(SslGcGeometry.Vector2.newBuilder().setX(location.getX()).setY(location.getY())))
                    .build();

        }
    }
}
