package pt.ipleiria.estg.dei.ei.dae.academics.dtos;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.hibernate.Hibernate;
import pt.ipleiria.estg.dei.ei.dae.academics.entities.Comment;
import pt.ipleiria.estg.dei.ei.dae.academics.entities.Publication;
import pt.ipleiria.estg.dei.ei.dae.academics.entities.Rating;
import pt.ipleiria.estg.dei.ei.dae.academics.entities.Tag;

import java.sql.Time;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class CommentDTO {

    private long id;
    private long postId;
    private AuthorDTO author;
    @NotNull(message = "Comment is required")
    private String comment;
    private Date createdAt;
    private Date updatedAt;
    private boolean visible;

    public static List<CommentDTO> from(List<Comment> comments) {
        return comments.stream()
                .map(CommentDTO::from)
                .collect(java.util.stream.Collectors.toList());
    }


    public static CommentDTO from(Comment p) {
        CommentDTO dto = new CommentDTO();
        dto.id = p.getId();
        dto.postId = p.getPublication().getId();
        dto.author = AuthorDTO.from(p.getAuthor());
        dto.comment = p.getContent();
        dto.createdAt = p.getCreatedAt();
        dto.updatedAt = p.getUpdatedAt();
        dto.visible = true;

        return dto;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getPostId() {
        return postId;
    }

    public void setPostId(long postId) {
        this.postId = postId;
    }

    public AuthorDTO getAuthor() {
        return author;
    }

    public void setAuthor(AuthorDTO author) {
        this.author = author;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }
}