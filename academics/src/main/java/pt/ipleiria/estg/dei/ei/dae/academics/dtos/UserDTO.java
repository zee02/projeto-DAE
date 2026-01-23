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
    private long id;
    private String name;
    private String email;
    private Role role;
    private boolean active;
    // Password é usada apenas na criação (input), não na saída
    private String password;
    private List<TagDTO> subscribedTags;

    public UserDTO() {
    }

    public UserDTO(long id, String name, String email, Role role, boolean active) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.role = role;
        this.active = active;
    }

    public UserDTO(long id, String name, String email, Role role, boolean active, List<TagDTO> subscribedTags) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.role = role;
        this.active = active;
        this.subscribedTags = subscribedTags;
    }

    public static UserDTO from(User user) {
        return new UserDTO(
                user.getId(),
                user.getName(),
                user.getEmail(),
                mapRole(user),
                user.isActive()
        );
    }

    public static UserDTO fromWithTags(User user) {
        List<TagDTO> tags = null;
        if (user.getSubscribedTags() != null && Hibernate.isInitialized(user.getSubscribedTags())) {
            tags = TagDTO.from(user.getSubscribedTags());
        }
        return new UserDTO(
                user.getId(),
                user.getName(),
                user.getEmail(),
                mapRole(user),
                user.isActive(),
                tags
        );
    }

    private static Role mapRole(User user) {
        if (user instanceof Administrator) return Role.Administrator;
        if (user instanceof Responsible)   return Role.Manager;
        if (user instanceof Collaborator)  return Role.Collaborator;
        throw new IllegalStateException("User sem role válido");
    }
    public static List<UserDTO> from(List<User> users) {
        return users.stream().map(UserDTO::from).collect(Collectors.toList());
    }
    // Getters e Setters Completos

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

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

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public List<TagDTO> getSubscribedTags() {
        return subscribedTags;
    }

    public void setSubscribedTags(List<TagDTO> subscribedTags) {
        this.subscribedTags = subscribedTags;
    }
}
