package nl.roboteamtwente.autoref;

import org.robocup.ssl.proto.SslGcGameEvent;

public interface RuleViolation {
    String toString();
    SslGcGameEvent.GameEvent toPacket();
}
