package pt.ipleiria.estg.dei.ei.dae.academics.entities;

import jakarta.persistence.*;

@Entity
@DiscriminatorValue("RESPONSAVEL")
@Table(name = "responsaveis")
public class Responsavel extends User {

    public Responsavel() {
        super();
    }

    public Responsavel(String username, String passwordHash, String email) {
        super(username, passwordHash, email);
    }

    @Override
    public String getRole() {
        return "RESPONSAVEL";
    }
}