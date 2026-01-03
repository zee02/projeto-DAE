package pt.ipleiria.estg.dei.ei.dae.academics.ejbs;

// . . .

import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import pt.ipleiria.estg.dei.ei.dae.academics.entities.Administrator;
import pt.ipleiria.estg.dei.ei.dae.academics.security.Hasher;

@Stateless
public class AdministratorBean {
    @PersistenceContext
    private EntityManager entityManager;

    public Administrator create( String password, String name, String email) {

        Administrator administrator = new Administrator(email, Hasher.hash(password), name);
        entityManager.persist(administrator);


        return administrator;
    }


}