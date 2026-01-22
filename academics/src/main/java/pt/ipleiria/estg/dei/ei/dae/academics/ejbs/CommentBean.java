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

import java.sql.Timestamp;
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

        em.persist(newComent);

        // Add comment to publication's list and recalculate
        publication.getComments().add(newComent);
        publication.recalculateComments();

        // Mark the publication as modified by updating a timestamp
        publication.setUpdatedAt(new Timestamp(System.currentTimeMillis()));

        // Merge to ensure the publication changes are persisted
        em.merge(publication);
        em.flush();

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

    // EP21 - Get hidden comments with pagination, search, and sorting
    public List<Comment> getHiddenComments(int page, int limit, String search, String sortBy, String order) {
        String orderDir = order != null && order.equalsIgnoreCase("asc") ? "ASC" : "DESC";
        String sortField = sortBy != null && sortBy.equals("createdAt") ? "c.createdAt" : "COALESCE(c.updatedAt, c.createdAt)";
        
        String jpql = "SELECT c FROM Comment c LEFT JOIN FETCH c.author LEFT JOIN FETCH c.publication WHERE c.visible = false";
        
        if (search != null && !search.isBlank()) {
            jpql += " AND (LOWER(c.content) LIKE LOWER(:search) OR LOWER(c.author.name) LIKE LOWER(:search))";
        }
        
        jpql += " ORDER BY " + sortField + " " + orderDir;
        
        var query = em.createQuery(jpql, Comment.class);
        
        if (search != null && !search.isBlank()) {
            query.setParameter("search", "%" + search + "%");
        }
        
        return query.setFirstResult((page - 1) * limit)
                    .setMaxResults(limit)
                    .getResultList();
    }

    public long countHiddenComments(String search) {
        String jpql = "SELECT COUNT(c) FROM Comment c WHERE c.visible = false";
        
        if (search != null && !search.isBlank()) {
            jpql += " AND (LOWER(c.content) LIKE LOWER(:search) OR LOWER(c.author.name) LIKE LOWER(:search))";
        }
        
        var query = em.createQuery(jpql, Long.class);
        
        if (search != null && !search.isBlank()) {
            query.setParameter("search", "%" + search + "%");
        }
        
        return query.getSingleResult();
    }

    public void delete(long commentId) throws MyEntityNotFoundException {
        Comment comment = find(commentId);

        if (comment == null) {
            throw new MyEntityNotFoundException("Comentário com id " + commentId + " não encontrado");
        }

        Publication publication = comment.getPublication();
        publication.getComments().remove(comment);
        em.remove(comment);
        publication.recalculateComments();

        // Mark the publication as modified by updating a timestamp
        publication.setUpdatedAt(new Timestamp(System.currentTimeMillis()));

        // Merge to ensure the publication changes are persisted
        em.merge(publication);
        em.flush();
    }
}
