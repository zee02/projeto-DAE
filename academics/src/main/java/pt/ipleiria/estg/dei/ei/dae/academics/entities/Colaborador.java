package pt.ipleiria.estg.dei.ei.dae.academics.entities;

import jakarta.persistence.*;

@Entity
@DiscriminatorValue("COLABORADOR")
@Table(name = "colaboradores")
public class Colaborador extends User {

    public Colaborador() {
        super();
    }

    public Colaborador(String username, String passwordHash, String email) {
        super(username, passwordHash, email);
    }

    @Override
    public String getRole() {
        return "COLABORADOR";
    }
}