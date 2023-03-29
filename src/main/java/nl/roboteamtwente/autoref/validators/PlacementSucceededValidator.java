package nl.roboteamtwente.autoref.validators;

import nl.roboteamtwente.autoref.RuleValidator;
import nl.roboteamtwente.autoref.RuleViolation;
import nl.roboteamtwente.autoref.model.Game;
import nl.roboteamtwente.autoref.model.TeamColor;
import nl.roboteamtwente.autoref.model.Vector2;
import org.robocup.ssl.proto.SslGcCommon;
import org.robocup.ssl.proto.SslGcGameEvent;
import org.robocup.ssl.proto.SslGcGeometry;

public class PlacementSucceededValidator implements RuleValidator {
    @Override
    public RuleViolation validate(Game game) {
        return null;
    }


    record PlacementSucceededViolation(TeamColor byTeam, float time_taken, float precision, float distance) implements RuleViolation {
        @Override
        public String toString() {
            return "Placement succeeded (by: "+ byTeam + " time taken: " + time_taken + ", precision: " + precision + ", distance: "+ distance   + " )";
        }

        @Override
        public SslGcGameEvent.GameEvent toPacket() {
            return SslGcGameEvent.GameEvent.newBuilder()
                    .setType(SslGcGameEvent.GameEvent.Type.PLACEMENT_SUCCEEDED)
                    .setPlacementSucceeded(SslGcGameEvent.GameEvent.PlacementSucceeded.newBuilder()
                            .setByTeam(byTeam == TeamColor.BLUE ? SslGcCommon.Team.BLUE : SslGcCommon.Team.YELLOW)
                            .setTimeTaken(time_taken)
                            .setPrecision(precision)
                            .setDistance(distance))
                    .build();

        }
    }

    @Override
    public boolean isActive(Game game) {
        return false;
    }
}
