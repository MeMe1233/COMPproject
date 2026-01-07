package application;

import java.time.LocalDate;

public class Order {
	private int id;
	private Customer customer;
	private LocalDate orderDate;

	public Order() {
	}

	public Order(int id, Customer customer, LocalDate orderDate) {
		this.id = id;
		this.customer = customer;
		this.orderDate = orderDate;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public Customer getCustomer() {
		return customer;
	}

	public void setCustomer(Customer customer) {
		this.customer = customer;
	}

	public LocalDate getOrderDate() {
		return orderDate;
	}

	public void setOrderDate(LocalDate orderDate) {
		this.orderDate = orderDate;
	}

}
