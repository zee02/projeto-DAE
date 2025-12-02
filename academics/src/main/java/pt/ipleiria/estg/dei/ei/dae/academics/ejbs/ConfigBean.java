package pt.ipleiria.estg.dei.ei.dae.academics.ejbs;

import jakarta.annotation.PostConstruct;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;
import jakarta.ejb.EJB;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import pt.ipleiria.estg.dei.ei.dae.academics.entities.Administrador;
import pt.ipleiria.estg.dei.ei.dae.academics.entities.Colaborador;
import pt.ipleiria.estg.dei.ei.dae.academics.entities.Responsavel;
import pt.ipleiria.estg.dei.ei.dae.academics.security.Hasher;
import java.util.logging.Logger;

@Singleton
@Startup
public class ConfigBean {

    private static final Logger logger = Logger.getLogger(ConfigBean.class.getName());

    @PersistenceContext(unitName = "AcademicsPersistenceUnit")
    private EntityManager em;

    @EJB
    private UserBean userBean;

    private Hasher hasher = new Hasher();

    @PostConstruct
    public void init() {
        logger.info("Inicializando dados do sistema...");

        try {
            // Verificar se o sistema já foi inicializado
            if (userBean.find("admin") != null) {
                logger.info("Sistema já inicializado. Pulando configuração inicial.");
                return;
            }

            // Criar Administrador
            Administrador admin = new Administrador(
                    "admin",
                    hasher.hash("admin123"),
                    "admin@academics.pt"
            );
            em.persist(admin);
            logger.info("Administrador criado: admin");

            // Criar Responsável
            Responsavel responsavel = new Responsavel(
                    "responsavel",
                    hasher.hash("resp123"),
                    "responsavel@academics.pt"
            );
            em.persist(responsavel);
            logger.info("Responsável criado: responsavel");

            // Criar Colaborador
            Colaborador colaborador = new Colaborador(
                    "colaborador",
                    hasher.hash("collab123"),
                    "colaborador@academics.pt"
            );
            em.persist(colaborador);
            logger.info("Colaborador criado: colaborador");

            // Commit automático da transação
            logger.info("Dados iniciais carregados com sucesso!");

        } catch (Exception e) {
            logger.severe("Erro ao inicializar dados do sistema: " + e.getMessage());
            e.printStackTrace();
        }
    }
}