package nl.roboteamtwente.autoref.validators;

import nl.roboteamtwente.autoref.RuleValidator;
import nl.roboteamtwente.autoref.RuleViolation;
import nl.roboteamtwente.autoref.model.*;
import org.robocup.ssl.proto.SslGcCommon;
import org.robocup.ssl.proto.SslGcGameEvent;
import org.robocup.ssl.proto.SslGcGeometry;

import java.text.DecimalFormat;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

public class BotTooFastInStopValidator implements RuleValidator {
    private static final float MAX_SPEED_ALLOWED = 1.5f;
    private static final double GRACE_PERIOD = 2.0;

    private static double startStop = Float.POSITIVE_INFINITY;
    //Map from robotId -> speed
    private final Map<RobotIdentifier, Float> violatorsMap = new HashMap<>();

    /**
     * Round float number to 1 decimal place
     * @param number
     * @return rounded float number
     */
    public float roundFloatTo1DecimalPlace(float number) {
        DecimalFormat df = new DecimalFormat("#.#"); // Creates a decimal format object with one decimal place
        String roundedFloatStr = df.format(number); // Formats the float as a string with one decimal place
        return Float.parseFloat(roundedFloatStr); // Parses the rounded string back into a float
    }

    @Override
    public RuleViolation validate(Game game) {
        if (game.getState() == GameState.STOP){
            if (game.getTime() - startStop <= GRACE_PERIOD) {
                return null;
            }
            for (Robot robot : game.getRobots()) {
                float robotSpeed = robot.getVelocity().xy().magnitude();
                //Rule state: A robot must not move faster than 1.5 meters per second during stop. A violation of this rule is only counted once per robot and stoppage.
                if ((robotSpeed > MAX_SPEED_ALLOWED) && (!violatorsMap.keySet().contains(robot.getIdentifier()))) {
                    violatorsMap.put(robot.getIdentifier(), robotSpeed);
                    return new BotTooFastInStopValidator.BotTooFastInStopViolation(robot.getId(), robot.getTeam().getColor(), robot.getPosition().xy(), robotSpeed);
                }
            }
        }
        return null;
    }

    @Override
    public void reset(Game game) {
        if (game.getState() == GameState.STOP) {
            startStop = game.getTime();
        } else {
            // clear violation after STOP
            violatorsMap.clear();
            startStop = Float.POSITIVE_INFINITY;
        }
    }

    @Override
    public EnumSet<GameState> activeStates() {
        return EnumSet.of(GameState.STOP);
    }

    record BotTooFastInStopViolation(int byBot, TeamColor byTeam, Vector2 location, Float speed) implements RuleViolation {
        @Override
        public String toString() {
            return "Bot too fast in stop (by: " + byTeam + ", bot #" + byBot + " speed: " + speed + " )";
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
