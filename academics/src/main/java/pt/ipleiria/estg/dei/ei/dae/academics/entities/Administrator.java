package pt.ipleiria.estg.dei.ei.dae.academics.entities;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import java.io.Serializable;

@Entity
@DiscriminatorValue("Administrador")

public class Administrator extends User implements Serializable {
    public Administrator() {}

    public Administrator(String email, String password, String name) {
        super(email, password, name);
    }
}