package pt.ipleiria.estg.dei.ei.dae.academics.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "publications")
@NamedQueries({
        @NamedQuery(
                name = "getAllPublicPosts",
                query = " SELECT DISTINCT p FROM Publication p LEFT JOIN FETCH p.tags WHERE p.isVisible = true ORDER BY p.submissionDate DESC"),
        @NamedQuery(
                name = "getMyPostIds",
                query = """
                            SELECT DISTINCT p.id, p.submissionDate
                            FROM Publication p
                            LEFT JOIN p.tags t
                            WHERE p.author.email = :email
                              AND (:isVisible IS NULL OR p.isVisible = :isVisible)
                              AND (:tagId IS NULL OR t.id = :tagId)
                            ORDER BY p.submissionDate DESC
                        """
        ),
        @NamedQuery(
                name = "getMyPostsWithTags",
                query = """
                            SELECT DISTINCT p
                            FROM Publication p
                            LEFT JOIN FETCH p.tags
                            WHERE p.id IN :ids
                            ORDER BY p.createdAt DESC
                        """
        ),
        @NamedQuery(
                name = "getHiddenPublications",
                query = "SELECT DISTINCT p FROM Publication p LEFT JOIN FETCH p.tags WHERE p.isVisible = false ORDER BY p.updatedAt DESC"
        ),
        @NamedQuery(
                name = "countMyPosts",
                query = """
                          SELECT COUNT(DISTINCT p.id)
                          FROM Publication p
                          JOIN p.author a
                          LEFT JOIN p.tags t
                          WHERE a.email = :email
                            AND (:isVisible IS NULL OR p.isVisible = :isVisible)
                            AND (:tagId IS NULL OR t.id = :tagId)
                        """
        )
})
public class Publication implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @NotBlank
    private String title;

    @NotBlank
    private String scientificArea;

    @Lob // Para textos longos
    private String summary;

    @Column(name = "file_name")
    private String fileName; // Caminho físico no disco
    @Column(name = "file_key")
    private String fileKey;
    private boolean isVisible; // Para controlo de visibilidade (EP20)
    private boolean isConfidential = false; // Publicações confidenciais - apenas utilizadores autenticados

    @Temporal(TemporalType.TIMESTAMP)
    private Date submissionDate;


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


    private Timestamp createdAt;
    private Timestamp updatedAt;

    public Publication() {
        this.tags = new ArrayList<>();
        this.comments = new ArrayList<>();
        this.ratings = new ArrayList<>();
        this.submissionDate = new Date();
        this.createdAt = new Timestamp(new Date().getTime());
        this.updatedAt = null;
    }

    public Publication(String title, String scientificArea, boolean isVisible, String summary, String fileName, String fileKey, User author) {
        this();
        this.title = title;
        this.scientificArea = scientificArea;
        this.summary = summary;
        this.fileName = fileName;
        this.fileKey = fileKey;
        this.author = author;
        this.isVisible = isVisible;
        this.tags = new ArrayList<>();
        this.comments = new ArrayList<>();
        this.ratings = new ArrayList<>();
        this.submissionDate = new Date();

        this.createdAt = new Timestamp(new Date().getTime());
        this.updatedAt = null;

    }


    public long getId() {
        return id;
    }

    public void setId(long id) {
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


    public boolean isVisible() {
        return isVisible;
    }

    public void setVisible(boolean visible) {
        isVisible = visible;
    }

    public boolean isConfidential() {
        return isConfidential;
    }

    public void setConfidential(boolean confidential) {
        isConfidential = confidential;
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

    public void addRating(Rating rating) {
        this.ratings.add(rating);
    }


    public void recalculateRatings() {
        if (ratings == null || ratings.isEmpty()) {
            this.averageRating = 0;
            this.ratingsCount = 0;
            return;
        }

        this.ratingsCount = ratings.size();

        double sum = ratings.stream().mapToDouble(Rating::getScore).sum();

        this.averageRating = sum / this.ratingsCount;
    }


    public void recalculateComments() {
        if (comments == null || comments.isEmpty()) {
            this.commentsCount = 0;
            return;
        }

        this.commentsCount = comments.size();
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }

    // Métodos Auxiliares
    public void addTag(Tag tag) {
        if (!tags.contains(tag)) {
            tags.add(tag);
        }
    }

    public void removeTag(Tag tag) {
        this.tags.remove(tag);
    }


}