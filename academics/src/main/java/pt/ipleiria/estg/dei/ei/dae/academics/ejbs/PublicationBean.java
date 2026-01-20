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
import pt.ipleiria.estg.dei.ei.dae.academics.dtos.PaginatedPublicationsDTO;
import pt.ipleiria.estg.dei.ei.dae.academics.dtos.PublicationDTO;
import pt.ipleiria.estg.dei.ei.dae.academics.dtos.TagDTO;
import pt.ipleiria.estg.dei.ei.dae.academics.entities.Publication;
import pt.ipleiria.estg.dei.ei.dae.academics.entities.Rating;
import pt.ipleiria.estg.dei.ei.dae.academics.entities.User;
import pt.ipleiria.estg.dei.ei.dae.academics.exceptions.MyEntityNotFoundException;
import pt.ipleiria.estg.dei.ei.dae.academics.utils.PublicationUtils;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import pt.ipleiria.estg.dei.ei.dae.academics.entities.PublicationEdit;


import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
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

    private static final String UPLOAD_DIR = "/app/uploads";

    private final Jsonb jsonb = JsonbBuilder.create();

    public Publication create(String title, String scientificArea, String authorEmail) {


        User author = userBean.findOrFail(authorEmail);

        Publication publication = new Publication(title, scientificArea, true, "Resumo AI", "", "", author);

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

        Publication publication = new Publication(title, area, false, summary, fileName, uniqueFileName, author);

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


    public PaginatedPublicationsDTO<PublicationDTO> findMyPublications(String userId, int page, int limit, Boolean isVisible, Long tagId) {
        User user = userBean.find(userId);

        // 🔹 TOTAL
        long total = em.createNamedQuery("countMyPosts", Long.class)
                .setParameter("email", user.getEmail())
                .setParameter("isVisible", isVisible)
                .setParameter("tagId", tagId)
                .getSingleResult();

        // 🔹 IDS PAGINADOS
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
            return new PaginatedPublicationsDTO<>(List.of(), total);
        }

        List<PublicationDTO> data = em.createNamedQuery("getMyPostsWithTags", Publication.class)
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

        return new PaginatedPublicationsDTO<>(data, total);
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


        // histórico (leve)
        recordEdit(publication, user, Map.of(
                "summary", "Resumo corrigido pelo autor."
        ));

        return publication;
    }

    // EP10 - Ordenar lista de publicações
    public List<Publication> getAllPublicSorted(String sortBy, String order) {

        String fieldName = switch (sortBy) {
            case "average_rating" -> "averageRating";
            case "comments_count" -> "commentsCount";
            case "ratings_count" -> "ratingsCount";
            default -> "averageRating";
        };

        String orderDirection = order.equalsIgnoreCase("asc") ? "ASC" : "DESC";

        // 1️⃣ Buscar publicações + tags
        String jpqlPublications = """
                    SELECT DISTINCT p
                    FROM Publication p
                    LEFT JOIN FETCH p.tags
                    WHERE p.isVisible = true
                    ORDER BY p.%s %s
                """.formatted(fieldName, orderDirection);

        List<Publication> publications =
                em.createQuery(jpqlPublications, Publication.class)
                        .getResultList();

        // 2️⃣ Buscar comentários (sem fetch múltiplo)
        if (!publications.isEmpty()) {
            em.createQuery("""
                                SELECT DISTINCT p
                                FROM Publication p
                                LEFT JOIN FETCH p.comments
                                WHERE p IN :publications
                            """, Publication.class)
                    .setParameter("publications", publications)
                    .getResultList();
        }

        return publications;
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

    // EP09 - Pesquisar publicações
    public List<Publication> searchPublications(String title, Long authorId, String scientificArea,
                                                List<Long> tagIds, String dateFrom, String dateTo) {
        StringBuilder jpql = new StringBuilder(
                "SELECT DISTINCT p FROM Publication p LEFT JOIN p.tags t WHERE p.isVisible = true");

        List<String> conditions = new ArrayList<>();

        if (title != null && !title.isBlank()) {
            conditions.add("LOWER(p.title) LIKE LOWER(:title)");
        }
        if (authorId != null) {
            conditions.add("p.author.id = :authorId");
        }
        if (scientificArea != null && !scientificArea.isBlank()) {
            conditions.add("LOWER(p.scientificArea) LIKE LOWER(:scientificArea)");
        }
        if (tagIds != null && !tagIds.isEmpty()) {
            conditions.add("t.id IN :tagIds");
        }
        if (dateFrom != null && !dateFrom.isBlank()) {
            conditions.add("p.createdAt >= :dateFrom");
        }
        if (dateTo != null && !dateTo.isBlank()) {
            conditions.add("p.createdAt <= :dateTo");
        }

        for (String condition : conditions) {
            jpql.append(" AND ").append(condition);
        }

        jpql.append(" ORDER BY p.createdAt DESC");

        var query = em.createQuery(jpql.toString(), Publication.class);

        if (title != null && !title.isBlank()) {
            query.setParameter("title", "%" + title + "%");
        }
        if (authorId != null) {
            query.setParameter("authorId", authorId);
        }
        if (scientificArea != null && !scientificArea.isBlank()) {
            query.setParameter("scientificArea", "%" + scientificArea + "%");
        }
        if (tagIds != null && !tagIds.isEmpty()) {
            query.setParameter("tagIds", tagIds);
        }
        if (dateFrom != null && !dateFrom.isBlank()) {
            LocalDate from = LocalDate.parse(dateFrom);
            Timestamp fromTs = Timestamp.valueOf(from.atStartOfDay());
            query.setParameter("dateFrom", fromTs);
        }
        if (dateTo != null && !dateTo.isBlank()) {
            LocalDate to = LocalDate.parse(dateTo);
            Timestamp toTs = Timestamp.valueOf(to.plusDays(1).atStartOfDay());
            query.setParameter("dateTo", toTs);
        }

        List<Publication> results = query.getResultList();

        // Inicializar tags manualmente para evitar LazyInitializationException
        for (Publication p : results) {
            p.getTags().size(); // Força o carregamento da coleção
        }

        return results;
    }


    private void recordEdit(Publication publication, User editor, Map<String, Object> changes) {
        String changesJson = jsonb.toJson(changes);

        PublicationEdit edit = new PublicationEdit(
                publication,
                editor,
                new Timestamp(new Date().getTime()),
                changesJson
        );

        em.persist(edit);
    }

    public void seedHistory(long postId, String editorEmail, Map<String, Object> changes) throws MyEntityNotFoundException {
        Publication publication = em.find(Publication.class, postId);
        if (publication == null) {
            throw new MyEntityNotFoundException("Publicação com id " + postId + " não encontrada");
        }
        User editor = userBean.findOrFail(editorEmail);
        recordEdit(publication, editor, changes);
    }


    public List<PublicationEdit> getPostHistory(long postId, String userId, int page, int limit)
            throws MyEntityNotFoundException {

        Publication publication = em.find(Publication.class, postId);
        if (publication == null) {
            throw new MyEntityNotFoundException("Publicação com id " + postId + " não encontrada");
        }

        User requester = userBean.find(userId);

        // regra do enunciado: só o autor pode ver o histórico (inclui posts não visíveis)
        if (publication.getAuthor().getId() != requester.getId()) {
            throw new ForbiddenException("Utilizador não é o autor da publicação");
        }

        int safePage = Math.max(page, 1);
        int safeLimit = Math.max(1, Math.min(limit, 100));

        return em.createNamedQuery("getPublicationHistory", PublicationEdit.class)
                .setParameter("postId", postId)
                .setFirstResult((safePage - 1) * safeLimit)
                .setMaxResults(safeLimit)
                .getResultList();
    }

    // EP21 - Get hidden publications with pagination
    public List<Publication> getHiddenPublications(int page, int limit) {
        return em.createNamedQuery("getHiddenPublications", Publication.class)
                .setFirstResult((page - 1) * limit)
                .setMaxResults(limit)
                .getResultList();
    }

    public long countHiddenPublications() {
        return em.createQuery("SELECT COUNT(p) FROM Publication p WHERE p.isVisible = false", Long.class)
                .getSingleResult();
    }


}