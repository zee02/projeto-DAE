package pt.ipleiria.estg.dei.ei.dae.academics.ejbs;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import pt.ipleiria.estg.dei.ei.dae.academics.entities.Responsavel;
import java.util.List;

@Stateless
public class ResponsavelBean {

    @PersistenceContext(unitName = "AcademicsPersistenceUnit")
    private EntityManager em;

    /**
     * Encontra um responsável pelo ID
     * @param id ID do responsável
     * @return Responsavel se encontrado, null caso contrário
     */
    public Responsavel find(Long id) {
        return em.find(Responsavel.class, id);
    }

    /**
     * Lista todos os responsáveis ativos
     * @return Lista de responsáveis
     */
    public List<Responsavel> findAll() {
        return em.createQuery("SELECT r FROM Responsavel r WHERE r.active = true", Responsavel.class)
                .getResultList();
    }

    /**
     * Cria um novo responsável
     * @param responsavel entidade do responsável
     */
    public void create(Responsavel responsavel) {
        em.persist(responsavel);
    }

    /**
     * Atualiza um responsável
     * @param responsavel entidade do responsável
     */
    public void update(Responsavel responsavel) {
        em.merge(responsavel);
    }

    /**
     * Remove um responsável (soft delete)
     * @param id ID do responsável
     */
    public void remove(Long id) {
        Responsavel responsavel = find(id);
        if (responsavel != null) {
            responsavel.setActive(false);
            update(responsavel);
        }
    }
}