package pt.ipleiria.estg.dei.ei.dae.academics.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class SortRequestDTO {
    @NotBlank(message = "O campo sort_by não pode estar vazio")
    @Pattern(regexp = "^(average_rating|comments_count|ratings_count)$",
             message = "sort_by deve ser: average_rating, comments_count ou ratings_count")
    private String sort_by;

    @NotBlank(message = "O campo order não pode estar vazio")
    @Pattern(regexp = "^(asc|desc)$",
             message = "order deve ser: asc ou desc")
    private String order;

    public SortRequestDTO() {
    }

    public SortRequestDTO(String sort_by, String order) {
        this.sort_by = sort_by;
        this.order = order;
    }

    public String getSort_by() {
        return sort_by;
    }

    public void setSort_by(String sort_by) {
        this.sort_by = sort_by;
    }

    public String getOrder() {
        return order;
    }

    public void setOrder(String order) {
        this.order = order;
    }
}

