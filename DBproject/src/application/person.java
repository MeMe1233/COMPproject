package application;

public class person {
    protected int id;
    protected String name;
    protected String phone;
    protected String email;

    // (for login/register)
    protected String username;
    protected String pass;

    public person() {
    }

    public person(int id, String name, String phone, String email) {
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.email = email;
    }

    // constructor if you need it later
    public person(int id, String name, String phone, String email, String username, String pass) {
        this(id, name, phone, email);
        this.username = username;
        this.pass = pass;
    }

    // Getters & Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

   
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPass() { return pass; }
    public void setPass(String pass) { this.pass = pass; }
}