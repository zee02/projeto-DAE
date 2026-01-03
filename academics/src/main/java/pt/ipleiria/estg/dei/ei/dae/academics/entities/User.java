package pt.ipleiria.estg.dei.ei.dae.academics.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;
import java.util.List;

@Entity
@Table(name = "users")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@NamedQueries({
        @NamedQuery(name = "getAllUsers", query = "SELECT u FROM User u ORDER BY u.name"),
        @NamedQuery(name = "findUserByEmail", query = "SELECT u FROM User u WHERE u.email = :email"),
        @NamedQuery(name = "findUser", query = "SELECT u FROM User u WHERE u.id = :id")
})
@DiscriminatorColumn(
        name = "dtype",
        discriminatorType = DiscriminatorType.STRING
)
public class User implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(unique = true, nullable = false)
    private long id;

    @NotNull
    @Email
    @Column(unique = true, nullable = false)
    private String email;

    @NotNull
    private String password;

    @NotNull
    private String name;


    // Novo campo para resolver o erro setActive
    private boolean active = true;

    @OneToMany(mappedBy = "author")
    public List<Publication> publications;

    @OneToMany(mappedBy = "author")
    public List<Comment> comments;

    public User() {
    }

    public User(String email, String password, String name) {
        this.email = email;
        this.password = password;
        this.name = name;
        this.active = true;
    }

    // Método auxiliar para compatibilidade com TokenIssuer
    public String getIdAsString() {
        return String.valueOf(this.id);
    }

    // Getters e Setters
    public long getId() {
        return this.id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    // O método que estava a faltar nos Beans
    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}