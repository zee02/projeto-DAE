package pt.ipleiria.estg.dei.ei.dae.academics.ejbs;

import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.persistence.*;
import jakarta.mail.MessagingException;
import pt.ipleiria.estg.dei.ei.dae.academics.dtos.CommentDTO;
import pt.ipleiria.estg.dei.ei.dae.academics.dtos.PublicationDTO;
import pt.ipleiria.estg.dei.ei.dae.academics.entities.Comment;
import pt.ipleiria.estg.dei.ei.dae.academics.entities.Publication;
import pt.ipleiria.estg.dei.ei.dae.academics.entities.Rating;
import pt.ipleiria.estg.dei.ei.dae.academics.entities.User;
import pt.ipleiria.estg.dei.ei.dae.academics.entities.Tag;
import pt.ipleiria.estg.dei.ei.dae.academics.exceptions.MyEntityNotFoundException;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.HashSet;
import java.util.Set;


@Stateless
public class CommentBean {
    @PersistenceContext
    private EntityManager em;

    @EJB
    private PublicationBean publicationBean;

    @EJB
    private UserBean userBean;

    @EJB
    private EmailBean emailBean;

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

        // Registrar no histórico
        try {
            String truncatedComment = comment.length() > 150 
                ? comment.substring(0, 150) + "..." 
                : comment;
            publicationBean.seedHistory(
                publication.getId(),
                user.getEmail(),
                java.util.Map.of("comment", "Comentário de " + user.getName() + ": " + truncatedComment)
            );
        } catch (Exception e) {
            // Não falhar se histórico não for gravado
        }

        // Notificar subscritores das tags da publicação
        notifyTagSubscribers(publication, user, comment);

        return newComent;

    }

    private void notifyTagSubscribers(Publication publication, User commentAuthor, String commentContent) {
        // Coletar subscritores únicos de todas as tags da publicação
        Set<User> subscribers = new HashSet<>();
        
        for (Tag tag : publication.getTags()) {
            subscribers.addAll(tag.getSubscribers());
        }
        
        // Remover o autor do comentário da lista (não notificar a si mesmo)
        subscribers.remove(commentAuthor);
        
        // Enviar email para cada subscritor
        for (User subscriber : subscribers) {
            try {
                emailBean.send(
                    subscriber.getEmail(),
                    "Novo comentário na publicação " + publication.getTitle(),
                    "Foi feito um novo comentário na publicação '" + publication.getTitle() + "' por " + commentAuthor.getName() + ".\n\n" +
                    "Comentário: " + commentContent + "\n\n" +
                    "Esta é uma notificação automática porque está subscrito a uma das tags desta publicação."
                );
            } catch (MessagingException e) {
                // Log do erro mas não falhar a criação do comentário
                System.err.println("Erro ao enviar email para " + subscriber.getEmail() + ": " + e.getMessage());
            }
        }
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

        // Registrar no histórico
        try {
            publicationBean.seedHistory(
                postId,
                "system", // Usar email de sistema ou do usuário logado se disponível
                java.util.Map.of("commentVisibility", 
                    visible ? "Todos os comentários tornados visíveis" : "Todos os comentários ocultados")
            );
        } catch (Exception e) {
            // Não falhar se histórico não for gravado
        }

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

        // Registrar no histórico
        try {
            publicationBean.seedHistory(
                postId,
                comment.getAuthor().getEmail(),
                java.util.Map.of("commentVisibility", 
                    visible ? "Comentário de " + comment.getAuthor().getName() + " tornado visível" 
                            : "Comentário de " + comment.getAuthor().getName() + " ocultado")
            );
        } catch (Exception e) {
            // Não falhar se histórico não for gravado
        }

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
