package pt.ipleiria.estg.dei.ei.dae.academics.dtos;

import jakarta.json.bind.annotation.JsonbProperty;
import jakarta.json.bind.annotation.JsonbTransient;
import jakarta.persistence.Column;
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
    private boolean confidential;
    private String fileUrl;
    private AuthorDTO author;
    private double averageRating;
    private String fileName;
    private String fileKey;
    private int ratingsCount;
    private int commentsCount;
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
        dto.confidential = p.isConfidential();
        dto.fileUrl = "/api/posts/" + p.getId() + "/file";
        dto.author = AuthorDTO.from(p.getAuthor());
        dto.averageRating = p.getAverageRating();
        dto.ratingsCount = p.getRatingsCount();
        dto.fileName = p.getFileName();
        dto.fileKey = p.getFileKey();
        dto.commentsCount = p.getCommentsCount();
        dto.tags = new LinkedList<>();
        dto.createdAt = p.getCreatedAt();
        dto.updatedAt = p.getUpdatedAt();
        
        // Include comments if they are loaded
        if (p.getComments() != null && !p.getComments().isEmpty()) {
            dto.comments = CommentDTO.from(p.getComments());
        }

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

    public boolean getConfidential() {
        return confidential;
    }

    public void setConfidential(boolean confidential) {
        this.confidential = confidential;
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

    public int getCommentsCount() {
        return commentsCount;
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

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileKey() {
        return fileKey;
    }

    public void setFileKey(String fileKey) {
        this.fileKey = fileKey;
    }
}

