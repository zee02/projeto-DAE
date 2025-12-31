package pt.ipleiria.estg.dei.ei.dae.academics.entities;

import jakarta.persistence.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "publications")
@NamedQueries({
        @NamedQuery(
                name = "getAllPublicPosts",
                query = " SELECT DISTINCT p FROM Publication p LEFT JOIN FETCH p.tags WHERE p.isVisible = true ORDER BY p.submissionDate DESC"),
        @NamedQuery(name = "getMyPosts", query = "SELECT p FROM Publication p WHERE p.author.email = :email ORDER BY p.submissionDate DESC")
})
public class Publication implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String scientificArea;

    @Lob // Para textos longos
    private String summary;

    @Column(name = "file_path")
    private String filePath; // Caminho físico no disco

    private boolean isVisible; // Para controlo de visibilidade (EP20)

    @Temporal(TemporalType.TIMESTAMP)
    private Date submissionDate;

    // Métricas para ordenação (EP10)
    private double averageRating;
    private int ratingsCount;
    private int commentsCount;

    @ManyToOne
    private User author; // Quem submeteu

    @ManyToMany
    @JoinTable(
            name = "publications_tags",
            joinColumns = @JoinColumn(
                    name = "publication_id",
                    referencedColumnName = "id"
            ),
            inverseJoinColumns = @JoinColumn(
                    name = "tag_id",
                    referencedColumnName = "id"
            )
    )
    private List<Tag> tags;

    @OneToMany(mappedBy = "publication")
    private List<Comment> comments;

    @OneToMany(mappedBy = "publication")
    private List<Rating> ratings; // Nova entidade Rating necessária

    public Publication() {
        this.tags = new ArrayList<>();
        this.comments = new ArrayList<>();
        this.ratings = new ArrayList<>();
        this.submissionDate = new Date();

    }

    public Publication(String title, String scientificArea, boolean isVisible, String summary, String filePath, User author) {
        this();
        this.title = title;
        this.scientificArea = scientificArea;
        this.summary = summary;
        this.filePath = filePath;
        this.author = author;
        this.isVisible = isVisible;
    }

    // Métodos Auxiliares
    public void addTag(Tag tag) {
        if (!tags.contains(tag)) {
            tags.add(tag);
        }
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

    public String getScientificArea() {
        return scientificArea;
    }

    public void setScientificArea(String scientificArea) {
        this.scientificArea = scientificArea;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public boolean isVisible() {
        return isVisible;
    }

    public void setVisible(boolean visible) {
        isVisible = visible;
    }

    public Date getSubmissionDate() {
        return submissionDate;
    }

    public void setSubmissionDate(Date submissionDate) {
        this.submissionDate = submissionDate;
    }

    public double getAverageRating() {
        return averageRating;
    }

    public void setAverageRating(double averageRating) {
        this.averageRating = averageRating;
    }

    public int getRatingsCount() {
        return ratingsCount;
    }

    public void setRatingsCount(int ratingsCount) {
        this.ratingsCount = ratingsCount;
    }

    public int getCommentsCount() {
        return commentsCount;
    }

    public void setCommentsCount(int commentsCount) {
        this.commentsCount = commentsCount;
    }

    public User getAuthor() {
        return author;
    }

    public void setAuthor(User author) {
        this.author = author;
    }

    public List<Tag> getTags() {
        return tags;
    }

    public void setTags(List<Tag> tags) {
        this.tags = tags;
    }

    public List<Comment> getComments() {
        return comments;
    }

    public void setComments(List<Comment> comments) {
        this.comments = comments;
    }

    public List<Rating> getRatings() {
        return ratings;
    }

    public void setRatings(List<Rating> ratings) {
        this.ratings = ratings;
    }
}