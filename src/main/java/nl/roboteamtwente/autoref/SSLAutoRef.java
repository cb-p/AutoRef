package nl.roboteamtwente.autoref;

import nl.roboteamtwente.autoref.model.*;
import nl.roboteamtwente.proto.StateOuterClass;
import nl.roboteamtwente.proto.WorldOuterClass;
import nl.roboteamtwente.proto.WorldRobotOuterClass;
import org.robocup.ssl.proto.SslVisionGeometry;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;

public class SSLAutoRef {
    private static final float BALL_TOUCHING_DISTANCE = 0.125f;

    private final Referee referee;

    private Thread worldThread;
    private GameControllerConnection gcConnection;

    private Consumer<RuleViolation> onViolation;
    private boolean active = false;

    public SSLAutoRef() {
        this.referee = new Referee();
    }

    public void processWorldState(StateOuterClass.State statePacket) {
        Game game = new Game();
        if (referee.getGame() != null) {
            game.setPrevious(referee.getGame());
        }

        WorldOuterClass.World world = statePacket.getLastSeenWorld();

        game.setTime(world.getTime() / 1000000000.0);

        game.setState(switch (statePacket.getReferee().getCommand()) {
            case HALT -> GameState.HALT;
            //noinspection deprecation
            case STOP, GOAL_YELLOW, GOAL_BLUE -> GameState.STOP;
            case NORMAL_START, FORCE_START -> GameState.RUNNING;
            case PREPARE_KICKOFF_YELLOW, PREPARE_KICKOFF_BLUE -> GameState.PREPARE_KICKOFF;
            case PREPARE_PENALTY_YELLOW, PREPARE_PENALTY_BLUE -> GameState.PREPARE_PENALTY;
            case DIRECT_FREE_YELLOW, DIRECT_FREE_BLUE -> GameState.DIRECT_FREE;
            //noinspection deprecation
            case INDIRECT_FREE_YELLOW, INDIRECT_FREE_BLUE -> GameState.INDIRECT_FREE;
            case TIMEOUT_YELLOW, TIMEOUT_BLUE -> GameState.TIMEOUT;
            case BALL_PLACEMENT_YELLOW, BALL_PLACEMENT_BLUE -> GameState.BALL_PLACEMENT;
        });

        game.getBall().getPosition().setX(world.getBall().getPos().getX());
        game.getBall().getPosition().setY(world.getBall().getPos().getY());
        game.getBall().getPosition().setZ(world.getBall().getZ());
        game.getBall().getVelocity().setX(world.getBall().getVel().getX());
        game.getBall().getVelocity().setY(world.getBall().getVel().getY());
        game.getBall().getVelocity().setZ(world.getBall().getZVel());

        for (WorldRobotOuterClass.WorldRobot robot : world.getBlueList()) {
            processRobotState(game, TeamColor.BLUE, robot);
        }

        for (WorldRobotOuterClass.WorldRobot robot : world.getYellowList()) {
            processRobotState(game, TeamColor.YELLOW, robot);
        }

        game.getTeam(TeamColor.BLUE).setRobotRadius(statePacket.getBlueRobotParameters().getParameters().getRadius());
        game.getTeam(TeamColor.YELLOW).setRobotRadius(statePacket.getYellowRobotParameters().getParameters().getRadius());

        game.getTeam(TeamColor.BLUE).setGoalkeeperId(statePacket.getReferee().getBlue().getGoalkeeper());
        game.getTeam(TeamColor.YELLOW).setGoalkeeperId(statePacket.getReferee().getYellow().getGoalkeeper());

        game.getTeam(TeamColor.BLUE).setSide(statePacket.getReferee().getBlueTeamOnPositiveHalf() ? Side.RIGHT : Side.LEFT);
        game.getTeam(TeamColor.YELLOW).setSide(statePacket.getReferee().getBlueTeamOnPositiveHalf() ? Side.LEFT : Side.RIGHT);

        game.getField().setBoundaryWidth(statePacket.getField().getField().getBoundaryWidth() / 1000.0f);
        game.getField().getSize().setX(statePacket.getField().getField().getFieldLength() / 1000.0f);
        game.getField().getSize().setY(statePacket.getField().getField().getFieldWidth() / 1000.0f);
        game.getField().getPosition().setX(-statePacket.getField().getField().getFieldLength() / 2.0f / 1000.0f);
        game.getField().getPosition().setY(-statePacket.getField().getField().getFieldWidth() / 2.0f / 1000.0f);

        for (SslVisionGeometry.SSL_FieldLineSegment lineSegment : statePacket.getField().getField().getFieldLinesList()) {
            Vector2 p1 = new Vector2(lineSegment.getP1().getX() / 1000.0f, lineSegment.getP1().getY() / 1000.0f);
            Vector2 p2 = new Vector2(lineSegment.getP2().getX() / 1000.0f, lineSegment.getP2().getY() / 1000.0f);
            FieldLine fieldLine = new FieldLine(lineSegment.getName(), p1, p2, lineSegment.getThickness() / 1000.0f);

            game.getField().addLine(fieldLine);
        }

        deriveWorldState(game);
        referee.setGame(game);
    }

