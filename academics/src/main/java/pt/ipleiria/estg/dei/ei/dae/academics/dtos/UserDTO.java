package pt.ipleiria.estg.dei.ei.dae.academics.dtos;

import org.hibernate.Hibernate;
import pt.ipleiria.estg.dei.ei.dae.academics.ejbs.Role;
import pt.ipleiria.estg.dei.ei.dae.academics.entities.Administrator;
import pt.ipleiria.estg.dei.ei.dae.academics.entities.Collaborator;
import pt.ipleiria.estg.dei.ei.dae.academics.entities.Responsible;
import pt.ipleiria.estg.dei.ei.dae.academics.entities.User;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

public class UserDTO implements Serializable {
    //private long id;
    private String name;
    private String email;
    private Role role;
    // Password é usada apenas na criação (input), não na saída
    private String password;

    public UserDTO() {
    }

    public UserDTO(String name, String email, Role role) {
        this.name = name;
        this.email = email;
        this.role = role;
    }

    public static UserDTO from(User user) {
        return new UserDTO(
                user.getName(),
                user.getEmail(),
                mapRole(user)
        );
    }

    private static Role mapRole(User user) {
        if (user instanceof Administrator) return Role.Administrador;
        if (user instanceof Responsible)   return Role.Responsavel;
        if (user instanceof Collaborator)  return Role.Colaborador;
        throw new IllegalStateException("User sem role válido");
    }
    public static List<UserDTO> from(List<User> users) {
        return users.stream().map(UserDTO::from).collect(Collectors.toList());
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

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}