package nl.roboteamtwente.autoref;

import nl.roboteamtwente.autoref.model.TeamColor;
import org.robocup.ssl.proto.SslGcGameEvent;

public interface RuleViolation {
    String toString();
    SslGcGameEvent.GameEvent toPacket();

    TeamColor byTeam();
}
