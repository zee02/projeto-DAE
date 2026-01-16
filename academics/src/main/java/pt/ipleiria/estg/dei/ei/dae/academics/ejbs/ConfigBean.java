package pt.ipleiria.estg.dei.ei.dae.academics.ejbs;

import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;
import pt.ipleiria.estg.dei.ei.dae.academics.dtos.CommentDTO;
import pt.ipleiria.estg.dei.ei.dae.academics.dtos.TagDTO;
import pt.ipleiria.estg.dei.ei.dae.academics.entities.Collaborator;
import pt.ipleiria.estg.dei.ei.dae.academics.entities.Publication;
import pt.ipleiria.estg.dei.ei.dae.academics.entities.Responsible;
import pt.ipleiria.estg.dei.ei.dae.academics.entities.Tag;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

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
    private TagBean tagBean;


    @EJB
    AdministratorBean administratorBean;

    @EJB
    ResponsibleBean responsibleBean;
    @EJB
    CollaboratorBean collaboratorBean;

    @EJB
    private CommentBean commentBean;

    @PostConstruct
    public void populateDB() {
        try {
            // O método create aceita: (password, name, email, role)


            administratorBean.create("123", "Administrador Geral", "tester@admin.pt");
            administratorBean.create("123", "Ana Martins", "admin@mail.com");
            administratorBean.create("123", "Duarte Pereira", "duarte@mail.com");

            responsibleBean.create("123", "Rui Almeida", "teste@mail.com");
            responsibleBean.create("123", "Carla Fernandes", "tester@responsavel.pt");
            responsibleBean.create("123", "Pedro Rodrigues", "resp@mail.com");

            collaboratorBean.create("123", "João Silva", "tester@colaborador.pt");
            collaboratorBean.create("123", "Miguel Costa", "colab@mail.com");




            Publication pub1  = publicationBean.create("Introdução à Programação em Java", "Engenharia Informática", "admin@mail.com");
            Publication pub2  = publicationBean.create("Análise Estrutural de Pontes", "Engenharia Civil", "colab@mail.com");
            Publication pub3  = publicationBean.create("Sistemas Distribuídos", "Engenharia Informática", "teste@mail.com");
            Publication pub4  = publicationBean.create("Gestão de Obras Públicas", "Engenharia Civil", "resp@mail.com");
            Publication pub5  = publicationBean.create("Bases de Dados Relacionais", "Engenharia Informática", "duarte@mail.com");
            Publication pub6  = publicationBean.create("Planeamento Urbano e Mobilidade", "Engenharia Civil", "colab@mail.com");
            Publication pub7  = publicationBean.create("Desenvolvimento de APIs REST", "Engenharia Informática", "teste@mail.com");
            Publication pub8  = publicationBean.create("Estruturas de Betão Armado", "Engenharia Civil", "resp@mail.com");
            Publication pub9  = publicationBean.create("Arquiteturas de Software", "Engenharia Informática", "duarte@mail.com");
            Publication pub10 = publicationBean.create("Reabilitação de Edifícios Antigos", "Engenharia Civil", "colab@mail.com");
            Publication pub11 = publicationBean.create("Segurança Aplicacional", "Engenharia Informática", "teste@mail.com");
            Publication pub12 = publicationBean.create("Impacto Ambiental de Infraestruturas", "Engenharia Civil", "resp@mail.com");
            Publication pub13 = publicationBean.create("Engenharia de Requisitos", "Engenharia Informática", "duarte@mail.com");
            Publication pub14 = publicationBean.create("Topografia e Cartografia Digital", "Engenharia Civil", "colab@mail.com");
            Publication pub15 = publicationBean.create("Programação Orientada a Objetos", "Engenharia Informática", "teste@mail.com");
            Publication pub16 = publicationBean.create("Dimensionamento de Estruturas Metálicas", "Engenharia Civil", "resp@mail.com");
            Publication pub17 = publicationBean.create("Testes e Qualidade de Software", "Engenharia Informática", "duarte@mail.com");
            Publication pub18 = publicationBean.create("Avaliação de Riscos em Construção", "Engenharia Civil", "colab@mail.com");
            Publication pub19 = publicationBean.create("Engenharia de Software Avançada", "Engenharia Informática", "teste@mail.com");
            Publication pub20 = publicationBean.create("Tecnologias BIM na Construção", "Engenharia Civil", "resp@mail.com");
            Publication pub21 = publicationBean.create("Integração Contínua e DevOps", "Engenharia Informática", "duarte@mail.com");
            Publication pub22 = publicationBean.create("Infraestruturas de Transportes", "Engenharia Civil", "colab@mail.com");
            Publication pub23 = publicationBean.create("Segurança em Sistemas de Informação", "Engenharia Informática", "teste@mail.com");
            Publication pub24 = publicationBean.create("Planeamento e Controlo de Custos", "Engenharia Civil", "resp@mail.com");
            Publication pub25 = publicationBean.create("Computação em Nuvem", "Engenharia Informática", "duarte@mail.com");
            Publication pub26 = publicationBean.create("Engenharia Sísmica", "Engenharia Civil", "colab@mail.com");
            Publication pub27 = publicationBean.create("Inteligência Artificial Aplicada", "Engenharia Informática", "teste@mail.com");
            Publication pub28 = publicationBean.create("Fiscalização de Obras", "Engenharia Civil", "resp@mail.com");
            Publication pub29 = publicationBean.create("Análise e Projeto de Algoritmos", "Engenharia Informática", "duarte@mail.com");
            Publication pub30 = publicationBean.create("Infraestruturas Hidráulicas", "Engenharia Civil", "colab@mail.com");
            Publication pub31 = publicationBean.create("Arquitetura de Computadores", "Engenharia Informática", "teste@mail.com");
            Publication pub32 = publicationBean.create("Modelo CNN para deteção de tumores", "Modelo Tumores", "tester@colaborador.pt");

            Publication pub33 = publicationBean.create("TESTE", "testeteste", "duarte@mail.com");

            commentBean.create(pub2, "teste@mail.com", "Este comentário foi criado no config");
            commentBean.create(pub2, "teste@mail.com", "Outro comentário");
            commentBean.create(pub5, "colab@mail.com", "Outro comentário");

            ratingBean.giveRating(1L, "2", 4);
            ratingBean.giveRating(2L, "4", 5);

            tagBean.create("Java");
            tagBean.create("Backend");
            tagBean.create("Frontend");
            tagBean.create("Bases de Dados");
            tagBean.create("Engenharia");
            tagBean.create("DAE");
            tagBean.create("REST");

            TagDTO tags = new TagDTO();
            ArrayList<Long> tagsIds = new ArrayList<>();
            tagsIds.add(1L);
            tagsIds.add(2L);
            tags.setTags(tagsIds);
            tagBean.associateTagToPublication(tags, 1);
            tagBean.associateTagToPublication(tags, 25);
            tagBean.associateTagToPublication(tags, 29);


            System.out.println("Users populated successfully!");





//////////////////////teste US08/////////////////////////////////////////////////////
            publicationBean.seedHistory(pub33.getId(), "duarte@mail.com",
                    java.util.Map.of("summary", "Resumo corrigido pelo autor."));

            publicationBean.seedHistory(pub33.getId(), "duarte@mail.com",
                    java.util.Map.of("title", "Introdução à Programação em Java - Versão Revista"));


            java.util.List<java.util.Map<String, Object>> tagsHistory = java.util.List.of(
                    java.util.Map.of("id", 33L, "name", "Java"),
                    java.util.Map.of("id", 33L, "name", "Backend")
            );

            publicationBean.seedHistory(pub33.getId(), "duarte@mail.com",
                    java.util.Map.of("tags", tagsHistory));


            publicationBean.seedHistory(pub33.getId(), "duarte@mail.com",
                    java.util.Map.of("visible", false));

            publicationBean.seedHistory(pub33.getId(), "duarte@mail.com",
                    java.util.Map.of("visible", true));






        } catch (Exception e) {
            System.err.println("Error populating DB: " + e.getMessage());
        }
    }
}