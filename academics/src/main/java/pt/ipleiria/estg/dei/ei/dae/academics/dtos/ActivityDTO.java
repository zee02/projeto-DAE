package pt.ipleiria.estg.dei.ei.dae.academics.dtos;

import pt.ipleiria.estg.dei.ei.dae.academics.entities.Comment;
import pt.ipleiria.estg.dei.ei.dae.academics.entities.Publication;
import pt.ipleiria.estg.dei.ei.dae.academics.entities.PublicationEdit;
import pt.ipleiria.estg.dei.ei.dae.academics.entities.Rating;

import java.util.Date;

public class ActivityDTO {
    private long id;
    private String type;
    private String title;
    private Date date;

    public ActivityDTO() {
    }

    public ActivityDTO(long id, String type, String title, Date date) {
        this.id = id;
        this.type = type;
        this.title = title;
        this.date = date;
    }

    public static ActivityDTO fromPublication(Publication p) {
        return new ActivityDTO(
                p.getId(),
                "upload",
                p.getTitle(),
                p.getSubmissionDate()
        );
    }

    public static ActivityDTO fromEdit(PublicationEdit e) {
        return new ActivityDTO(
                e.getId(),
                "edit",
                "Edição de " + e.getPublication().getTitle(),
                new Date(e.getEditedAt().getTime())
        );
    }

    public static ActivityDTO fromComment(Comment c) {
        return new ActivityDTO(
                c.getId(),
                "comment",
                "Comentário em " + c.getPublication().getTitle(),
                c.getCreatedAt()
        );
    }

    public static ActivityDTO fromRating(Rating r) {
        return new ActivityDTO(
                r.getId(),
                "rating",
                "Avaliação de " + r.getPublication().getTitle(),
                r.getCreatedAt()
        );
    }

    // Getters and Setters
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}
