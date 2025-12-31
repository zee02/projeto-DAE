package pt.ipleiria.estg.dei.ei.dae.academics.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;

@Entity
@NamedQueries({
        @NamedQuery(
                name = "findRatingByPublicationAndUser",
                query = "SELECT r FROM Rating r " +
                        "WHERE r.publication.id = :postId AND r.user = :user"
        )
})
@Table(name = "ratings")
public class Rating implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private Integer score;

    @ManyToOne
    private Publication publication;

    @ManyToOne
    private User user;

    public Rating() {
    }

    public Rating(int score, Publication publication, User user) {
        this.score = score;
        this.publication = publication;
        this.user = user;
    }




    // Getters/Setters básicos
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public Publication getPublication() {
        return publication;
    }

    public void setPublication(Publication publication) {
        this.publication = publication;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}