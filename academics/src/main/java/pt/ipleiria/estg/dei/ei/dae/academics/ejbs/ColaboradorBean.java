package pt.ipleiria.estg.dei.ei.dae.academics.ejbs;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import pt.ipleiria.estg.dei.ei.dae.academics.entities.Colaborador;
import java.util.List;

@Stateless
public class ColaboradorBean {

    @PersistenceContext(unitName = "AcademicsPersistenceUnit")
    private EntityManager em;

    /**
     * Encontra um colaborador pelo ID
     * @param id ID do colaborador
     * @return Colaborador se encontrado, null caso contrário
     */
    public Colaborador find(Long id) {
        return em.find(Colaborador.class, id);
    }

    /**
     * Lista todos os colaboradores ativos
     * @return Lista de colaboradores
     */
    public List<Colaborador> findAll() {
        return em.createQuery("SELECT c FROM Colaborador c WHERE c.active = true", Colaborador.class)
                .getResultList();
    }

    /**
     * Cria um novo colaborador
     * @param colaborador entidade do colaborador
     */
    public void create(Colaborador colaborador) {
        em.persist(colaborador);
    }

    /**
     * Atualiza um colaborador
     * @param colaborador entidade do colaborador
     */
    public void update(Colaborador colaborador) {
        em.merge(colaborador);
    }

    /**
     * Remove um colaborador (soft delete)
     * @param id ID do colaborador
     */
    public void remove(Long id) {
        Colaborador colaborador = find(id);
        if (colaborador != null) {
            colaborador.setActive(false);
            update(colaborador);
        }
    }
}