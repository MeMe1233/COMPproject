package application;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class StartView {

    private final VBox root = new VBox(18);

    public StartView(Stage stage) {

        Label title = new Label("Start Interface");

        Button btnUser = new Button("User / Admin");
        Button btnCustomer = new Button("Customer");

        btnUser.setPrefWidth(240);
        btnCustomer.setPrefWidth(240);

        // Open User Dashboard
        btnUser.setOnAction(e -> {
            UserView dash = new UserView(stage);
            stage.setScene(new Scene(dash.getRoot(), 1100, 650));
        });

        // Open Customer Interface (placeholder for now)
        btnCustomer.setOnAction(e -> {
            CustomerView view = new CustomerView(stage);
            stage.setScene(new Scene(view.getRoot(), 900, 600));
        });

        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(30));
        root.getChildren().addAll(title, btnUser, btnCustomer);
    }

    public Parent getRoot() {
        return root;
    }
}
