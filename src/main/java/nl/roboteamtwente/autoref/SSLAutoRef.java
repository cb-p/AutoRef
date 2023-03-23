package nl.roboteamtwente.autoref;

import nl.roboteamtwente.autoref.model.*;
import nl.roboteamtwente.proto.StateOuterClass;
import nl.roboteamtwente.proto.WorldOuterClass;
import nl.roboteamtwente.proto.WorldRobotOuterClass;
import org.robocup.ssl.proto.SslVisionGeometry;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class SSLAutoRef {
    private static final float BALL_TOUCHING_DISTANCE = 0.025f;

    private final Referee referee;

    private Thread worldThread;
    private GameControllerConnection gcConnection;
    private Thread gcThread;

    private WorldConnection worldConnection;

    private Consumer<RuleViolation> onViolation;
    private boolean active = false;

    private int commands = 0;
    private int nextTouchId = 0;

    public SSLAutoRef() {
        this.referee = new Referee();
    }

    public void processWorldState(StateOuterClass.State statePacket) {
        Game game = new Game();
        if (referee.getGame() != null) {
            referee.getGame().setPrevious(null);
            game.setPrevious(referee.getGame());
        }

        WorldOuterClass.World world = statePacket.getCommandExtrapolatedWorld();

        game.setTime(world.getTime() / 1_000_000_000.0);
        game.setForceStarted(game.getPrevious().isForceStarted());

        if (game.getState() == null || statePacket.getReferee().getCommandCounter() != commands) {
            commands = statePacket.getReferee().getCommandCounter();

            switch (statePacket.getReferee().getCommand()) {
                case HALT -> {
                    game.setForceStarted(false);
                    game.setState(GameState.HALT);
                }
                case STOP -> {
                    game.setForceStarted(false);
                    game.setState(GameState.STOP);
                }
                case FORCE_START -> {
                    game.setForceStarted(true);
                    game.setState(GameState.RUNNING);
                }
                //noinspection deprecation
                case NORMAL_START, GOAL_YELLOW, GOAL_BLUE -> {
                    game.setForceStarted(false);
                    game.setState(GameState.RUNNING);
                }
                case PREPARE_KICKOFF_YELLOW, PREPARE_KICKOFF_BLUE -> {
                    game.setState(GameState.PREPARE_KICKOFF);
                }
                case PREPARE_PENALTY_YELLOW, PREPARE_PENALTY_BLUE -> {
                    game.setState(GameState.PREPARE_PENALTY);
                }
                case DIRECT_FREE_YELLOW, DIRECT_FREE_BLUE -> {
                    game.setState(GameState.DIRECT_FREE);
                }
                //noinspection deprecation
                case INDIRECT_FREE_YELLOW, INDIRECT_FREE_BLUE -> {
                    game.setState(GameState.INDIRECT_FREE);
                }
                case TIMEOUT_YELLOW, TIMEOUT_BLUE -> {
                    game.setState(GameState.TIMEOUT);
                }
                case BALL_PLACEMENT_YELLOW, BALL_PLACEMENT_BLUE -> {
                    game.setState(GameState.BALL_PLACEMENT);
                }
            }
            ;
        } else {
            game.setState(game.getPrevious().getState());
        }

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
        game.getTeam(TeamColor.BLUE).setRobotHeight(statePacket.getBlueRobotParameters().getParameters().getHeight());
        game.getTeam(TeamColor.YELLOW).setRobotHeight(statePacket.getYellowRobotParameters().getParameters().getHeight());

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

        if (game.getPrevious().getState() == GameState.RUNNING && game.getState() != GameState.RUNNING) {
            System.out.println("reset");

            game.getBall().setLastTouchStarted(null);
            game.setKickIntoPlay(null);
            game.getTouches().clear();

            for (Robot robot : game.getRobots()) {
                robot.setTouch(null);
            }
        }

        if (game.getState() != game.getPrevious().getState()) {
            System.out.println("game state: " + game.getPrevious().getState() + " -> " + game.getState());
        }

        referee.setGame(game);
    }

    private void deriveWorldState(Game game) {
        game.getBall().setLastTouchStarted(game.getPrevious().getBall().getLastTouchStarted());
        game.getTouches().addAll(game.getPrevious().getFinishedTouches());

        game.setKickIntoPlay(game.getPrevious().getKickIntoPlay());

        Ball ball = game.getBall();
        Vector3 ballPosition = ball.getPosition();

        for (Robot robot : game.getRobots()) {
            Robot oldRobot = game.getPrevious().getRobot(robot.getIdentifier());

            if (oldRobot != null) {
                robot.setTouch(oldRobot.getTouch());
                robot.setJustTouchedBall(oldRobot.hasJustTouchedBall());
            }

            Touch touch = robot.getTouch();

            // FIXME: is this a good way to detect if a robot is touching the ball?
            float distance = robot.getPosition().xy().distance(ballPosition.xy());
            if (distance <= robot.getTeam().getRobotRadius() + BALL_TOUCHING_DISTANCE && ball.getPosition().getZ() <= robot.getTeam().getRobotHeight() + BALL_TOUCHING_DISTANCE) {
                ball.getRobotsTouching().add(robot);
                robot.setJustTouchedBall(oldRobot == null || !oldRobot.isTouchingBall());
            } else {
                robot.setJustTouchedBall(false);
                robot.setTouch(null);

                if (touch != null) {
                    touch = new Touch(touch.id(), touch.startLocation(), ballPosition, touch.startTime(), game.getTime(), robot.getIdentifier());

                    if (Objects.equals(touch, game.getKickIntoPlay())) {
                        game.setKickIntoPlay(touch);
                    }
                }
            }

            if (robot.hasJustTouchedBall()) {
                touch = new Touch(nextTouchId++, ballPosition, null, game.getTime(), null, robot.getIdentifier());
                ball.setLastTouchStarted(touch);
                robot.setTouch(touch);

                System.out.print("touch #" + touch.id() + " by " + robot.getIdentifier());

                if ((game.getState() == GameState.INDIRECT_FREE || game.getState() == GameState.DIRECT_FREE || (game.getState() == GameState.RUNNING && !game.isForceStarted())) && game.getKickIntoPlay() == null) {
                    game.setKickIntoPlay(touch);
                    game.setState(GameState.RUNNING);

                    System.out.print(" (kick into play)");
                }

                System.out.println();
            }

            if (touch != null) {
                game.getTouches().add(touch);
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
     * Setup connections with all other software
     *
     * @param ip                 ip where all software is running on
     * @param portGameController port GameContoller
     * @param portWorld          port World
     */
    public void start(String ip, int portGameController, int portWorld) {
        //setup connection with GameControl
        gcConnection = new GameControllerConnection();
        gcConnection.setIp(ip);
        gcConnection.setPort(portGameController);
        gcThread = new Thread(gcConnection);
        gcThread.start();
        //setup connection with World
        worldConnection = new WorldConnection(ip, portWorld, this);
        worldThread = new Thread(worldConnection);
        worldThread.start();
    }

    /**
     * Process received packet and check for violations
     *
     * @param packet
     */
    public void checkViolations(StateOuterClass.State packet) {
        processWorldState(packet);
        //check for any violations
        List<RuleViolation> violations = getReferee().validate();
        for (RuleViolation violation : violations) {
            //violation to ui/AutoRefController.java
            if (onViolation != null) {
                onViolation.accept(violation);
            }

            if (isActive()) {
                gcConnection.addToQueue(violation.toPacket());
            }
        }
    }

    public void stop() {
        gcConnection.disconnect();
        gcThread.interrupt();
        worldConnection.close();
        worldThread.interrupt();
    }

    public void setOnViolation(Consumer<RuleViolation> onViolation) {
        this.onViolation = onViolation;
    }

    public void setActive(boolean active) {
        gcConnection.setActive(active);
        this.active = active;
    }

    public boolean isActive() {
        return active;
    }

    public Referee getReferee() {
        return referee;
    }
}
