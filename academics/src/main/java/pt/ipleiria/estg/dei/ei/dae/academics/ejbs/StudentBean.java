package pt.ipleiria.estg.dei.ei.dae.academics.ejbs;

import jakarta.ejb.Stateless; // [cite: 1693, 1699]
import jakarta.persistence.EntityManager; // [cite: 1695, 1702]
import jakarta.persistence.PersistenceContext; // [cite: 1695, 1701]
import pt.ipleiria.estg.dei.ei.dae.academics.entities.Student; // [cite: 1705]

@Stateless
public class StudentBean {

    @PersistenceContext // [cite: 1701]
    private EntityManager entityManager;

    public void create(String username, String password, String name, String email) {

        var student = new Student(username, password, name, email);

        entityManager.persist(student);
    }
}