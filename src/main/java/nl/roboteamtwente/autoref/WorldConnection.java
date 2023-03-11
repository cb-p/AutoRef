package nl.roboteamtwente.autoref;

import com.google.protobuf.InvalidProtocolBufferException;
import nl.roboteamtwente.proto.StateOuterClass;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import java.util.List;
import java.util.function.Consumer;

public class WorldConnection implements Runnable {
    private final String ip;
    private final int port;
    private ZMQ.Socket worldSocket;
    private final SSLAutoRef ref;
    private Consumer<RuleViolation> onViolation;

    /**
     * Establish connection with World
     */
    public void connect() {
        try (ZContext context = new ZContext()) {
            //Create connection
            worldSocket = context.createSocket(SocketType.SUB);
            worldSocket.subscribe("");
            worldSocket.connect("tcp:// " + ip + ":" + port);
            listener();
        }
    }

    /**
     * Receive and process messages
     */
    public void listener() {
        try {
            while (!Thread.currentThread().isInterrupted() && worldSocket != null) {
                byte[] buffer = worldSocket.recv();
                StateOuterClass.State packet = StateOuterClass.State.parseFrom(buffer);
                ref.processWorldState(packet);
                //TODO test what happens if thread is running but world closes (what kind of error, do we need to take action?)

                //check for any violations
                List<RuleViolation> violations = ref.getReferee().validate();
                for (RuleViolation violation : violations) {
                    //violation to ui/AutoRefController.java
                    if (onViolation != null) {
                        onViolation.accept(violation);
                    }

                    //FIXME send to gamecontroller connection
                }
            }
        } catch (InvalidProtocolBufferException e) {
            //FIXME do something useful with exception
            throw new RuntimeException(e);
        }
    }

    /**
     * Close connection to world
     */
    public void close() {
        worldSocket.close();
        worldSocket = null;
    }

    @Override
    public void run() {
        connect();
    }

    public void setOnViolation(Consumer<RuleViolation> onViolation) {
        this.onViolation = onViolation;
    }

    public WorldConnection(String ip, int port, SSLAutoRef ref) {
        this.ip = ip;
        this.port = port;
        this.ref = ref;
        this.worldSocket = null;
    }
}
