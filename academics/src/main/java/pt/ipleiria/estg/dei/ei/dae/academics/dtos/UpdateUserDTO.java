package pt.ipleiria.estg.dei.ei.dae.academics.dtos;

import jakarta.validation.constraints.Email;

public class UpdateUserDTO {
    private String name;

    @Email(message = "Email inválido")
    private String email;

    public UpdateUserDTO() {
    }

    public UpdateUserDTO(String name, String email) {
        this.name = name;
        this.email = email;
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
}

