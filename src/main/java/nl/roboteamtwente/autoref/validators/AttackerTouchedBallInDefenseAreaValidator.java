package nl.roboteamtwente.autoref.validators;

import nl.roboteamtwente.autoref.RuleValidator;
import nl.roboteamtwente.autoref.RuleViolation;
import nl.roboteamtwente.autoref.model.*;
import org.robocup.ssl.proto.SslGcCommon;
import org.robocup.ssl.proto.SslGcGameEvent;
import org.robocup.ssl.proto.SslGcGeometry;

public class AttackerTouchedBallInDefenseAreaValidator implements RuleValidator {
    @Override
    public RuleViolation validate(Game game) {
        // FIXME: This should only return if the ball is in play, we should make an
        //        abstraction for that based on the current game state.
        // FIXME: There should probably be some kind of more general way to implement
        //        grace periods and repeated fouls.


        // FIXME: This doesn't work for non-straight lines
        for (Robot robot : game.getBall().getRobotsTouching()) {
            Team team = robot.getTeam();
            Side side = team.getSide();
            String sideString = side == Side.LEFT ? "Left" : "Right";

            FieldLine penaltyStretch = game.getField().getLineByName(sideString + "PenaltyStretch");
            if (robot.getPosition().getX() * side.getCardinality() < penaltyStretch.p1().getX() * side.getCardinality()) {
                continue;
            }

            FieldLine rightPenaltyStretch = game.getField().getLineByName(sideString + "FieldRightPenaltyStretch");
            if (robot.getPosition().getY() < rightPenaltyStretch.p1().getY()) {
                continue;
            }

            FieldLine leftPenaltyStretch = game.getField().getLineByName(sideString + "FieldLeftPenaltyStretch");
            if (robot.getPosition().getY() > leftPenaltyStretch.p1().getY()) {
                continue;
            }

            return new Violation(team.getColor().getOpponentColor(), robot.getId(), robot.getPosition().xy(), 0.0f);
        }

        return null;
    }

    record Violation(TeamColor byTeam, int byBot, Vector2 location, float distance) implements RuleViolation {
        @Override
        public String toString() {
            return "Attacker in defense area (by: " + byTeam + ", by bot #" + byBot + ", at " + location + ", distance: " + distance + ")";
        }

        @Override
        public SslGcGameEvent.GameEvent toPacket() {
            return SslGcGameEvent.GameEvent.newBuilder()
                    .setType(SslGcGameEvent.GameEvent.Type.ATTACKER_TOUCHED_BALL_IN_DEFENSE_AREA)
                    .setAttackerTouchedBallInDefenseArea(SslGcGameEvent.GameEvent.AttackerTouchedBallInDefenseArea.newBuilder()
                            .setByTeam(byTeam == TeamColor.BLUE ? SslGcCommon.Team.BLUE : SslGcCommon.Team.YELLOW)
                            .setByBot(byBot)
                            .setLocation(SslGcGeometry.Vector2.newBuilder().setX(location.getX()).setY(location.getY()))
                            .setDistance(distance))
                    .build();
        }
    }
}
