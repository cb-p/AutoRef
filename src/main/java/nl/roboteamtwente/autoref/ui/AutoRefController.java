package nl.roboteamtwente.autoref.ui;

import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import nl.roboteamtwente.autoref.SSLAutoRef;

import java.net.URL;
import java.util.ResourceBundle;

public class AutoRefController implements Initializable {
    private SSLAutoRef sslAutoRef;

    @FXML
    public ComboBox<String> modeBox;

    @FXML
    public ListView<TextFlow> logList;

    @FXML
    public GameCanvas canvas;

    @FXML
    public Button clearButton;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        sslAutoRef = new SSLAutoRef();
        canvas.setSslAutoRef(sslAutoRef);

        sslAutoRef.setOnViolation((violation) -> {
            double time = sslAutoRef.getReferee().getGame().getTime();
            String timeString = String.format("%d:%05.2f", (int) (time / 60), time % 60);
            System.out.println("[" + timeString + "] " + violation);

            Text timeText = new Text("[" + timeString + "] ");
            timeText.setStyle("-fx-font-weight: bold");

            Platform.runLater(() -> {
                logList.getItems().add(new TextFlow(timeText, new Text(violation.toString())));
            });
        });

        modeBox.getItems().addAll("Passive", "Active");
        modeBox.setValue("Passive");

        clearButton.setOnAction((event) -> {
            logList.getItems().clear();
        });

        AnimationTimer anim = new AnimationTimer() {
            public void handle(long now) {
                canvas.redraw();
            }
        };
        anim.start();
    }

    public void start() {
        sslAutoRef.start();
    }

    public void stop() {
        sslAutoRef.stop();
    }
}
