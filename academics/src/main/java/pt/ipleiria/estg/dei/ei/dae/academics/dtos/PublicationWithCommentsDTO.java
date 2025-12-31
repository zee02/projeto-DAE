package pt.ipleiria.estg.dei.ei.dae.academics.dtos;

import pt.ipleiria.estg.dei.ei.dae.academics.entities.Publication;

import java.util.List;

public class PublicationWithCommentsDTO {
    private Long id;
    private String title;
    private List<CommentDTO> comments;

    public static PublicationWithCommentsDTO from(Publication p) {
        PublicationWithCommentsDTO dto = new PublicationWithCommentsDTO();
        dto.id = p.getId();
        dto.title = p.getTitle();
        dto.comments = p.getComments()
                .stream()
                .map(CommentDTO::from)
                .toList();
        return dto;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<CommentDTO> getComments() {
        return comments;
    }

    public void setComments(List<CommentDTO> comments) {
        this.comments = comments;
    }
}