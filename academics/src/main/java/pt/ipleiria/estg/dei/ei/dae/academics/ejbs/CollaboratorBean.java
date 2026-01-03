package pt.ipleiria.estg.dei.ei.dae.academics.ejbs;

// . . .

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import pt.ipleiria.estg.dei.ei.dae.academics.entities.Collaborator;
import pt.ipleiria.estg.dei.ei.dae.academics.entities.Responsible;
import pt.ipleiria.estg.dei.ei.dae.academics.security.Hasher;

@Stateless
public class CollaboratorBean {
    @PersistenceContext
    private EntityManager entityManager;

    public Collaborator create( String password, String name, String email) {

        Collaborator collaborator = new Collaborator(email, Hasher.hash(password), name);
        entityManager.persist(collaborator);
        return collaborator;
    }


}