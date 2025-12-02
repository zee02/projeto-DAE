package pt.ipleiria.estg.dei.ei.dae.academics.ejbs;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import pt.ipleiria.estg.dei.ei.dae.academics.entities.Administrador;
import pt.ipleiria.estg.dei.ei.dae.academics.entities.User;
import java.util.List;

@Stateless
public class AdministradorBean {

    @PersistenceContext(unitName = "AcademicsPersistenceUnit")
    private EntityManager em;

    /**
     * Encontra um administrador pelo ID
     * @param id ID do administrador
     * @return Administrador se encontrado, null caso contrário
     */
    public Administrador find(Long id) {
        return em.find(Administrador.class, id);
    }

    /**
     * Lista todos os administradores ativos
     * @return Lista de administradores
     */
    public List<Administrador> findAll() {
        return em.createQuery("SELECT a FROM Administrador a WHERE a.active = true", Administrador.class)
                .getResultList();
    }

    /**
     * Cria um novo administrador
     * @param administrador entidade do administrador
     */
    public void create(Administrador administrador) {
        em.persist(administrador);
    }

    /**
     * Atualiza um administrador
     * @param administrador entidade do administrador
     */
    public void update(Administrador administrador) {
        em.merge(administrador);
    }

    /**
     * Remove um administrador (soft delete)
     * @param id ID do administrador
     */
    public void remove(Long id) {
        Administrador administrador = find(id);
        if (administrador != null) {
            administrador.setActive(false);
            update(administrador);
        }
    }

    /**
     * Lista todos os utilizadores do sistema
     * @return Lista de utilizadores
     */
    public List<User> getAllUsers() {
        return em.createQuery("SELECT u FROM User u WHERE u.active = true", User.class)
                .getResultList();
    }
}