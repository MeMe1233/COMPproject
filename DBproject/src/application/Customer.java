package application;

public class Customer extends person {

    private String address;

    public Customer() {}

    public Customer(int id, String name, String phone, String email, String address) {
        super(id, name, phone, email);
        this.address = address;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
