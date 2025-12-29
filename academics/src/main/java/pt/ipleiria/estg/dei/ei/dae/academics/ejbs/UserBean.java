package pt.ipleiria.estg.dei.ei.dae.academics.ejbs;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import pt.ipleiria.estg.dei.ei.dae.academics.entities.*;
import pt.ipleiria.estg.dei.ei.dae.academics.security.Hasher;
import java.util.List;
import jakarta.persistence.EntityNotFoundException;

@Stateless
public class UserBean {
    @PersistenceContext
    private EntityManager em;

    public User find(String email) {
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
        User user = find(email);
        if (user == null) {
            throw new IllegalArgumentException("User not found with email: " + email);
        }
        return user;
    }

    public boolean canLogin(String email, String password) {
        User user = find(email);
        return user != null && Hasher.verify(password, user.getPassword());
    }

    public User create(String password, String name, String email, String role) {
        User user = find(email);
        if (user != null) {
            throw new IllegalArgumentException("User already exists with email: " + email);
        }

        String hashedPassword = Hasher.hash(password);

        switch (role) {
            case "Administrador":
                user = new Administrator(email, hashedPassword, name);
                break;
            case "Responsável":
                user = new Responsible(email, hashedPassword, name);
                break;
            case "Colaborador":
                user = new Collaborator(email, hashedPassword, name);
                break;
            default:
                throw new IllegalArgumentException("Invalid Role: " + role);
        }

        em.persist(user);
        return user;
    }

    public void deactivate(long userId) throws EntityNotFoundException {
        User u = em.find(User.class, userId);
        if (u == null) throw new EntityNotFoundException();
        u.setActive(false);
    }

    public List<User> findAll() {
        return em.createNamedQuery("getAllUsers", User.class).getResultList();
    }
}
