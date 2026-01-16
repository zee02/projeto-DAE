package pt.ipleiria.estg.dei.ei.dae.academics.ejbs;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import pt.ipleiria.estg.dei.ei.dae.academics.entities.*;
import pt.ipleiria.estg.dei.ei.dae.academics.exceptions.MyEntityExistsException;
import pt.ipleiria.estg.dei.ei.dae.academics.security.Hasher;
import java.util.List;
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
            case "Colaborador" -> "Collaborator";
            case "Responsavel" -> "Responsible";
            case "Administrador" -> "Administrator";
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
}
