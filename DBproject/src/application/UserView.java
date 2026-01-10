package application;

import java.time.LocalDate;

import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class UserView {

	private final BorderPane root = new BorderPane();
	private final StackPane contentPane = new StackPane();

	private Mysqlmethods db;

	public UserView(Stage stage) {

		// Create DB instance safely
		try {
			db = new Mysqlmethods();
		} catch (Exception ignored) {
		}

		// ===== Top Bar =====
		Label title = new Label("User");
		Button back = new Button("Back");
		back.setOnAction(e -> stage.setScene(new Scene(new StartView(stage).getRoot(), 900, 600)));

		Region spacer = new Region();
		HBox.setHgrow(spacer, Priority.ALWAYS);

		HBox top = new HBox(10, title, spacer, back);
		top.setPadding(new Insets(10));
		root.setTop(top);

		// ===== Sidebar =====
		VBox side = new VBox(8);
		side.setPadding(new Insets(10));
		side.setPrefWidth(180);

		Button bEmployees = new Button("Employees");
		Button bOrders = new Button("Orders");
		Button bCustomers = new Button("Customers");
		Button bProducts = new Button("Products");
		Button bSupplier = new Button("Supplier");
		Button bStock = new Button("Stock");

		for (Button b : new Button[] { bEmployees, bOrders, bCustomers, bProducts, bSupplier, bStock }) {
			b.setMaxWidth(Double.MAX_VALUE);
		}

		side.getChildren().addAll(bEmployees, bOrders, bCustomers, bProducts, bSupplier, bStock);
		root.setLeft(side);

		// ===== Content area =====
		contentPane.setPadding(new Insets(10));
		root.setCenter(contentPane);

		// Default page
		showPage(buildWelcomePage());

		// Pages
		bEmployees.setOnAction(e -> showPage(buildEmployeesPage()));
		bOrders.setOnAction(e -> showPage(buildOrdersPage()));
		bCustomers.setOnAction(e -> showPage(buildCustomersPage()));
		bSupplier.setOnAction(e -> showPage(buildPlaceholder("Supplier")));
		bStock.setOnAction(e -> showPage(buildPlaceholder("Stock")));

		// Products page is real (TableView + DB load)
		bProducts.setOnAction(e -> showPage(buildProductsPage()));
	}

	public Parent getRoot() {
		return root;
	}

	private void showPage(Parent page) {
		contentPane.getChildren().setAll(page);
	}

	private Parent buildWelcomePage() {
		VBox box = new VBox(10);
		box.setPadding(new Insets(20));
		box.getChildren().addAll(new Label("Welcome!"), new Label("Choose a section from the left menu."));
		return box;
	}

	private Parent buildPlaceholder(String title) {
		VBox box = new VBox(10);
		box.setPadding(new Insets(20));
		box.getChildren().addAll(new Label(title + " Page"), new Label("Placeholder for now."));
		return box;
	}

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

		TableColumn<Product, String> colSupplier = new TableColumn<>("Supplier");
		colSupplier.setCellValueFactory(cell -> {
			Supplier s = cell.getValue().getSupplier();
			return new SimpleObjectProperty(s != null ? s.getName() : "None");
		});

		table.getColumns().addAll(colId, colName, colSize, colColor, colPrice, colSupplier);
		table.getSortOrder().add(colId);

		// ===== Search =====
		TextField searchField = new TextField();
		searchField.setPromptText("Search by Name or ID");

		Button searchBtn = new Button("Search");
		Button resetBtn = new Button("Reset");

		searchBtn.setOnAction(e -> {
			String keyword = searchField.getText().trim();
			if (!keyword.isEmpty()) {
				table.setItems(FXCollections.observableArrayList(db.searchProducts(keyword)));
			}
		});

		resetBtn.setOnAction(e -> table.setItems(FXCollections.observableArrayList(db.getAllProducts())));

		// ===== Form for Add =====
		TextField name = new TextField();
		name.setPromptText("Name");
		TextField size = new TextField();
		size.setPromptText("Size");
		TextField color = new TextField();
		color.setPromptText("Color");
		TextField price = new TextField();
		price.setPromptText("Price");

		// ===== Supplier selection =====
		ComboBox<Supplier> supplierCombo = new ComboBox<>();
		supplierCombo.setPromptText("Select Supplier");
		supplierCombo.setItems(FXCollections.observableArrayList(db.getAllSuppliers()));
		supplierCombo.setCellFactory(lv -> new ListCell<>() {
			@Override
			protected void updateItem(Supplier item, boolean empty) {
				super.updateItem(item, empty);
				setText(empty || item == null ? null : item.getName());
			}
		});
		supplierCombo.setButtonCell(new ListCell<>() {
			@Override
			protected void updateItem(Supplier item, boolean empty) {
				super.updateItem(item, empty);
				setText(empty || item == null ? null : item.getName());
			}
		});

		Button addBtn = new Button("Add");
		Button deleteBtn = new Button("Delete");

		addBtn.setOnAction(e -> {
			try {
				Supplier selectedSupplier = supplierCombo.getValue();
				Product p = new Product(0, name.getText(), size.getText(), color.getText(),
						Double.parseDouble(price.getText()), selectedSupplier);
				db.addProduct(p); // using Mysqlmethods
				table.setItems(FXCollections.observableArrayList(db.getAllProducts()));
			} catch (Exception ignored) {
			}
		});

		deleteBtn.setOnAction(e -> {
			Product p = table.getSelectionModel().getSelectedItem();
			if (p != null) {
				db.deleteProduct(p.getId());
				table.setItems(FXCollections.observableArrayList(db.getAllProducts()));
			}
		});

		VBox searchBox = new VBox(5, searchField, new HBox(5, searchBtn, resetBtn));
		VBox formBox = new VBox(5, name, size, color, price, supplierCombo, new HBox(5, addBtn, deleteBtn));

		VBox box = new VBox(10, title, searchBox, table, formBox);
		box.setPadding(new Insets(10));

		return box;
	}

	private Parent buildEmployeesPage() {

		Label title = new Label("Employees");

		// ===== Table =====
		TableView<Employee> table = new TableView<>();

		TableColumn<Employee, Integer> colId = new TableColumn<>("ID");
		colId.setCellValueFactory(new PropertyValueFactory<>("id"));

		TableColumn<Employee, String> colName = new TableColumn<>("Name");
		colName.setCellValueFactory(new PropertyValueFactory<>("name"));

		TableColumn<Employee, String> colPhone = new TableColumn<>("Phone");
		colPhone.setCellValueFactory(new PropertyValueFactory<>("phone"));

		TableColumn<Employee, String> colEmail = new TableColumn<>("Email");
		colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));

		TableColumn<Employee, Double> colSalary = new TableColumn<>("Salary");
		colSalary.setCellValueFactory(new PropertyValueFactory<>("salary"));

		table.getColumns().addAll(colId, colName, colPhone, colEmail, colSalary);
		table.getSortOrder().add(colId); // sorting enabled

		// ===== Search =====
		TextField searchId = new TextField();
		searchId.setPromptText("Employee ID");

		Button search = new Button("Search");
		Button reset = new Button("Reset");

		// ===== Form =====
		TextField name = new TextField();
		TextField phone = new TextField();
		TextField email = new TextField();
		TextField salary = new TextField();

		name.setPromptText("Name");
		phone.setPromptText("Phone");
		email.setPromptText("Email");
		salary.setPromptText("Salary");

		// ===== Buttons =====
		Button add = new Button("Add");
		Button update = new Button("Update");
		Button delete = new Button("Delete");
		Button refresh = new Button("Refresh");

		Runnable load = () -> table.setItems(FXCollections.observableArrayList(db.getAllEmployees()));

		refresh.setOnAction(e -> load.run());

		add.setOnAction(e -> {
			db.addEmployee(new Employee(0, name.getText(), phone.getText(), email.getText(),
					Double.parseDouble(salary.getText())));
			load.run();
		});

		update.setOnAction(e -> {
			Employee emp = table.getSelectionModel().getSelectedItem();
			if (emp != null) {
				emp.setName(name.getText());
				emp.setPhone(phone.getText());
				emp.setEmail(email.getText());
				emp.setSalary(Double.parseDouble(salary.getText()));
				db.updateEmployee(emp);
				load.run();
			}
		});

		delete.setOnAction(e -> {
			Employee emp = table.getSelectionModel().getSelectedItem();
			if (emp != null) {
				db.deleteEmployee(emp.getId());
				load.run();
			}
		});

		search.setOnAction(e -> {
			try {
				Employee emp = db.getEmployeeById(Integer.parseInt(searchId.getText()));
				table.setItems(
						emp == null ? FXCollections.observableArrayList() : FXCollections.observableArrayList(emp));
			} catch (Exception ignored) {
			}
		});

		reset.setOnAction(e -> load.run());

		table.getSelectionModel().selectedItemProperty().addListener((obs, o, emp) -> {
			if (emp != null) {
				name.setText(emp.getName());
				phone.setText(emp.getPhone());
				email.setText(emp.getEmail());
				salary.setText(String.valueOf(emp.getSalary()));
			}
		});

		load.run();

		VBox searchBox = new VBox(5, new Label("Search by ID"), searchId, new HBox(5, search, reset));
		VBox form = new VBox(5, name, phone, email, salary, new HBox(5, add, update, delete));
		VBox box = new VBox(10, title, refresh, searchBox, table, form);

		box.setPadding(new Insets(10));
		return box;
	}

	private Parent buildOrdersPage() {

		Label title = new Label("Orders");

		TableView<OrderSummary> table = new TableView<>();

		TableColumn<OrderSummary, Integer> colId = new TableColumn<>("Order ID");
		colId.setCellValueFactory(new PropertyValueFactory<>("orderId"));

		TableColumn<OrderSummary, String> colCustomer = new TableColumn<>("Customer");
		colCustomer.setCellValueFactory(new PropertyValueFactory<>("customerName"));

		TableColumn<OrderSummary, LocalDate> colDate = new TableColumn<>("Date");
		colDate.setCellValueFactory(new PropertyValueFactory<>("orderDate"));

		TableColumn<OrderSummary, Double> colTotal = new TableColumn<>("Total");
		colTotal.setCellValueFactory(new PropertyValueFactory<>("total"));

		table.getColumns().addAll(colId, colCustomer, colDate, colTotal);
		table.getSortOrder().add(colDate); // sorting enabled

		Label totalValue = new Label("Total Value: 0");

		Button refresh = new Button("Refresh");

		Runnable load = () -> {
			var data = FXCollections.observableArrayList(db.getAllOrdersWithTotal());
			table.setItems(data);

			double sum = data.stream().mapToDouble(OrderSummary::getTotal).sum();
			totalValue.setText("Total Value: " + sum);
		};

		refresh.setOnAction(e -> load.run());
		load.run();

		VBox box = new VBox(10, title, refresh, table, totalValue);
		box.setPadding(new Insets(10));

		return box;
	}

	private Parent buildCustomersPage() {

		Label title = new Label("Customers");

		TableView<Customer> table = new TableView<>();

		TableColumn<Customer, Integer> colId = new TableColumn<>("ID");
		colId.setCellValueFactory(new PropertyValueFactory<>("id"));

		TableColumn<Customer, String> colName = new TableColumn<>("Name");
		colName.setCellValueFactory(new PropertyValueFactory<>("name"));

		TableColumn<Customer, String> colPhone = new TableColumn<>("Phone");
		colPhone.setCellValueFactory(new PropertyValueFactory<>("phone"));

		TableColumn<Customer, String> colEmail = new TableColumn<>("Email");
		colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));

		TableColumn<Customer, String> colAddress = new TableColumn<>("Address");
		colAddress.setCellValueFactory(new PropertyValueFactory<>("address"));

		table.getColumns().addAll(colId, colName, colPhone, colEmail, colAddress);

		// sorting enabled by column headers
		table.getSortOrder().add(colId);

		Button refresh = new Button("Refresh");

		Runnable load = () -> table.setItems(FXCollections.observableArrayList(db.getAllCustomers()));

		refresh.setOnAction(e -> load.run());
		load.run();

		VBox box = new VBox(10, title, refresh, table);
		box.setPadding(new Insets(10));

		return box;
	}

}
