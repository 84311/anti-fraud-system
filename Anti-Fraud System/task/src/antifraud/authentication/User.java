package antifraud.authentication;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.Hibernate;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Objects;

@Entity(name = "user")
@Table(name = "user")
@Getter
@Setter
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @NotNull
    private String name;

    @NotNull
    private String username;

    @JsonIgnore
    @NotNull
    private String password;

    @NotNull
    @JsonIgnore
    private String role;

    @JsonIgnore
    private boolean accountNonLocked;

    public User() {
    }

    public User(String name, String username, String password, String role, boolean isAccountNonLocked) {
        this.name = name;
        this.username = username;
        this.password = password;
        this.role = role;
        this.accountNonLocked = isAccountNonLocked;
    }

    @JsonProperty("role")
    public String getRoleWithoutPrefix() {
        return role.substring(5);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        User user = (User) o;
        return id != null && Objects.equals(id, user.id);
    }
}
