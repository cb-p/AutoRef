package nl.roboteamtwente.autoref.validators;

import nl.roboteamtwente.autoref.RuleValidator;
import nl.roboteamtwente.autoref.RuleViolation;
import nl.roboteamtwente.autoref.model.*;
import org.robocup.ssl.proto.MessagesRobocupSslGameControllerCommon;
import org.robocup.ssl.proto.MessagesRobocupSslGameControllerGeometry;
import org.robocup.ssl.proto.MessagesRobocupSslGameEvent;

public class AttackerTouchedBallInDefenseAreaValidator implements RuleValidator {
    @Override
    public RuleViolation validate(Game game) {
        for (TeamColor teamColor : TeamColor.values()) {
            Side side = game.getTeam(teamColor).getSide();
            String sideString = side == Side.LEFT ? "Left" : "Right";

            // FIXME: This doesn't work for non-straight lines
            for (Robot robot : game.getTeam(teamColor.getOpponentColor()).getRobots()) {
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

                return new Violation(teamColor.getOpponentColor(), robot.getId(), robot.getPosition().xy(), 0.0f);
            }
        }

        return null;
    }

    record Violation(TeamColor byTeam, int byBot, Vector2 location, float distance) implements RuleViolation {
        @Override
        public String toString() {
            return "Attacker in defense area (by: " + byTeam + ", by bot #" + byBot + ", at " + location + ", distance: " + distance + ")";
        }

        @Override
        public MessagesRobocupSslGameEvent.GameEvent toPacket() {
            return MessagesRobocupSslGameEvent.GameEvent.newBuilder()
                    .setType(MessagesRobocupSslGameEvent.GameEvent.Type.ATTACKER_TOUCHED_BALL_IN_DEFENSE_AREA)
                    .setAttackerTouchedBallInDefenseArea(MessagesRobocupSslGameEvent.GameEvent.AttackerTouchedBallInDefenseArea.newBuilder()
                            .setByTeam(byTeam == TeamColor.BLUE ? MessagesRobocupSslGameControllerCommon.Team.BLUE : MessagesRobocupSslGameControllerCommon.Team.YELLOW)
                            .setByBot(byBot)
                            .setLocation(MessagesRobocupSslGameControllerGeometry.Vector2.newBuilder().setX(location.getX()).setY(location.getY()))
                            .setDistance(distance))
                    .build();
        }
    }
}
