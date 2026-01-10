package application;

public class Product {
	private int id;
	private String name;
	private String size;
	private String color;
	private double price;
	private Supplier supplier;

	public Product() {
	}

	public Product(int id, String name, String size, String color, double price, Supplier supplier) {
        this.id = id;
        this.name = name;
        this.size = size;
        this.color = color;
        this.price = price;
        this.supplier = supplier; // NEW
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

	public Supplier getSupplier() {
		return supplier;
	}

	public void setSupplier(Supplier supplier) {
		this.supplier = supplier;
	}
	

}
