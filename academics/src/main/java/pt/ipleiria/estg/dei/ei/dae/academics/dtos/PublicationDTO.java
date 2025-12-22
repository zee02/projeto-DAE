package pt.ipleiria.estg.dei.ei.dae.academics.dtos;

import pt.ipleiria.estg.dei.ei.dae.academics.entities.Publication;
import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

public class PublicationDTO implements Serializable {
    private Long id;
    private String title;
    private String scientificArea;
    private String summary;
    private boolean isVisible;
    private String authorName; // Simplificado para DTO
    private double averageRating;
    private int ratingsCount;
    // Falta incluir TagsDTO, mas por agora fica simples

    public static PublicationDTO from(Publication p) {
        PublicationDTO dto = new PublicationDTO();
        dto.setId(p.getId());
        dto.setTitle(p.getTitle());
        dto.setScientificArea(p.getScientificArea());
        dto.setSummary(p.getSummary());
        dto.setVisible(p.isVisible());
        dto.setAuthorName(p.getAuthor().getName());
        dto.setAverageRating(p.getAverageRating());
        dto.setRatingsCount(p.getRatingsCount());
        return dto;
    }

    public static List<PublicationDTO> from(List<Publication> posts) {
        return posts.stream().map(PublicationDTO::from).collect(Collectors.toList());
    }

    // Getters e Setters...
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getScientificArea() { return scientificArea; }
    public void setScientificArea(String scientificArea) { this.scientificArea = scientificArea; }
    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }
    public boolean isVisible() { return isVisible; }
    public void setVisible(boolean visible) { isVisible = visible; }
    public String getAuthorName() { return authorName; }
    public void setAuthorName(String authorName) { this.authorName = authorName; }
    public double getAverageRating() { return averageRating; }
    public void setAverageRating(double averageRating) { this.averageRating = averageRating; }
    public int getRatingsCount() { return ratingsCount; }
    public void setRatingsCount(int ratingsCount) { this.ratingsCount = ratingsCount; }
}