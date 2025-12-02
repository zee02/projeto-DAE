package pt.ipleiria.estg.dei.ei.dae.academics.ejbs;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.NoResultException;
import pt.ipleiria.estg.dei.ei.dae.academics.entities.User;
import pt.ipleiria.estg.dei.ei.dae.academics.security.Hasher;

import java.util.List;

@Stateless
public class UserBean {

    @PersistenceContext(unitName = "AcademicsPersistenceUnit")
    private EntityManager em;

    private Hasher hasher = new Hasher();

    /**
     * Encontra um utilizador pelo username
     * @param username nome do utilizador
     * @return User se encontrado, null caso contrário
     */
    public User find(String username) {
        try {
            return em.createQuery("SELECT u FROM User u WHERE u.username = :username", User.class)
                    .setParameter("username", username)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    /**
     * Verifica se um utilizador pode fazer login
     * @param username nome do utilizador
     * @param password password em texto plano
     * @return true se credenciais corretas e utilizador ativo, false caso contrário
     */
    public boolean canLogin(String username, String password) {
        User user = find(username);
        if (user == null || !user.isActive()) {
            return false;
        }
        return hasher.verify(password, user.getPasswordHash());
    }

    /**
     * Cria um novo utilizador
     * @param user entidade do utilizador
     */
    public void create(User user) {
        em.persist(user);
    }

    /**
     * Atualiza um utilizador
     * @param user entidade do utilizador
     */
    public void update(User user) {
        em.merge(user);
    }

    /**
     * Remove um utilizador
     * @param userId ID do utilizador
     */
    public void remove(Long userId) {
        User user = em.find(User.class, userId);
        if (user != null) {
            em.remove(user);
        }
    }

    /**
     * Encontra todos os utilizadores
     * @return Lista de todos os utilizadores
     */
    public List<User> findAll() {
        return em.createQuery("SELECT u FROM User u", User.class).getResultList();
    }

    /**
     * Encontra um utilizador pelo ID
     * @param id ID do utilizador
     * @return User se encontrado, null caso contrário
     */
    public User findById(long id) {
        return em.find(User.class, id);
    }
}