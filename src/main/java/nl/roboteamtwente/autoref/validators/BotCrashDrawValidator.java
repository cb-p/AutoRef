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

public class BotCrashDrawValidator implements RuleValidator {

    private static final float BOT_CRASH_DISTANCE = 0.2f;
    private static final float SPEED_VECTOR_THRESHOLD = 1.5f;

    private static final double MIN_SPEED_DIFFERENCE = 0.3;

    private static final double GRACE_PERIOD = 2.0;

    //Map from robotId -> last violation time
    private final Map<RobotIdentifier, Double> lastViolations = new HashMap<>();

    public float angleBetweenVectors(Vector2 velocity1, Vector2 velocity2) {
        float epsilon = 0.000001f;
        float dotProduct = velocity1.dotProduct(velocity2);
        float cosTheta = dotProduct / (velocity1.magnitude() * velocity2.magnitude());
        if (Math.abs(dotProduct - 0) < epsilon) {
            cosTheta = 0.0f;
        }
        return (float) Math.acos(cosTheta);
    }


    public static float calculateCollisionVelocity(Vector2 position1, Vector2 velocity1, Vector2 position2, Vector2 velocity2) {
        // Calculate velocity difference vector
        Vector2 velocityDifference = velocity2.subtract(velocity1);

        // Calculate position difference vector
        Vector2 positionDifference = position2.subtract(position1);

        // Calculate projection of velocity difference vector onto position difference vector
        float scalar = velocityDifference.dotProduct(positionDifference) / (positionDifference.magnitude() * positionDifference.magnitude());

        Vector2 projection = new Vector2(positionDifference.getX() * scalar, positionDifference.getY() * scalar);

        // Return the result
        return projection.magnitude();
    }

    private boolean botStillOnCoolDown(RobotIdentifier bot, double currentTS)
    {
        if (lastViolations.containsKey(bot))
        {
            Double ts = lastViolations.get(bot);
            return (currentTS < ts + GRACE_PERIOD);
        }
        return false;
    }

    @Override
    public RuleViolation validate(Game game) {
        for (TeamColor teamColor : TeamColor.values()) {

            for (Robot robotYellow : game.getTeam(teamColor.YELLOW).getRobots()) {
                if (botStillOnCoolDown(robotYellow.getIdentifier(), game.getTime())) {
                    continue;
                }
                for (Robot robotBlue : game.getTeam(teamColor.BLUE).getRobots()) {
                    if (botStillOnCoolDown(robotBlue.getIdentifier(), game.getTime())) {
                        continue;
                    }
                    Vector2 robotYellowPos = robotYellow.getPosition().xy();
                    Vector2 robotBluePos = robotBlue.getPosition().xy();
                    Vector2 robotYellowVel = robotYellow.getVelocity().xy();
                    Vector2 robotBlueVel = robotBlue.getVelocity().xy();

                    if (robotYellowPos.distance(robotBluePos) <= BOT_CRASH_DISTANCE) {
                        float crashSpeed = calculateCollisionVelocity(robotBluePos, robotBlueVel, robotYellowPos, robotYellowVel);
                        float speedDiff = robotBlueVel.magnitude() - robotYellowVel.magnitude();
                        //center position of 2 robots
                        Vector2 location = new Vector2((float) ((robotBluePos.getX() + robotYellowPos.getX()) * 0.5)
                                , (float) ((robotBluePos.getY() + robotYellowPos.getY()) * 0.5));
                        float crashAngle = angleBetweenVectors(robotBlueVel, robotYellowVel);
                        if (crashSpeed > SPEED_VECTOR_THRESHOLD) {
                            if (Math.abs(speedDiff) >= MIN_SPEED_DIFFERENCE) {
                                continue;
                            } else {
                                int botBlue = robotBlue.getId();
                                int botYellow = robotYellow.getId();
                                if ((!botStillOnCoolDown(robotBlue.getIdentifier(), game.getTime())) && (!botStillOnCoolDown(robotYellow.getIdentifier(), game.getTime()))) {
                                    System.out.println("Here");
                                    lastViolations.put(robotBlue.getIdentifier(), game.getTime());
                                    lastViolations.put(robotYellow.getIdentifier(), game.getTime());
                                    return new BotCrashDrawValidator.Violation(botBlue, botYellow, location, crashSpeed, speedDiff, crashAngle);
                                }
                            }
                        }
                    }
                }
            }

        }

        return null;
    }

    @Override
    public EnumSet<GameState> activeStates() {
        return EnumSet.of(GameState.RUNNING);
    }


    record Violation(int botBlue, int botYellow, Vector2 location, float crash_speed, float speed_diff, float crash_angle) implements RuleViolation {
        @Override
        public String toString() {
            return "Bot crash drawn (bot blue #" + botBlue + " , bot yellow #" + botYellow + ", at " + location + " crash speed :" + crash_speed
                    + " speed diff: " + speed_diff +  " angle:" + crash_angle + " )";
        }

        @Override
        public SslGcGameEvent.GameEvent toPacket() {
            return SslGcGameEvent.GameEvent.newBuilder()
                    .setType(SslGcGameEvent.GameEvent.Type.BOT_CRASH_DRAWN)
                    .setBotCrashDrawn(SslGcGameEvent.GameEvent.BotCrashDrawn.newBuilder()
                            .setBotBlue(botBlue)
                            .setBotYellow(botYellow)
                            .setCrashSpeed(crash_speed)
                            .setSpeedDiff(speed_diff)
                            .setCrashAngle(crash_angle)
                            .setLocation(SslGcGeometry.Vector2.newBuilder().setX(location.getX()).setY(location.getY())))
                    .build();

        }
    }
}
