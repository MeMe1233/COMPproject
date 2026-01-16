package application;

import java.time.LocalDate;

public class Order {
    private int id;
    private Customer customer;
    private LocalDate orderDate;

    
    private String status; // "CART" or "PLACED"

    public Order() {
    }

    public Order(int id, Customer customer, LocalDate orderDate) {
        this.id = id;
        this.customer = customer;
        this.orderDate = orderDate;
        this.status = "PLACED";
    }

    //constructor
    public Order(int id, Customer customer, LocalDate orderDate, String status) {
        this.id = id;
        this.customer = customer;
        this.orderDate = orderDate;
        this.status = status;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public Customer getCustomer() { return customer; }
    public void setCustomer(Customer customer) { this.customer = customer; }

    public LocalDate getOrderDate() { return orderDate; }
    public void setOrderDate(LocalDate orderDate) { this.orderDate = orderDate; }

    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
