package nl.roboteamtwente.autoref;

import nl.roboteamtwente.autoref.model.*;
import nl.roboteamtwente.proto.StateOuterClass;
import nl.roboteamtwente.proto.WorldOuterClass;
import nl.roboteamtwente.proto.WorldRobotOuterClass;
import org.robocup.ssl.proto.MessagesRobocupSslGeometry;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

public class SSLAutoRef {
    public Game game = new Game();

    public void processWorldState(StateOuterClass.State statePacket) {
        WorldOuterClass.World world = statePacket.getLastSeenWorld();

        game.getBall().getPosition().setX(world.getBall().getPos().getX() * 1000.0f);
        game.getBall().getPosition().setY(world.getBall().getPos().getY() * 1000.0f);
        game.getBall().getPosition().setZ(world.getBall().getZ() * 1000.0f);
        game.getBall().getVelocity().setX(world.getBall().getVel().getX());
        game.getBall().getVelocity().setY(world.getBall().getVel().getY());
        game.getBall().getVelocity().setZ(world.getBall().getZVel());

        for (WorldRobotOuterClass.WorldRobot robot : world.getBlueList()) {
            processRobotState(TeamColor.BLUE, robot);
        }

        for (WorldRobotOuterClass.WorldRobot robot : world.getYellowList()) {
            processRobotState(TeamColor.YELLOW, robot);
        }

        game.getTeam(TeamColor.BLUE).setRobotRadius(statePacket.getBlueRobotParameters().getParameters().getRadius() * 1000.0f);
        game.getTeam(TeamColor.YELLOW).setRobotRadius(statePacket.getYellowRobotParameters().getParameters().getRadius() * 1000.0f);

        game.getTeam(TeamColor.BLUE).setGoalkeeperId(statePacket.getReferee().getBlue().getGoalkeeper());
        game.getTeam(TeamColor.YELLOW).setGoalkeeperId(statePacket.getReferee().getYellow().getGoalkeeper());

        game.getField().getSize().setX(statePacket.getField().getField().getFieldLength());
        game.getField().getSize().setY(statePacket.getField().getField().getFieldWidth());
        game.getField().getPosition().setX(-statePacket.getField().getField().getFieldLength() / 2.0f);
        game.getField().getPosition().setY(-statePacket.getField().getField().getFieldWidth() / 2.0f);

        for (MessagesRobocupSslGeometry.SSL_FieldLineSegment lineSegment : statePacket.getField().getField().getFieldLinesList()) {
            Vector2 p1 = new Vector2(lineSegment.getP1().getX(), lineSegment.getP1().getY());
            Vector2 p2 = new Vector2(lineSegment.getP2().getX(), lineSegment.getP2().getY());
            FieldLine fieldLine = new FieldLine(lineSegment.getName(), p1, p2, lineSegment.getThickness());

            game.getField().addLine(fieldLine);
        }
    }

    private void processRobotState(TeamColor teamColor, WorldRobotOuterClass.WorldRobot worldRobot) {
        Robot robot = game.getTeam(teamColor).getRobotById(worldRobot.getId());
        if (robot == null) {
            robot = new Robot(worldRobot.getId());
            game.addRobot(robot);
            game.getTeam(teamColor).addRobot(robot);
        }

        robot.getPosition().setX(worldRobot.getPos().getX() * 1000.0f);
        robot.getPosition().setY(worldRobot.getPos().getY() * 1000.0f);
        robot.getVelocity().setX(worldRobot.getVel().getX() * 1000.0f);
        robot.getVelocity().setY(worldRobot.getVel().getY() * 1000.0f);
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
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }, "World receiver").start();
    }
}