    private void deriveWorldState(Game game) {
        // FIXME: When ball goes out of play, reset state variables.

        Ball ball = game.getBall();
        Vector3 ballPosition = ball.getPosition();

        game.getBall().getRobotsTouching().clear();
        for (Robot robot : game.getRobots()) {
            Robot oldRobot = game.getPrevious().getRobot(robot.getIdentifier());

            // FIXME: is this a good way to detect if a robot is touching the ball?
            float distance = robot.getPosition().xy().distance(ballPosition.xy());
            if (distance <= BALL_TOUCHING_DISTANCE) {
                ball.getRobotsTouching().add(robot);

                robot.setJustTouchedBall(oldRobot == null || !oldRobot.isTouchingBall());
                robot.setTouchingBall(true);
            } else {
                robot.setJustTouchedBall(false);
                robot.setTouchingBall(false);
            }

            if (robot.hasJustTouchedBall()) {
                ball.setLastTouchedBy(robot);
                ball.setLastTouchedAt(ball.getPosition());
            } else {
                ball.setLastTouchedBy(game.getPrevious().getBall().getLastTouchedBy());
                ball.setLastTouchedAt(game.getPrevious().getBall().getLastTouchedAt());
            }
        }
    }

    private void processRobotState(Game game, TeamColor teamColor, WorldRobotOuterClass.WorldRobot worldRobot) {
        Robot robot = game.getTeam(teamColor).getRobotById(worldRobot.getId());
        if (robot == null) {
            robot = new Robot(worldRobot.getId());
            game.addRobot(robot);
            game.getTeam(teamColor).addRobot(robot);
        }

        robot.getPosition().setX(worldRobot.getPos().getX());
        robot.getPosition().setY(worldRobot.getPos().getY());
        robot.getVelocity().setX(worldRobot.getVel().getX());
        robot.getVelocity().setY(worldRobot.getVel().getY());
        robot.setAngle(worldRobot.getAngle());
    }

    /**
     *
     * @param ip ip where all software is running on
     * @param portGameController port GameContoller
     * @param portWorld port World
     */
    public void start(String ip, int portGameController, int portWorld) {
        //setup connection with GameControl
        try {
            gcConnection = new GameControllerConnection();
            gcConnection.connect(ip, portGameController);
            createWorldThread(ip, portGameController, portWorld);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void createWorldThread(String ip, int portGameController, int portWorld) {
        worldThread = new Thread(() -> {
            try (ZContext context = new ZContext()) {
                //Create connection
                ZMQ.Socket worldSocket = context.createSocket(SocketType.SUB);
                worldSocket.subscribe("");
                worldSocket.connect("tcp:// " + ip + ":" + portWorld);

                //While AutoRef is running
                while (!Thread.currentThread().isInterrupted()) {
                    try {
                        //receive packet from world
                        byte[] buffer = worldSocket.recv();
                        StateOuterClass.State packet = StateOuterClass.State.parseFrom(buffer);
                        processWorldState(packet);

                        //check for any violations
                        List<RuleViolation> violations = referee.validate();
                        for (RuleViolation violation : violations) {
                            //TODO what does this do?
                            if (onViolation != null) {
                                onViolation.accept(violation);
                            }

                            //if AutoRef is in active mode, try to send gameEvent
                            if (active) {
                                if (gcConnection.isConnected()) {
                                    gcConnection.sendGameEvent(violation.toPacket());
                                } else {
                                    //reconnect
                                    gcConnection.connect(ip, portGameController);
                                    gcConnection.sendGameEvent(violation.toPacket());
                                }
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }, "World Connection");
        worldThread.start();
    }


    public void start() {
        // FIXME: All still pretty temporary.
        try {
            gcConnection = new GameControllerConnection();
            gcConnection.connect("localhost", 10007);
        } catch (Exception e) {
            e.printStackTrace();
        }

        worldThread = new Thread(() -> {
            try (ZContext context = new ZContext()) {
                ZMQ.Socket worldSocket = context.createSocket(SocketType.SUB);

                worldSocket.subscribe("");
                worldSocket.connect("tcp://127.0.0.1:5558");

                while (!Thread.currentThread().isInterrupted()) {
                    try {
                        byte[] buffer = worldSocket.recv();
                        StateOuterClass.State packet = StateOuterClass.State.parseFrom(buffer);
                        processWorldState(packet);

                        List<RuleViolation> violations = referee.validate();
                        for (RuleViolation violation : violations) {
                            if (onViolation != null) {
                                onViolation.accept(violation);
                            }

                            if (active && gcConnection.isConnected()) {
                                gcConnection.sendGameEvent(violation.toPacket());
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }, "World Connection");
        worldThread.start();
    }

    public void stop() {
        // FIXME: Very dirty way to stop everything.
        try {
            gcConnection.disconnect();
            worldThread.stop();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void setOnViolation(Consumer<RuleViolation> onViolation) {
        this.onViolation = onViolation;
    }

    public void setActive(boolean active) {
        // FIXME: Disconnect from game controller while not active.
        this.active = active;
    }

    public Referee getReferee() {
        return referee;
    }
}
