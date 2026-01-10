package application;

import java.time.LocalDate;

public class OrderSummary {

    private int orderId;
    private String customerName;
    private LocalDate orderDate;
    private double total;

    public OrderSummary(int orderId, String customerName, LocalDate orderDate, double total) {
        this.orderId = orderId;
        this.customerName = customerName;
        this.orderDate = orderDate;
        this.total = total;
    }

    public int getOrderId() {
        return orderId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public LocalDate getOrderDate() {
        return orderDate;
    }

    public double getTotal() {
        return total;
    }
}
