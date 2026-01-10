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
		String password = "0000"; // MySQL password
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
	    String sql = "INSERT INTO product(name, size, color, price, supplier_id) VALUES (?, ?, ?, ?, ?)";
	    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
	        stmt.setString(1, p.getName());
	        stmt.setString(2, p.getSize());
	        stmt.setString(3, p.getColor());
	        stmt.setDouble(4, p.getPrice());
	        if (p.getSupplier() != null) {
	            stmt.setInt(5, p.getSupplier().getId());
	        } else {
	            stmt.setNull(5, java.sql.Types.INTEGER);
	        }
	        return stmt.executeUpdate() > 0;
	    } catch (SQLException e) {
	        e.printStackTrace();
	        return false;
	    }
	}


	public List<Product> getAllProducts() {
	    List<Product> list = new ArrayList<>();
	    String sql = """
	        SELECT pr.*, s.person_id, s.name AS supplier_name, s.phone, s.email, s.company_name
	        FROM product pr
	        LEFT JOIN supplier s ON pr.supplier_id = s.person_id
	        LEFT JOIN person p ON s.person_id = p.id
	        """;
	    try (Statement stmt = conn.createStatement()) {
	        ResultSet rs = stmt.executeQuery(sql);
	        while (rs.next()) {
	            Supplier supplier = null;
	            int supplierId = rs.getInt("supplier_id");
	            if (!rs.wasNull()) {
	                supplier = new Supplier(
	                    supplierId,
	                    rs.getString("supplier_name"),
	                    rs.getString("phone"),
	                    rs.getString("email"),
	                    rs.getString("company_name")
	                );
	            }

	            list.add(new Product(
	                rs.getInt("id"),
	                rs.getString("name"),
	                rs.getString("size"),
	                rs.getString("color"),
	                rs.getDouble("price"),
	                supplier
	            ));
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	    return list;
	}


	public boolean deleteProduct(int id) {
	    String sql = "DELETE FROM product WHERE id=?";
	    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
	        stmt.setInt(1, id);
	        return stmt.executeUpdate() > 0;
	    } catch (SQLException e) {
	        e.printStackTrace();
	        return false;
	    }
	}

	public Product getProductById(int id) {
	    String sql = """
	        SELECT pr.*, s.person_id, s.name AS supplier_name, s.phone, s.email, s.company_name
	        FROM product pr
	        LEFT JOIN supplier s ON pr.supplier_id = s.person_id
	        LEFT JOIN person p ON s.person_id = p.id
	        WHERE pr.id = ?
	        """;
	    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
	        stmt.setInt(1, id);
	        ResultSet rs = stmt.executeQuery();
	        if (rs.next()) {
	            Supplier supplier = null;
	            int supplierId = rs.getInt("supplier_id");
	            if (!rs.wasNull()) {
	                supplier = new Supplier(
	                    supplierId,
	                    rs.getString("supplier_name"),
	                    rs.getString("phone"),
	                    rs.getString("email"),
	                    rs.getString("company_name")
	                );
	            }

	            return new Product(
	                rs.getInt("id"),
	                rs.getString("name"),
	                rs.getString("size"),
	                rs.getString("color"),
	                rs.getDouble("price"),
	                supplier
	            );
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	    return null;
	}

	public List<Product> searchProducts(String keyword) {
	    List<Product> list = new ArrayList<>();
	    String sql = """
	        SELECT pr.*, s.person_id, s.name AS supplier_name, s.phone, s.email, s.company_name
	        FROM product pr
	        LEFT JOIN supplier s ON pr.supplier_id = s.person_id
	        WHERE pr.name LIKE ? OR pr.id LIKE ?
	        """;
	    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
	        stmt.setString(1, "%" + keyword + "%");
	        stmt.setString(2, "%" + keyword + "%");
	        ResultSet rs = stmt.executeQuery();
	        while (rs.next()) {
	            Supplier supplier = null;
	            int supplierId = rs.getInt("supplier_id");
	            if (!rs.wasNull()) {
	                supplier = new Supplier(
	                    supplierId,
	                    rs.getString("supplier_name"),
	                    rs.getString("phone"),
	                    rs.getString("email"),
	                    rs.getString("company_name")
	                );
	            }

	            list.add(new Product(
	                rs.getInt("id"),
	                rs.getString("name"),
	                rs.getString("size"),
	                rs.getString("color"),
	                rs.getDouble("price"),
	                supplier
	            ));
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

	public List<Employee> getAllEmployees() {
		List<Employee> list = new ArrayList<>();
		String sql = """
				SELECT p.id, p.name, p.phone, p.email, e.salary
				FROM person p
				JOIN employee e ON p.id = e.person_id
				WHERE p.type = 'employee'
				""";
		try (Statement stmt = conn.createStatement()) {
			ResultSet rs = stmt.executeQuery(sql);
			while (rs.next()) {
				list.add(new Employee(rs.getInt("id"), rs.getString("name"), rs.getString("phone"),
						rs.getString("email"), rs.getDouble("salary")));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return list;
	}

	public boolean updateEmployee(Employee emp) {
		String sql1 = "UPDATE person SET name=?, phone=?, email=? WHERE id=?";
		String sql2 = "UPDATE employee SET salary=? WHERE person_id=?";
		try (PreparedStatement s1 = conn.prepareStatement(sql1); PreparedStatement s2 = conn.prepareStatement(sql2)) {
			s1.setString(1, emp.getName());
			s1.setString(2, emp.getPhone());
			s1.setString(3, emp.getEmail());
			s1.setInt(4, emp.getId());
			s1.executeUpdate();

			s2.setDouble(1, emp.getSalary());
			s2.setInt(2, emp.getId());
			s2.executeUpdate();

			return true;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	public boolean deleteEmployee(int id) {
		try (PreparedStatement s1 = conn.prepareStatement("DELETE FROM employee WHERE person_id=?");
				PreparedStatement s2 = conn.prepareStatement("DELETE FROM person WHERE id=?")) {
			s1.setInt(1, id);
			s1.executeUpdate();

			s2.setInt(1, id);
			s2.executeUpdate();
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	public Employee getEmployeeById(int id) {
		String sql = """
				SELECT p.id, p.name, p.phone, p.email, e.salary
				FROM person p
				JOIN employee e ON p.id = e.person_id
				WHERE p.id=? AND p.type='employee'
				""";
		try (PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setInt(1, id);
			ResultSet rs = stmt.executeQuery();
			if (rs.next()) {
				return new Employee(rs.getInt("id"), rs.getString("name"), rs.getString("phone"), rs.getString("email"),
						rs.getDouble("salary"));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
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
	


	public List<Supplier> getAllSuppliers() {
	    List<Supplier> list = new ArrayList<>();
	    String sql = """
	        SELECT s.person_id, p.name, p.phone, p.email, s.company_name
	        FROM supplier s
	        JOIN person p ON s.person_id = p.id
	    """;
	    try (Statement stmt = conn.createStatement()) {
	        ResultSet rs = stmt.executeQuery(sql);
	        while (rs.next()) {
	            list.add(new Supplier(
	                rs.getInt("person_id"),
	                rs.getString("name"),
	                rs.getString("phone"),
	                rs.getString("email"),
	                rs.getString("company_name")
	            ));
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	    return list;
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

	public List<OrderSummary> getAllOrdersWithTotal() {
	    List<OrderSummary> list = new ArrayList<>();

	    String sql = """
	        SELECT o.id AS order_id,
	               p.name AS customer_name,
	               o.order_date,
	               SUM(oi.quantity * oi.price) AS total
	        FROM order_table o
	        JOIN person p ON o.customer_id = p.id
	        JOIN order_item oi ON o.id = oi.order_id
	        GROUP BY o.id, p.name, o.order_date
	        """;

	    try (Statement stmt = conn.createStatement()) {
	        ResultSet rs = stmt.executeQuery(sql);
	        while (rs.next()) {
	            list.add(new OrderSummary(
	                    rs.getInt("order_id"),
	                    rs.getString("customer_name"),
	                    rs.getDate("order_date").toLocalDate(),
	                    rs.getDouble("total")
	            ));
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	    return list;
	}


}
