package pt.ipleiria.estg.dei.ei.dae.academics.ejbs;

import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.persistence.*;
import pt.ipleiria.estg.dei.ei.dae.academics.dtos.TagDTO;
import pt.ipleiria.estg.dei.ei.dae.academics.entities.Publication;
import pt.ipleiria.estg.dei.ei.dae.academics.entities.Rating;
import pt.ipleiria.estg.dei.ei.dae.academics.entities.Tag;
import pt.ipleiria.estg.dei.ei.dae.academics.entities.User;
import pt.ipleiria.estg.dei.ei.dae.academics.exceptions.MyEntityConflictException;
import pt.ipleiria.estg.dei.ei.dae.academics.exceptions.MyEntityExistsException;
import pt.ipleiria.estg.dei.ei.dae.academics.exceptions.MyEntityNotFoundException;

import java.util.ArrayList;
import java.util.List;


@Stateless
public class TagBean {
    @PersistenceContext
    private EntityManager em;

    @EJB
    private UserBean userBean;

    @EJB
    private PublicationBean publicationBean;

    public List<Tag> findAll() {

        List<Tag> results = em.createNamedQuery(
                "findAllTags",
                Tag.class
        ).getResultList();

        return results.isEmpty() ? null : results;
    }

    public Tag findByName(String name) {
        try {
            return em.createQuery("SELECT t FROM Tag t WHERE t.name = :name", Tag.class)
                    .setParameter("name", name)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    // EP16 - Criar nova tag
    public Tag create(String nomeTag, String userId) throws MyEntityExistsException {

        if (nomeTag == null || nomeTag.trim().isEmpty()) {
            throw new IllegalArgumentException("O nome da tag não pode ser vazio");
        }

        // Verificar se já existe uma tag com esse nome
        Tag existingTag = findByName(nomeTag.trim());
        if (existingTag != null) {
            throw new MyEntityExistsException("Já existe uma tag com o nome: " + nomeTag);
        }

        User creator = userBean.find(userId);

        Tag newTag = new Tag();
        newTag.setName(nomeTag.trim());
        newTag.setCreatedBy(creator);
        newTag.setCreatedAt(new java.util.Date());
        em.persist(newTag);
        em.flush(); // Força a geração do ID imediatamente
        return newTag;
    }


    public void associateTagToPublication(TagDTO tagIds, long postId)
            throws MyEntityNotFoundException {

        Publication publication = em.find(Publication.class, postId);
        if (publication == null) {
            throw new MyEntityNotFoundException(
                    "Publication with id " + postId + " not found."
            );
        }

        java.util.List<String> addedTags = new java.util.ArrayList<>();

        for (Long tagId : tagIds.getTags()) {

            Tag tag = em.find(Tag.class, tagId);
            if (tag == null) {
                throw new MyEntityNotFoundException(
                        "Tag with id " + tagId + " not found."
                );
            }


            if (!publication.getTags().contains(tag)) {
                publication.addTag(tag);
                tag.getPublications().add(publication);
                addedTags.add(tag.getName());
            }
        }

        // Registrar no histórico se tags foram adicionadas
        if (!addedTags.isEmpty()) {
            try {
                publicationBean.seedHistory(
                    postId,
                    publication.getAuthor().getEmail(),
                    java.util.Map.of("tags", "Tags adicionadas: " + String.join(", ", addedTags))
                );
            } catch (Exception e) {
                // Não falhar se histórico não for gravado
            }
        }
    }


    public void dissociateTagsFromPublication(TagDTO tagIds, long postId) throws MyEntityNotFoundException {

        Publication publication = em.find(Publication.class, postId);
        if (publication == null) {
            throw new MyEntityNotFoundException("Publication with id " + postId + " not found.");
        }

        java.util.List<String> removedTags = new java.util.ArrayList<>();

        for (Long tagId : tagIds.getTags()) {

            Tag tag = em.find(Tag.class, tagId);
            if (tag == null) {
                throw new MyEntityNotFoundException("Tag with id " + tagId + " not found.");
            }


            if (publication.getTags().contains(tag)) {
                publication.removeTag(tag);
                tag.getPublications().remove(publication);
                removedTags.add(tag.getName());
            }
        }

        // Registrar no histórico se tags foram removidas
        if (!removedTags.isEmpty()) {
            try {
                publicationBean.seedHistory(
                    postId,
                    publication.getAuthor().getEmail(),
                    java.util.Map.of("tags", "Tags removidas: " + String.join(", ", removedTags))
                );
            } catch (Exception e) {
                // Não falhar se histórico não for gravado
            }
        }
    }

    public Tag find(long id) {
        return em.find(Tag.class, id);
    }

    // EP11 - Subscrever tag
    public Tag subscribeUserToTag(String userId, long tagId) throws MyEntityNotFoundException, MyEntityConflictException {
        System.out.println("=== DEBUG: Tentando subscrever tag " + tagId + " para user " + userId);
        
        User user = userBean.find(userId);
        if (user == null) {
            System.out.println("=== DEBUG: User não encontrado!");
            throw new MyEntityNotFoundException("Utilizador com id " + userId + " não encontrado");
        }
        System.out.println("=== DEBUG: User encontrado: " + user.getName());

        // Tenta buscar a tag de diferentes formas para garantir que existe
        Tag tag = em.find(Tag.class, tagId);
        System.out.println("=== DEBUG: Tag no 1º find: " + (tag != null ? tag.getName() : "NULL"));
        
        // Se não encontrar, tenta fazer refresh do contexto
        if (tag == null) {
            System.out.println("=== DEBUG: Tag null, limpando cache...");
            em.clear(); // Limpa o cache do EntityManager
            tag = em.find(Tag.class, tagId);
            System.out.println("=== DEBUG: Tag após clear: " + (tag != null ? tag.getName() : "AINDA NULL"));
        }
        
        if (tag == null) {
            System.out.println("=== DEBUG: Tag definitivamente não encontrada!");
            throw new MyEntityNotFoundException("Tag com id " + tagId + " não encontrada");
        }

        if (user.getSubscribedTags().contains(tag)) {
            throw new MyEntityConflictException("Tag já subscrita pelo utilizador");
        }

        user.subscribeTag(tag);
        tag.getSubscribers().add(user);
        
        System.out.println("=== DEBUG: Subscrição bem-sucedida!");
        return tag;
    }

    // Cancelar subscrição de tag
    public Tag unsubscribeUserFromTag(String userId, long tagId) throws MyEntityNotFoundException, MyEntityConflictException {
        User user = userBean.find(userId);
        if (user == null) {
            throw new MyEntityNotFoundException("Utilizador com id " + userId + " não encontrado");
        }

        Tag tag = em.find(Tag.class, tagId);
        if (tag == null) {
            throw new MyEntityNotFoundException("Tag com id " + tagId + " não encontrada");
        }

        if (!user.getSubscribedTags().contains(tag)) {
            throw new MyEntityConflictException("Tag não está subscrita pelo utilizador");
        }

        user.unsubscribeTag(tag);
        tag.getSubscribers().remove(user);

        return tag;
    }

    // EP17 - Eliminar tag
    public void delete(long tagId) throws MyEntityNotFoundException {
        Tag tag = em.find(Tag.class, tagId);

        if (tag == null) {
            throw new MyEntityNotFoundException("Tag não encontrada");
        }

        // Verificar se a tag está associada a publicações
        if (!tag.getPublications().isEmpty()) {
            throw new IllegalArgumentException("A tag não pode ser eliminada porque está em uso");
        }

        // Remover subscrições de utilizadores (criar cópia para evitar ConcurrentModificationException)
        List<User> subscribersCopy = new ArrayList<>(tag.getSubscribers());
        for (User user : subscribersCopy) {
            user.unsubscribeTag(tag);
            tag.getSubscribers().remove(user);
        }

        em.remove(tag);
    }

    // Ocultar ou mostrar tag
    public Tag updateVisibility(long tagId, boolean visible) throws MyEntityNotFoundException {
        Tag tag = em.find(Tag.class, tagId);

        if (tag == null) {
            throw new MyEntityNotFoundException("Tag com id " + tagId + " não encontrada");
        }

        tag.setVisible(visible);

        return tag;
    }

    // EP21 - Get hidden tags with pagination, search, and sorting
    public List<Tag> getHiddenTags(int page, int limit, String search, String sortBy, String order) {
        String orderDir = order != null && order.equalsIgnoreCase("asc") ? "ASC" : "DESC";
        String sortField = "t.name"; // Tags only sort by name
        
        String jpql = "SELECT t FROM Tag t WHERE t.visible = false";
        
        if (search != null && !search.isBlank()) {
            jpql += " AND LOWER(t.name) LIKE LOWER(:search)";
        }
        
        jpql += " ORDER BY " + sortField + " " + orderDir;
        
        var query = em.createQuery(jpql, Tag.class);
        
        if (search != null && !search.isBlank()) {
            query.setParameter("search", "%" + search + "%");
        }
        
        return query.setFirstResult((page - 1) * limit)
                    .setMaxResults(limit)
                    .getResultList();
    }

    public long countHiddenTags(String search) {
        String jpql = "SELECT COUNT(t) FROM Tag t WHERE t.visible = false";
        
        if (search != null && !search.isBlank()) {
            jpql += " AND LOWER(t.name) LIKE LOWER(:search)";
        }
        
        var query = em.createQuery(jpql, Long.class);
        
        if (search != null && !search.isBlank()) {
            query.setParameter("search", "%" + search + "%");
        }
        
        return query.getSingleResult();
    }

}