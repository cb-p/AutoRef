package nl.roboteamtwente.autoref.validators;

import nl.roboteamtwente.autoref.RuleValidator;
import nl.roboteamtwente.autoref.RuleViolation;
import nl.roboteamtwente.autoref.model.*;
import org.robocup.ssl.proto.SslGcCommon;
import org.robocup.ssl.proto.SslGcGameEvent;

import java.text.DecimalFormat;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

public class BotTooFastInStopValidator implements RuleValidator {
    private static final float MAX_SPEED_ALLOWED = 1.5f;
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
            return (currentTimeStamp < timestampLastViolation + GRACE_PERIOD);
        }
        return false;
    }

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
        game.getState();
        return null;
    }

    @Override
    public void reset() {

    }

    @Override
    public EnumSet<GameState> activeStates() {
        return EnumSet.of(GameState.RUNNING);
    }

    record BotTooFastInStopViolation(int byBot, TeamColor byTeam) implements RuleViolation {
        @Override
        public String toString() {
            return "Bot too fast in stop (by: " + byTeam + ", bot #" + byBot + " )";
        }

        @Override
        public SslGcGameEvent.GameEvent toPacket() {
            return SslGcGameEvent.GameEvent.newBuilder()
                    .setType(SslGcGameEvent.GameEvent.Type.BOT_TOO_FAST_IN_STOP)
                    .setBotTooFastInStop(SslGcGameEvent.GameEvent.BotTooFastInStop.newBuilder()
                            .setByBot(byBot)
                            .setByTeam(byTeam == TeamColor.BLUE ? SslGcCommon.Team.BLUE : SslGcCommon.Team.YELLOW))
                    .build();

        }
    }
}
