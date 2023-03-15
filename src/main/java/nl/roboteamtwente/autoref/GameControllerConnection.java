package nl.roboteamtwente.autoref;

import com.google.protobuf.ByteString;
import org.robocup.ssl.proto.SslGcCommon;
import org.robocup.ssl.proto.SslGcGameEvent;
import org.robocup.ssl.proto.SslGcRcon;
import org.robocup.ssl.proto.SslGcRconAutoref;

import java.io.IOException;
import java.net.Socket;
import java.security.*;

public class GameControllerConnection {
    private Socket socket;
    private Signature signature;
    private String token;

    public void connect(String ip, int port) throws Exception {
        this.socket = new Socket(ip, port);

        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(512, new SecureRandom());
        KeyPair keyPair = keyGen.generateKeyPair();

        this.signature = Signature.getInstance("SHA256withRSA");
        signature.initSign(keyPair.getPrivate());

        SslGcRcon.ControllerReply reply = receivePacket();
        if (!reply.hasNextToken()) {
            throw new RuntimeException("Missing next token");
        }

        SslGcRconAutoref.AutoRefRegistration registration = SslGcRconAutoref.AutoRefRegistration.newBuilder()
                .setIdentifier("RoboTeam Twente")
                .setSignature(getSignature())
                .build();

        registration.writeDelimitedTo(socket.getOutputStream());
        socket.getOutputStream().flush();

        reply = receivePacket();
        if (reply.getStatusCode() != SslGcRcon.ControllerReply.StatusCode.OK) {
            throw new RuntimeException("Registration rejected: " + reply.getReason());
        }
    }

    public void disconnect() throws IOException {
        if (socket != null) {
            socket.close();
            socket = null;
        }
    }

    public void sendGameEvent(SslGcGameEvent.GameEvent gameEvent) throws IOException, SignatureException {
        SslGcRconAutoref.AutoRefToController.newBuilder()
                .setSignature(getSignature())
                .setGameEvent(gameEvent)
                .build()
                .writeDelimitedTo(socket.getOutputStream());
        socket.getOutputStream().flush();

        SslGcRcon.ControllerReply reply = receivePacket();
        if (reply.getStatusCode() != SslGcRcon.ControllerReply.StatusCode.OK) {
            throw new RuntimeException("Game event rejected: " + reply.getReason());
        }
    }

    private SslGcRcon.Signature getSignature() throws SignatureException {
        return SslGcRcon.Signature.newBuilder()
                .setToken(this.token)
                .setPkcs1V15(ByteString.copyFrom(signature.sign())).build();
    }

    private SslGcRcon.ControllerReply receivePacket() throws IOException, SignatureException {
        SslGcRcon.ControllerReply reply = SslGcRconAutoref.ControllerToAutoRef.parseDelimitedFrom(this.socket.getInputStream()).getControllerReply();
        if (reply.hasNextToken()) {
            this.token = reply.getNextToken();
            signature.update(reply.getNextTokenBytes().toByteArray());
        }

        return reply;
    }

    public boolean isConnected() {
        return socket != null && socket.isConnected();
    }

    public static void main(String[] args) throws Exception {
        // We test the connection by sending a single game event.

        GameControllerConnection connection = new GameControllerConnection();

        try {
            connection.connect("localhost", 10007);
            connection.sendGameEvent(SslGcGameEvent.GameEvent.newBuilder()
                    .setType(SslGcGameEvent.GameEvent.Type.AIMLESS_KICK)
                    .setAimlessKick(SslGcGameEvent.GameEvent.AimlessKick.newBuilder()
                            .setByTeam(SslGcCommon.Team.BLUE)
                            .setByBot(5))
                    .build());
        } finally {
            connection.disconnect();
        }
    }
}
