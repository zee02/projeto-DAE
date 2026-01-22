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
            User user = em.createNamedQuery("findUser", User.class)
                    .setParameter("id",  Long.parseLong(id))
                    .getSingleResult();
            
            // Eagerly load subscribedTags to avoid lazy initialization issues
            if (user != null) {
                try {
                    org.hibernate.Hibernate.initialize(user.getSubscribedTags());
                } catch (Exception ex) {
                    // Ignore if initialization fails, collection will be empty
                }
            }
            
            return user;
        } catch (Exception e) {
            return null;
        }
    }

    public User findByEmail(String email) {
        try {
            User user = em.createNamedQuery("findUserByEmail", User.class)
                    .setParameter("email", email)
                    .getSingleResult();
            
            // Eagerly load subscribedTags to avoid lazy initialization issues
            if (user != null) {
                try {
                    org.hibernate.Hibernate.initialize(user.getSubscribedTags());
                } catch (Exception ex) {
                    // Ignore if initialization fails, collection will be empty
                }
            }
            
            return user;
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
            // Check if user is active
            if (!user.isActive()) {
                return null; // Inactive users cannot login
            }
            // subscribedTags already initialized in findByEmail
            return user;
        }

        return null;
    }
    public void delete(long id, String adminId) {
        User user = em.find(User.class, id);

        if (user == null) {
            throw new EntityNotFoundException("User not found");
        }

        String userName = user.getName();
        String userEmail = user.getEmail();
        
        em.remove(user);
        
        // Log activity for the admin who deleted the user
        User admin = find(adminId);
        if (admin != null) {
            logActivity(admin, "DELETE_USER", 
                "Eliminou o utilizador: " + userName, 
                "Email: " + userEmail + ", ID: " + id);
        }
    }



    public void activate(long userId, String adminId) throws EntityNotFoundException {
        User u = em.find(User.class, userId);
        if (u == null) throw new EntityNotFoundException();
        u.setActive(true);
        
        // Log activity for the admin who activated the user
        User admin = find(adminId);
        if (admin != null) {
            logActivity(admin, "ACTIVATE_USER", 
                "Ativou o utilizador: " + u.getName(), 
                "Email: " + u.getEmail() + ", ID: " + userId);
        }
    }
    public void deactivate(long userId, String adminId) throws EntityNotFoundException {
        User u = em.find(User.class, userId);
        if (u == null) throw new EntityNotFoundException();
        u.setActive(false);
        
        // Log activity for the admin who deactivated the user
        User admin = find(adminId);
        if (admin != null) {
            logActivity(admin, "DEACTIVATE_USER", 
                "Desativou o utilizador: " + u.getName(), 
                "Email: " + u.getEmail() + ", ID: " + userId);
        }
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

        boolean hasChanges = false;
        StringBuilder changes = new StringBuilder();

        // Atualizar apenas os campos que foram fornecidos
        if (name != null && !name.isBlank() && !name.equals(user.getName())) {
            String oldName = user.getName();
            user.setName(name);
            changes.append(String.format("Nome: '%s' → '%s'; ", oldName, name));
            hasChanges = true;
            logActivity(user, "UPDATE_NAME", "Atualizou o nome", 
                String.format("De '%s' para '%s'", oldName, name));
        }

        if (email != null && !email.isBlank() && !email.equals(user.getEmail())) {
            // Verificar se o email já existe (e não é do próprio utilizador)
            User existingUser = findByEmail(email);
            if (existingUser != null && existingUser.getId() != user.getId()) {
                throw new MyEntityExistsException("Email já está em uso por outro utilizador");
            }
            String oldEmail = user.getEmail();
            user.setEmail(email);
            changes.append(String.format("Email: '%s' → '%s'; ", oldEmail, email));
            hasChanges = true;
            logActivity(user, "UPDATE_EMAIL", "Atualizou o email", 
                String.format("De '%s' para '%s'", oldEmail, email));
        }

        if (hasChanges) {
            logActivity(user, "UPDATE_PROFILE", "Atualizou dados pessoais", changes.toString());
        }

        return user;
    }

    // EP27 - Alterar o papel (role) de um utilizador
    public User updateRole(long userId, String newRole, String adminId) {
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
        
        User updatedUser = em.find(User.class, userId);
        
        // Log activity for the admin who changed the role
        User admin = find(adminId);
        if (admin != null) {
            logActivity(admin, "CHANGE_USER_ROLE", 
                "Alterou o role do utilizador: " + updatedUser.getName(), 
                String.format("De '%s' para '%s'", currentType, newRole));
        }

        return updatedUser;
    }

    // EP23 - Editar dados de um utilizador (Admin)
    public User updateUser(long userId, String name, String email, String role, String adminId) {
        User user = em.find(User.class, userId);

        if (user == null) {
            throw new EntityNotFoundException("Utilizador com id " + userId + " não encontrado");
        }

        StringBuilder changes = new StringBuilder();
        boolean hasChanges = false;

        // Atualizar nome
        if (name != null && !name.isBlank() && !name.equals(user.getName())) {
            String oldName = user.getName();
            user.setName(name);
            changes.append(String.format("Nome: '%s' → '%s'; ", oldName, name));
            hasChanges = true;
        }

        // Atualizar email (verificar duplicados)
        if (email != null && !email.isBlank() && !email.equals(user.getEmail())) {
            User existingUser = findByEmail(email);
            if (existingUser != null && existingUser.getId() != user.getId()) {
                throw new MyEntityExistsException("Email já está em uso por outro utilizador");
            }
            String oldEmail = user.getEmail();
            user.setEmail(email);
            changes.append(String.format("Email: '%s' → '%s'; ", oldEmail, email));
            hasChanges = true;
        }

        // Atualizar role (validar primeiro)
        String oldRole = null;
        if (role != null && !role.isBlank()) {
            if (!role.equals("Colaborador") && !role.equals("Responsavel") && !role.equals("Administrador")) {
                throw new IllegalArgumentException("Role inválido: " + role + ". Deve ser: Colaborador, Responsavel ou Administrador");
            }
            oldRole = user.getClass().getSimpleName();
            if (!oldRole.equals(role)) {
                updateRole(userId, role, adminId);
                em.flush();
                em.clear();
                changes.append(String.format("Role: '%s' → '%s'; ", oldRole, role));
                hasChanges = true;
            }
        }
        
        // Log activity for the admin who edited the user
        if (hasChanges) {
            User admin = find(adminId);
            if (admin != null) {
                logActivity(admin, "EDIT_USER", 
                    "Editou o utilizador: " + user.getName(), 
                    changes.toString());
            }
        }

        return user;
    }

    // EP15 - Reset password para "123"
    public void resetPassword(String email) {
        User user = findByEmail(email);
        if (user != null) {
            user.setPassword(Hasher.hash("123"));
            // Log activity
            logActivity(user, "PASSWORD_RESET_REQUEST", 
                "Solicitou reset de password por email", 
                "Nova password temporária enviada para: " + email);
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
        
        // Log activity
        logActivity(user, "UPDATE_PASSWORD", 
            "Alterou a password", 
            "Password atualizada com sucesso");

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

        // Fetch user activities (logged actions)
        List<UserActivity> userActivities = em.createQuery(
                "SELECT ua FROM UserActivity ua WHERE ua.user.id = :userId ORDER BY ua.timestamp DESC", 
                UserActivity.class)
                .setParameter("userId", userId)
                .getResultList();
        
        for (UserActivity ua : userActivities) {
            Map<String, Object> activity = new HashMap<>();
            activity.put("id", ua.getId());
            activity.put("type", ua.getType());
            activity.put("description", ua.getDescription());
            activity.put("details", ua.getDetails());
            activity.put("date", ua.getTimestamp());
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

    // Helper method to log user activities
    public void logActivity(User user, String type, String description, String details) {
        try {
            UserActivity activity = new UserActivity(user, type, description, details);
            em.persist(activity);
            System.out.println("📝 Activity logged: " + type + " for user " + user.getEmail());
        } catch (Exception e) {
            System.err.println("❌ Error logging activity: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void logActivity(User user, String type, String description) {
        logActivity(user, type, description, null);
    }
}
