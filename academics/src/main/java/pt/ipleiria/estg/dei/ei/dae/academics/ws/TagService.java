package pt.ipleiria.estg.dei.ei.dae.academics.ws;

import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.EJB;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import pt.ipleiria.estg.dei.ei.dae.academics.dtos.*;
import pt.ipleiria.estg.dei.ei.dae.academics.ejbs.TagBean;
import pt.ipleiria.estg.dei.ei.dae.academics.entities.Tag;
import pt.ipleiria.estg.dei.ei.dae.academics.security.Authenticated;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Path("tags")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Authenticated
public class TagService {

    @EJB
    private TagBean tagBean;

    // EP32 - Consultar Todas as tags
    @GET
    @RolesAllowed({"Administrador"})
    public List<TagDTO> getAllTags() {
        return TagDTO.from(tagBean.findAll());
    }

    // EP16 - Criar uma nova tag
    @POST
    @RolesAllowed({"Responsavel", "Administrador"})
    public Response createTag(@Valid CreateTagDTO dto) {
        Tag newTag = tagBean.create(dto.getName());

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Tag criada com sucesso");
        response.put("tag", TagDTO.from(newTag));

        return Response.status(Response.Status.CREATED).entity(response).build();
    }
}