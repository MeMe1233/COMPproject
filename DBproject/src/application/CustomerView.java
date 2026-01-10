package application;



import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class CustomerView {

    private final VBox root = new VBox(12);

    public CustomerView(Stage stage) {

        Label lbl = new Label(" Customer Interface ");

        Button back = new Button("Back");
        back.setOnAction(e -> stage.setScene(new Scene(new StartView(stage).getRoot(), 900, 600)));

        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(30));
        root.getChildren().addAll(lbl, back);
    }

    public Parent getRoot() {
        return root;
    }
}
