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

        // Title
        Label title = new Label("🧵 Cloth Market System");
        title.getStyleClass().add("h1");

        Label sub = new Label("✨ Choose your interface to continue");
        sub.getStyleClass().add("sub");

        // Buttons
        Button btnUser = new Button("🛡️ User / Admin");
        Button btnCustomer = new Button("🛒 Customer");

        btnUser.setPrefWidth(280);
        btnCustomer.setPrefWidth(280);

        btnUser.getStyleClass().add("btn-primary");
        btnCustomer.getStyleClass().add("btn-secondary");

        // Open User Dashboard
        btnUser.setOnAction(e -> {
            UserView dash = new UserView(stage);
            Scene sc = new Scene(dash.getRoot(), 1100, 650);
            sc.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
            stage.setScene(sc);
        });

        // Open Customer Interface
        btnCustomer.setOnAction(e -> {
            CustomerView view = new CustomerView(stage);
            Scene sc = new Scene(view.getRoot(), 900, 600);
            sc.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
            stage.setScene(sc);
        });


        // Root styling
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(30));
        root.setMaxWidth(480);
        root.getStyleClass().add("card");

        root.getChildren().addAll(title, sub, btnUser, btnCustomer);
    }

    public Parent getRoot() {
        return root;
    }
}
