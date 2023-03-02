package nl.roboteamtwente.autoref;

import org.robocup.ssl.proto.MessagesRobocupSslGameEvent;

public interface RuleViolation {
    String toString();
    MessagesRobocupSslGameEvent.GameEvent toPacket();
}
