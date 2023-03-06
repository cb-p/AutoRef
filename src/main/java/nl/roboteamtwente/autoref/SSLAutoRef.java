package nl.roboteamtwente.autoref;

import nl.roboteamtwente.autoref.model.*;
import nl.roboteamtwente.proto.StateOuterClass;
import nl.roboteamtwente.proto.WorldOuterClass;
import nl.roboteamtwente.proto.WorldRobotOuterClass;
import org.robocup.ssl.proto.SslVisionGeometry;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import java.util.List;

public class SSLAutoRef {
    private static final float BALL_TOUCHING_DISTANCE = 125.0f;

    private final Referee referee;

    private ZMQ.Socket worldSocket;
    private GameControllerConnection gcConnection;

    public SSLAutoRef() {
        this.referee = new Referee();
    }

    public void processWorldState(StateOuterClass.State statePacket) {
        WorldOuterClass.World world = statePacket.getLastSeenWorld();

        referee.getGame().getBall().getPosition().setX(world.getBall().getPos().getX() * 1000.0f);
        referee.getGame().getBall().getPosition().setY(world.getBall().getPos().getY() * 1000.0f);
        referee.getGame().getBall().getPosition().setZ(world.getBall().getZ() * 1000.0f);
        referee.getGame().getBall().getVelocity().setX(world.getBall().getVel().getX());
        referee.getGame().getBall().getVelocity().setY(world.getBall().getVel().getY());
        referee.getGame().getBall().getVelocity().setZ(world.getBall().getZVel());

        for (WorldRobotOuterClass.WorldRobot robot : world.getBlueList()) {
            processRobotState(TeamColor.BLUE, robot);
        }

        for (WorldRobotOuterClass.WorldRobot robot : world.getYellowList()) {
            processRobotState(TeamColor.YELLOW, robot);
        }

        referee.getGame().getTeam(TeamColor.BLUE).setRobotRadius(statePacket.getBlueRobotParameters().getParameters().getRadius() * 1000.0f);
        referee.getGame().getTeam(TeamColor.YELLOW).setRobotRadius(statePacket.getYellowRobotParameters().getParameters().getRadius() * 1000.0f);

        referee.getGame().getTeam(TeamColor.BLUE).setGoalkeeperId(statePacket.getReferee().getBlue().getGoalkeeper());
        referee.getGame().getTeam(TeamColor.YELLOW).setGoalkeeperId(statePacket.getReferee().getYellow().getGoalkeeper());

        referee.getGame().getTeam(TeamColor.BLUE).setSide(statePacket.getReferee().getBlueTeamOnPositiveHalf() ? Side.RIGHT : Side.LEFT);
        referee.getGame().getTeam(TeamColor.YELLOW).setSide(statePacket.getReferee().getBlueTeamOnPositiveHalf() ? Side.LEFT : Side.RIGHT);

        referee.getGame().getField().setBoundaryWidth(statePacket.getField().getField().getBoundaryWidth());
        referee.getGame().getField().getSize().setX(statePacket.getField().getField().getFieldLength());
        referee.getGame().getField().getSize().setY(statePacket.getField().getField().getFieldWidth());
        referee.getGame().getField().getPosition().setX(-statePacket.getField().getField().getFieldLength() / 2.0f);
        referee.getGame().getField().getPosition().setY(-statePacket.getField().getField().getFieldWidth() / 2.0f);

        for (SslVisionGeometry.SSL_FieldLineSegment lineSegment : statePacket.getField().getField().getFieldLinesList()) {
            Vector2 p1 = new Vector2(lineSegment.getP1().getX(), lineSegment.getP1().getY());
            Vector2 p2 = new Vector2(lineSegment.getP2().getX(), lineSegment.getP2().getY());
            FieldLine fieldLine = new FieldLine(lineSegment.getName(), p1, p2, lineSegment.getThickness());

            referee.getGame().getField().addLine(fieldLine);
        }
    }

    private void deriveWorldState() {
        // FIXME: When ball goes out of play, reset state variables.

        Ball ball = getReferee().getGame().getBall();
        Vector3 ballPosition = ball.getPosition();

        getReferee().getGame().getBall().getRobotsTouching().clear();
        for (Robot robot : getReferee().getGame().getRobots()) {
            // FIXME: is this a good way to detect if a robot is touching the ball?
            float distance = robot.getPosition().xy().distance(ballPosition.xy());
            if (distance <= BALL_TOUCHING_DISTANCE) {
                ball.getRobotsTouching().add(robot);

                robot.setJustTouchedBall(!robot.isTouchingBall());
                robot.setTouchingBall(true);
            } else {
                robot.setJustTouchedBall(false);
                robot.setTouchingBall(false);
            }

            if (robot.hasJustTouchedBall()) {
                ball.setLastTouchedBy(robot);
                ball.setLastTouchedAt(ball.getPosition());
            }
        }
    }

    private void processRobotState(TeamColor teamColor, WorldRobotOuterClass.WorldRobot worldRobot) {
        Robot robot = referee.getGame().getTeam(teamColor).getRobotById(worldRobot.getId());
        if (robot == null) {
            robot = new Robot(worldRobot.getId());
            referee.getGame().addRobot(robot);
            referee.getGame().getTeam(teamColor).addRobot(robot);
        }

        robot.getPosition().setX(worldRobot.getPos().getX() * 1000.0f);
        robot.getPosition().setY(worldRobot.getPos().getY() * 1000.0f);
        robot.getVelocity().setX(worldRobot.getVel().getX() * 1000.0f);
        robot.getVelocity().setY(worldRobot.getVel().getY() * 1000.0f);
        robot.setAngle(worldRobot.getAngle());
    }

    public void start() {
        // FIXME: All still pretty temporary.
        try {
            gcConnection = new GameControllerConnection();
            gcConnection.connect("localhost", 10007);
        } catch (Exception e) {
            e.printStackTrace();
        }

        new Thread(() -> {
            try (ZContext context = new ZContext()) {
                this.worldSocket = context.createSocket(SocketType.SUB);

                this.worldSocket.subscribe("");
                this.worldSocket.connect("tcp://127.0.0.1:5558");

                while (!Thread.currentThread().isInterrupted()) {
                    try {
                        byte[] buffer = this.worldSocket.recv();
                        StateOuterClass.State packet = StateOuterClass.State.parseFrom(buffer);
                        processWorldState(packet);
                        deriveWorldState();

                        System.out.println(referee.getGame().getBall().getRobotsTouching());

                        List<RuleViolation> violations = referee.validate();
                        // FIXME: do something with this
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }, "World Connection").start();
    }

    public Referee getReferee() {
        return referee;
    }
}
