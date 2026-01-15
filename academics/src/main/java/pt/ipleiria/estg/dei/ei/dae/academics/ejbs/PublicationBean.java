package pt.ipleiria.estg.dei.ei.dae.academics.ejbs;

import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.hibernate.Hibernate;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;
import pt.ipleiria.estg.dei.ei.dae.academics.dtos.PublicationDTO;
import pt.ipleiria.estg.dei.ei.dae.academics.dtos.TagDTO;
import pt.ipleiria.estg.dei.ei.dae.academics.entities.Publication;
import pt.ipleiria.estg.dei.ei.dae.academics.entities.Rating;
import pt.ipleiria.estg.dei.ei.dae.academics.entities.User;
import pt.ipleiria.estg.dei.ei.dae.academics.exceptions.MyEntityNotFoundException;
import pt.ipleiria.estg.dei.ei.dae.academics.utils.PublicationUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Stateless
public class PublicationBean {
    @PersistenceContext
    private EntityManager em;

    @EJB
    private UserBean userBean;

    private static final String UPLOAD_DIR = "/tmp/uploads";

    public Publication create(String title, String scientificArea, String authorEmail) {


        User author = userBean.findOrFail(authorEmail);

        Publication publication = new Publication(title, scientificArea, true, "Resumo AI", "file", author);

        em.persist(publication);

        return publication;
    }


    public Publication create(MultipartFormDataInput input, String user_id) throws IOException {

        Map<String, List<InputPart>> formParts = input.getFormDataMap();


        if (!formParts.containsKey("title") || !formParts.containsKey("scientific_area") || !formParts.containsKey("file")) {
            throw new WebApplicationException("Missing required fields", Response.Status.BAD_REQUEST);
        }

        String title = formParts.get("title").get(0).getBodyAsString();
        String area = formParts.get("scientific_area").get(0).getBodyAsString();

        String summary = null;
        if (formParts.containsKey("summary")) {
            summary = formParts.get("summary").get(0).getBodyAsString();
        }

        InputPart filePart = formParts.get("file").get(0);
        String fileName = PublicationUtils.getFileName(filePart.getHeaders());
        InputStream fileData = filePart.getBody(InputStream.class, null);

        User author = userBean.find(user_id);

        // Guardar ficheiro
        String uniqueFileName = UUID.randomUUID() + "_" + fileName;
        Path path = Paths.get(UPLOAD_DIR, uniqueFileName);
        Files.createDirectories(path.getParent());
        Files.copy(fileData, path, StandardCopyOption.REPLACE_EXISTING);

        // Summary automático
        if (summary == null || summary.isBlank()) {
            summary = "Resumo pendente de geração automática.";
        }

        Publication publication = new Publication(title, area, false, summary, path.toString(), author);

        em.persist(publication);
        return publication;
    }


    public List<Publication> getAllPublic() {
        return em.createNamedQuery("getAllPublicPosts", Publication.class).getResultList();
    }

    public Publication findWithTags(long id) {
        Publication p = em.find(Publication.class, id);
        Hibernate.initialize(p.getTags());
        return p;
    }


    public Publication findWithComments(long id) {
        Publication p = em.find(Publication.class, id);

        Hibernate.initialize(p.getComments());


        return p;
    }


    public List<PublicationDTO> findMyPublications(String userId, int page, int limit, Boolean isVisible, Long tagId) {
        User user = userBean.find(userId);

        List<Long> ids = em.createNamedQuery("getMyPostIds", Object[].class)
                .setParameter("email", user.getEmail())
                .setParameter("isVisible", isVisible)
                .setParameter("tagId", tagId)
                .setFirstResult((page - 1) * limit)
                .setMaxResults(limit)
                .getResultList()
                .stream()
                .map(r -> (Long) r[0])
                .toList();

        if (ids.isEmpty()) {
            return List.of();
        }

        return em.createNamedQuery("getMyPostsWithTags", Publication.class)
                .setParameter("ids", ids)
                .getResultList()
                .stream()
                .map(p -> {
                    PublicationDTO dto = PublicationDTO.from(p);
                    dto.setTags(
                            p.getTags().stream()
                                    .map(TagDTO::from)
                                    .toList()
                    );
                    return dto;
                })
                .toList();
    }

    // EP02 - Corrigir resumo gerado por IA
    public Publication updateSummary(long postId, String userId, String newSummary) throws MyEntityNotFoundException {
        Publication publication = em.find(Publication.class, postId);

        if (publication == null) {
            throw new MyEntityNotFoundException("Publicação com id " + postId + " não encontrada");
        }

        User user = userBean.find(userId);

        // Verificar se o utilizador é o autor da publicação
        if (publication.getAuthor().getId() != user.getId()) {
            throw new ForbiddenException("Utilizador não é o autor da publicação");
        }

        publication.setSummary(newSummary);
        publication.setUpdatedAt(new Timestamp(new Date().getTime()));

        return publication;
    }

    // EP10 - Ordenar lista de publicações
    public List<Publication> getAllPublicSorted(String sortBy, String order) {
        // Mapear campo do DTO para campo da entidade
        String fieldName = switch (sortBy) {
            case "average_rating" -> "averageRating";
            case "comments_count" -> "commentsCount";
            case "ratings_count" -> "ratingsCount";
            default -> "averageRating";
        };

        String orderDirection = order.equalsIgnoreCase("asc") ? "ASC" : "DESC";

        String jpql = "SELECT DISTINCT p FROM Publication p LEFT JOIN FETCH p.tags WHERE p.isVisible = true ORDER BY p." + fieldName + " " + orderDirection;

        return em.createQuery(jpql, Publication.class).getResultList();
    }

    // EP20 - Ocultar ou mostrar publicação
    public Publication updateVisibility(long postId, boolean visible) throws MyEntityNotFoundException {
        Publication publication = em.find(Publication.class, postId);

        if (publication == null) {
            throw new MyEntityNotFoundException("Publicação com id " + postId + " não encontrada");
        }

        publication.setVisible(visible);
        publication.setUpdatedAt(new Timestamp(new Date().getTime()));

        return publication;
    }
}