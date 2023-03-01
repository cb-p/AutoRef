package nl.roboteamtwente.autoref.ui;

import javafx.animation.AnimationTimer;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import nl.roboteamtwente.autoref.SSLAutoRef;

import java.net.URL;
import java.util.ResourceBundle;

public class AutoRefController implements Initializable {
    private SSLAutoRef sslAutoRef;

    @FXML
    public ComboBox<String> modeBox;

    @FXML
    public ListView<String> logList;

    @FXML
    public GameCanvas canvas;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        sslAutoRef = new SSLAutoRef();
        canvas.setSslAutoRef(sslAutoRef);

        // FIXME: This is very temporary.
        sslAutoRef.startReceivingWorldPackets();

        modeBox.getItems().addAll("Passive", "Active");
        modeBox.setValue("Active");

        logList.getItems().addAll(
                "[01:23] Aimless Kick (by: RED, at: -2, 4, ...)"
        );

        AnimationTimer anim = new AnimationTimer() {
            public void handle(long now) {
                canvas.redraw();
            }
        };
        anim.start();
    }
}
