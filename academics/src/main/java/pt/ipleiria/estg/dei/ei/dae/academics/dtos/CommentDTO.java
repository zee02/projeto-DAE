package pt.ipleiria.estg.dei.ei.dae.academics.dtos;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import pt.ipleiria.estg.dei.ei.dae.academics.entities.Comment;
import pt.ipleiria.estg.dei.ei.dae.academics.entities.Rating;

public class CommentDTO {

    @NotNull(message = "Comment is required")
    private String comment;

    public static CommentDTO from(Comment p) {
        CommentDTO dto = new CommentDTO();
        dto.comment = p.getContent();
        return dto;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}