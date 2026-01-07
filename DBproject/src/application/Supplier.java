package application;

public class Supplier extends person {

    private String companyName;

    public Supplier() {}

    public Supplier(int id, String name, String phone, String email, String companyName) {
        super(id, name, phone, email);
        this.companyName = companyName;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }
}

