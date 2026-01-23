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




            Publication pub1  = publicationBean.create("Introdução à Programação em Java", "Software (open source)", "admin@mail.com");
            Publication pub2  = publicationBean.create("Análise Estrutural de Pontes", "Technical reports", "colab@mail.com");
            Publication pub3  = publicationBean.create("Sistemas Distribuídos", "Peer-reviewed scientific articles", "teste@mail.com");
            Publication pub4  = publicationBean.create("Gestão de Obras Públicas", "Conference proceedings", "resp@mail.com");
            Publication pub5  = publicationBean.create("Bases de Dados Relacionais", "Databases", "duarte@mail.com");
            Publication pub6  = publicationBean.create("Planeamento Urbano e Mobilidade", "Technical reports", "colab@mail.com");
            Publication pub7  = publicationBean.create("Desenvolvimento de APIs REST", "Software (open source)", "teste@mail.com");
            Publication pub8  = publicationBean.create("Estruturas de Betão Armado", "Book chapters or scientific books", "resp@mail.com");
            Publication pub9  = publicationBean.create("Arquiteturas de Software", "Peer-reviewed scientific articles", "duarte@mail.com");
            Publication pub10 = publicationBean.create("Reabilitação de Edifícios Antigos", "Conference proceedings", "colab@mail.com");
            Publication pub11 = publicationBean.create("Segurança Aplicacional", "Peer-reviewed scientific articles", "teste@mail.com");
            Publication pub12 = publicationBean.create("Impacto Ambiental de Infraestruturas", "Scientific data (datasets)", "resp@mail.com");
            Publication pub13 = publicationBean.create("Engenharia de Requisitos", "Book chapters or scientific books", "duarte@mail.com");
            Publication pub14 = publicationBean.create("Topografia e Cartografia Digital", "Scientific data (datasets)", "colab@mail.com");
            Publication pub15 = publicationBean.create("Programação Orientada a Objetos", "Software (open source)", "teste@mail.com");
            Publication pub16 = publicationBean.create("Dimensionamento de Estruturas Metálicas", "Technical reports", "resp@mail.com");
            Publication pub17 = publicationBean.create("Testes e Qualidade de Software", "Conference proceedings", "duarte@mail.com");
            Publication pub18 = publicationBean.create("Avaliação de Riscos em Construção", "Technical reports", "colab@mail.com");
            Publication pub19 = publicationBean.create("Engenharia de Software Avançada", "Peer-reviewed scientific articles", "teste@mail.com");
            Publication pub20 = publicationBean.create("Tecnologias BIM na Construção", "Software (open source)", "resp@mail.com");
            Publication pub21 = publicationBean.create("Integração Contínua e DevOps", "Conference proceedings", "duarte@mail.com");
            Publication pub22 = publicationBean.create("Infraestruturas de Transportes", "Technical reports", "colab@mail.com");
            Publication pub23 = publicationBean.create("Segurança em Sistemas de Informação", "Peer-reviewed scientific articles", "teste@mail.com");
            Publication pub24 = publicationBean.create("Planeamento e Controlo de Custos", "Book chapters or scientific books", "resp@mail.com");
            Publication pub25 = publicationBean.create("Computação em Nuvem", "Software (open source)", "duarte@mail.com");
            Publication pub26 = publicationBean.create("Engenharia Sísmica", "Scientific data (datasets)", "colab@mail.com");
            Publication pub27 = publicationBean.create("Inteligência Artificial Aplicada", "AI models", "teste@mail.com");
            Publication pub28 = publicationBean.create("Fiscalização de Obras", "Technical reports", "resp@mail.com");
            Publication pub29 = publicationBean.create("Análise e Projeto de Algoritmos", "Peer-reviewed scientific articles", "duarte@mail.com");
            Publication pub30 = publicationBean.create("Infraestruturas Hidráulicas", "Technical reports", "colab@mail.com");
            Publication pub31 = publicationBean.create("Arquitetura de Computadores", "Book chapters or scientific books", "teste@mail.com");
            Publication pub32 = publicationBean.create("Modelo CNN para deteção de tumores", "AI models", "tester@colaborador.pt");

            Publication pub33 = publicationBean.create("TESTE", "Master's or doctoral theses", "duarte@mail.com");
            
            // Publicação mais antiga para testar filtro por data
            Publication pubOld = publicationBean.create("Publicação Antiga para Teste", "Peer-reviewed scientific articles", "admin@mail.com");
            // Definir data para 15 de janeiro de 2026 (8 dias atrás)
            java.util.Calendar cal = java.util.Calendar.getInstance();
            cal.set(2026, java.util.Calendar.JANUARY, 15, 10, 0, 0);
            cal.set(java.util.Calendar.MILLISECOND, 0);
            pubOld.setCreatedAt(new java.sql.Timestamp(cal.getTimeInMillis()));
            pubOld.setUpdatedAt(new java.sql.Timestamp(cal.getTimeInMillis()));

            commentBean.create(pub2, "2", "Este comentário foi criado no config");
            commentBean.create(pub2, "2", "Outro comentário");
            commentBean.create(pub5, "5", "Outro comentário");

            ratingBean.giveRating(1L, "2", 4);
            ratingBean.giveRating(2L, "4", 5);

            tagBean.create("Java", "1");
            tagBean.create("Backend", "1");
            tagBean.create("Frontend", "1");
            tagBean.create("Database", "1");
            tagBean.create("Engineering", "1");
            tagBean.create("DAE", "1");
            tagBean.create("REST", "1");

            TagDTO tags = new TagDTO();
            ArrayList<Long> tagsIds = new ArrayList<>();
            tagsIds.add(1L);
            tagsIds.add(2L);
            tags.setTags(tagsIds);
            tagBean.associateTagToPublication(tags, 1, "1");
            tagBean.associateTagToPublication(tags, 25, "1");
            tagBean.associateTagToPublication(tags, 29, "1");

            tagBean.subscribeUserToTag("7",1);

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