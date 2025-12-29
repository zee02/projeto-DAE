package pt.ipleiria.estg.dei.ei.dae.academics.dtos;

import jakarta.json.bind.annotation.JsonbProperty;
import pt.ipleiria.estg.dei.ei.dae.academics.entities.Publication;
import pt.ipleiria.estg.dei.ei.dae.academics.entities.Tag;

import java.io.Serializable;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class PublicationDTO implements Serializable {

    private Long id;
    private String title;

    @JsonbProperty("scientific_area")
    private String scientificArea;

    private String summary;

    @JsonbProperty("is_visible")
    private boolean visible;

    @JsonbProperty("file_url")
    private String fileUrl;

    private AuthorDTO author;

    @JsonbProperty("average_rating")
    private double averageRating;

    @JsonbProperty("ratings_count")
    private int ratingsCount;

    private List<String> tags;

    @JsonbProperty("created_at")
    private String createdAt;


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

        dto.tags = p.getTags() == null
                ? List.of()
                : p.getTags().stream()
                .map(Tag::getName)   // ⚠️ ajusta ao getter real
                .collect(Collectors.toList());

        dto.createdAt = p.getSubmissionDate()
                .toInstant()
                .atOffset(ZoneOffset.UTC)
                .format(DateTimeFormatter.ISO_INSTANT);

        return dto;
    }

    /* getters JavaBeans */

    public Long getId() {
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

    public List<String> getTags() {
        return tags;
    }

    public String getCreatedAt() {
        return createdAt;
    }


}
