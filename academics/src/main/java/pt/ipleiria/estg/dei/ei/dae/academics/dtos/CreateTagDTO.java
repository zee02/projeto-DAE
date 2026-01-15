package pt.ipleiria.estg.dei.ei.dae.academics.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CreateTagDTO {
    @NotBlank(message = "O nome da tag não pode estar vazio")
    @Size(max = 50, message = "O nome da tag não pode ter mais de 50 caracteres")
    private String name;

    public CreateTagDTO() {
    }

    public CreateTagDTO(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}

