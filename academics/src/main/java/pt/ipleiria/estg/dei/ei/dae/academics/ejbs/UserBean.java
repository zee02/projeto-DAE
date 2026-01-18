package pt.ipleiria.estg.dei.ei.dae.academics.ejbs;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import pt.ipleiria.estg.dei.ei.dae.academics.entities.*;
import pt.ipleiria.estg.dei.ei.dae.academics.exceptions.MyEntityExistsException;
import pt.ipleiria.estg.dei.ei.dae.academics.exceptions.MyEntityNotFoundException;
import pt.ipleiria.estg.dei.ei.dae.academics.security.Hasher;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import jakarta.persistence.EntityNotFoundException;

@Stateless
public class UserBean {
    @PersistenceContext
    private EntityManager em;


    public User find(String id) {
        try {
            return em.createNamedQuery("findUser", User.class)
                    .setParameter("id",  Long.parseLong(id))
                    .getSingleResult();
        } catch (Exception e) {
            return null;
        }
    }

    public User findByEmail(String email) {
        try {
            return em.createNamedQuery("findUserByEmail", User.class)
                    .setParameter("email", email)
                    .getSingleResult();
        } catch (Exception e) {
            return null;
        }
    }

    // OBRIGATORIO: Retorno e do tipo 'User'
    public User findOrFail(String email) {
        User user = findByEmail(email);
        if (user == null) {
            throw new IllegalArgumentException("User not found with email: " + email);
        }
        return user;
    }

    public User canLogin(String email, String password) {
        User user = findByEmail(email);

        if (user != null && Hasher.verify(password, user.getPassword())) {
            return user;
        }

        return null;
    }
    public void delete(long id) {
        User user = em.find(User.class, id);

        if (user == null) {
            throw new EntityNotFoundException("User not found");
        }

        em.remove(user);
    }



    public void activate(long userId) throws EntityNotFoundException {
        User u = em.find(User.class, userId);
        if (u == null) throw new EntityNotFoundException();
        u.setActive(true);
    }
    public void deactivate(long userId) throws EntityNotFoundException {
        User u = em.find(User.class, userId);
        if (u == null) throw new EntityNotFoundException();
        u.setActive(false);
    }

    public List<User> findAll() {
        return em.createNamedQuery("getAllUsers", User.class).getResultList();
    }

    // EP13 - Editar dados pessoais
    public User updatePersonalData(String userId, String name, String email) {
        User user = find(userId);

        if (user == null) {
            throw new EntityNotFoundException("Utilizador não encontrado");
        }

        // Atualizar apenas os campos que foram fornecidos
        if (name != null && !name.isBlank()) {
            user.setName(name);
        }

        if (email != null && !email.isBlank()) {
            // Verificar se o email já existe (e não é do próprio utilizador)
            User existingUser = findByEmail(email);
            if (existingUser != null && existingUser.getId() != user.getId()) {
                throw new MyEntityExistsException("Email já está em uso por outro utilizador");
            }
            user.setEmail(email);
        }

        return user;
    }

    // EP27 - Alterar o papel (role) de um utilizador
    public User updateRole(long userId, String newRole) {
        User user = em.find(User.class, userId);

        if (user == null) {
            throw new EntityNotFoundException("Utilizador não encontrado");
        }

        // O JPA com SINGLE_TABLE inheritance não permite mudar o tipo diretamente
        // Precisamos criar um novo utilizador do tipo correto e copiar os dados

        String currentType = user.getClass().getSimpleName();
        String targetType = switch (newRole) {
            case "Colaborador" -> "Colaborador";
            case "Responsavel" -> "Responsavel";
            case "Administrador" -> "Administrador";
            default -> throw new IllegalArgumentException("Role inválido: " + newRole);
        };

        // Se já é do tipo correto, não precisa fazer nada
        if (currentType.equals(targetType)) {
            return user;
        }

        // Atualizar o dtype diretamente na base de dados
        em.createNativeQuery("UPDATE users SET dtype = :dtype WHERE id = :id")
                .setParameter("dtype", targetType)
                .setParameter("id", userId)
                .executeUpdate();

        em.flush();
        em.clear(); // Limpar cache para recarregar

        return em.find(User.class, userId);
    }

    // EP23 - Editar dados de um utilizador (Admin)
    public User updateUser(long userId, String name, String email, String role) {
        User user = em.find(User.class, userId);

        if (user == null) {
            throw new EntityNotFoundException("Utilizador com id " + userId + " não encontrado");
        }

        // Atualizar nome
        if (name != null && !name.isBlank()) {
            user.setName(name);
        }

        // Atualizar email (verificar duplicados)
        if (email != null && !email.isBlank()) {
            User existingUser = findByEmail(email);
            if (existingUser != null && existingUser.getId() != user.getId()) {
                throw new MyEntityExistsException("Email já está em uso por outro utilizador");
            }
            user.setEmail(email);
        }

        // Atualizar role (validar primeiro)
        if (role != null && !role.isBlank()) {
            if (!role.equals("Colaborador") && !role.equals("Responsavel") && !role.equals("Administrador")) {
                throw new IllegalArgumentException("Role inválido: " + role + ". Deve ser: Colaborador, Responsavel ou Administrador");
            }
            updateRole(userId, role);
            em.flush();
            em.clear();
            user = em.find(User.class, userId);
        }

        return user;
    }

