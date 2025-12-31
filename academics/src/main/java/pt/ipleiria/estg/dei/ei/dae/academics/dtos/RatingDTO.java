package pt.ipleiria.estg.dei.ei.dae.academics.dtos;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import pt.ipleiria.estg.dei.ei.dae.academics.entities.Publication;
import pt.ipleiria.estg.dei.ei.dae.academics.entities.Rating;
import pt.ipleiria.estg.dei.ei.dae.academics.entities.Tag;

import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class RatingDTO {

    @NotNull(message = "Score is required")
    @Min(value = 1, message = "Score must be between 1 and 5")
    @Max(value = 5, message = "Score must be between 1 and 5")
    private Integer score;


    public static RatingDTO from(Rating p) {
        RatingDTO dto = new RatingDTO();
        dto.score = p.getScore();
        return dto;
    }
    public Integer getRating() {
        return score;
    }

    public void setRating(Integer rating) {
        this.score = rating;
    }
}