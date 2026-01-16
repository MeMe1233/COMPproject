package application;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
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
	private Map<Product, Integer> cart = new HashMap<>();

	private Mysqlmethods db;
	private Customer loggedCustomer = null;

	public CustomerView(Stage stage) {

		try {
			db = new Mysqlmethods();
		} catch (Exception ignored) {
		}

		// Top bar
		Label title = new Label("Customer Interface");
		Button back = new Button("Back");
		back.setOnAction(e -> stage.setScene(new Scene(new StartView(stage).getRoot(), 900, 600)));

		Region spacer = new Region();
		HBox.setHgrow(spacer, Priority.ALWAYS);

		HBox top = new HBox(10, title, spacer, back);
		top.setPadding(new Insets(10));
		root.setTop(top);

		// Center
		content.setPadding(new Insets(10));
		root.setCenter(content);

		// First page: login/register like the sketch
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

		Label t = new Label("Customer Interface");

		Button loginBtn = new Button("Login");
		Button registerBtn = new Button("Register");

		loginBtn.setPrefWidth(240);
		registerBtn.setPrefWidth(240);

		loginBtn.setOnAction(e -> showPage(buildLoginPage()));
		registerBtn.setOnAction(e -> showPage(buildRegisterPage()));

		VBox box = new VBox(12, t, loginBtn, registerBtn);
		box.setAlignment(Pos.CENTER);
		box.setPadding(new Insets(25));
		return box;
	}

	// ------------------- PAGE 2: Login (Name + ID) -------------------
	private Parent buildLoginPage() {

		Label t = new Label("Login");

		TextField name = new TextField();
		name.setPromptText("Customer Name");

		TextField id = new TextField();
		id.setPromptText("Customer ID");

		Label msg = new Label();

		Button enter = new Button("Enter");
		Button back = new Button("Back");

		enter.setOnAction(e -> {
			try {
				int cid = Integer.parseInt(id.getText().trim());
				String cname = name.getText().trim();

				Customer c = db.loginCustomerByIdAndName(cid, cname);
				if (c == null) {
					msg.setText("Wrong name or ID!");
					return;
				}

				loggedCustomer = c;
				showPage(buildCustomerHome()); // go to Products page
			} catch (Exception ex) {
				msg.setText("Enter valid ID + Name");
			}
		});

		back.setOnAction(e -> showPage(buildCustomerStartPage(null)));

		VBox box = new VBox(10, t, name, id, new HBox(10, enter, back), msg);
		box.setPadding(new Insets(20));
		return box;
	}

	// ------------------- PAGE 3: Register Customer -------------------
	private Parent buildRegisterPage() {

		Label t = new Label("Register");

		TextField name = new TextField();
		name.setPromptText("Name");
		TextField phone = new TextField();
		phone.setPromptText("Phone");
		TextField email = new TextField();
		email.setPromptText("Email");
		TextField address = new TextField();
		address.setPromptText("Address");

		Label msg = new Label();

		Button create = new Button("Create");
		Button back = new Button("Back");

		create.setOnAction(e -> {
			try {
				Customer c = new Customer(0, name.getText(), phone.getText(), email.getText(), address.getText());
				boolean ok = db.addCustomer(c);
				msg.setText(ok ? "Created! Now Login." : "Failed!");
			} catch (Exception ex) {
				msg.setText("Failed!");
			}
		});

		back.setOnAction(e -> showPage(buildCustomerStartPage(null)));

		VBox box = new VBox(10, t, name, phone, email, address, new HBox(10, create, back), msg);
		box.setPadding(new Insets(20));
		return box;
	}

	// ------------------- CUSTOMER HOME (Products + Cart button)
	// -------------------
	private Parent buildCustomerHome() {

		// Left menu like sketch: Products + Cart
		Button bProducts = new Button("Products");
		Button bCart = new Button("Cart");
		Button bLogout = new Button("Logout");

		for (Button b : new Button[] { bProducts, bCart, bLogout }) {
			b.setMaxWidth(Double.MAX_VALUE);
		}

		VBox side = new VBox(10, bProducts, bCart, bLogout);
		side.setPadding(new Insets(10));
		side.setPrefWidth(160);

		StackPane center = new StackPane();
		center.setPadding(new Insets(10));

		BorderPane pane = new BorderPane();
		pane.setLeft(side);
		pane.setCenter(center);

		// default: products
		center.getChildren().setAll(buildProductsPage());

		bProducts.setOnAction(e -> center.getChildren().setAll(buildProductsPage()));
		bCart.setOnAction(e -> center.getChildren().setAll(buildCartPage()));
		bLogout.setOnAction(e -> {
			loggedCustomer = null;
			showPage(buildCustomerStartPage(null));
		});

		return pane;
	}

	// ------------------- PRODUCTS PAGE (Add to cart) -------------------
	private Parent buildProductsPage() {
	    Label title = new Label("Products");

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

	    TableColumn<Product, Integer> colStock = new TableColumn<>("Available");
	    colStock.setCellValueFactory(cell -> {
	        Product p = cell.getValue();
	        int quantity = 0;
	        for (Stock s : db.getAllStock()) {
	            if (s.getProduct() != null && s.getProduct().getId() == p.getId()) {
	                quantity = s.getQuantity();
	                break;
	            }
	        }
	        return new SimpleObjectProperty<>(quantity);
	    });

	    table.getColumns().addAll(colId, colName, colSize, colColor, colPrice, colStock);

	    TextField qtyField = new TextField();
	    qtyField.setPromptText("Quantity");
	    Button addToCartBtn = new Button("Add to Cart");

	    addToCartBtn.setOnAction(e -> {
	        Product selected = table.getSelectionModel().getSelectedItem();
	        if (selected == null) return;

	        int qty;
	        try {
	            qty = Integer.parseInt(qtyField.getText().trim());
	        } catch (Exception ex) {
	            qty = 0;
	        }

	        if (qty <= 0) {
	            new Alert(Alert.AlertType.WARNING, "Enter a valid quantity!").showAndWait();
	            return;
	        }

	        // Check stock
	        int available = 0;
	        Stock stockItem = null;
	        for (Stock s : db.getAllStock()) {
	            if (s.getProduct() != null && s.getProduct().getId() == selected.getId()) {
	                available = s.getQuantity();
	                stockItem = s;
	                break;
	            }
	        }

	        if (qty > available) {
	            new Alert(Alert.AlertType.WARNING, "Quantity exceeds available stock!").showAndWait();
	            return;
	        }

	        // Add to in-memory cart
	        cart.put(selected, cart.getOrDefault(selected, 0) + qty);

	        // Reduce stock visually immediately
	        if (stockItem != null) stockItem.setQuantity(available - qty);
	        table.refresh();

	        qtyField.clear();
	        new Alert(Alert.AlertType.INFORMATION, "Added to cart!").showAndWait();
	    });

	    VBox box = new VBox(10, title, table, new VBox(5, qtyField, addToCartBtn));
	    box.setPadding(new Insets(10));

	    table.setItems(FXCollections.observableArrayList(db.getAllProducts()));

	    return box;
	}


	// ------------------- CART PAGE (table + total + delete + print/place)
	// -------------------
	// ------------------- CART PAGE (table + total + delete + print)
	private Parent buildCartPage() {
	    Label title = new Label("Your Cart");

	    TableView<Map.Entry<Product, Integer>> cartTable = new TableView<>();

	    TableColumn<Map.Entry<Product, Integer>, String> nameCol = new TableColumn<>("Product");
	    nameCol.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getKey().getName()));

	    TableColumn<Map.Entry<Product, Integer>, Integer> qtyCol = new TableColumn<>("Quantity");
	    qtyCol.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue().getValue()));

	    TableColumn<Map.Entry<Product, Integer>, Double> priceCol = new TableColumn<>("Price per Item");
	    priceCol.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue().getKey().getPrice()));

	    TableColumn<Map.Entry<Product, Integer>, Double> totalCol = new TableColumn<>("Total");
	    totalCol.setCellValueFactory(cell ->
	            new SimpleObjectProperty<>(cell.getValue().getKey().getPrice() * cell.getValue().getValue())
	    );

	    cartTable.getColumns().addAll(nameCol, qtyCol, priceCol, totalCol);

	    // Load in-memory cart
	    cartTable.setItems(FXCollections.observableArrayList(cart.entrySet()));

	    Label totalLabel = new Label();
	    updateCartTotal(totalLabel);

	    Button deleteBtn = new Button("Remove Selected");
	    deleteBtn.setOnAction(e -> {
	        Map.Entry<Product, Integer> selected = cartTable.getSelectionModel().getSelectedItem();
	        if (selected != null) {
	            Product p = selected.getKey();
	            int qty = selected.getValue();
	            cart.remove(p);

	            // Return stock visually
	            for (Stock s : db.getAllStock()) {
	                if (s.getProduct().getId() == p.getId()) {
	                    s.setQuantity(s.getQuantity() + qty);
	                    break;
	                }
	            }

	            cartTable.setItems(FXCollections.observableArrayList(cart.entrySet()));
	            updateCartTotal(totalLabel);
	        }
	    });

	    Button printBtn = new Button("Print Cart");
	    printBtn.setOnAction(e -> {
	        if (cart.isEmpty()) return;
	        StringBuilder sb = new StringBuilder("Cart Items:\n");
	        double total = 0;
	        for (Map.Entry<Product, Integer> entry : cart.entrySet()) {
	            double itemTotal = entry.getKey().getPrice() * entry.getValue();
	            sb.append(entry.getKey().getName())
	              .append(" x").append(entry.getValue())
	              .append(" = ").append(itemTotal)
	              .append("\n");
	            total += itemTotal;
	        }
	        sb.append("Total: ").append(total);
	        new Alert(Alert.AlertType.INFORMATION, sb.toString()).showAndWait();
	    });

	    Button orderBtn = new Button("Place Order");
	    orderBtn.setOnAction(e -> {
	        if (cart.isEmpty()) return;

	        if (db.placeOrder(loggedCustomer.getId(), cart)) {
	            cart.clear();
	            cartTable.setItems(FXCollections.observableArrayList(cart.entrySet()));
	            updateCartTotal(totalLabel);
	            new Alert(Alert.AlertType.INFORMATION, "Order placed successfully!").showAndWait();
	        } else {
	            new Alert(Alert.AlertType.ERROR, "Failed to place order!").showAndWait();
	        }
	    });


	    HBox buttons = new HBox(10, deleteBtn, printBtn, orderBtn);
	    VBox box = new VBox(10, title, cartTable, totalLabel, buttons);
	    box.setPadding(new Insets(10));

	    return box;
	}

	private void updateCartTotal(Label totalLabel) {
	    double total = cart.entrySet().stream()
	            .mapToDouble(en -> en.getKey().getPrice() * en.getValue())
	            .sum();
	    totalLabel.setText("Total: " + total);
	}


}
