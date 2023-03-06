package nl.roboteamtwente.autoref;

import com.google.protobuf.InvalidProtocolBufferException;
import org.robocup.ssl.proto.SslGcRefereeMessage;
import org.robocup.ssl.proto.SslVisionWrapper;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class GameLog {
    private static final String IDENTIFIER = "SSL_LOG_FILE";

    private final List<Message> messages = new ArrayList<>();

    public GameLog(InputStream inputStream) throws IOException, IllegalStateException {
        DataInputStream dataInputStream = new DataInputStream(inputStream);

        for (char c : IDENTIFIER.toCharArray()) {
            if ((char) dataInputStream.readByte() != c) {
                throw new IllegalStateException("Incorrect file header.");
            }
        }

        int version = dataInputStream.readInt();
        if (version != 1) {
            throw new IllegalStateException("Unsupported log file format version " + version + ".");
        }


        while (dataInputStream.available() != 0) {
            long timestamp = dataInputStream.readLong();
            int type = dataInputStream.readInt();
            int size = dataInputStream.readInt();
            byte[] buffer = new byte[size];
            dataInputStream.readFully(buffer, 0, size);

            Message message = Message.parse(type, buffer);
            message.timestamp = timestamp;
            message.size = size;
            messages.add(message);
        }
    }

    public List<Message> getMessages() {
        return messages;
    }

    public static abstract class Message {
        public long timestamp;
        public int size;

        public static Message parse(int type, byte[] buffer) throws InvalidProtocolBufferException {
            return switch (type) {
                case 3 -> new Refbox2013(buffer);
                case 4 -> new Vision2014(buffer);
                default -> throw new IllegalStateException("Unhandled log message type " + type + ".");
            };
        }

        public static class Refbox2013 extends Message {
            public SslGcRefereeMessage.SSL_Referee packet;

            public Refbox2013(byte[] buffer) throws InvalidProtocolBufferException {
                this.packet = SslGcRefereeMessage.SSL_Referee.parseFrom(buffer);
            }
        }

        public static class Vision2014 extends Message {
            public SslVisionWrapper.SSL_WrapperPacket packet;

            public Vision2014(byte[] buffer) throws InvalidProtocolBufferException {
                this.packet = SslVisionWrapper.SSL_WrapperPacket.parseFrom(buffer);
            }
        }
    }
}
