package pt.ipleiria.estg.dei.ei.dae.academics.dtos;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public class RoleDTO {
    @NotNull(message = "O campo role é obrigatório")
    @Pattern(regexp = "^(Colaborador|Responsavel|Administrador)$",
             message = "Role deve ser: Colaborador, Responsavel ou Administrador")
    private String role;

    public RoleDTO() {
    }

    public RoleDTO(String role) {
        this.role = role;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}

