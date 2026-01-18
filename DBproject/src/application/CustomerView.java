package application;

import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class CustomerView {

    private final BorderPane root = new BorderPane();
    private final StackPane content = new StackPane();

    private Mysqlmethods db;
    private Customer loggedCustomer = null;

    public CustomerView(Stage stage) {

        try { db = new Mysqlmethods(); } catch (Exception ignored) {}

        // ===== Top bar (fancy) =====
        Label title = new Label("🛒 Customer Interface");
        title.getStyleClass().add("top-title");

        Button back = new Button("⬅ Back");
        back.getStyleClass().add("top-btn");
        back.setOnAction(e -> {
            Scene sc = new Scene(new StartView(stage).getRoot(), 900, 600);
            sc.getStylesheets().add(
                getClass().getResource("application.css").toExternalForm()
            );
            stage.setScene(sc);
        });


        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox top = new HBox(10, title, spacer, back);
        top.getStyleClass().add("top-bar");
        top.setPadding(new Insets(10));
        root.setTop(top);

        // Center
        content.setPadding(new Insets(10));
        root.setCenter(content);

        // First page
        showPage(buildCustomerStartPage(stage));
    }

    public Parent getRoot() {
        return root;
    }

    private void showPage(Parent p) {
        content.getChildren().setAll(p);
    }

    // ------------------- PAGE 1: Login/Register Buttons -------------------
    private Parent buildCustomerStartPage(Stage stage) {

        Label t = new Label("🧑‍🤝‍🧑 Customer Portal");
        t.getStyleClass().add("h1");

        Label sub = new Label("✨ Login or create a new account");
        sub.getStyleClass().add("sub");

        Button loginBtn = new Button("🔐 Login");
        Button registerBtn = new Button("📝 Register");

        loginBtn.setPrefWidth(280);
        registerBtn.setPrefWidth(280);

        loginBtn.getStyleClass().add("btn-primary");
        registerBtn.getStyleClass().add("btn-secondary");

        loginBtn.setOnAction(e -> showPage(buildLoginPage(stage)));
        registerBtn.setOnAction(e -> showPage(buildRegisterPage(stage)));

        VBox box = new VBox(12, t, sub, loginBtn, registerBtn);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(25));
        box.setMaxWidth(520);
        box.getStyleClass().add("card");

        StackPane wrap = new StackPane(box);
        wrap.setPadding(new Insets(20));
        return wrap;
    }

    // ------------------- PAGE 2: Login (Name + ID) -------------------
    private Parent buildLoginPage(Stage stage) {

        Label t = new Label("🔐 Login");
        t.getStyleClass().add("h1");

        Label sub = new Label("Enter your name and ID to continue");
        sub.getStyleClass().add("sub");

        TextField name = new TextField();
        name.setPromptText("👤 Customer Name");

        TextField id = new TextField();
        id.setPromptText("🆔 Customer ID");

        Label msg = new Label();
        msg.getStyleClass().add("sub");

        Button enter = new Button("✅ Enter");
        Button back = new Button("⬅ Back");

        enter.getStyleClass().add("btn-primary");
        back.getStyleClass().add("btn-secondary");

        enter.setOnAction(e -> {
            try {
                int cid = Integer.parseInt(id.getText().trim());
                String cname = name.getText().trim();

                Customer c = db.loginCustomerByIdAndName(cid, cname);
                if (c == null) {
                    msg.setText("❌ Wrong name or ID!");
                    return;
                }

                loggedCustomer = c;
                showPage(buildCustomerHome(stage)); // go to home
            } catch (Exception ex) {
                msg.setText("⚠ Enter valid ID + Name");
            }
        });

        back.setOnAction(e -> showPage(buildCustomerStartPage(stage)));



        HBox buttons = new HBox(10, enter, back);
        buttons.setAlignment(Pos.CENTER_LEFT);

        VBox box = new VBox(10, t, sub, name, id, buttons, msg);
        box.setPadding(new Insets(20));
        box.setMaxWidth(520);
        box.getStyleClass().add("card");

        StackPane wrap = new StackPane(box);
        wrap.setPadding(new Insets(20));
        return wrap;
    }

    // ------------------- PAGE 3: Register Customer -------------------
    private Parent buildRegisterPage(Stage stage) {

        Label t = new Label("📝 Register");
        t.getStyleClass().add("h1");

        Label sub = new Label("Create your account information");
        sub.getStyleClass().add("sub");

        TextField name = new TextField();    name.setPromptText("👤 Name");
        TextField phone = new TextField();   phone.setPromptText("📞 Phone");
        TextField email = new TextField();   email.setPromptText("📧 Email");
        TextField address = new TextField(); address.setPromptText("📍 Address");

        Label msg = new Label();
        msg.getStyleClass().add("sub");

        Button create = new Button("✅ Create");
        Button back = new Button("⬅ Back");

        create.getStyleClass().add("btn-primary");
        back.getStyleClass().add("btn-secondary");

        create.setOnAction(e -> {
            try {
                Customer c = new Customer(0, name.getText(), phone.getText(), email.getText(), address.getText());
                boolean ok = db.addCustomer(c);
                msg.setText(ok ? "🎉 Created! Now Login." : "❌ Failed!");
            } catch (Exception ex) {
                msg.setText("❌ Failed!");
            }
        });

        back.setOnAction(e -> showPage(buildCustomerStartPage(stage)));

        HBox buttons = new HBox(10, create, back);
        buttons.setAlignment(Pos.CENTER_LEFT);

        VBox box = new VBox(10, t, sub, name, phone, email, address, buttons, msg);
        box.setPadding(new Insets(20));
        box.setMaxWidth(520);
        box.getStyleClass().add("card");

        StackPane wrap = new StackPane(box);
        wrap.setPadding(new Insets(20));
        return wrap;
    }

    // ------------------- CUSTOMER HOME (Products + Cart button) -------------------
    private Parent buildCustomerHome(Stage stage) {

        // Left menu
        Button bProducts = new Button("👕 Products");
        Button bCart = new Button("🛒 Cart");
        Button bLogout = new Button("🚪 Logout");

        for (Button b : new Button[]{bProducts, bCart, bLogout}) {
            b.setMaxWidth(Double.MAX_VALUE);
        }

        VBox side = new VBox(10, bProducts, bCart, bLogout);
        side.setPadding(new Insets(12));
        side.setPrefWidth(190);
        side.getStyleClass().add("sidebar");

        StackPane center = new StackPane();
        center.setPadding(new Insets(10));

        BorderPane pane = new BorderPane();
        pane.setLeft(side);
        pane.setCenter(center);

        center.getChildren().setAll(buildProductsPage(center));

        bProducts.setOnAction(e -> center.getChildren().setAll(buildProductsPage(center)));
        bCart.setOnAction(e -> center.getChildren().setAll(buildCartPage(center)));
        bLogout.setOnAction(e -> {
            loggedCustomer = null;
            showPage(buildCustomerStartPage(stage));
        });

        // Wrap center content in a card feel
        pane.setPadding(new Insets(10));
        return pane;
    }

    // ------------------- PRODUCTS PAGE (Add to cart) -------------------
    private Parent buildProductsPage(StackPane centerHolder) {

        Label t = new Label("👕 Products");
        t.getStyleClass().add("h1");

        TableView<Product> table = new TableView<>();

        TableColumn<Product, Integer> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));

        TableColumn<Product, String> colName = new TableColumn<>("Name");
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn<Product, String> colSize = new TableColumn<>("Size");
        colSize.setCellValueFactory(new PropertyValueFactory<>("size"));

        TableColumn<Product, String> colColor = new TableColumn<>("Color");
        colColor.setCellValueFactory(new PropertyValueFactory<>("color"));

        TableColumn<Product, Double> colPrice = new TableColumn<>("Price");
        colPrice.setCellValueFactory(new PropertyValueFactory<>("price"));

        table.getColumns().addAll(colId, colName, colSize, colColor, colPrice);
        table.setItems(FXCollections.observableArrayList(db.getAllProducts()));

        TextField qty = new TextField();
        qty.setPromptText("🧮 Qty");

        Button addToCart = new Button("➕ Add To Cart");
        addToCart.getStyleClass().add("btn-primary");

        Label msg = new Label();
        msg.getStyleClass().add("sub");

        addToCart.setOnAction(e -> {
            try {
                Product p = table.getSelectionModel().getSelectedItem();
                if (p == null) { msg.setText("⚠ Select product first"); return; }

                int q = Integer.parseInt(qty.getText().trim());
                if (q <= 0) { msg.setText("⚠ Qty must be > 0"); return; }

                boolean ok = db.addToCart(loggedCustomer.getId(), p.getId(), q);
                msg.setText(ok ? "✅ Added!" : "❌ Failed!");
            } catch (Exception ex) {
                msg.setText("⚠ Enter valid qty");
            }
        });

        HBox actions = new HBox(10, qty, addToCart);
        actions.setAlignment(Pos.CENTER_LEFT);

        VBox box = new VBox(10, t, table, actions, msg);
        box.setPadding(new Insets(16));
        box.getStyleClass().add("card");
        return box;
    }

    // ------------------- CART PAGE (table + total + delete + print/place) -------------------
    private Parent buildCartPage(StackPane centerHolder) {

        Label t = new Label("🛒 Cart");
        t.getStyleClass().add("h1");

        TableView<OrderItem> table = new TableView<>();

        TableColumn<OrderItem, Integer> colPid = new TableColumn<>("Product ID");
        colPid.setCellValueFactory(cell ->
                new SimpleObjectProperty<>(cell.getValue().getProduct().getId()));

        TableColumn<OrderItem, String> colPname = new TableColumn<>("Product");
        colPname.setCellValueFactory(cell ->
                new SimpleObjectProperty<>(cell.getValue().getProduct().getName()));

        TableColumn<OrderItem, Integer> colQty = new TableColumn<>("Amount");
        colQty.setCellValueFactory(new PropertyValueFactory<>("quantity"));

        TableColumn<OrderItem, Double> colPrice = new TableColumn<>("Price");
        colPrice.setCellValueFactory(new PropertyValueFactory<>("price"));

        table.getColumns().addAll(colPid, colPname, colQty, colPrice);

        Runnable load = () -> table.setItems(FXCollections.observableArrayList(db.getCartItems(loggedCustomer.getId())));
        load.run();

        Label total = new Label("Total: 0");
        total.getStyleClass().add("sub");

        Runnable calc = () -> {
            double sum = 0;
            for (OrderItem it : table.getItems()) sum += it.getQuantity() * it.getPrice();
            total.setText("💰 Total: " + sum);
        };
        calc.run();

        Button delete = new Button("🗑 Delete");
        Button printOrder = new Button("🧾 Print / Place Order");

        delete.getStyleClass().add("btn-secondary");
        printOrder.getStyleClass().add("btn-primary");

        Label msg = new Label();
        msg.getStyleClass().add("sub");

        delete.setOnAction(e -> {
            OrderItem it = table.getSelectionModel().getSelectedItem();
            if (it == null) { msg.setText("⚠ Select row first"); return; }
            db.deleteCartItem(it.getId());
            load.run();
            calc.run();
        });

        printOrder.setOnAction(e -> {
            boolean ok = db.placeOrder(loggedCustomer.getId());
            msg.setText(ok ? "✅ Order Placed!" : "❌ Failed!");
            load.run();
            calc.run();
        });

        HBox actions = new HBox(10, delete, printOrder);
        actions.setAlignment(Pos.CENTER_LEFT);

        VBox box = new VBox(10, t, table, actions, total, msg);
        box.setPadding(new Insets(16));
        box.getStyleClass().add("card");
        return box;
    }

 
    private void showPageAfterLogin() {
 
    }
}

