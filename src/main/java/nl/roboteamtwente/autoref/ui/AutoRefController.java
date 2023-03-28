package nl.roboteamtwente.autoref.ui;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import nl.roboteamtwente.autoref.SSLAutoRef;
import nl.roboteamtwente.autoref.model.Division;

import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;

public class AutoRefController implements Initializable {
    private SSLAutoRef sslAutoRef;

    @FXML
    public ComboBox<String> modeBox;

    @FXML
    public ComboBox<String> divisionBox;

    @FXML
    public ListView<TextFlow> logList;

    @FXML
    public GameCanvas canvas;

    @FXML
    public Button clearButton;

    @FXML
    public Label worldStatus;

    @FXML
    public Label gcStatus;

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
                logList.scrollTo(logList.getItems().size() - 1);
            });
        });

        modeBox.getItems().addAll("Passive", "Active");
        divisionBox.getItems().addAll("A", "B");

        modeBox.setOnAction((event) -> {
            sslAutoRef.setActive(Objects.equals(modeBox.getValue(), "Active"));
        });

        divisionBox.setOnAction((event) -> {
            sslAutoRef.setDivision(Objects.equals(divisionBox.getValue(), "A") ? Division.A : Division.B);
        });

        clearButton.setOnAction((event) -> {
            logList.getItems().clear();
        });

        AnimationTimer anim = new AnimationTimer() {
            public void handle(long now) {
                worldStatus.setTextFill(sslAutoRef.isWorldConnected() ? Color.GREEN : Color.RED);
                gcStatus.setTextFill(sslAutoRef.isGCConnected() ? Color.GREEN : Color.RED);
                canvas.redraw();
            }
        };
        anim.start();
    }

    //FIXME still hard coded
    public void start(Application.Parameters parameters) {
        try {
            String ipWorld = parameters.getNamed().getOrDefault("world-ip", "127.0.0.1");
            String ipGameController = parameters.getNamed().getOrDefault("gc-ip", "127.0.0.1");
            int portWorld = Integer.parseInt(parameters.getNamed().getOrDefault("world-port", "5558"));
            int portGameController = Integer.parseInt(parameters.getNamed().getOrDefault("gc-port", "10007"));

            boolean active = parameters.getUnnamed().contains("--active");
            String divisionString = parameters.getNamed().getOrDefault("division", "B").toLowerCase();

            Division division;
            if (divisionString.equals("a")) {
                division = Division.A;
            } else if (divisionString.equals("b")) {
                division = Division.B;
            } else {
                System.err.println("Unknown division " + divisionString);
                System.exit(1);
                return;
            }

            modeBox.setValue(active ? "Active" : "Passive");
            divisionBox.setValue(division.toString());

            sslAutoRef.setActive(active);
            sslAutoRef.setDivision(division);
            sslAutoRef.start(ipWorld, ipGameController, portWorld, portGameController);
        } catch (NumberFormatException e) {
            System.err.println("Failed to parse port program argument.");
            System.exit(1);
        }
    }

    public void stop() {
        sslAutoRef.stop();
    }
}
