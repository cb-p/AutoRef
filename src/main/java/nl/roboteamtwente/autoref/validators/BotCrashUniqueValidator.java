package nl.roboteamtwente.autoref.validators;

import nl.roboteamtwente.autoref.RuleValidator;
import nl.roboteamtwente.autoref.RuleViolation;
import nl.roboteamtwente.autoref.model.*;
import org.robocup.ssl.proto.SslGcCommon;
import org.robocup.ssl.proto.SslGcGameEvent;
import org.robocup.ssl.proto.SslGcGeometry;

import java.util.EnumSet;

public class BotCrashUniqueValidator implements RuleValidator {

    private static final float BOT_CRASH_DISTANCE = 0.21f;
    private static final float SPEED_VECTOR_THRESHOLD = 1.5f;

    private static final double MIN_SPEED_DIFFERENCE = 0.3;

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

    @Override
    public RuleViolation validate(Game game) {

        for (TeamColor teamColor : TeamColor.values()) {
            for (Robot robotYellow : game.getTeam(teamColor.YELLOW).getRobots()) {
                for (Robot robotBlue : game.getTeam(teamColor.BLUE).getRobots()) {
                    Vector2 robotYellowPos = robotYellow.getPosition().xy();
                    Vector2 robotBluePos = robotBlue.getPosition().xy();
                    Vector2 robotYellowVel = robotYellow.getVelocity().xy();
                    Vector2 robotBlueVel = robotBlue.getVelocity().xy();
                    float distanceBetweenRobots = robotYellowPos.distance(robotBluePos);

                    if (distanceBetweenRobots <= BOT_CRASH_DISTANCE) {

//                        TODO check if bot in cool down Wait time before reporting a crash with a robot again
                        float crashSpeed = calculateCollisionVelocity(robotBluePos, robotBlueVel, robotYellowPos, robotYellowVel);
                        float speedDiff = robotBlueVel.magnitude() - robotYellowVel.magnitude();
                        //center position of 2 robots

                        Vector2 location = new Vector2((float) ((robotBluePos.getX() + robotYellowPos.getX()) * 0.5)
                                , (float) ((robotBluePos.getY() + robotYellowPos.getY()) * 0.5));
                        float crashAngle = angleBetweenVectors(robotBlueVel, robotYellowVel);
                        if (crashSpeed > SPEED_VECTOR_THRESHOLD) {
                            if (Math.abs(speedDiff) < MIN_SPEED_DIFFERENCE) {
                                continue;
                            } else {
                                int violator;
                                int victim;
                                TeamColor byTeam;
                                if (speedDiff > 0) {
                                    violator = robotBlue.getId();
                                    victim = robotYellow.getId();
                                    byTeam = TeamColor.BLUE;
                                } else {
                                    violator = robotYellow.getId();
                                    victim = robotBlue.getId();
                                    byTeam = TeamColor.YELLOW;
                                }
//                                System.out.println("VIOLATION");
//                                System.out.println(byTeam);
//                                System.out.println(violator);
//                                System.out.println(victim);
//                                System.out.println(crashSpeed);
//                                System.out.println(speedDiff);
//                                System.out.println(location);
//                                System.out.println(crashAngle);
                                return new Violation(distanceBetweenRobots, byTeam, violator, victim, location, crashSpeed, speedDiff, crashAngle);
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


    record Violation(float distance, TeamColor byTeam, int violator, int victim, Vector2 location, float crash_speed, float speed_diff, float crash_angle) implements RuleViolation {
        @Override
        public String toString() {
            return "Bot crash unique (by: " + byTeam + ", main violator #" + violator + " , victim #" + victim + "distance: " + distance + ", at " + location + " crash speed :" + crash_speed
                    + " speed diff: " + speed_diff + " angle:" + crash_angle + " )";
        }

        @Override
        public SslGcGameEvent.GameEvent toPacket() {
            return SslGcGameEvent.GameEvent.newBuilder()
                    .setType(SslGcGameEvent.GameEvent.Type.BOT_CRASH_UNIQUE)
                    .setBotCrashUnique(SslGcGameEvent.GameEvent.BotCrashUnique.newBuilder()
                            .setByTeam(byTeam == TeamColor.BLUE ? SslGcCommon.Team.BLUE : SslGcCommon.Team.YELLOW)
                            .setViolator(violator)
                            .setVictim(victim)
                            .setCrashSpeed(crash_speed)
                            .setSpeedDiff(speed_diff)
                            .setCrashAngle(crash_angle)
                            .setLocation(SslGcGeometry.Vector2.newBuilder().setX(location.getX()).setY(location.getY())))
                    .build();

        }
    }
}