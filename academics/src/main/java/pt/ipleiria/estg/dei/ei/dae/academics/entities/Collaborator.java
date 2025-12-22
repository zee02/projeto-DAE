package pt.ipleiria.estg.dei.ei.dae.academics.entities;

import jakarta.persistence.Entity;
import java.io.Serializable;

@Entity
public class Collaborator extends User implements Serializable {
    public Collaborator() {}

    public Collaborator(String email, String password, String name) {
        super(email, password, name, "Colaborador");
    }
}