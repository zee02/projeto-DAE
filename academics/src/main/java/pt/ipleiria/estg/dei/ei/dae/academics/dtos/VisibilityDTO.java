package pt.ipleiria.estg.dei.ei.dae.academics.dtos;

import jakarta.validation.constraints.NotNull;

public class VisibilityDTO {
    @NotNull(message = "O campo visible é obrigatório")
    private Boolean visible;

    public VisibilityDTO() {
    }

    public VisibilityDTO(Boolean visible) {
        this.visible = visible;
    }

    public Boolean getVisible() {
        return visible;
    }

    public void setVisible(Boolean visible) {
        this.visible = visible;
    }
}

