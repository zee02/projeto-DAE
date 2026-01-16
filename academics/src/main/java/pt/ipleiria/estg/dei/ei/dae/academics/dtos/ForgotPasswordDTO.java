package pt.ipleiria.estg.dei.ei.dae.academics.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class ForgotPasswordDTO {
    @NotBlank(message = "O campo email é obrigatório")
    @Email(message = "Email inválido")
    private String email;

    public ForgotPasswordDTO() {
    }

    public ForgotPasswordDTO(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
