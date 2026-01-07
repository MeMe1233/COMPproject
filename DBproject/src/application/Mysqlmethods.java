package application;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Mysqlmethods {

	private Connection conn;

	// ------------------- CONNECTION -------------------
	public Mysqlmethods() {
		String url = "jdbc:mysql://localhost:3306/cloth_market"; // DB name
		String user = "root"; // MySQL username
		String password = "000"; // MySQL password
		try {
			conn = DriverManager.getConnection(url, user, password);
			System.out.println("Database connected!");
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("Database connection failed!");
		}
	}

	// ------------------- PRODUCT -------------------
	public boolean addProduct(Product p) {
		String sql = "INSERT INTO product(name, size, color, price) VALUES (?, ?, ?, ?)";
		try (PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setString(1, p.getName());
			stmt.setString(2, p.getSize());
			stmt.setString(3, p.getColor());
			stmt.setDouble(4, p.getPrice());
			return stmt.executeUpdate() > 0;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	public List<Product> getAllProducts() {
		List<Product> list = new ArrayList<>();
		String sql = "SELECT * FROM product";
		try (Statement stmt = conn.createStatement()) {
			ResultSet rs = stmt.executeQuery(sql);
			while (rs.next()) {
				list.add(new Product(rs.getInt("id"), rs.getString("name"), rs.getString("size"), rs.getString("color"),
						rs.getDouble("price")));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return list;
	}

	// ------------------- CUSTOMER -------------------
	public boolean addCustomer(Customer c) {
		String sql1 = "INSERT INTO person(name, phone, email, type) VALUES (?, ?, ?, 'customer')";
		String sql2 = "INSERT INTO customer(person_id, address) VALUES (?, ?)";
		try (PreparedStatement stmt1 = conn.prepareStatement(sql1, Statement.RETURN_GENERATED_KEYS);
				PreparedStatement stmt2 = conn.prepareStatement(sql2)) {
			stmt1.setString(1, c.getName());
			stmt1.setString(2, c.getPhone());
			stmt1.setString(3, c.getEmail());
			stmt1.executeUpdate();
			ResultSet rs = stmt1.getGeneratedKeys();
			if (rs.next()) {
				int personId = rs.getInt(1);
				stmt2.setInt(1, personId);
				stmt2.setString(2, c.getAddress());
				stmt2.executeUpdate();
			}
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	public List<Customer> getAllCustomers() {
		List<Customer> list = new ArrayList<>();
		String sql = "SELECT p.id, p.name, p.phone, p.email, c.address "
				+ "FROM person p JOIN customer c ON p.id = c.person_id";
		try (Statement stmt = conn.createStatement()) {
			ResultSet rs = stmt.executeQuery(sql);
			while (rs.next()) {
				list.add(new Customer(rs.getInt("id"), rs.getString("name"), rs.getString("phone"),
						rs.getString("email"), rs.getString("address")));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return list;
	}

	// ------------------- EMPLOYEE -------------------
	public boolean addEmployee(Employee e) {
		String sql1 = "INSERT INTO person(name, phone, email, type) VALUES (?, ?, ?, 'employee')";
		String sql2 = "INSERT INTO employee(person_id, salary) VALUES (?, ?)";
		try (PreparedStatement stmt1 = conn.prepareStatement(sql1, Statement.RETURN_GENERATED_KEYS);
				PreparedStatement stmt2 = conn.prepareStatement(sql2)) {
			stmt1.setString(1, e.getName());
			stmt1.setString(2, e.getPhone());
			stmt1.setString(3, e.getEmail());
			stmt1.executeUpdate();
			ResultSet rs = stmt1.getGeneratedKeys();
			if (rs.next()) {
				int personId = rs.getInt(1);
				stmt2.setInt(1, personId);
				stmt2.setDouble(2, e.getSalary());
				stmt2.executeUpdate();
			}
			return true;
		} catch (SQLException ex) {
			ex.printStackTrace();
			return false;
		}
	}

	// ------------------- SUPPLIER -------------------
	public boolean addSupplier(Supplier s) {
		String sql1 = "INSERT INTO person(name, phone, email, type) VALUES (?, ?, ?, 'supplier')";
		String sql2 = "INSERT INTO supplier(person_id, company_name) VALUES (?, ?)";
		try (PreparedStatement stmt1 = conn.prepareStatement(sql1, Statement.RETURN_GENERATED_KEYS);
				PreparedStatement stmt2 = conn.prepareStatement(sql2)) {
			stmt1.setString(1, s.getName());
			stmt1.setString(2, s.getPhone());
			stmt1.setString(3, s.getEmail());
			stmt1.executeUpdate();
			ResultSet rs = stmt1.getGeneratedKeys();
			if (rs.next()) {
				int personId = rs.getInt(1);
				stmt2.setInt(1, personId);
				stmt2.setString(2, s.getCompanyName());
				stmt2.executeUpdate();
			}
			return true;
		} catch (SQLException ex) {
			ex.printStackTrace();
			return false;
		}
	}

	// ------------------- ORDER -------------------
	public boolean addOrder(Order o, List<OrderItem> items) {
		String sqlOrder = "INSERT INTO order_table(customer_id, order_date) VALUES (?, ?)";
		String sqlItem = "INSERT INTO order_item(order_id, product_id, quantity, price) VALUES (?, ?, ?, ?)";
		try (PreparedStatement stmtOrder = conn.prepareStatement(sqlOrder, Statement.RETURN_GENERATED_KEYS);
				PreparedStatement stmtItem = conn.prepareStatement(sqlItem)) {
			stmtOrder.setInt(1, o.getCustomer().getId());
			stmtOrder.setDate(2, Date.valueOf(o.getOrderDate()));
			stmtOrder.executeUpdate();
			ResultSet rs = stmtOrder.getGeneratedKeys();
			if (rs.next()) {
				int orderId = rs.getInt(1);
				for (OrderItem item : items) {
					stmtItem.setInt(1, orderId);
					stmtItem.setInt(2, item.getProduct().getId());
					stmtItem.setInt(3, item.getQuantity());
					stmtItem.setDouble(4, item.getPrice());
					stmtItem.addBatch();
				}
				stmtItem.executeBatch();
			}
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

}
