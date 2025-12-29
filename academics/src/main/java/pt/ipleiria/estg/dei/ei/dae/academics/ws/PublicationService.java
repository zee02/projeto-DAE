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
import pt.ipleiria.estg.dei.ei.dae.academics.utils.PublicationUtils;

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
    @Produces(MediaType.APPLICATION_JSON)
    public Response createPost(MultipartFormDataInput input) throws IOException {

        String email = securityContext.getUserPrincipal().getName();

        Publication publication = publicationBean.create(input, email);

        return Response.status(Response.Status.CREATED).entity(PublicationDTO.from(publication)).build();
    }


}