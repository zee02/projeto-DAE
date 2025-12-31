package pt.ipleiria.estg.dei.ei.dae.academics.ejbs;

import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.persistence.*;
import pt.ipleiria.estg.dei.ei.dae.academics.dtos.CommentDTO;
import pt.ipleiria.estg.dei.ei.dae.academics.dtos.PublicationDTO;
import pt.ipleiria.estg.dei.ei.dae.academics.entities.Comment;
import pt.ipleiria.estg.dei.ei.dae.academics.entities.Publication;
import pt.ipleiria.estg.dei.ei.dae.academics.entities.Rating;
import pt.ipleiria.estg.dei.ei.dae.academics.entities.User;
import pt.ipleiria.estg.dei.ei.dae.academics.exceptions.MyEntityNotFoundException;

import java.time.Instant;
import java.util.List;


@Stateless
public class CommentBean {
    @PersistenceContext
    private EntityManager em;

    @EJB
    private PublicationBean publicationBean;

    @EJB
    private UserBean userBean;

    public Comment findByPublication(Long publicationId) {

        List<Comment> results = em.createNamedQuery(
                        "findCommentsByPublication",
                        Comment.class
                )
                .setParameter("publicationId", publicationId)
                .getResultList();

        return results.isEmpty() ? null : results.get(0);
    }


    public Comment create(Publication publication, String email, CommentDTO comment) throws MyEntityNotFoundException {
        User user = userBean.find(email);

        Comment newComent = new Comment();
        newComent.setAuthor(user);
        newComent.setContent(comment.getComment());
        newComent.setPublication(publication);
        newComent.setCreatedAt(Instant.now());

        publication.getComments().add(newComent);
        em.merge(newComent);
        return newComent;

    }
}