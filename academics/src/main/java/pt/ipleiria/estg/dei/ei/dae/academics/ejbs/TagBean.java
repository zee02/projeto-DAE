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
        
        // Log activity
        userBean.logActivity(creator, "CREATE_TAG", 
            "Criou a tag: " + newTag.getName(), 
            "Tag ID: " + newTag.getId());
        
        return newTag;
    }


    public void associateTagToPublication(TagDTO tagIds, long postId, String userId)
            throws MyEntityNotFoundException {

        Publication publication = em.find(Publication.class, postId);
        if (publication == null) {
            throw new MyEntityNotFoundException(
                    "Publication with id " + postId + " not found."
            );
        }

        User user = userBean.find(userId);
        List<String> tagNames = new ArrayList<>();

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
                tagNames.add(tag.getName());
            }
        }
        
        // Log activity
        if (user != null && !tagNames.isEmpty()) {
            userBean.logActivity(user, "ASSOCIATE_TAG", 
                "Associou tag(s) à publicação: " + publication.getTitle(), 
                "Tags: " + String.join(", ", tagNames));
        }
    }


    public void dissociateTagsFromPublication(TagDTO tagIds, long postId, String userId) throws MyEntityNotFoundException {

        Publication publication = em.find(Publication.class, postId);
        if (publication == null) {
            throw new MyEntityNotFoundException("Publication with id " + postId + " not found.");
        }

        User user = userBean.find(userId);
        List<String> tagNames = new ArrayList<>();

        for (Long tagId : tagIds.getTags()) {

            Tag tag = em.find(Tag.class, tagId);
            if (tag == null) {
                throw new MyEntityNotFoundException("Tag with id " + tagId + " not found.");
            }


            if (publication.getTags().contains(tag)) {
                publication.removeTag(tag);
                tag.getPublications().remove(publication);
                tagNames.add(tag.getName());
            }
        }
        
        // Log activity
        if (user != null && !tagNames.isEmpty()) {
            userBean.logActivity(user, "DISSOCIATE_TAG", 
                "Removeu tag(s) da publicação: " + publication.getTitle(), 
                "Tags: " + String.join(", ", tagNames));
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
        
        // Log activity
        userBean.logActivity(user, "SUBSCRIBE_TAG", 
            "Subscreveu a tag: " + tag.getName(), 
            "Tag ID: " + tag.getId());
        
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

        // Log activity
        userBean.logActivity(user, "UNSUBSCRIBE_TAG", 
            "Cancelou subscrição da tag: " + tag.getName(), 
            "Tag ID: " + tag.getId());

        return tag;
    }

    // EP17 - Eliminar tag
    public void delete(long tagId, String userId) throws MyEntityNotFoundException {
        Tag tag = em.find(Tag.class, tagId);

        if (tag == null) {
            throw new MyEntityNotFoundException("Tag não encontrada");
        }

        // Verificar se a tag está associada a publicações
        if (!tag.getPublications().isEmpty()) {
            throw new IllegalArgumentException("A tag não pode ser eliminada porque está em uso");
        }

        String tagName = tag.getName(); // Save name before deletion

        // Remover subscrições de utilizadores (criar cópia para evitar ConcurrentModificationException)
        List<User> subscribersCopy = new ArrayList<>(tag.getSubscribers());
        for (User user : subscribersCopy) {
            user.unsubscribeTag(tag);
            tag.getSubscribers().remove(user);
        }

        em.remove(tag);
        
        // Log activity
        User deleter = userBean.find(userId);
        if (deleter != null) {
            userBean.logActivity(deleter, "DELETE_TAG", 
                "Eliminou a tag: " + tagName, 
                "Tag ID: " + tagId);
        }
    }

    // Ocultar ou mostrar tag
    public Tag updateVisibility(long tagId, boolean visible, String userId) throws MyEntityNotFoundException {
        Tag tag = em.find(Tag.class, tagId);

        if (tag == null) {
            throw new MyEntityNotFoundException("Tag com id " + tagId + " não encontrada");
        }

        tag.setVisible(visible);

        // Log activity
        User user = userBean.find(userId);
        if (user != null) {
            String activityType = visible ? "SHOW_TAG" : "HIDE_TAG";
            String description = visible ? "Tornou visível a tag: " + tag.getName() : "Ocultou a tag: " + tag.getName();
            userBean.logActivity(user, activityType, description, null);
        }

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