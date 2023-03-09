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

    /**
     * Connect AutoRef to GameControl by:
     * First establish TCP connection
     * GameControl sends a token to us
     * AutoRef identifies itself by sending AutoRefRegistration
     * GameControl verifies
     * GameControl sends reply (OK|REJECT)
     * @param ip ip to establish connection on
     * @param port port to establish connection on
     * @throws Exception RunTimeExceptions
     */
    public void connect(String ip, int port) throws Exception {
        this.socket = new Socket(ip, port);

        //Generate keyPair
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(512, new SecureRandom());
        KeyPair keyPair = keyGen.generateKeyPair();

        //Generate signature
        this.signature = Signature.getInstance("SHA256withRSA");
        signature.initSign(keyPair.getPrivate());

        //receive token from GameController
        SslGcRcon.ControllerReply reply = receivePacket();
        if (!reply.hasNextToken()) {
            throw new RuntimeException("Missing next token");
        }

        //send registration
        SslGcRconAutoref.AutoRefRegistration registration = SslGcRconAutoref.AutoRefRegistration.newBuilder()
                .setIdentifier("RoboTeam Twente")
                .setSignature(getSignature())
                .build();

        registration.writeDelimitedTo(socket.getOutputStream());
        socket.getOutputStream().flush();

        //receive reply
        reply = receivePacket();
        if (reply.getStatusCode() != SslGcRcon.ControllerReply.StatusCode.OK) {
            throw new RuntimeException("Registration rejected: " + reply.getReason());
        }
    }

    /**
     * close connection
     * @throws IOException
     */

    public void disconnect() throws IOException {
        if (socket != null) {
            socket.close();
            socket = null;
        }
    }

    /**
     * Send Game Event to GameController
     * @param gameEvent game event with details about the violation
     * @throws IOException thrown when there is something wrong with the connection
     * @throws SignatureException exception with signature
     */
    public void sendGameEvent(SslGcGameEvent.GameEvent gameEvent) throws IOException, SignatureException {
        //build packet
        SslGcRconAutoref.AutoRefToController.newBuilder()
                .setSignature(getSignature())
                .setGameEvent(gameEvent)
                .build()
                .writeDelimitedTo(socket.getOutputStream());
        socket.getOutputStream().flush();

        //FIXME make sure we do not get stuck here (how long could it take to get back controller reply?)
        SslGcRcon.ControllerReply reply = receivePacket();
        if (reply.getStatusCode() != SslGcRcon.ControllerReply.StatusCode.OK) {
            //FIXME rejection is not necessarily a bad thing
            throw new RuntimeException("Game event rejected: " + reply.getReason());
        }
    }

    /**
     * @return signature
     * @throws SignatureException generic exception
     */
    private SslGcRcon.Signature getSignature() throws SignatureException {
        return SslGcRcon.Signature.newBuilder()
                .setToken(this.token)
                .setPkcs1V15(ByteString.copyFrom(signature.sign())).build();
    }

    /**
     * Receive controller reply
     * If reply has a nextToken, set this.token to nextToken and update signature
     * @return reply
     * @throws IOException
     * @throws SignatureException
     */
    private SslGcRcon.ControllerReply receivePacket() throws IOException, SignatureException {
        SslGcRcon.ControllerReply reply = SslGcRconAutoref.ControllerToAutoRef.parseDelimitedFrom(this.socket.getInputStream()).getControllerReply();
        if (reply.hasNextToken()) {
            this.token = reply.getNextToken();
            signature.update(reply.getNextTokenBytes().toByteArray());
        }

        return reply;
    }

    /**
     * @return if there is a connection
     */
    public boolean isConnected() {
        return socket != null && socket.isConnected();
    }

    //TODO deprecate
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
