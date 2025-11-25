package pt.ipleiria.estg.dei.ei.dae.academics.ejbs;


import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;

@Singleton // this EJB will have only one instance in the application;
@Startup //this EJB will be automatically instantiated once the application is
//deployed onto the Wildfly application server.
public class ConfigBean {
    @EJB
    private StudentBean studentBean;

    @PostConstruct
    public void populateDB() {
        System.out.println("Hello Java EE!");
        String username = "rafa";
        String name = "Coelho";
        String password = "pass";
        String email = "email";

        studentBean.create(username,name,password,email);
    }

}
