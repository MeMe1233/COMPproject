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
		back.setOnAction(e -> {
		    Scene sc = new Scene(new StartView(stage).getRoot(), 900, 600);
		    sc.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
		    stage.setScene(sc);
		});


		Region spacer = new Region();
		HBox.setHgrow(spacer, Priority.ALWAYS);

		HBox top = new HBox(10, title, spacer, back);
		top.setPadding(new Insets(10));
		root.setTop(top);

		// ===== Sidebar =====
		VBox side = new VBox(8);
		side.setPadding(new Insets(10));
		side.setPrefWidth(180);
		side.getStyleClass().add("sidebar");

		Button bEmployees = new Button("👷 Employees");
		Button bOrders    = new Button("🧾 Orders");
		Button bCustomers = new Button("🧑‍🤝‍🧑 Customers");
		Button bProducts  = new Button("👕 Products");
		Button bSupplier  = new Button("🏭 Supplier");
		Button bStock     = new Button("📦 Stock");


		for (Button b : new Button[] { bEmployees, bOrders, bCustomers, bProducts, bSupplier, bStock }) {
			b.setMaxWidth(Double.MAX_VALUE);
		}

		side.getChildren().addAll(bEmployees, bOrders, bCustomers, bProducts, bSupplier, bStock);
		root.setLeft(side);

		// ===== Content area =====
		contentPane.setPadding(new Insets(10));
		root.setCenter(contentPane);

		showPage(buildUserLoginPage(stage));

		// Pages
		bEmployees.setOnAction(e -> showPage(buildEmployeesPage()));
		bOrders.setOnAction(e -> showPage(buildOrdersPage()));
		bCustomers.setOnAction(e -> showPage(buildCustomersPage()));
		bSupplier.setOnAction(e -> showPage(buildSupplierPage()));
		bStock.setOnAction(e -> showPage(buildStockPage()));

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
		table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

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
		table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

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
		table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

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
		table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

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
	private Parent buildSupplierPage() {

	    Label title = new Label("Suppliers");
	   

	    TableView<Supplier> table = new TableView<>();
	    table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
	    TableColumn<Supplier, Integer> colId = new TableColumn<>("ID");
	    colId.setCellValueFactory(new PropertyValueFactory<>("id"));

	    TableColumn<Supplier, String> colName = new TableColumn<>("Name");
	    colName.setCellValueFactory(new PropertyValueFactory<>("name"));

	    TableColumn<Supplier, String> colPhone = new TableColumn<>("Phone");
	    colPhone.setCellValueFactory(new PropertyValueFactory<>("phone"));

	    TableColumn<Supplier, String> colEmail = new TableColumn<>("Email");
	    colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));

	    TableColumn<Supplier, String> colCompany = new TableColumn<>("Company");
	    colCompany.setCellValueFactory(new PropertyValueFactory<>("companyName"));

	    table.getColumns().addAll(colId, colName, colPhone, colEmail, colCompany);
	    table.getSortOrder().add(colId);

	    // Search
	    TextField searchField = new TextField();
	    searchField.setPromptText("Search by ID / Name / Company");
	    Button searchBtn = new Button("Search");
	    Button resetBtn = new Button("Reset");

	    // Form
	    TextField name = new TextField(); name.setPromptText("Name");
	    TextField phone = new TextField(); phone.setPromptText("Phone");
	    TextField email = new TextField(); email.setPromptText("Email");
	    TextField company = new TextField(); company.setPromptText("Company Name");

	    Button addBtn = new Button("Add");
	    Button updateBtn = new Button("Update");
	    Button deleteBtn = new Button("Delete");

	    Runnable load = () -> table.setItems(FXCollections.observableArrayList(db.getAllSuppliers()));

	    addBtn.setOnAction(e -> {
	        Supplier s = new Supplier(0, name.getText(), phone.getText(), email.getText(), company.getText());
	        db.addSupplier(s);
	        load.run();
	    });

	    updateBtn.setOnAction(e -> {
	        Supplier s = table.getSelectionModel().getSelectedItem();
	        if (s != null) {
	            s.setName(name.getText());
	            s.setPhone(phone.getText());
	            s.setEmail(email.getText());
	            s.setCompanyName(company.getText());
	            db.updateSupplier(s);
	            load.run();
	        }
	    });

	    deleteBtn.setOnAction(e -> {
	        Supplier s = table.getSelectionModel().getSelectedItem();
	        if (s != null) {
	            db.deleteSupplier(s.getId());
	            load.run();
	        }
	    });

	    searchBtn.setOnAction(e -> {
	        String k = searchField.getText().trim();
	        if (!k.isEmpty()) {
	            table.setItems(FXCollections.observableArrayList(db.searchSuppliers(k)));
	        }
	    });

	    resetBtn.setOnAction(e -> load.run());

	    table.getSelectionModel().selectedItemProperty().addListener((obs, old, s) -> {
	        if (s != null) {
	            name.setText(s.getName());
	            phone.setText(s.getPhone());
	            email.setText(s.getEmail());
	            company.setText(s.getCompanyName());
	        }
	    });

	    load.run();

	    VBox searchBox = new VBox(5, searchField, new HBox(5, searchBtn, resetBtn));
	    VBox formBox = new VBox(5, name, phone, email, company, new HBox(5, addBtn, updateBtn, deleteBtn));
	    VBox box = new VBox(10, title, searchBox, table, formBox);
	    box.setPadding(new Insets(10));
	    return box;
	}
	private Parent buildStockPage() {

	    Label title = new Label("Stock");

	    TableView<Stock> table = new TableView<>();
	    table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

	    TableColumn<Stock, Integer> colId = new TableColumn<>("Stock ID");
	    colId.setCellValueFactory(new PropertyValueFactory<>("id"));

	    TableColumn<Stock, Integer> colProdId = new TableColumn<>("Product ID");
	    colProdId.setCellValueFactory(cell ->
	        new SimpleObjectProperty<>(cell.getValue().getProduct() != null ? cell.getValue().getProduct().getId() : 0)
	    );

	    TableColumn<Stock, String> colProdName = new TableColumn<>("Product");
	    colProdName.setCellValueFactory(cell ->
	        new SimpleObjectProperty<>(cell.getValue().getProduct() != null ? cell.getValue().getProduct().getName() : "")
	    );

	    TableColumn<Stock, Integer> colQty = new TableColumn<>("Quantity");
	    colQty.setCellValueFactory(new PropertyValueFactory<>("quantity"));

	    table.getColumns().addAll(colId, colProdId, colProdName, colQty);
	    table.getSortOrder().add(colId);

	    // Search
	    TextField searchField = new TextField();
	    searchField.setPromptText("Search by Stock ID / Product ID / Product Name");

	    Button searchBtn = new Button("Search");
	    Button resetBtn = new Button("Reset");

	    // choose product + quantity

	    ComboBox<Product> productCombo = new ComboBox<>();
	    productCombo.setPromptText("Select Product");

	    //  if db connection failed, avoid crash
	    if (db != null) {
	        productCombo.setItems(FXCollections.observableArrayList(db.getAllProducts()));
	    } else {
	        productCombo.setItems(FXCollections.observableArrayList());
	    }

	    productCombo.setCellFactory(lv -> new ListCell<>() {
	        @Override
	        protected void updateItem(Product item, boolean empty) {
	            super.updateItem(item, empty);
	            setText(empty || item == null ? null : (item.getId() + " - " + item.getName()));
	        }
	    });
	    productCombo.setButtonCell(new ListCell<>() {
	        @Override
	        protected void updateItem(Product item, boolean empty) {
	            super.updateItem(item, empty);
	            setText(empty || item == null ? null : (item.getId() + " - " + item.getName()));
	        }
	    });

	    TextField qtyField = new TextField();
	    qtyField.setPromptText("Quantity");

	    Button addBtn = new Button("Add (+)");
	    Button updateBtn = new Button("Update Qty");
	    Button deleteBtn = new Button("Delete Row");

	    // LOAD 
	    Runnable load = () -> {
	        if (db == null) {
	            table.setItems(FXCollections.observableArrayList());
	            return;
	        }
	        table.setItems(FXCollections.observableArrayList(db.getAllStock()));
	    };

	    // clear inputs after actions (optional but useful)
	    Runnable clearForm = () -> {
	        qtyField.clear();
	        productCombo.getSelectionModel().clearSelection();
	        table.getSelectionModel().clearSelection();
	    };


	    // Actions

	    addBtn.setOnAction(e -> {
	        try {
	            if (db == null) return;

	            Product p = productCombo.getValue();
	            int q = Integer.parseInt(qtyField.getText().trim());

	            if (p != null && q > 0) {
	                db.addStock(p.getId(), q);
	                load.run();
	                clearForm.run();
	            }
	        } catch (Exception ignored) {}
	    });

	    updateBtn.setOnAction(e -> {
	        try {
	            if (db == null) return;

	            Stock st = table.getSelectionModel().getSelectedItem();
	            if (st != null) {
	                int newQ = Integer.parseInt(qtyField.getText().trim());
	                db.updateStockQuantity(st.getId(), newQ);
	                load.run();
	                clearForm.run();
	            }
	        } catch (Exception ignored) {}
	    });

	    deleteBtn.setOnAction(e -> {
	        if (db == null) return;

	        Stock st = table.getSelectionModel().getSelectedItem();
	        if (st != null) {
	            db.deleteStock(st.getId());
	            load.run();
	            clearForm.run();
	        }
	    });

	    searchBtn.setOnAction(e -> {
	        if (db == null) return;

	        String k = searchField.getText().trim();
	        if (!k.isEmpty()) {
	            table.setItems(FXCollections.observableArrayList(db.searchStock(k)));
	        }
	    });

	    resetBtn.setOnAction(e -> {
	        searchField.clear(); 
	        load.run();
	    });

	    table.getSelectionModel().selectedItemProperty().addListener((obs, old, st) -> {
	        if (st != null) {
	            qtyField.setText(String.valueOf(st.getQuantity()));
	        }
	    });

	    load.run();

	    VBox searchBox = new VBox(5, searchField, new HBox(5, searchBtn, resetBtn));
	    VBox formBox = new VBox(5, productCombo, qtyField, new HBox(5, addBtn, updateBtn, deleteBtn));

	    VBox box = new VBox(10, title, searchBox, table, formBox);
	    box.setPadding(new Insets(10));
	    return box;
	}
	private Parent buildUserLoginPage(Stage stage) {

	    Label t = new Label("🔐 Admin Login");
	    t.getStyleClass().add("h1");

	    Label sub = new Label("Please enter your username and password");
	    sub.getStyleClass().add("sub");

	    TextField user = new TextField();
	    user.setPromptText("👤 Username");

	    PasswordField pass = new PasswordField();
	    pass.setPromptText("🔑 Password");

	    Label msg = new Label();
	    msg.getStyleClass().add("sub");

	    Button login = new Button("✅ Login");
	    login.getStyleClass().add("btn-primary");

	    Button backBtn = new Button("⬅ Back");
	    backBtn.getStyleClass().add("btn-secondary");

	    login.setOnAction(e -> {
	        try {
	            String u = user.getText().trim();
	            String p = pass.getText().trim();

	            if (u.isEmpty() || p.isEmpty()) {
	                msg.setText("⚠ Please fill username & password");
	                return;
	            }

	            boolean ok = db != null && db.loginAdmin(u, p);
	            if (!ok) {
	                msg.setText("❌ Wrong username or password");
	                return;
	            }

	            // success -> show dashboard welcome page
	            showPage(buildWelcomePage());
	        } catch (Exception ex) {
	            msg.setText("❌ Login failed");
	        }
	    });
	    backBtn.setOnAction(e -> {
	        Scene sc = new Scene(new StartView(stage).getRoot(), 900, 600);
	        sc.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
	        stage.setScene(sc);
	    });

	    VBox form = new VBox(10, t, sub, user, pass, new HBox(10, login, backBtn), msg);
	    form.getStyleClass().add("card");
	    form.setMaxWidth(460);
	    form.setPadding(new Insets(20));

	    StackPane wrap = new StackPane(form);
	    wrap.setPadding(new Insets(20));
	    return wrap;
	}





}
