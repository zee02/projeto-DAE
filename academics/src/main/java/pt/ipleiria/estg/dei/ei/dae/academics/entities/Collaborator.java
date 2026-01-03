package pt.ipleiria.estg.dei.ei.dae.academics.entities;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;

import java.io.Serializable;
import java.util.List;

@Entity
@DiscriminatorValue("Colaborador")
public class Collaborator extends User implements Serializable {




    public Collaborator() {

    }

    public Collaborator(String email, String password, String name) {
        super(email, password, name);
    }
}