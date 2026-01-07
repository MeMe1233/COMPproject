package application;

public class Admin extends person {

	private String role;

	public Admin() {
	}

	public Admin(int id, String name, String phone, String email, String role) {
		super(id, name, phone, email);
		this.role = role;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}
}
