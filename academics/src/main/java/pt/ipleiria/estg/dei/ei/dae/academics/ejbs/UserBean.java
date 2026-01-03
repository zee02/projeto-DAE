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

    public void deactivate(long userId) throws EntityNotFoundException {
        User u = em.find(User.class, userId);
        if (u == null) throw new EntityNotFoundException();
        u.setActive(false);
    }

    public List<User> findAll() {
        return em.createNamedQuery("getAllUsers", User.class).getResultList();
    }
}
