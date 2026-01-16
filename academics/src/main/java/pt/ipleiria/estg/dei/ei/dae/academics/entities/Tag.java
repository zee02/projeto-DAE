package pt.ipleiria.estg.dei.ei.dae.academics.entities;

import jakarta.persistence.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@NamedQueries({
        @NamedQuery(
                name = "findAllTags",
                query = "SELECT t FROM Tag t "
        ),
        @NamedQuery(
                name = "getHiddenTags",
                query = "SELECT t FROM Tag t WHERE t.visible = false ORDER BY t.name"
        )
})
@Table(name = "tags")
public class Tag implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Column(unique = true, nullable = false)
    private String name;

    private boolean visible = true;

    @ManyToMany(mappedBy = "tags") // O dono da relação é a Publication
    private List<Publication> publications;

    @ManyToMany(mappedBy = "subscribedTags")
    private List<User> subscribers = new ArrayList<>();

    public Tag() {
        this.publications = new ArrayList<>();
        this.subscribers = new ArrayList<>();
    }

    public Tag(long id, String name) {
        this.id = id;
        this.name = name;
        this.publications = new ArrayList<>();
        this.subscribers = new ArrayList<>();
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Publication> getPublications() {
        return publications;
    }

    public void setPublications(List<Publication> publications) {
        this.publications = publications;
    }

    public void addPublication(Publication publication) {
        this.publications.add(publication);
    }

    public List<User> getSubscribers() {
        return subscribers;
    }

    public void setSubscribers(List<User> subscribers) {
        this.subscribers = subscribers;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    //equals and hash code gerados nos intelij
    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Tag tag = (Tag) o;
        return id == tag.id && Objects.equals(name, tag.name) && Objects.equals(publications, tag.publications);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, publications);
    }
}