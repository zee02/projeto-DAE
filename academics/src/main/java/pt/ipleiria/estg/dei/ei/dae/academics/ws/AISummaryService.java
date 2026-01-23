package pt.ipleiria.estg.dei.ei.dae.academics.ws;

import jakarta.ejb.EJB;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import pt.ipleiria.estg.dei.ei.dae.academics.ejbs.AsyncOpenAISummaryBean;
import pt.ipleiria.estg.dei.ei.dae.academics.ejbs.OpenAISummaryBean;
import pt.ipleiria.estg.dei.ei.dae.academics.ejbs.PublicationBean;
import pt.ipleiria.estg.dei.ei.dae.academics.entities.Publication;
import pt.ipleiria.estg.dei.ei.dae.academics.security.Authenticated;

import java.io.File;
import java.nio.file.Paths;
import java.util.logging.Logger;

@Path("ai-summary")
@Produces({MediaType.APPLICATION_JSON})
@Consumes({MediaType.APPLICATION_JSON})
public class AISummaryService {
    private static final Logger logger = Logger.getLogger(AISummaryService.class.getName());
    private static final String UPLOAD_DIR = "/app/uploads";

    @EJB
    private PublicationBean publicationBean;

    @EJB
    private OpenAISummaryBean openAISummaryBean;

    @EJB
    private AsyncOpenAISummaryBean asyncOpenAISummaryBean;

    /**
     * Endpoint para regenerar resumo de uma publicação
     * POST /api/ai-summary/regenerate/{publicationId}
     */
    @POST
    @Path("regenerate/{id}")
    @Authenticated
    public Response regenerateSummary(@PathParam("id") Long publicationId,
                                     @QueryParam("async") @DefaultValue("true") boolean async) {
        try {
            Publication publication = publicationBean.findWithTags(publicationId);

            if (publication == null) {
                return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"error\":\"Publicação não encontrada\"}")
                    .build();
            }

            String fileKey = publication.getFileKey();

            if (fileKey == null || fileKey.isBlank()) {
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\":\"Publicação não tem ficheiro PDF\"}")
                    .build();
            }

            File pdfFile = Paths.get(UPLOAD_DIR, fileKey).toFile();

            if (!pdfFile.exists()) {
                return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"error\":\"Ficheiro PDF não encontrado\"}")
                    .build();
            }

            if (async) {
                // Modo assíncrono (recomendado) - resposta imediata
                asyncOpenAISummaryBean.generateSummaryAsync(publicationId, fileKey);
                return Response.ok()
                    .entity("{\"message\":\"Resumo está a ser gerado em background. Aguarde alguns segundos e atualize a página.\"}")
                    .build();
            } else {
                // Modo síncrono - espera pela geração (pode demorar)
                String summary = openAISummaryBean.generateSummaryFromPDF(pdfFile);
                return Response.ok()
                    .entity("{\"summary\":\"" + escapeJson(summary) + "\"}")
                    .build();
            }

        } catch (Exception e) {
            logger.severe("Erro ao regenerar resumo: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("{\"error\":\"Erro ao gerar resumo: " + e.getMessage() + "\"}")
                .build();
        }
    }

    /**
     * Endpoint para verificar se a API key do OpenAI está configurada
     * GET /api/ai-summary/check
     */
    @GET
    @Path("check")
    public Response checkApiKey() {
        String apiKey = System.getenv("OPENAI_API_KEY");
        boolean configured = apiKey != null && !apiKey.isBlank();

        return Response.ok()
            .entity("{\"configured\":" + configured +
                   ",\"message\":\"" + (configured ? "OpenAI API configurada" : "Configure OPENAI_API_KEY") + "\"}")
            .build();
    }

    private String escapeJson(String text) {
        if (text == null) return "";
        return text
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t");
    }
}

