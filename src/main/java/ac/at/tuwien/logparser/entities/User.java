package ac.at.tuwien.logparser.entities;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by Agnes on 02.09.18.
 */
public class User {

    @JsonProperty(value = "@type")
    private String type;
    private String domain;
    private String username;

    public User(){}

    public User(String domain, String username) {
        this.domain = domain;
        this.username = username;
    }

    public User(String username) {
        this.username = username;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public String toString() {
        return "User{" +
                "type='" + type + '\'' +
                ", domain='" + domain + '\'' +
                ", username='" + username + '\'' +
                '}';
    }
}
