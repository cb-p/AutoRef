package nl.roboteamtwente.autoref;

import java.io.FileInputStream;
import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        GameLog gameLog = new GameLog(new FileInputStream(args[0]));

        for (GameLog.Message message : gameLog.getMessages()) {
            if (message instanceof GameLog.Message.Refbox2013 refbox) {
                System.out.println("refbox -> " + refbox.packet);
            } else if (message instanceof GameLog.Message.Vision2014 vision) {
                System.out.println("vision -> " + vision.packet);
            }
        }
    }
}
