package pt.ipleiria.estg.dei.ei.dae.academics.entities;

import jakarta.persistence.*;

@Entity
@DiscriminatorValue("ADMINISTRADOR")
@Table(name = "administradores")
public class Administrador extends User {

    public Administrador() {
        super();
    }

    public Administrador(String username, String passwordHash, String email) {
        super(username, passwordHash, email);
    }

    @Override
    public String getRole() {
        return "ADMINISTRADOR";
    }
}