package pt.ipleiria.estg.dei.ei.dae.academics.entities;

import jakarta.persistence.Entity;
import java.io.Serializable;

@Entity
public class Responsible extends User implements Serializable {

    public Responsible() {
    }

    public Responsible(String email, String password, String name) {
        // CORREÇÃO: Passa "Responsável" para o construtor do User
        // User(email, password, name, role)
        super(email, password, name, "Responsável");
    }
}