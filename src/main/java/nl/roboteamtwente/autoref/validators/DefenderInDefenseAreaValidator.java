package nl.roboteamtwente.autoref.validators;

import nl.roboteamtwente.autoref.RuleValidator;
import nl.roboteamtwente.autoref.RuleViolation;
import nl.roboteamtwente.autoref.model.*;
import org.robocup.ssl.proto.SslGcCommon;
import org.robocup.ssl.proto.SslGcGameEvent;
import org.robocup.ssl.proto.SslGcGeometry;

public class DefenderInDefenseAreaValidator implements RuleValidator {


    //FIXME: The logic here is implemented correctly and works well, however what should happen if there are 2 robots from each team both in the defense area?
    @Override
    public RuleViolation validate(Game game) {
        for (TeamColor teamColor : TeamColor.values()) {
            Side side = game.getTeam(teamColor).getSide();
            String sideString = side == Side.LEFT ? "Left" : "Right";
            for (Robot robot : game.getTeam(teamColor).getRobots()) {
                FieldLine penaltyStretch = game.getField().getLineByName(sideString + "PenaltyStretch");
                if (robot.getPosition().getX()  * side.getCardinality() < penaltyStretch.p1().getX()  * side.getCardinality() ) {
                    continue;
                }

                FieldLine rightPenaltyStretch = game.getField().getLineByName(sideString + "FieldRightPenaltyStretch");
                if (robot.getPosition().getY()  * side.getCardinality() > rightPenaltyStretch.p1().getY() * side.getCardinality() ) {
                    continue;
                }

                FieldLine leftPenaltyStretch = game.getField().getLineByName(sideString + "FieldLeftPenaltyStretch");
                if (robot.getPosition().getY() * side.getCardinality() < leftPenaltyStretch.p1().getY() * side.getCardinality()) {
                    continue;
                }


                return new Violation(teamColor, robot.getId(), robot.getPosition().xy(), 0.0f);

            }
        }
        return null;
    }

    record Violation(TeamColor byTeam, int byBot, Vector2 location, float distance) implements RuleViolation {
        @Override
        public String toString() {
            return "Defender in defense area (by: " + byTeam + ", by bot #" + byBot + ", at " + location + ", distance: " + distance + ")";
        }

        @Override
        public SslGcGameEvent.GameEvent toPacket() {
            return SslGcGameEvent.GameEvent.newBuilder()
                    .setType(SslGcGameEvent.GameEvent.Type.DEFENDER_IN_DEFENSE_AREA)
                    .setDefenderInDefenseArea(SslGcGameEvent.GameEvent.DefenderInDefenseArea.newBuilder()
                            .setByTeam(byTeam == TeamColor.BLUE ? SslGcCommon.Team.BLUE : SslGcCommon.Team.YELLOW)
                            .setByBot(byBot)
                            .setLocation(SslGcGeometry.Vector2.newBuilder().setX(location.getX()).setY(location.getY()))
                            .setDistance(distance))
                    .build();
        }
    }
}
