package application;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage stage) {
        StartView startView = new StartView(stage);
        Scene scene = new Scene(startView.getRoot(), 900, 600);

        // If you have css keep it, if not comment it
        // scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());

        stage.setTitle("Cloth Market System");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}