package pt.ipleiria.estg.dei.ei.dae.academics.ejbs;

import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.hibernate.Hibernate;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;
import pt.ipleiria.estg.dei.ei.dae.academics.dtos.PaginatedPublicationsDTO;
import pt.ipleiria.estg.dei.ei.dae.academics.dtos.PublicationDTO;
import pt.ipleiria.estg.dei.ei.dae.academics.dtos.SearchPublicationDTO;
import pt.ipleiria.estg.dei.ei.dae.academics.dtos.TagDTO;
import pt.ipleiria.estg.dei.ei.dae.academics.entities.Publication;
import pt.ipleiria.estg.dei.ei.dae.academics.entities.Rating;
import pt.ipleiria.estg.dei.ei.dae.academics.entities.User;
import pt.ipleiria.estg.dei.ei.dae.academics.exceptions.MyEntityNotFoundException;
import pt.ipleiria.estg.dei.ei.dae.academics.utils.PublicationUtils;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import pt.ipleiria.estg.dei.ei.dae.academics.entities.PublicationEdit;


import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.sql.Timestamp;
import java.text.Normalizer;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Predicate;

@Stateless
public class PublicationBean {
    @PersistenceContext
    private EntityManager em;

    @EJB
    private UserBean userBean;

    @EJB
    private EmailBean emailBean;

    private static final String UPLOAD_DIR = "/app/uploads";

    private final Jsonb jsonb = JsonbBuilder.create();

    public Publication create(String title, String scientificArea, String authorEmail) {


        User author = userBean.findOrFail(authorEmail);

        Publication publication = new Publication(title, scientificArea, true, "Resumo AI", "", "", author);

        em.persist(publication);

        return publication;
    }

    private String readUtf8(InputPart part) throws IOException {
        InputStream inputStream = part.getBody(InputStream.class, null);
        String content = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        return Normalizer.normalize(content, Normalizer.Form.NFC);
    }

