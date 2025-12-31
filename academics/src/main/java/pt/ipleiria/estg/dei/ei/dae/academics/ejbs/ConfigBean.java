package pt.ipleiria.estg.dei.ei.dae.academics.ejbs;

import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;

import java.io.IOException;
import java.io.InputStream;

@Startup
@Singleton
public class ConfigBean {
    @EJB
    private UserBean userBean;

    @EJB
    private PublicationBean publicationBean;

    @EJB
    private RatingBean ratingBean;

    @EJB
    private TagBean tagBean ;

    @PostConstruct
    public void populateDB() {
        try {
            // O método create aceita: (password, name, email, role)
            userBean.create("123", "Admin Chief", "admin@mail.com", "Administrador");
            userBean.create("123", "Collaborator Joe", "colab@mail.com", "Colaborador");
            userBean.create("123", "Responsible Guy", "resp@mail.com", "Responsável");
            userBean.create("123", "duarte", "duarte@mail.com", "Responsável");


            publicationBean.create("Primeira publicação", "Engenharia Informática", "duarte@mail.com");
            publicationBean.create("Segunda Publicaçao", "Engenharia Civil", "colab@mail.com");


            ratingBean.giveRating(1L, "duarte@mail.com", 4);
            ratingBean.giveRating(2L, "duarte@mail.com", 5);

            tagBean.create("Tag 1");
            tagBean.create("Tag 2");
            tagBean.create("Tag 4");

            tagBean.associateTagtoPublication(1,1);

            System.out.println("Users populated successfully!");
        } catch (Exception e) {
            System.err.println("Error populating DB: " + e.getMessage());
        }
    }
}