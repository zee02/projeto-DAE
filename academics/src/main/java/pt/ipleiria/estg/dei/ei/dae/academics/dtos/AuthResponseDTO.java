package pt.ipleiria.estg.dei.ei.dae.academics.dtos;


import pt.ipleiria.estg.dei.ei.dae.academics.entities.User;

import java.io.Serializable;
public class AuthResponseDTO implements Serializable {

    private String message;
    private String token;
    private UserDTO user;

    public AuthResponseDTO(String message, String token, User user) {
        this.message = message;
        this.token = token;
        this.user = UserDTO.from(user);
    }

    public String getMessage() {
        return message;
    }

    public String getToken() {
        return token;
    }

    public UserDTO getUser() {
        return user;
    }
}