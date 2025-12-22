package pt.ipleiria.estg.dei.ei.dae.academics.ejbs;

import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;

@Startup
@Singleton
public class ConfigBean {
    @EJB
    private UserBean userBean;

    @PostConstruct
    public void populateDB() {
        try {
            // O método create aceita: (password, name, email, role)
            userBean.create("123", "Admin Chief", "admin@mail.com", "Administrador");
            userBean.create("123", "Collaborator Joe", "colab@mail.com", "Colaborador");
            userBean.create("123", "Responsible Guy", "resp@mail.com", "Responsável");

            System.out.println("Users populated successfully!");
        } catch (Exception e) {
            System.err.println("Error populating DB: " + e.getMessage());
        }
    }
}