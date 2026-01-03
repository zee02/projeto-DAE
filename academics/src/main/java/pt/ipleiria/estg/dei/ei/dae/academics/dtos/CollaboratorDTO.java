package pt.ipleiria.estg.dei.ei.dae.academics.dtos;

import org.hibernate.Hibernate;
import pt.ipleiria.estg.dei.ei.dae.academics.entities.User;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

public class CollaboratorDTO implements Serializable {

    private String name;
    private String email;
    private String role;


    public CollaboratorDTO() {
    }

    public CollaboratorDTO(String name, String email, String role) {
        this.name = name;
        this.email = email;
        this.role = role;
    }

    public static CollaboratorDTO from(User user) {
        return new CollaboratorDTO(
                user.getName(),
                user.getEmail(),
                Hibernate.getClass(user).getSimpleName()
        );
    }
    public static List<CollaboratorDTO> from(List<User> users) {
        return users.stream().map(CollaboratorDTO::from).collect(Collectors.toList());
    }
    // Getters e Setters Completos


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }


}