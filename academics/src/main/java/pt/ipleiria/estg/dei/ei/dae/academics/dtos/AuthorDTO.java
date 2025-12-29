package pt.ipleiria.estg.dei.ei.dae.academics.dtos;

import pt.ipleiria.estg.dei.ei.dae.academics.entities.User;
import java.io.Serializable;

public class AuthorDTO implements Serializable {
    private Long id;
    private String name;

    public static AuthorDTO from(User user) {
        AuthorDTO dto = new AuthorDTO();
        dto.id = user.getId();
        dto.name = user.getName();
        return dto;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
}
