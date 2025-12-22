package pt.ipleiria.estg.dei.ei.dae.academics.ws;

import jakarta.ejb.EJB;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import pt.ipleiria.estg.dei.ei.dae.academics.dtos.PublicationDTO;
import pt.ipleiria.estg.dei.ei.dae.academics.ejbs.PublicationBean;
import pt.ipleiria.estg.dei.ei.dae.academics.entities.Publication;
import pt.ipleiria.estg.dei.ei.dae.academics.security.Authenticated;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

@Path("posts")
@Produces(MediaType.APPLICATION_JSON)
public class PublicationService {
    @EJB
    private PublicationBean publicationBean;

    @Context
    private SecurityContext securityContext;

    // EP06 - Consultar Públicas
    @GET
    public List<PublicationDTO> getAllPublicPosts() {
        return PublicationDTO.from(publicationBean.getAllPublic());
    }

    // EP01 - Criar Publicação (Multipart)
    @POST
    @Authenticated
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response createPost(MultipartFormDataInput input) throws IOException {
        String email = securityContext.getUserPrincipal().getName();

        Map<String, List<InputPart>> formParts = input.getFormDataMap();

        // Extrair campos simples
        String title = formParts.get("title").get(0).getBodyAsString();
        String area = formParts.get("scientific_area").get(0).getBodyAsString();

        // Resumo é opcional (pode ser gerado por IA depois)
        String summary = "";
        if (formParts.containsKey("summary")) {
            summary = formParts.get("summary").get(0).getBodyAsString();
        }

        // Extrair Ficheiro
        List<InputPart> fileParts = formParts.get("file");
        if (fileParts == null || fileParts.isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST).entity("File is required").build();
        }
        InputPart filePart = fileParts.get(0);

        // Obter nome do ficheiro (Header Parsing simplificado)
        String fileName = getFileName(filePart.getHeaders());
        InputStream inputStream = filePart.getBody(InputStream.class, null);

        // Criar no Bean
        Publication newPost = publicationBean.create(title, area, summary, email, inputStream, fileName);

        return Response.status(Response.Status.CREATED).entity(PublicationDTO.from(newPost)).build();
    }

    // Helper para sacar o nome do ficheiro
    private String getFileName(MultivaluedMap<String, String> headers) {
        String[] contentDisposition = headers.getFirst("Content-Disposition").split(";");
        for (String filename : contentDisposition) {
            if ((filename.trim().startsWith("filename"))) {
                String[] name = filename.split("=");
                return name[1].trim().replaceAll("\"", "");
            }
        }
        return "unknown";
    }
}