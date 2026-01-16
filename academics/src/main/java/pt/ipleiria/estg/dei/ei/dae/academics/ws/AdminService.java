package pt.ipleiria.estg.dei.ei.dae.academics.ws;

import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.EJB;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import pt.ipleiria.estg.dei.ei.dae.academics.dtos.CommentDTO;
import pt.ipleiria.estg.dei.ei.dae.academics.dtos.PublicationDTO;
import pt.ipleiria.estg.dei.ei.dae.academics.dtos.TagDTO;
import pt.ipleiria.estg.dei.ei.dae.academics.ejbs.CommentBean;
import pt.ipleiria.estg.dei.ei.dae.academics.ejbs.PublicationBean;
import pt.ipleiria.estg.dei.ei.dae.academics.ejbs.TagBean;
import pt.ipleiria.estg.dei.ei.dae.academics.entities.Comment;
import pt.ipleiria.estg.dei.ei.dae.academics.entities.Publication;
import pt.ipleiria.estg.dei.ei.dae.academics.entities.Tag;
import pt.ipleiria.estg.dei.ei.dae.academics.security.Authenticated;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Path("admin")
@Produces({MediaType.APPLICATION_JSON})
@Consumes({MediaType.APPLICATION_JSON})
@Authenticated
public class AdminService {

    @EJB
    private PublicationBean publicationBean;

    @EJB
    private CommentBean commentBean;

    @EJB
    private TagBean tagBean;

    // EP21 - Consultar informação oculta
    @GET
    @Path("hidden")
    @RolesAllowed({"Responsavel", "Administrador"})
    public Response getHiddenContent(
            @QueryParam("type") String type,
            @QueryParam("page") @DefaultValue("1") int page,
            @QueryParam("limit") @DefaultValue("10") int limit) {

        // Validate pagination parameters
        if (page < 1 || limit < 1 || limit > 100) {
            throw new IllegalArgumentException("Parâmetros de paginação inválidos. Page >= 1, Limit entre 1 e 100.");
        }

        // Validate type parameter
        if (type == null || type.isBlank()) {
            throw new IllegalArgumentException("O parâmetro 'type' é obrigatório.");
        }

        Map<String, Object> response = new HashMap<>();
        response.put("page", page);
        response.put("limit", limit);

        switch (type.toLowerCase()) {
            case "publications":
                List<Publication> publications = publicationBean.getHiddenPublications(page, limit);
                long totalPublications = publicationBean.countHiddenPublications();
                
                List<PublicationDTO> publicationDTOs = publications.stream()
                        .map(PublicationDTO::from)
                        .collect(Collectors.toList());
                
                response.put("type", "publications");
                response.put("total", totalPublications);
                response.put("data", publicationDTOs);
                break;

            case "comments":
                List<Comment> comments = commentBean.getHiddenComments(page, limit);
                long totalComments = commentBean.countHiddenComments();
                
                List<CommentDTO> commentDTOs = comments.stream()
                        .map(CommentDTO::from)
                        .collect(Collectors.toList());
                
                response.put("type", "comments");
                response.put("total", totalComments);
                response.put("data", commentDTOs);
                break;

            case "tags":
                List<Tag> tags = tagBean.getHiddenTags(page, limit);
                long totalTags = tagBean.countHiddenTags();
                
                List<TagDTO> tagDTOs = tags.stream()
                        .map(TagDTO::from)
                        .collect(Collectors.toList());
                
                response.put("type", "tags");
                response.put("total", totalTags);
                response.put("data", tagDTOs);
                break;

            default:
                throw new IllegalArgumentException("Tipo inválido. Use 'publications', 'comments', ou 'tags'.");
        }

        return Response.ok(response).build();
    }
}
