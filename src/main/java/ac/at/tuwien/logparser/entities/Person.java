package ac.at.tuwien.logparser.entities;

import java.util.List;

public class Person {

    private String id;
    private String firstName;
    private String lastName;
    private String email;
    private List<String> userName;

    public Person(String firstName, String lastName, String email, List<String> userName) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.userName = userName;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public List<String> getUserName() {
        return userName;
    }

    public void setUserName(List<String> userName) {
        this.userName = userName;
    }
}
