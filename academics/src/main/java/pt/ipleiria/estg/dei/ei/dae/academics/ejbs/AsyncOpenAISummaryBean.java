package pt.ipleiria.estg.dei.ei.dae.academics.ejbs;

import jakarta.ejb.Asynchronous;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import pt.ipleiria.estg.dei.ei.dae.academics.entities.Publication;

import java.io.File;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.util.Date;
import java.util.logging.Logger;

@Stateless
public class AsyncOpenAISummaryBean {
    private static final Logger logger = Logger.getLogger(AsyncOpenAISummaryBean.class.getName());
    private static final String UPLOAD_DIR = "/app/uploads";

    @PersistenceContext
    private EntityManager em;

    @EJB
    private OpenAISummaryBean openAISummaryBean;

    /**
     * Gera resumo de forma assíncrona usando OpenAI
     * Não bloqueia o request HTTP - executa em background
     */
    @Asynchronous
    public void generateSummaryAsync(Long publicationId, String fileKey) {
        logger.info("🤖 [ASYNC] Iniciando geração de resumo com OpenAI para publicação ID: " + publicationId);

        try {
            Publication publication = em.find(Publication.class, publicationId);

            if (publication == null) {
                logger.warning("❌ Publicação não encontrada: " + publicationId);
                return;
            }

            File pdfFile = Paths.get(UPLOAD_DIR, fileKey).toFile();

            if (!pdfFile.exists()) {
                logger.warning("❌ Ficheiro PDF não encontrado: " + fileKey);
                publication.setSummary("Resumo não disponível (ficheiro não encontrado)");
                publication.setUpdatedAt(new Timestamp(new Date().getTime()));
                em.merge(publication);
                return;
            }

            logger.info("📄 Processando PDF com OpenAI...");
            String summary = openAISummaryBean.generateSummaryFromPDF(pdfFile);

            // Atualizar publicação com o resumo gerado
            publication.setSummary(summary);
            publication.setUpdatedAt(new Timestamp(new Date().getTime()));
            em.merge(publication);

            logger.info("✅ [ASYNC] Resumo gerado e guardado para publicação ID: " + publicationId);

        } catch (Exception e) {
            logger.severe("❌ [ASYNC] Erro ao gerar resumo: " + e.getMessage());
            e.printStackTrace();

            try {
                Publication publication = em.find(Publication.class, publicationId);
                if (publication != null) {
                    publication.setSummary("Resumo não disponível (erro na geração)");
                    publication.setUpdatedAt(new Timestamp(new Date().getTime()));
                    em.merge(publication);
                }
            } catch (Exception ex) {
                logger.severe("Erro ao atualizar publicação: " + ex.getMessage());
            }
        }
    }
}

