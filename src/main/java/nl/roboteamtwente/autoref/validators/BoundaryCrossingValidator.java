package nl.roboteamtwente.autoref.validators;

import nl.roboteamtwente.autoref.RuleValidator;
import nl.roboteamtwente.autoref.RuleViolation;
import nl.roboteamtwente.autoref.model.*;
import org.robocup.ssl.proto.SslGcCommon;
import org.robocup.ssl.proto.SslGcGameEvent;
import org.robocup.ssl.proto.SslGcGeometry;

import java.util.EnumSet;

public class BoundaryCrossingValidator implements RuleValidator {


    @Override
    public RuleViolation validate(Game game) {
        FieldLine rightGoalLine = game.getField().getLineByName("RightGoalLine");
        FieldLine leftGoalLine = game.getField().getLineByName("LeftGoalLine");

        Vector2 topLeft = new Vector2(leftGoalLine.p2().getX()-game.getField().getBoundaryWidth(), leftGoalLine.p2().getY()+game.getField().getBoundaryWidth());
        Vector2 bottomLeft = new Vector2(leftGoalLine.p1().getX()-game.getField().getBoundaryWidth(), leftGoalLine.p1().getY()-game.getField().getBoundaryWidth());

        Vector2 topRight = new Vector2(rightGoalLine.p2().getX()+game.getField().getBoundaryWidth(), rightGoalLine.p2().getY()+game.getField().getBoundaryWidth());
        Vector2 bottomRight = new Vector2(rightGoalLine.p2().getX()+game.getField().getBoundaryWidth(), rightGoalLine.p2().getY()-game.getField().getBoundaryWidth());

        FieldLine topBoundaryCrossing = new FieldLine("TopBoundaryCrossing", topLeft, topRight, 2);
        FieldLine bottomBoundaryCrossing = new FieldLine("BottomBoundaryCrossing", bottomLeft, bottomRight, 2);

        FieldLine rightBoundaryCrossing = new FieldLine("RightBoundaryCrossing", topRight, bottomRight, 2);
        FieldLine leftBoundaryCrossing = new FieldLine("RightBoundaryCrossing", topRight, bottomRight, 2);

        Vector2 location = null;
        TeamColor byTeam = null;
        Vector3 ball = game.getBall().getPosition();


        if (ball.getY() > topBoundaryCrossing.p1().getY()) {
            location = ball.xy();
            return new Violation(TeamColor.BLUE, location);
        }


        if (ball.getY() < bottomBoundaryCrossing.p1().getY()){
            location = ball.xy();
            return new Violation(TeamColor.BLUE, null);
        }


        if (ball.getX() > rightBoundaryCrossing.p1().getX()){
            location = ball.xy();
            return new Violation(TeamColor.BLUE, location);
        }

        if (ball.getX() < leftBoundaryCrossing.p1().getX()){
            location = ball.xy();
            return new Violation(TeamColor.BLUE, location);
        }

//        for (TeamColor teamColor : TeamColor.values()) {
//            for (Robot robot : game.getTeam(teamColor.getOpponentColor()).getRobots()) {
//                if (robot.hasJustTouchedBall() && ball.getY() > topBoundaryCrossing.p1().getY()) {
//                    byTeam = robot.getTeam().getColor();
//                    location = ball.xy();
//                    continue;
//                }
//
//
//                if (robot.hasJustTouchedBall() && ball.getY() < bottomBoundaryCrossing.p1().getY()){
//                    byTeam = robot.getTeam().getColor();
//                    location = ball.xy();
//                    continue;
//                }
//
//
//                if (robot.hasJustTouchedBall() && ball.getX() > rightBoundaryCrossing.p1().getX()){
//                    byTeam = robot.getTeam().getColor();
//                    location = ball.xy();
//                    continue;
//                }
//
//                if (robot.hasJustTouchedBall() && ball.getX() < leftBoundaryCrossing.p1().getX()){
//                    byTeam = robot.getTeam().getColor();
//                    location = ball.xy();
//                    continue;
//                }
//                return new Violation(byTeam, location);
//            }
//        }

        return null;
    }

    @Override
    public EnumSet<GameState> activeStates() {
        return null;
    }

    record Violation(TeamColor byTeam, Vector2 location) implements RuleViolation {
        @Override
        public String toString() {
            return "Ball left the Boundary Crossing (by: " + byTeam + ", at" + location +")";
        }

        @Override
        public SslGcGameEvent.GameEvent toPacket() {
            return SslGcGameEvent.GameEvent.newBuilder()
                    .setType(SslGcGameEvent.GameEvent.Type.BOUNDARY_CROSSING)
                    .setBoundaryCrossing(SslGcGameEvent.GameEvent.BoundaryCrossing.newBuilder()
                            .setByTeam(byTeam == TeamColor.BLUE ? SslGcCommon.Team.BLUE : SslGcCommon.Team.YELLOW)
                            .setLocation(SslGcGeometry.Vector2.newBuilder().setX(location.getX()).setY(location.getY())))
                    .build();

        }
    }
}
