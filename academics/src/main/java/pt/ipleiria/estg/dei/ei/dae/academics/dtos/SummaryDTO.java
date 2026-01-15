package pt.ipleiria.estg.dei.ei.dae.academics.dtos;

import jakarta.validation.constraints.NotBlank;

public class SummaryDTO {
    @NotBlank(message = "O campo summary não pode estar vazio")
    private String summary;

    public SummaryDTO() {
    }

    public SummaryDTO(String summary) {
        this.summary = summary;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }
}

