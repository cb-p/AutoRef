package nl.roboteamtwente.autoref;

import nl.roboteamtwente.autoref.model.*;
import nl.roboteamtwente.proto.StateOuterClass;
import nl.roboteamtwente.proto.WorldOuterClass;
import nl.roboteamtwente.proto.WorldRobotOuterClass;
import org.robocup.ssl.proto.MessagesRobocupSslGeometry;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import java.util.List;

public class SSLAutoRef {
    public Referee referee = new Referee();

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

        referee.getGame().getField().getSize().setX(statePacket.getField().getField().getFieldLength());
        referee.getGame().getField().getSize().setY(statePacket.getField().getField().getFieldWidth());
        referee.getGame().getField().getPosition().setX(-statePacket.getField().getField().getFieldLength() / 2.0f);
        referee.getGame().getField().getPosition().setY(-statePacket.getField().getField().getFieldWidth() / 2.0f);

        for (MessagesRobocupSslGeometry.SSL_FieldLineSegment lineSegment : statePacket.getField().getField().getFieldLinesList()) {
            Vector2 p1 = new Vector2(lineSegment.getP1().getX(), lineSegment.getP1().getY());
            Vector2 p2 = new Vector2(lineSegment.getP2().getX(), lineSegment.getP2().getY());
            FieldLine fieldLine = new FieldLine(lineSegment.getName(), p1, p2, lineSegment.getThickness());

            referee.getGame().getField().addLine(fieldLine);
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


    public void startReceivingWorldPackets() {
        new Thread(() -> {
            try (ZContext context = new ZContext()) {
                ZMQ.Socket socket = context.createSocket(SocketType.SUB);

                socket.subscribe("");
                socket.connect("tcp://127.0.0.1:5558");

                while (!Thread.currentThread().isInterrupted()) {
                    try {
                        byte[] buffer = socket.recv();
                        StateOuterClass.State packet = StateOuterClass.State.parseFrom(buffer);
                        processWorldState(packet);

                        List<RuleViolation> violations = referee.validate();
//                        System.out.println(violations.toString());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }, "World receiver").start();
    }
}
