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
                ref.checkViolations(packet);
            }
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
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


    public WorldConnection(String ip, int port, SSLAutoRef ref) {
        this.ip = ip;
        this.port = port;
        this.ref = ref;
        this.worldSocket = null;
    }
}