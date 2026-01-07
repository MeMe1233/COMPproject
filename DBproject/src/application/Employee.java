package application;

public class Employee extends person {

	private double salary;

	public Employee() {
	}

	public Employee(int id, String name, String phone, String email, double salary) {
		super(id, name, phone, email);
		this.salary = salary;
	}

	public double getSalary() {
		return salary;
	}

	public void setSalary(double salary) {
		this.salary = salary;
	}
}
