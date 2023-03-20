package nl.roboteamtwente.autoref.validators;

import nl.roboteamtwente.autoref.RuleValidator;
import nl.roboteamtwente.autoref.RuleViolation;
import nl.roboteamtwente.autoref.model.Game;
import nl.roboteamtwente.autoref.model.GameState;
import nl.roboteamtwente.autoref.model.TeamColor;
import nl.roboteamtwente.autoref.model.Vector2;
import org.robocup.ssl.proto.SslGcCommon;
import org.robocup.ssl.proto.SslGcGameEvent;
import org.robocup.ssl.proto.SslGcGeometry;

import java.util.EnumSet;

public class BotInterferedPlacement implements RuleValidator {

    @Override
    public RuleViolation validate(Game game) {
        if (game.getState() == GameState.BALL_PLACEMENT){
            Vector2 designated_position = game.getDesignated_position();
            System.out.println(designated_position.getX());
            System.out.println(designated_position.getY());
            System.out.println(game.getForTeam());
        }
        return null;
    }

    @Override
    public EnumSet<GameState> activeStates() {
        return EnumSet.of(GameState.BALL_PLACEMENT);
    }

    record BotInterferedPlacementViolation(TeamColor byTeam, int byBot, Vector2 location) implements RuleViolation {
        @Override
        public String toString() {
            return "Bot interfered placement (by: " + byTeam + ", bot #" + byBot + " location: " + location + " )";
        }

        @Override
        public SslGcGameEvent.GameEvent toPacket() {
            return SslGcGameEvent.GameEvent.newBuilder()
                    .setType(SslGcGameEvent.GameEvent.Type.BOT_TOO_FAST_IN_STOP)
                    .setBotInterferedPlacement(SslGcGameEvent.GameEvent.BotInterferedPlacement.newBuilder()
                            .setByBot(byBot)
                            .setByTeam(byTeam == TeamColor.BLUE ? SslGcCommon.Team.BLUE : SslGcCommon.Team.YELLOW)
                            .setLocation(SslGcGeometry.Vector2.newBuilder().setX(location.getX()).setY(location.getY())))
                    .build();
        }
    }
}
