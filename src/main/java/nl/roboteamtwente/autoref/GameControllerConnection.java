package nl.roboteamtwente.autoref;

import com.google.protobuf.ByteString;
import org.robocup.ssl.proto.SslGcGameEvent;
import org.robocup.ssl.proto.SslGcRcon;
import org.robocup.ssl.proto.SslGcRconAutoref;

import java.io.IOException;
import java.net.Socket;
import java.security.*;
import java.util.ArrayList;
import java.util.List;

public class GameControllerConnection implements Runnable {
    private Socket socket;
    private Signature signature;
    private String token;
    private String ip;
    private int port;
    private List<SslGcGameEvent.GameEvent> queue;


    /**
     * Connect AutoRef to GameControl by:
     * First establish TCP connection
     * GameControl sends a token to us
     * AutoRef identifies itself by sending AutoRefRegistration
     * GameControl verifies
     * GameControl sends reply (OK|REJECT)
     * @throws Exception RunTimeExceptions
     */
    public void connect() throws InterruptedException{
        try {
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
            if (reply == null) {
                return;
            }
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
            //FIXME handle reply == null
            if (reply.getStatusCode() != SslGcRcon.ControllerReply.StatusCode.OK) {
                //FIXME MESSAGE TO UI
                reconnect();
            }
        } catch (IOException | SignatureException e) {
            //prevent spamming of trying to reconnect
            Thread.sleep(1000);
            reconnect();
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            e.printStackTrace();
        }
    }

    /**
     * Check if something is in the queue, if there is, send the first item in the queue to GC.
     * queue follows FIFO principle
     * @throws InterruptedException
     */
    private void processQueue() throws InterruptedException {
        while (!Thread.currentThread().isInterrupted()) {
            if (isConnected()) {
                if (!queue.isEmpty()) {
                    SslGcGameEvent.GameEvent gameEvent = queue.remove(0);
                    sendGameEvent(gameEvent);
                }
            } else {
                reconnect();
            }
            Thread.sleep(1000/100); //1 second / 100Hz
        }
    }

    /**
     * close connection
     * @throws IOException
     */

    public void disconnect() throws IOException {
        //FIXME remove throws
        if (socket != null) {
            socket.close();
            socket = null;
        }
    }


    /**
     * Reconnect
     */
    public void reconnect() {
        //FIXME message to UI (perhaps at place where reconnect is called)
        try {
            this.socket = null;
            this.signature = null;
            this.connect();
        } catch (InterruptedException e ) {
            //FIXME handle exception
        }
    }

    /**
     * Send Game Event to GameController
     * @param gameEvent game event with details about the violation
     */
    public void sendGameEvent(SslGcGameEvent.GameEvent gameEvent) {
        try {
            //build packet
            SslGcRconAutoref.AutoRefToController.newBuilder()
                    .setSignature(getSignature())
                    .setGameEvent(gameEvent)
                    .build()
                    .writeDelimitedTo(socket.getOutputStream());
            socket.getOutputStream().flush();

            //FIXME make sure we do not get stuck here (how long could it take to get back controller reply?)
            SslGcRcon.ControllerReply reply = receivePacket();
            //FIXME handle null
            if (reply.getStatusCode() != SslGcRcon.ControllerReply.StatusCode.OK) {
                //FIXME rejection is not necessarily a bad thing I think, just log to UI?
                throw new RuntimeException("Game event rejected: " + reply.getReason());
            }
        } catch (IOException | SignatureException e) {
            reconnect();
        }
    }

    /**
     * @return signature
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
     */
    private SslGcRcon.ControllerReply receivePacket() {
        try {
            SslGcRcon.ControllerReply reply = SslGcRconAutoref.ControllerToAutoRef.parseDelimitedFrom(this.socket.getInputStream()).getControllerReply();
            if (reply.hasNextToken()) {
                this.token = reply.getNextToken();
                signature.update(reply.getNextTokenBytes().toByteArray());
            }
            return reply;
        } catch (IOException e) {
            this.reconnect();
        } catch (SignatureException e) {
            //FIXME handle error -> reconnect?
        }

        //FIXME handle null when calling this method
        return null;
    }

    /**
     * @return if there is a connection
     */
    public boolean isConnected() {
        return socket != null && socket.isConnected();
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void addToQueue(SslGcGameEvent.GameEvent gameEvent) {
        this.queue.add(gameEvent);
    }

    @Override
    public void run() {
        this.queue = new ArrayList<>();
        try {
            this.connect();
            this.processQueue();
        } catch (InterruptedException e) {
            //FIXME handle exception
        }
    }
}
