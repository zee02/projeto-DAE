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

            // ==================== DADOS ADICIONAIS ====================
            
            // Novos utilizadores
            responsibleBean.create("123", "Maria Santos", "maria.resp@mail.com");
            responsibleBean.create("123", "Fernando Costa", "fernando@mail.com");
            collaboratorBean.create("123", "Rita Gonçalves", "rita@mail.com");
            collaboratorBean.create("123", "André Ferreira", "andre@mail.com");
            collaboratorBean.create("123", "Catarina Dias", "catarina@mail.com");
            collaboratorBean.create("123", "Tiago Marques", "tiago@mail.com");
            collaboratorBean.create("123", "Inês Ribeiro", "ines@mail.com");
            collaboratorBean.create("123", "Bruno Carvalho", "bruno@mail.com");

            // Novas tags
            tagBean.create("Python", "1");
            tagBean.create("Machine Learning", "1");
            tagBean.create("Cloud Computing", "1");
            tagBean.create("DevOps", "1");
            tagBean.create("Security", "1");
            tagBean.create("Mobile", "1");
            tagBean.create("IoT", "1");
            tagBean.create("Big Data", "1");

            // Novas publicações
            Publication pub34 = publicationBean.create("Framework React para Aplicações Empresariais", "Software (open source)", "rita@mail.com");
            Publication pub35 = publicationBean.create("Análise de Dados com Pandas e NumPy", "Scientific data (datasets)", "andre@mail.com");
            Publication pub36 = publicationBean.create("Desenvolvimento Mobile com Flutter", "Software (open source)", "catarina@mail.com");
            Publication pub37 = publicationBean.create("Redes Neurais Convolucionais para Visão Computacional", "AI models", "tiago@mail.com");
            Publication pub38 = publicationBean.create("Microserviços com Docker e Kubernetes", "Software (open source)", "ines@mail.com");
            Publication pub39 = publicationBean.create("Internet das Coisas: Protocolos e Aplicações", "Peer-reviewed scientific articles", "bruno@mail.com");
            Publication pub40 = publicationBean.create("Blockchain e Smart Contracts", "Conference proceedings", "maria.resp@mail.com");
            Publication pub41 = publicationBean.create("Natural Language Processing com Transformers", "AI models", "fernando@mail.com");
            Publication pub42 = publicationBean.create("GraphQL vs REST: Estudo Comparativo", "Peer-reviewed scientific articles", "rita@mail.com");
            Publication pub43 = publicationBean.create("Padrões de Arquitetura para Sistemas Escaláveis", "Book chapters or scientific books", "admin@mail.com");
            Publication pub44 = publicationBean.create("Tese: Deep Learning para Processamento de Imagem", "Master's or doctoral theses", "teste@mail.com");
            Publication pub45 = publicationBean.create("Automação de Testes com Selenium e Cypress", "Software (open source)", "colab@mail.com");
            Publication pub46 = publicationBean.create("Big Data Analytics com Apache Spark", "Scientific data (datasets)", "resp@mail.com");
            Publication pub47 = publicationBean.create("Energias Renováveis e Sustentabilidade", "Technical reports", "duarte@mail.com");
            Publication pub48 = publicationBean.create("Gestão de Projetos com Metodologias Ágeis", "Conference proceedings", "andre@mail.com");
            Publication pub49 = publicationBean.create("Patente: Sistema de Monitorização Ambiental IoT", "Patents", "catarina@mail.com");
            Publication pub50 = publicationBean.create("Divulgação Científica: IA no Quotidiano", "Scientific outreach articles", "tiago@mail.com");

            // Novos comentários
            commentBean.create(pub1, "7", "Excelente introdução ao Java! Muito bem estruturado.");
            commentBean.create(pub1, "8", "Gostei muito dos exemplos práticos.");
            commentBean.create(pub3, "6", "Ótima abordagem sobre sistemas distribuídos.");
            commentBean.create(pub7, "5", "Spring Boot é essencial hoje em dia!");
            commentBean.create(pub11, "4", "Segurança é fundamental em qualquer sistema.");
            commentBean.create(pub15, "3", "Python é uma linguagem fantástica para OOP.");
            commentBean.create(pub21, "7", "DevOps mudou completamente a forma de trabalhar.");
            commentBean.create(pub25, "8", "Cloud computing é o futuro!");
            commentBean.create(pub27, "6", "IA está a revolucionar a medicina.");
            commentBean.create(pub27, "5", "Resultados impressionantes nos testes.");
            commentBean.create(pub32, "4", "Modelo CNN muito preciso!");
            commentBean.create(pub34, "3", "React é fantástico para frontend.");
            commentBean.create(pub37, "7", "Redes neurais explicadas de forma acessível.");
            commentBean.create(pub38, "8", "Docker simplifica muito o deployment.");
            commentBean.create(pub41, "6", "Transformers revolucionaram o NLP.");
            commentBean.create(pub44, "5", "Tese muito bem elaborada.");
            commentBean.create(pub44, "4", "Metodologia robusta e bem fundamentada.");

            // Novos ratings
            ratingBean.giveRating(pub1.getId(), "7", 5);
            ratingBean.giveRating(pub1.getId(), "8", 5);
            ratingBean.giveRating(pub1.getId(), "6", 4);
            ratingBean.giveRating(pub3.getId(), "7", 4);
            ratingBean.giveRating(pub3.getId(), "5", 5);
            ratingBean.giveRating(pub5.getId(), "4", 5);
            ratingBean.giveRating(pub5.getId(), "3", 4);
            ratingBean.giveRating(pub7.getId(), "8", 5);
            ratingBean.giveRating(pub7.getId(), "6", 5);
            ratingBean.giveRating(pub7.getId(), "5", 4);
            ratingBean.giveRating(pub11.getId(), "4", 4);
            ratingBean.giveRating(pub11.getId(), "3", 5);
            ratingBean.giveRating(pub15.getId(), "7", 5);
            ratingBean.giveRating(pub15.getId(), "8", 5);
            ratingBean.giveRating(pub21.getId(), "6", 4);
            ratingBean.giveRating(pub21.getId(), "5", 5);
            ratingBean.giveRating(pub25.getId(), "4", 5);
            ratingBean.giveRating(pub25.getId(), "3", 4);
            ratingBean.giveRating(pub25.getId(), "7", 5);
            ratingBean.giveRating(pub27.getId(), "8", 5);
            ratingBean.giveRating(pub27.getId(), "6", 5);
            ratingBean.giveRating(pub27.getId(), "5", 4);
            ratingBean.giveRating(pub32.getId(), "4", 5);
            ratingBean.giveRating(pub32.getId(), "3", 5);
            ratingBean.giveRating(pub34.getId(), "7", 5);
            ratingBean.giveRating(pub34.getId(), "8", 4);
            ratingBean.giveRating(pub37.getId(), "6", 4);
            ratingBean.giveRating(pub37.getId(), "5", 5);
            ratingBean.giveRating(pub38.getId(), "4", 5);
            ratingBean.giveRating(pub38.getId(), "3", 5);
            ratingBean.giveRating(pub41.getId(), "7", 5);
            ratingBean.giveRating(pub41.getId(), "8", 5);
            ratingBean.giveRating(pub44.getId(), "6", 5);
            ratingBean.giveRating(pub44.getId(), "5", 5);
            ratingBean.giveRating(pub44.getId(), "4", 4);

            // Associar novas tags a publicações
            TagDTO newTags = new TagDTO();
            ArrayList<Long> newTagsIds = new ArrayList<>();
            
            // Python + Machine Learning
            newTagsIds.clear();
            newTagsIds.add(8L);  // Python
            newTagsIds.add(9L);  // Machine Learning
            newTags.setTags(new ArrayList<>(newTagsIds));
            tagBean.associateTagToPublication(newTags, pub35.getId(), "1");
            tagBean.associateTagToPublication(newTags, pub37.getId(), "1");
            tagBean.associateTagToPublication(newTags, pub41.getId(), "1");
            
            // Cloud + DevOps
            newTagsIds.clear();
            newTagsIds.add(10L); // Cloud Computing
            newTagsIds.add(11L); // DevOps
            newTags.setTags(new ArrayList<>(newTagsIds));
            tagBean.associateTagToPublication(newTags, pub38.getId(), "1");
            tagBean.associateTagToPublication(newTags, pub21.getId(), "1");
            
            // Security
            newTagsIds.clear();
            newTagsIds.add(12L); // Security
            newTags.setTags(new ArrayList<>(newTagsIds));
            tagBean.associateTagToPublication(newTags, pub11.getId(), "1");
            tagBean.associateTagToPublication(newTags, pub23.getId(), "1");
            
            // Mobile
            newTagsIds.clear();
            newTagsIds.add(13L); // Mobile
            newTags.setTags(new ArrayList<>(newTagsIds));
            tagBean.associateTagToPublication(newTags, pub36.getId(), "1");
            
            // IoT
            newTagsIds.clear();
            newTagsIds.add(14L); // IoT
            newTags.setTags(new ArrayList<>(newTagsIds));
            tagBean.associateTagToPublication(newTags, pub39.getId(), "1");
            tagBean.associateTagToPublication(newTags, pub49.getId(), "1");
            
            // Big Data
            newTagsIds.clear();
            newTagsIds.add(15L); // Big Data
            newTags.setTags(new ArrayList<>(newTagsIds));
            tagBean.associateTagToPublication(newTags, pub46.getId(), "1");

            // Subscrições de tags
            tagBean.subscribeUserToTag("8", 8);   // Python
            tagBean.subscribeUserToTag("8", 9);   // Machine Learning
            tagBean.subscribeUserToTag("6", 10);  // Cloud Computing
            tagBean.subscribeUserToTag("5", 12);  // Security

            System.out.println("✅ Additional test data created successfully!");

        } catch (Exception e) {
            System.err.println("Error populating DB: " + e.getMessage());
        }
    }
}