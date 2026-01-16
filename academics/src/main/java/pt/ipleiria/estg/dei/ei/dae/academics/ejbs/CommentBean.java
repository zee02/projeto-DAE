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
import java.util.Date;
import java.util.List;


@Stateless
public class CommentBean {
    @PersistenceContext
    private EntityManager em;

    @EJB
    private PublicationBean publicationBean;

    @EJB
    private UserBean userBean;

    public Comment findByPublication(long publicationId) {

        List<Comment> results = em.createNamedQuery(
                        "findCommentsByPublication",
                        Comment.class
                )
                .setParameter("publicationId", publicationId)
                .getResultList();

        return results.isEmpty() ? null : results.get(0);
    }


    public Comment create(Publication publication, String user_id, String comment)  {


        User user = userBean.find(user_id);

        Comment newComent = new Comment();
        newComent.setAuthor(user);
        newComent.setContent(comment);
        newComent.setPublication(publication);
        newComent.setCreatedAt(new Date());

        publication.getComments().add(newComent);
        em.persist(newComent);

        return newComent;

    }

    //EP19 - Ocultar ou mostrar comentários de uma publicação
    public Date updateAllVisibilityByPublication(long postId, boolean visible) throws MyEntityNotFoundException {
        Publication publication = publicationBean.findWithComments(postId);

        if (publication == null) {
            throw new MyEntityNotFoundException("Publicação com id " + postId + " não encontrada");
        }

        Date updatedAt = new Date();

        em.createQuery("UPDATE Comment c SET c.visible = :visible, c.updatedAt = :updatedAt WHERE c.publication.id = :postId")
                .setParameter("visible", visible)
                .setParameter("updatedAt", updatedAt)
                .setParameter("postId", postId)
                .executeUpdate();

        return updatedAt;
    }

    public Comment find(long commentId) {
        return em.find(Comment.class, commentId);
    }

    //EP19 - Ocultar ou mostrar um comentário específico
    public Comment updateVisibility(long postId, long commentId, boolean visible) throws MyEntityNotFoundException {
        Comment comment = find(commentId);

        if (comment == null) {
            throw new MyEntityNotFoundException("Comentário com id " + commentId + " não encontrado");
        }

        if (comment.getPublication().getId() != postId) {
            throw new MyEntityNotFoundException("Comentário com id " + commentId + " não pertence à publicação com id " + postId);
        }

        comment.setVisible(visible);
        comment.setUpdatedAt(new Date());

        return comment;
    }

    // EP21 - Get hidden comments with pagination
    public List<Comment> getHiddenComments(int page, int limit) {
        return em.createNamedQuery("getHiddenComments", Comment.class)
                .setFirstResult((page - 1) * limit)
                .setMaxResults(limit)
                .getResultList();
    }

    public long countHiddenComments() {
        return em.createQuery("SELECT COUNT(c) FROM Comment c WHERE c.visible = false", Long.class)
                .getSingleResult();
    }
}