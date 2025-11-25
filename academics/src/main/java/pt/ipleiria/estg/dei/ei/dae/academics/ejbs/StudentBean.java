package pt.ipleiria.estg.dei.ei.dae.academics.ejbs;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import pt.ipleiria.estg.dei.ei.dae.academics.entities.Student;

@Stateless
public class StudentBean {

    @PersistenceContext
    private EntityManager entityManager;

    public void create( String username, String name, String password, String email ) {
        var student = new Student( username,  name,  password, email);
        entityManager.persist(student);
    }

}