    // EP15 - Reset password para "123"
    public void resetPassword(String email) {
        User user = findByEmail(email);
        if (user != null) {
            user.setPassword(Hasher.hash("123"));
        }
        // Não lançar exceção se não encontrar - por segurança não revelamos se email existe
    }

    // EP14 - Alterar palavra-passe
    public User changePassword(String userId, String currentPassword, String newPassword) {
        User user = find(userId);

        if (user == null) {
            throw new EntityNotFoundException("Utilizador não encontrado");
        }

        // Verificar se a palavra-passe atual está correta
        if (!Hasher.verify(currentPassword, user.getPassword())) {
            throw new IllegalArgumentException("Palavra-passe atual incorreta");
        }

        // Atualizar com a nova palavra-passe (hashed)
        user.setPassword(Hasher.hash(newPassword));

        return user;
    }

    // EP28 - Get user activity history
    public Map<String, Object> getUserActivity(long userId, int page, int limit) throws MyEntityNotFoundException {
        User user = em.find(User.class, userId);
        if (user == null) {
            throw new MyEntityNotFoundException("Utilizador com id " + userId + " não encontrado");
        }

        // Validate pagination
        if (page < 1) {
            throw new IllegalArgumentException("Número de página inválido");
        }
        if (limit < 1 || limit > 100) {
            throw new IllegalArgumentException("Limite deve estar entre 1 e 100");
        }

        // Fetch all activities separately and merge
        List<Map<String, Object>> allActivities = new java.util.ArrayList<>();

        // Fetch publications (uploads)
        List<Publication> publications = em.createQuery(
                "SELECT p FROM Publication p WHERE p.author.id = :userId ORDER BY p.submissionDate DESC", 
                Publication.class)
                .setParameter("userId", userId)
                .getResultList();
        
        for (Publication p : publications) {
            Map<String, Object> activity = new HashMap<>();
            activity.put("id", p.getId());
            activity.put("type", "upload");
            activity.put("title", p.getTitle());
            activity.put("date", p.getSubmissionDate());
            allActivities.add(activity);
        }

        // Fetch edits
        List<PublicationEdit> edits = em.createQuery(
                "SELECT pe FROM PublicationEdit pe WHERE pe.editedBy.id = :userId ORDER BY pe.editedAt DESC", 
                PublicationEdit.class)
                .setParameter("userId", userId)
                .getResultList();
        
        for (PublicationEdit e : edits) {
            Map<String, Object> activity = new HashMap<>();
            activity.put("id", e.getId());
            activity.put("type", "edit");
            activity.put("title", "Edição de " + e.getPublication().getTitle());
            activity.put("date", new java.util.Date(e.getEditedAt().getTime()));
            allActivities.add(activity);
        }

        // Fetch comments
        List<Comment> comments = em.createQuery(
                "SELECT c FROM Comment c WHERE c.author.id = :userId ORDER BY c.createdAt DESC", 
                Comment.class)
                .setParameter("userId", userId)
                .getResultList();
        
        for (Comment c : comments) {
            Map<String, Object> activity = new HashMap<>();
            activity.put("id", c.getId());
            activity.put("type", "comment");
            activity.put("title", "Comentário em " + c.getPublication().getTitle());
            activity.put("date", c.getCreatedAt());
            allActivities.add(activity);
        }

        // Fetch ratings
        List<Rating> ratings = em.createQuery(
                "SELECT r FROM Rating r WHERE r.user.id = :userId ORDER BY r.createdAt DESC", 
                Rating.class)
                .setParameter("userId", userId)
                .getResultList();
        
        for (Rating r : ratings) {
            Map<String, Object> activity = new HashMap<>();
            activity.put("id", r.getId());
            activity.put("type", "rating");
            activity.put("title", "Avaliação de " + r.getPublication().getTitle());
            activity.put("date", r.getCreatedAt());
            allActivities.add(activity);
        }

        // Fetch tags created
        List<Tag> tags = em.createQuery(
                "SELECT t FROM Tag t WHERE t.createdBy.id = :userId ORDER BY t.createdAt DESC", 
                Tag.class)
                .setParameter("userId", userId)
                .getResultList();
        
        for (Tag t : tags) {
            Map<String, Object> activity = new HashMap<>();
            activity.put("id", t.getId());
            activity.put("type", "tag_creation");
            activity.put("title", "Criação da tag " + t.getName());
            activity.put("date", t.getCreatedAt());
            allActivities.add(activity);
        }

        // Sort all activities by date descending
        allActivities.sort((a1, a2) -> {
            java.util.Date d1 = (java.util.Date) a1.get("date");
            java.util.Date d2 = (java.util.Date) a2.get("date");
            return d2.compareTo(d1);
        });

        // Calculate totals
        int totalActivities = allActivities.size();
        int totalPages = (int) Math.ceil((double) totalActivities / limit);

        // Apply pagination manually
        int fromIndex = (page - 1) * limit;
        int toIndex = Math.min(fromIndex + limit, totalActivities);
        List<Map<String, Object>> activities = fromIndex < totalActivities 
                ? allActivities.subList(fromIndex, toIndex) 
                : new java.util.ArrayList<>();

        Map<String, Object> response = new HashMap<>();
        response.put("activities", activities);
        response.put("page", page);
        response.put("total_pages", totalPages);
        response.put("total_activities", totalActivities);

        return response;
    }
}
