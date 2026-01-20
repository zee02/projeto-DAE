package pt.ipleiria.estg.dei.ei.dae.academics.dtos;

import jakarta.json.bind.annotation.JsonbProperty;
import jakarta.json.bind.annotation.JsonbTransient;
import org.hibernate.Hibernate;
import pt.ipleiria.estg.dei.ei.dae.academics.entities.Publication;
import pt.ipleiria.estg.dei.ei.dae.academics.entities.Tag;

import java.io.Serializable;
import java.sql.Timestamp;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class PublicationDTO implements Serializable {

    private long id;
    private String title;
    private String scientificArea;
    private String summary;
    private boolean visible;
    private String fileUrl;
    private AuthorDTO author;
    private double averageRating;
    private int ratingsCount;
    private List<TagDTO> tags;
    private List<CommentDTO> comments;
    private Timestamp createdAt;
    private Timestamp updatedAt;


    public static List<PublicationDTO> from(List<Publication> publications) {
        return publications.stream()
                .map(PublicationDTO::from)
                .collect(java.util.stream.Collectors.toList());
    }

    public static PublicationDTO from(Publication p) {
        PublicationDTO dto = new PublicationDTO();
        dto.id = p.getId();
        dto.title = p.getTitle();
        dto.scientificArea = p.getScientificArea();
        dto.summary = p.getSummary();
        dto.visible = p.isVisible();
        dto.fileUrl = "/api/posts/" + p.getId() + "/file";
        dto.author = AuthorDTO.from(p.getAuthor());
        dto.averageRating = p.getAverageRating();
        dto.ratingsCount = p.getRatingsCount();
        dto.tags = new LinkedList<>();
        dto.createdAt = p.getCreatedAt();
        dto.updatedAt = p.getUpdatedAt();

        return dto;
    }

    /* getters JavaBeans */

    public long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getScientificArea() {
        return scientificArea;
    }

    public String getSummary() {
        return summary;
    }

    public boolean isVisible() {
        return visible;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public AuthorDTO getAuthor() {
        return author;
    }

    public double getAverageRating() {
        return averageRating;
    }

    public int getRatingsCount() {
        return ratingsCount;
    }

    public List<TagDTO> getTags() {
        return tags;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public List<CommentDTO> getComments() {
        return comments;
    }

    public void setTags(List<TagDTO> tags) {
        this.tags = tags;
    }

    public void setComments(List<CommentDTO> comments) {
        this.comments = comments;
    }
}
