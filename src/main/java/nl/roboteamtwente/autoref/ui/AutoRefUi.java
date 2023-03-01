package nl.roboteamtwente.autoref.ui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class AutoRefUi extends Application {
    @Override
    public void start(Stage primaryStage) throws IOException {
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/auto_ref.fxml")));
        primaryStage.setTitle("RoboTeam Twente: AutoRef");
        primaryStage.setScene(new Scene(root, 640, 480));
        primaryStage.show();
    }
}