    public Publication create(MultipartFormDataInput input, String user_id) throws IOException {

        Map<String, List<InputPart>> formParts = input.getFormDataMap();


        if (!formParts.containsKey("title") || !formParts.containsKey("scientific_area") || !formParts.containsKey("file")) {
            throw new WebApplicationException("Missing required fields", Response.Status.BAD_REQUEST);
        }


        String title = readUtf8(formParts.get("title").get(0));
        String area = readUtf8(formParts.get("scientific_area").get(0));

        String summary = null;
        if (formParts.containsKey("summary")) {
            summary = readUtf8(formParts.get("summary").get(0));
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

        Publication publication = new Publication(title, area, true, summary, fileName, uniqueFileName, author);

        em.persist(publication);

        // Registrar criação no histórico
        em.flush(); // Garantir que a publicação tem ID
        recordEdit(publication, author, Map.of(
                "title", publication.getTitle(),
                "scientific_area", publication.getScientificArea(),
                "summary", publication.getSummary(),
                "file", publication.getFileName(),
                "is_visible", Map.of("valueType", publication.isVisible() ? "TRUE" : "FALSE")
        ));

        return publication;
    }


    public List<Publication> getAllPublic() {
        List<Publication> publications = em.createNamedQuery("getAllPublicPosts", Publication.class).getResultList();

        // Fetch comments in a separate query to avoid cartesian product
        if (!publications.isEmpty()) {
            em.createQuery("""
                                SELECT DISTINCT p
                                FROM Publication p
                                LEFT JOIN FETCH p.comments
                                WHERE p IN :publications
                            """, Publication.class)
                    .setParameter("publications", publications)
                    .getResultList();

            // Initialize authors of comments
            for (Publication p : publications) {
                for (var comment : p.getComments()) {
                    Hibernate.initialize(comment.getAuthor());
                }
            }
        }

        return publications;
    }

    public List<Publication> getAllPublications() {
        // Fetch all publications with tags (similar to getHiddenPublications)
        List<Publication> publications = em.createQuery(
                "SELECT DISTINCT p FROM Publication p LEFT JOIN FETCH p.tags ORDER BY p.updatedAt DESC",
                Publication.class
        ).getResultList();

        System.out.println("=== getAllPublications DEBUG ===");
        System.out.println("Total publications: " + publications.size());
        long visibleCount = publications.stream().filter(Publication::isVisible).count();
        long hiddenCount = publications.stream().filter(p -> !p.isVisible()).count();
        System.out.println("Visible: " + visibleCount + ", Hidden: " + hiddenCount);
        publications.forEach(p -> System.out.println("ID: " + p.getId() + ", Title: " + p.getTitle() + ", Visible: " + p.isVisible()));

        // Fetch comments in a separate query to avoid cartesian product
        if (!publications.isEmpty()) {
            em.createQuery("""
                                SELECT DISTINCT p
                                FROM Publication p
                                LEFT JOIN FETCH p.comments
                                WHERE p IN :publications
                            """, Publication.class)
                    .setParameter("publications", publications)
                    .getResultList();

            // Initialize authors of comments
            for (Publication p : publications) {
                for (var comment : p.getComments()) {
                    Hibernate.initialize(comment.getAuthor());
                }
            }
        }

        return publications;
    }

    public Publication findWithTags(long id) {
        Publication p = em.find(Publication.class, id);

        if (p == null) {
            return null;
        }

        Hibernate.initialize(p.getTags());
        Hibernate.initialize(p.getComments());
        return p;
    }


    public Publication findWithComments(long id) {
        Publication p = em.find(Publication.class, id);

        if (p == null) {
            return null;
        }

        Hibernate.initialize(p.getComments());
        Hibernate.initialize(p.getTags());

        // Inicializar subscribers de cada tag para notificações
        if (p.getTags() != null) {
            p.getTags().forEach(tag -> Hibernate.initialize(tag.getSubscribers()));
        }

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

        List<Publication> publications = em.createNamedQuery("getMyPostsWithTags", Publication.class)
                .setParameter("ids", ids)
                .getResultList();

        // Fetch comments in a separate query to avoid cartesian product
        if (!publications.isEmpty()) {
            em.createQuery("""
                                SELECT DISTINCT p
                                FROM Publication p
                                LEFT JOIN FETCH p.comments
                                WHERE p IN :publications
                            """, Publication.class)
                    .setParameter("publications", publications)
                    .getResultList();

            // Initialize authors of comments
            for (Publication p : publications) {
                for (var comment : p.getComments()) {
                    Hibernate.initialize(comment.getAuthor());
                }
            }
        }

        List<PublicationDTO> data = publications.stream()
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


        String jpqlPublications = """
                    SELECT DISTINCT p
                    FROM Publication p
                    LEFT JOIN FETCH p.tags
                    WHERE p.isVisible = true
                    ORDER BY p.%s %s
                """.formatted(fieldName, orderDirection);

        List<Publication> publications = em.createQuery(jpqlPublications, Publication.class).getResultList();


        if (!publications.isEmpty()) {
            em.createQuery("""
                                SELECT DISTINCT p
                                FROM Publication p
                                LEFT JOIN FETCH p.comments
                                WHERE p IN :publications
                            """, Publication.class)
                    .setParameter("publications", publications)
                    .getResultList();

            // Initialize authors of comments
            for (Publication p : publications) {
                for (var comment : p.getComments()) {
                    Hibernate.initialize(comment.getAuthor());
                }
            }
        }

        return publications;
    }

    // Ordenar todas as publicações (incluindo ocultas) - para usuários autenticados
    public List<Publication> getAllPublicationsSorted(String sortBy, String order) {

        String fieldName = switch (sortBy) {
            case "average_rating" -> "averageRating";
            case "comments_count" -> "commentsCount";
            case "ratings_count" -> "ratingsCount";
            default -> "averageRating";
        };

        String orderDirection = order.equalsIgnoreCase("asc") ? "ASC" : "DESC";

        String jpqlPublications = """
                    SELECT DISTINCT p
                    FROM Publication p
                    LEFT JOIN FETCH p.tags
                    ORDER BY p.%s %s
                """.formatted(fieldName, orderDirection);

        List<Publication> publications = em.createQuery(jpqlPublications, Publication.class).getResultList();

        if (!publications.isEmpty()) {
            em.createQuery("""
                                SELECT DISTINCT p
                                FROM Publication p
                                LEFT JOIN FETCH p.comments
                                WHERE p IN :publications
                            """, Publication.class)
                    .setParameter("publications", publications)
                    .getResultList();

            // Initialize authors of comments
            for (Publication p : publications) {
                for (var comment : p.getComments()) {
                    Hibernate.initialize(comment.getAuthor());
                }
            }
        }

        return publications;
    }

    // EP20 - Ocultar ou mostrar publicação
    public Publication updateVisibility(long postId, boolean visible) throws MyEntityNotFoundException {
        Publication publication = em.find(Publication.class, postId);

        if (publication == null) {
            throw new MyEntityNotFoundException("Publicação com id " + postId + " não encontrada");
        }

        boolean oldVisible = publication.isVisible();
        System.out.println("=== updateVisibility DEBUG ===");
        System.out.println("Publication ID: " + postId + ", Title: " + publication.getTitle());
        System.out.println("Old visibility: " + oldVisible + " -> New visibility: " + visible);

        publication.setVisible(visible);
        publication.setUpdatedAt(new Timestamp(new Date().getTime()));
        em.flush(); // Force immediate persistence

        System.out.println("After setVisible - publication.isVisible(): " + publication.isVisible());

        // Registar no histórico se houve mudança
        if (oldVisible != visible) {
            User author = publication.getAuthor();
            recordEdit(publication, author, java.util.Map.of(
                    "is_visible", java.util.Map.of("old", oldVisible, "new", visible)
            ));
        }

        return publication;
    }

    // EP09 - Pesquisar publicações
    public List<Publication> searchPublications(SearchPublicationDTO dto) {

        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Publication> cq = cb.createQuery(Publication.class);
        Root<Publication> publication = cq.from(Publication.class);

        List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();

        // isVisible = true
        predicates.add(cb.isTrue(publication.get("isVisible")));

        // Title search
        if (dto.getTitle() != null && !dto.getTitle().isBlank()) {
            predicates.add(cb.like(
                cb.lower(publication.get("title")), 
                "%" + dto.getTitle().toLowerCase() + "%"
            ));
        }

        // Author ID search
        if (dto.getAuthorId() != null) {
            predicates.add(cb.equal(publication.get("author").get("id"), dto.getAuthorId()));
        }

        // Scientific area search
        if (dto.getScientificArea() != null && !dto.getScientificArea().isBlank()) {
            predicates.add(cb.like(
                cb.lower(publication.get("scientificArea")), 
                "%" + dto.getScientificArea().toLowerCase() + "%"
            ));
        }

        // Date from
        if (dto.getDateFrom() != null && !dto.getDateFrom().isBlank()) {
            Timestamp fromTs = Timestamp.valueOf(LocalDate.parse(dto.getDateFrom()).atStartOfDay());
            predicates.add(cb.greaterThanOrEqualTo(publication.get("createdAt"), fromTs));
        }

        // Date to
        if (dto.getDateTo() != null && !dto.getDateTo().isBlank()) {
            Timestamp toTs = Timestamp.valueOf(LocalDate.parse(dto.getDateTo()).plusDays(1).atStartOfDay());
            predicates.add(cb.lessThan(publication.get("createdAt"), toTs));
        }

        cq.where(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        cq.orderBy(cb.desc(publication.get("createdAt")));

        TypedQuery<Publication> query = em.createQuery(cq);
        List<Publication> allResults = query.getResultList();

        allResults.forEach(p -> {
            p.getTags().size();
            p.getComments().size();
        });

        // 👉 paginação MANUAL
        int safePage = Math.max(dto.getPage(), 1);
        int safeLimit = Math.max(1, Math.min(dto.getLimit(), 100));

        int fromIndex = (safePage - 1) * safeLimit;
        if (fromIndex >= allResults.size()) {
            return List.of();
        }

        int toIndex = Math.min(fromIndex + safeLimit, allResults.size());

        return allResults.subList(fromIndex, toIndex);
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

        // Qualquer usuário autenticado pode ver o histórico de uma publicação visível
        // Apenas o autor pode ver histórico de publicação não visível
        User requester = userBean.find(userId);
        if (!publication.isVisible() && publication.getAuthor().getId() != requester.getId()) {
            throw new ForbiddenException("Apenas o autor pode ver histórico de publicação oculta");
        }

        int safePage = Math.max(page, 1);
        int safeLimit = Math.max(1, Math.min(limit, 100));

        return em.createNamedQuery("getPublicationHistory", PublicationEdit.class)
                .setParameter("postId", postId)
                .setFirstResult((safePage - 1) * safeLimit)
                .setMaxResults(safeLimit)
                .getResultList();
    }

    public long countPostHistory(long postId) {
        return em.createNamedQuery("countPublicationHistory", Long.class)
                .setParameter("postId", postId)
                .getSingleResult();
    }

    // EP21 - Get hidden publications with pagination, search, and sorting
    public List<Publication> getHiddenPublications(int page, int limit, String search, String sortBy, String order) {
        String orderDir = order != null && order.equalsIgnoreCase("asc") ? "ASC" : "DESC";
        String sortField = sortBy != null && sortBy.equals("title") ? "p.title" : "p.updatedAt";

        String jpql = "SELECT DISTINCT p FROM Publication p LEFT JOIN FETCH p.tags WHERE p.isVisible = false";

        if (search != null && !search.isBlank()) {
            jpql += " AND (LOWER(p.title) LIKE LOWER(:search) OR LOWER(p.scientificArea) LIKE LOWER(:search) OR LOWER(p.author.name) LIKE LOWER(:search))";
        }

        jpql += " ORDER BY " + sortField + " " + orderDir;

        var query = em.createQuery(jpql, Publication.class);

        if (search != null && !search.isBlank()) {
            query.setParameter("search", "%" + search + "%");
        }

        List<Publication> publications = query.setFirstResult((page - 1) * limit)
                .setMaxResults(limit)
                .getResultList();

        // Initialize comments to avoid LazyInitializationException
        publications.forEach(p -> Hibernate.initialize(p.getComments()));

        return publications;
    }

    public long countHiddenPublications(String search) {
        String jpql = "SELECT COUNT(DISTINCT p) FROM Publication p WHERE p.isVisible = false";

        if (search != null && !search.isBlank()) {
            jpql += " AND (LOWER(p.title) LIKE LOWER(:search) OR LOWER(p.scientificArea) LIKE LOWER(:search) OR LOWER(p.author.name) LIKE LOWER(:search))";
        }

        var query = em.createQuery(jpql, Long.class);

        if (search != null && !search.isBlank()) {
            query.setParameter("search", "%" + search + "%");
        }

        return query.getSingleResult();
    }

    private void notifySubscribers(Publication publication, Map<String, Object> changes) {
        System.out.println("🔔 Iniciando notificação de subscribers para publicação: " + publication.getTitle());

        // Inicializar tags para evitar LazyInitializationException
        Hibernate.initialize(publication.getTags());

        System.out.println("📌 Publicação tem " + publication.getTags().size() + " tags");

        // Coletar todos os subscribers únicos de todas as tags da publicação
        java.util.Set<User> subscribers = new java.util.HashSet<>();

        publication.getTags().forEach(tag -> {
            System.out.println("📌 Processando tag: " + tag.getName());
            Hibernate.initialize(tag.getSubscribers());
            System.out.println("📌 Tag tem " + tag.getSubscribers().size() + " subscribers");
            subscribers.addAll(tag.getSubscribers());
        });

        System.out.println("📧 Total de subscribers únicos: " + subscribers.size());

        // Remover o autor da lista de subscribers (não notificar a si mesmo)
        subscribers.remove(publication.getAuthor());

        System.out.println("📧 Subscribers após remover autor: " + subscribers.size());

        if (subscribers.isEmpty()) {
            System.out.println("⚠️ Nenhum subscriber para notificar");
            return;
        }

        // Construir mensagem com as alterações
        StringBuilder changesText = new StringBuilder();
        changes.forEach((field, change) -> {
            Map<String, Object> changeMap = (Map<String, Object>) change;
            changesText.append(String.format("- %s: \"%s\" → \"%s\"\n",
                    field, changeMap.get("old"), changeMap.get("new")));
        });

        // Enviar email para cada subscriber
        String subject = "Publicação Atualizada: " + publication.getTitle();
        String message = String.format(
                "Olá,\n\n" +
                        "A publicação \"%s\" foi atualizada por %s.\n\n" +
                        "Alterações realizadas:\n%s\n" +
                        "Acesse o sistema para ver mais detalhes.\n\n" +
                        "Atenciosamente,\n" +
                        "Sistema de Publicações Académicas",
                publication.getTitle(),
                publication.getAuthor().getName(),
                changesText.toString()
        );

        subscribers.forEach(subscriber -> {
            try {
                System.out.println("📧 Enviando email para: " + subscriber.getEmail());
                emailBean.send(subscriber.getEmail(), subject, message);
                System.out.println("✅ Email enviado com sucesso para: " + subscriber.getEmail());
            } catch (Exception e) {
                System.err.println("❌ Erro ao enviar email para " + subscriber.getEmail() + ": " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    public Publication edit(Long publicationId, MultipartFormDataInput input, Long user_id) throws IOException {

        Map<String, List<InputPart>> formParts = input.getFormDataMap();

        Publication publication = em.find(Publication.class, publicationId);
        if (publication == null) {
            throw new WebApplicationException("Publication not found", Response.Status.NOT_FOUND);
        }

        // (Opcional) garantir que só o autor pode editar
        if (publication.getAuthor().getId() != user_id) {
            throw new WebApplicationException("Unauthorized", Response.Status.FORBIDDEN);
        }

        User editor = userBean.find(user_id.toString());
        Map<String, Object> changes = new java.util.HashMap<>();

        // Atualizar título
        if (formParts.containsKey("title")) {
            String title = readUtf8(formParts.get("title").get(0));
            String oldTitle = publication.getTitle();
            if (!title.equals(oldTitle)) {
                changes.put("title", Map.of("old", oldTitle, "new", title));
            }
            publication.setTitle(title);
        }

        // Atualizar área científica
        if (formParts.containsKey("scientific_area")) {
            String area = readUtf8(formParts.get("scientific_area").get(0));
            String oldArea = publication.getScientificArea();
            if (!area.equals(oldArea)) {
                changes.put("scientific_area", Map.of("old", oldArea, "new", area));
            }
            publication.setScientificArea(area);
        }

        // Atualizar resumo
        if (formParts.containsKey("summary")) {
            String summary = readUtf8(formParts.get("summary").get(0));
            if (summary != null && !summary.isBlank()) {
                String oldSummary = publication.getSummary();
                if (!summary.equals(oldSummary)) {
                    changes.put("summary", Map.of("old", oldSummary, "new", summary));
                }
                publication.setSummary(summary);
            }
        }

        // Atualizar visibilidade
        if (formParts.containsKey("is_visible")) {
            String isVisibleStr = readUtf8(formParts.get("is_visible").get(0));
            boolean isVisible = Boolean.parseBoolean(isVisibleStr);
            boolean oldVisible = publication.isVisible();
            if (isVisible != oldVisible) {
                changes.put("is_visible", Map.of("old", oldVisible, "new", isVisible));
            }
            publication.setVisible(isVisible);
        }

        // Atualizar ficheiro (opcional)
        if (formParts.containsKey("file")) {
            InputPart filePart = formParts.get("file").get(0);
            String fileName = PublicationUtils.getFileName(filePart.getHeaders());
            InputStream fileData = filePart.getBody(InputStream.class, null);

            // Guardar ficheiro
            String uniqueFileName = UUID.randomUUID() + "_" + fileName;
            Path path = Paths.get(UPLOAD_DIR, uniqueFileName);
            Files.createDirectories(path.getParent());
            Files.copy(fileData, path, StandardCopyOption.REPLACE_EXISTING);

            String oldFileName = publication.getFileName();
            publication.setFileKey(uniqueFileName);
            publication.setFileName(fileName);
            if (!fileName.equals(oldFileName)) {
                changes.put("file", Map.of("old", oldFileName != null ? oldFileName : "", "new", fileName));
            }
        }

        // Update timestamp
        publication.setUpdatedAt(new Timestamp(new Date().getTime()));

        // Record edit history if there are changes
        if (!changes.isEmpty()) {
            recordEdit(publication, editor, changes);
        }

        publication = em.merge(publication);

        // Inicializar collections lazy para evitar LazyInitializationException
        Hibernate.initialize(publication.getComments());
        Hibernate.initialize(publication.getTags());
        Hibernate.initialize(publication.getRatings());

        // Notificar subscribers se houver alterações
        if (!changes.isEmpty()) {
            notifySubscribers(publication, changes);
        }

        return publication;
    }


    public File getPublicationFile(long publicationId) {

        Publication publication = em.find(Publication.class, publicationId);


        if (publication == null) {
            throw new WebApplicationException("Publication not found", Response.Status.NOT_FOUND);
        }

        String storedFileName = publication.getFileKey();
        if (storedFileName == null || storedFileName.isBlank()) {
            throw new WebApplicationException(
                    "Publication has no file",
                    Response.Status.NOT_FOUND
            );
        }

        Path filePath = Paths.get(UPLOAD_DIR, storedFileName);

        if (!Files.exists(filePath)) {
            throw new WebApplicationException(
                    "File not found on server",
                    Response.Status.NOT_FOUND
            );
        }

        return filePath.toFile();
    }


    public Publication removeFile(Long publicationId, Long userId) {

        Publication publication = em.find(Publication.class, publicationId);
        if (publication == null) {
            throw new WebApplicationException("Publication not found",
                    Response.Status.NOT_FOUND);
        }

        // (Opcional) garantir que só o autor pode editar
        if (publication.getAuthor().getId() != userId) {
            throw new WebApplicationException("Unauthorized", Response.Status.FORBIDDEN);
        }

        // Se não houver ficheiro, não há nada a fazer
        if (publication.getFileKey() != null) {
            Path filePath = Paths.get(UPLOAD_DIR, publication.getFileKey());

            try {
                Files.deleteIfExists(filePath);
            } catch (IOException e) {
                throw new WebApplicationException("Error deleting file", Response.Status.INTERNAL_SERVER_ERROR);
            }

            // Limpar campos
            publication.setFileKey(null);
            publication.setFileName(null);
        }

        return em.merge(publication);
    }

}
