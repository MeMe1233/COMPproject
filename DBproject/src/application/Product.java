package application;

public class Product {
	private int id;
	private String name;
	private String size;
	private String color;
	private double price;

	public Product() {
	}

	public Product(int id, String name, String size, String color, double price) {
		this.id = id;
		this.name = name;
		this.size = size;
		this.color = color;
		this.price = price;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getSize() {
		return size;
	}

	public void setSize(String size) {
		this.size = size;
	}

	public String getColor() {
		return color;
	}

	public void setColor(String color) {
		this.color = color;
	}

	public double getPrice() {
		return price;
	}

	public void setPrice(double price) {
		this.price = price;
	}

}
