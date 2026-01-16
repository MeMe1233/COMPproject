package application;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

    // ✅ NEW: small safety helpers (won't change your logic)
    private boolean isConnected() {
        try {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }

    private void ensureConnection() {
        if (!isConnected()) {
            throw new RuntimeException("Database is not connected. Check MySQL service / DB name / user/pass.");
        }
    }

    // ------------------- PRODUCT -------------------
    public boolean addProduct(Product p) {
        ensureConnection();

        String sql = "INSERT INTO product(name, size, color, price, supplier_id) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, p.getName());
            stmt.setString(2, p.getSize());
            stmt.setString(3, p.getColor());
            stmt.setDouble(4, p.getPrice());
            if (p.getSupplier() != null) stmt.setInt(5, p.getSupplier().getId());
            else stmt.setNull(5, Types.INTEGER);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Product> getAllProducts() {
        ensureConnection();

        List<Product> list = new ArrayList<>();

        String sql = """
            SELECT pr.id, pr.name, pr.size, pr.color, pr.price, pr.supplier_id,
                   p.name  AS supplier_name,
                   p.phone AS supplier_phone,
                   p.email AS supplier_email,
                   s.company_name
            FROM product pr
            LEFT JOIN supplier s ON pr.supplier_id = s.person_id
            LEFT JOIN person   p ON s.person_id = p.id
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
                            rs.getString("supplier_phone"),
                            rs.getString("supplier_email"),
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
        ensureConnection();

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
        ensureConnection();

        String sql = """
            SELECT pr.id, pr.name, pr.size, pr.color, pr.price, pr.supplier_id,
                   p.name  AS supplier_name,
                   p.phone AS supplier_phone,
                   p.email AS supplier_email,
                   s.company_name
            FROM product pr
            LEFT JOIN supplier s ON pr.supplier_id = s.person_id
            LEFT JOIN person   p ON s.person_id = p.id
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
                            rs.getString("supplier_phone"),
                            rs.getString("supplier_email"),
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
        ensureConnection();

        List<Product> list = new ArrayList<>();

        String sql = """
            SELECT pr.id, pr.name, pr.size, pr.color, pr.price, pr.supplier_id,
                   p.name  AS supplier_name,
                   p.phone AS supplier_phone,
                   p.email AS supplier_email,
                   s.company_name
            FROM product pr
            LEFT JOIN supplier s ON pr.supplier_id = s.person_id
            LEFT JOIN person   p ON s.person_id = p.id
            WHERE pr.name LIKE ? OR pr.id LIKE ?
            """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            String k = "%" + keyword + "%";
            stmt.setString(1, k);
            stmt.setString(2, k);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Supplier supplier = null;

                int supplierId = rs.getInt("supplier_id");
                if (!rs.wasNull()) {
                    supplier = new Supplier(
                            supplierId,
                            rs.getString("supplier_name"),
                            rs.getString("supplier_phone"),
                            rs.getString("supplier_email"),
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
        ensureConnection();

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
        ensureConnection();

        List<Customer> list = new ArrayList<>();
        String sql = "SELECT p.id, p.name, p.phone, p.email, c.address "
                + "FROM person p JOIN customer c ON p.id = c.person_id";
        try (Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                list.add(new Customer(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("phone"),
                        rs.getString("email"),
                        rs.getString("address")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // ------------------- CUSTOMER LOGIN / REGISTER -------------------
    public boolean registerCustomer(Customer c, String username, String password) {
        ensureConnection();

        String sql1 = "INSERT INTO person(name, phone, email, type, username, pass) VALUES (?, ?, ?, 'customer', ?, ?)";
        String sql2 = "INSERT INTO customer(person_id, address) VALUES (?, ?)";

        try (PreparedStatement stmt1 = conn.prepareStatement(sql1, Statement.RETURN_GENERATED_KEYS);
             PreparedStatement stmt2 = conn.prepareStatement(sql2)) {

            stmt1.setString(1, c.getName());
            stmt1.setString(2, c.getPhone());
            stmt1.setString(3, c.getEmail());
            stmt1.setString(4, username);
            stmt1.setString(5, password);
            stmt1.executeUpdate();

            ResultSet rs = stmt1.getGeneratedKeys();
            if (rs.next()) {
                int personId = rs.getInt(1);
                stmt2.setInt(1, personId);
                stmt2.setString(2, c.getAddress());
                stmt2.executeUpdate();
                return true;
            }

            return false;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public Customer loginCustomer(String username, String password) {
        ensureConnection();

        String sql = """
            SELECT p.id, p.name, p.phone, p.email, c.address
            FROM person p
            JOIN customer c ON p.id = c.person_id
            WHERE p.type='customer' AND p.username=? AND p.pass=?
            """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.setString(2, password);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new Customer(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("phone"),
                        rs.getString("email"),
                        rs.getString("address")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    // ------------------- EMPLOYEE -------------------
    public boolean addEmployee(Employee e) {
        ensureConnection();

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
        ensureConnection();

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
                list.add(new Employee(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("phone"),
                        rs.getString("email"),
                        rs.getDouble("salary")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean updateEmployee(Employee emp) {
        ensureConnection();

        String sql1 = "UPDATE person SET name=?, phone=?, email=? WHERE id=?";
        String sql2 = "UPDATE employee SET salary=? WHERE person_id=?";
        try (PreparedStatement s1 = conn.prepareStatement(sql1);
             PreparedStatement s2 = conn.prepareStatement(sql2)) {

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
        ensureConnection();

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
        ensureConnection();

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
                return new Employee(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("phone"),
                        rs.getString("email"),
                        rs.getDouble("salary")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // ------------------- SUPPLIER -------------------
    public boolean addSupplier(Supplier s) {
        ensureConnection();

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
        ensureConnection();

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

    public boolean updateSupplier(Supplier s) {
        ensureConnection();

        String sql1 = "UPDATE person SET name=?, phone=?, email=? WHERE id=? AND type='supplier'";
        String sql2 = "UPDATE supplier SET company_name=? WHERE person_id=?";
        try (PreparedStatement p1 = conn.prepareStatement(sql1);
             PreparedStatement p2 = conn.prepareStatement(sql2)) {

            p1.setString(1, s.getName());
            p1.setString(2, s.getPhone());
            p1.setString(3, s.getEmail());
            p1.setInt(4, s.getId());
            p1.executeUpdate();

            p2.setString(1, s.getCompanyName());
            p2.setInt(2, s.getId());
            p2.executeUpdate();

            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteSupplier(int id) {
        ensureConnection();

        try (PreparedStatement s1 = conn.prepareStatement("DELETE FROM supplier WHERE person_id=?");
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

    public Supplier getSupplierById(int id) {
        ensureConnection();

        String sql = """
            SELECT s.person_id, p.name, p.phone, p.email, s.company_name
            FROM supplier s
            JOIN person p ON s.person_id = p.id
            WHERE s.person_id = ?
            """;
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new Supplier(
                        rs.getInt("person_id"),
                        rs.getString("name"),
                        rs.getString("phone"),
                        rs.getString("email"),
                        rs.getString("company_name")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Supplier> searchSuppliers(String keyword) {
        ensureConnection();

        List<Supplier> list = new ArrayList<>();
        String sql = """
            SELECT s.person_id, p.name, p.phone, p.email, s.company_name
            FROM supplier s
            JOIN person p ON s.person_id = p.id
            WHERE p.name LIKE ? OR s.person_id LIKE ? OR s.company_name LIKE ?
            """;
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            String k = "%" + keyword + "%";
            stmt.setString(1, k);
            stmt.setString(2, k);
            stmt.setString(3, k);

            ResultSet rs = stmt.executeQuery();
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
        ensureConnection();

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
        ensureConnection();

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
    
 // Delete an order without restoring stock
    public boolean deleteOrder(int orderId) {
        ensureConnection();

        // Delete order items first
        String deleteItems = "DELETE FROM order_item WHERE order_id=?";
        try (PreparedStatement stmt = conn.prepareStatement(deleteItems)) {
            stmt.setInt(1, orderId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }

        // Delete the order itself
        String deleteOrder = "DELETE FROM order_table WHERE id=?";
        try (PreparedStatement stmt = conn.prepareStatement(deleteOrder)) {
            stmt.setInt(1, orderId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


    // ------------------- STOCK (NO PLACE) -------------------
    public boolean addStock(int productId, int quantity) {
        ensureConnection();

        if (quantity <= 0) return false;

        String sql = """
            INSERT INTO stock(product_id, quantity)
            VALUES (?, ?)
            ON DUPLICATE KEY UPDATE quantity = quantity + VALUES(quantity)
            """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, productId);
            stmt.setInt(2, quantity);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateStockQuantity(int stockId, int newQuantity) {
        ensureConnection();

        String sql = "UPDATE stock SET quantity=? WHERE id=?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, newQuantity);
            stmt.setInt(2, stockId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteStock(int stockId) {
        ensureConnection();

        String sql = "DELETE FROM stock WHERE id=?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, stockId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Stock> getAllStock() {
        ensureConnection();

        List<Stock> list = new ArrayList<>();

        String sql = """
            SELECT st.id AS stock_id, st.quantity,
                   pr.id AS product_id, pr.name, pr.size, pr.color, pr.price,
                   s.person_id AS supplier_id,
                   p.name  AS supplier_name,
                   p.phone AS supplier_phone,
                   p.email AS supplier_email,
                   s.company_name
            FROM stock st
            JOIN product pr ON st.product_id = pr.id
            LEFT JOIN supplier s ON pr.supplier_id = s.person_id
            LEFT JOIN person   p ON s.person_id = p.id
            ORDER BY st.id
            """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {

                Supplier supplier = null;
                int sid = rs.getInt("supplier_id");
                if (!rs.wasNull()) {
                    supplier = new Supplier(
                            sid,
                            rs.getString("supplier_name"),
                            rs.getString("supplier_phone"),
                            rs.getString("supplier_email"),
                            rs.getString("company_name")
                    );
                }

                Product prod = new Product(
                        rs.getInt("product_id"),
                        rs.getString("name"),
                        rs.getString("size"),
                        rs.getString("color"),
                        rs.getDouble("price"),
                        supplier
                );

                list.add(new Stock(
                        rs.getInt("stock_id"),
                        prod,
                        rs.getInt("quantity")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    public List<Stock> searchStock(String keyword) {
        ensureConnection();

        List<Stock> list = new ArrayList<>();

        String sql = """
            SELECT st.id AS stock_id, st.quantity,
                   pr.id AS product_id, pr.name, pr.size, pr.color, pr.price,
                   s.person_id AS supplier_id,
                   p.name  AS supplier_name,
                   p.phone AS supplier_phone,
                   p.email AS supplier_email,
                   s.company_name
            FROM stock st
            JOIN product pr ON st.product_id = pr.id
            LEFT JOIN supplier s ON pr.supplier_id = s.person_id
            LEFT JOIN person   p ON s.person_id = p.id
            WHERE (pr.name LIKE ? OR pr.id LIKE ? OR st.id LIKE ?)
            ORDER BY st.id
            """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            String k = "%" + keyword + "%";
            stmt.setString(1, k);
            stmt.setString(2, k);
            stmt.setString(3, k);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {

                Supplier supplier = null;
                int sid = rs.getInt("supplier_id");
                if (!rs.wasNull()) {
                    supplier = new Supplier(
                            sid,
                            rs.getString("supplier_name"),
                            rs.getString("supplier_phone"),
                            rs.getString("supplier_email"),
                            rs.getString("company_name")
                    );
                }

                Product prod = new Product(
                        rs.getInt("product_id"),
                        rs.getString("name"),
                        rs.getString("size"),
                        rs.getString("color"),
                        rs.getDouble("price"),
                        supplier
                );

                list.add(new Stock(
                        rs.getInt("stock_id"),
                        prod,
                        rs.getInt("quantity")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }
    
    

    // ------------------- CUSTOMER LOGIN (Name + ID) -------------------
    public Customer loginCustomerByIdAndName(int id, String name) {
        ensureConnection();

        String sql = """
            SELECT p.id, p.name, p.phone, p.email, c.address
            FROM person p
            JOIN customer c ON p.id = c.person_id
            WHERE p.id = ? AND p.name = ?
            """;
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.setString(2, name);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new Customer(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("phone"),
                        rs.getString("email"),
                        rs.getString("address")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // ------------------- CART: get or create CART order -------------------
    public int getOrCreateCartOrderId(int customerId) {
        ensureConnection();

        // 1️⃣ Always create a new order for this cart
        String create = "INSERT INTO order_table(customer_id, order_date) VALUES (?, ?)";
        try (PreparedStatement s2 = conn.prepareStatement(create, Statement.RETURN_GENERATED_KEYS)) {
            s2.setInt(1, customerId);
            s2.setDate(2, java.sql.Date.valueOf(LocalDate.now())); // must not be null
            s2.executeUpdate();

            ResultSet rs = s2.getGeneratedKeys();
            if (rs.next()) return rs.getInt(1); // return the new order id
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return -1;
    }

 // Only create a new order when placing an order
    public int createOrder(int customerId) {
        ensureConnection();

        String sql = "INSERT INTO order_table(customer_id, order_date) VALUES (?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, customerId);
            stmt.setDate(2, java.sql.Date.valueOf(LocalDate.now())); // must not be null
            stmt.executeUpdate();

            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }



    // ------------------- CART: add item (increase qty if exists) -------------------
    public boolean addToCart(int customerId, int productId, int qty) {
        ensureConnection();

        int cartId = getOrCreateCartOrderId(customerId);
        if (cartId == -1) return false;

        double price = 0;
        String psql = "SELECT price FROM product WHERE id=?";
        try (PreparedStatement ps = conn.prepareStatement(psql)) {
            ps.setInt(1, productId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) price = rs.getDouble("price");
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }

        String check = "SELECT id, quantity FROM order_item WHERE order_id=? AND product_id=? LIMIT 1";
        try (PreparedStatement c = conn.prepareStatement(check)) {
            c.setInt(1, cartId);
            c.setInt(2, productId);
            ResultSet rs = c.executeQuery();
            if (rs.next()) {
                int itemId = rs.getInt("id");
                int oldQ = rs.getInt("quantity");
                String up = "UPDATE order_item SET quantity=? WHERE id=?";
                try (PreparedStatement u = conn.prepareStatement(up)) {
                    u.setInt(1, oldQ + qty);
                    u.setInt(2, itemId);
                    return u.executeUpdate() > 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }

        String ins = "INSERT INTO order_item(order_id, product_id, quantity, price) VALUES (?,?,?,?)";
        try (PreparedStatement i = conn.prepareStatement(ins)) {
            i.setInt(1, cartId);
            i.setInt(2, productId);
            i.setInt(3, qty);
            i.setDouble(4, price);
            return i.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ------------------- CART: read items for customer cart -------------------
    public List<OrderItem> getCartItems(int customerId) {
        ensureConnection();

        List<OrderItem> list = new ArrayList<>();
        int cartId = getOrCreateCartOrderId(customerId);
        if (cartId == -1) return list;

        String sql = """
            SELECT oi.id, oi.quantity, oi.price,
                   pr.id AS product_id, pr.name, pr.size, pr.color, pr.price AS product_price
            FROM order_item oi
            JOIN product pr ON oi.product_id = pr.id
            WHERE oi.order_id = ?
            """;
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, cartId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Product p = new Product(
                        rs.getInt("product_id"),
                        rs.getString("name"),
                        rs.getString("size"),
                        rs.getString("color"),
                        rs.getDouble("product_price"),
                        null
                );
                OrderItem it = new OrderItem(
                        rs.getInt("id"),
                        p,
                        rs.getInt("quantity"),
                        rs.getDouble("price")
                );
                list.add(it);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // ------------------- CART: delete item row -------------------
    public boolean deleteCartItem(int orderItemId) {
        ensureConnection();

        String sql = "DELETE FROM order_item WHERE id=?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, orderItemId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ------------------- CART: place order (CART -> PLACED) -------------------
    public boolean placeOrder(int customerId) {
        ensureConnection();

        int orderId = getOrCreateCartOrderId(customerId);
        if (orderId == -1) return false;

        String sql = "UPDATE order_table SET order_date=? WHERE id=?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDate(1, java.sql.Date.valueOf(LocalDate.now()));
            stmt.setInt(2, orderId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
 // Add item to an existing order
    public boolean addOrderItem(int orderId, int productId, int qty) {
        ensureConnection();

        double price = 0;
        String psql = "SELECT price FROM product WHERE id=?";
        try (PreparedStatement ps = conn.prepareStatement(psql)) {
            ps.setInt(1, productId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) price = rs.getDouble("price");
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }

        String ins = "INSERT INTO order_item(order_id, product_id, quantity, price) VALUES (?,?,?,?)";
        try (PreparedStatement i = conn.prepareStatement(ins)) {
            i.setInt(1, orderId);
            i.setInt(2, productId);
            i.setInt(3, qty);
            i.setDouble(4, price);
            return i.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public boolean placeOrder(int customerId, Map<Product, Integer> cart) {
        if (cart.isEmpty()) return false;

        ensureConnection();

        // 1️⃣ Create a new order
        int orderId = createOrder(customerId);
        if (orderId == -1) return false;

        // 2️⃣ Insert items
        for (Map.Entry<Product, Integer> entry : cart.entrySet()) {
            Product p = entry.getKey();
            int qty = entry.getValue();

            if (!addOrderItem(orderId, p.getId(), qty)) {
                System.out.println("Failed to add item: " + p.getName());
                return false;
            }

            // 3️⃣ Reduce stock in DB
            Stock stockItem = null;
            for (Stock s : getAllStock()) {
                if (s.getProduct().getId() == p.getId()) {
                    stockItem = s;
                    break;
                }
            }

            if (stockItem != null) {
                int newQty = stockItem.getQuantity() - qty;
                if (newQty < 0) newQty = 0;
                updateStockQuantity(stockItem.getId(), newQty);
                stockItem.setQuantity(newQty); // update memory
            }
        }

        return true;
    }


}
