package pt.ipleiria.estg.dei.ei.dae.academics.ejbs;

// . . .

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import pt.ipleiria.estg.dei.ei.dae.academics.entities.Administrator;
import pt.ipleiria.estg.dei.ei.dae.academics.entities.Responsible;
import pt.ipleiria.estg.dei.ei.dae.academics.security.Hasher;

@Stateless
public class ResponsibleBean {
    @PersistenceContext
    private EntityManager entityManager;

    public Responsible create( String password, String name, String email) {

        Responsible responsible = new Responsible(email, Hasher.hash(password), name);
        entityManager.persist(responsible);

        return responsible;
    }


}