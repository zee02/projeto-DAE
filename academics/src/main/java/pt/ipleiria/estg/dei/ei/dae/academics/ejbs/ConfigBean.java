package pt.ipleiria.estg.dei.ei.dae.academics.ejbs;

import jakarta.annotation.PostConstruct;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;
import jakarta.ejb.EJB;

@Startup
@Singleton
public class ConfigBean {

    @EJB
    private StudentBean studentBean;

    @PostConstruct
    public void populateDB() {
        System.out.println("Populating database...");

        studentBean.create("alice.silva", "pass123", "Alice Silva", "alice@email.com");

        System.out.println("Database population completed!");
    }
}