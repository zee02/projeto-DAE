package pt.ipleiria.estg.dei.ei.dae.academics.entities;

import jakarta.persistence.*;
import java.io.Serializable;
import java.sql.Timestamp;

@Entity
@Table(name = "publication_edits")
@NamedQueries({
        @NamedQuery(
                name = "getPublicationHistory",
                query = """
                        SELECT e
                        FROM PublicationEdit e
                        WHERE e.publication.id = :postId
                        ORDER BY e.editedAt DESC
                        """
        ),
        @NamedQuery(
                name = "countPublicationHistory",
                query = """
                        SELECT COUNT(e)
                        FROM PublicationEdit e
                        WHERE e.publication.id = :postId
                        """
        )
})
public class PublicationEdit implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @ManyToOne(optional = false)
    private Publication publication;

    @ManyToOne(optional = false)
    private User editedBy;

    @Column(nullable = false)
    private Timestamp editedAt;

    // JSON leve com as alterações (ex.: {"summary":"..."} ou {"tags":[{id,name}]})
    @Lob
    @Column(nullable = false)
    private String changesJson;

    public PublicationEdit() {}

    public PublicationEdit(Publication publication, User editedBy, Timestamp editedAt, String changesJson) {
        this.publication = publication;
        this.editedBy = editedBy;
        this.editedAt = editedAt;
        this.changesJson = changesJson;
    }

    public long getId() { return id; }
    public Publication getPublication() { return publication; }
    public User getEditedBy() { return editedBy; }
    public Timestamp getEditedAt() { return editedAt; }
    public String getChangesJson() { return changesJson; }
}
