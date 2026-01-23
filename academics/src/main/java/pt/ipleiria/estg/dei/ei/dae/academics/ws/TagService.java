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
import pt.ipleiria.estg.dei.ei.dae.academics.ejbs.UserBean;
import pt.ipleiria.estg.dei.ei.dae.academics.entities.Tag;
import pt.ipleiria.estg.dei.ei.dae.academics.entities.User;
import pt.ipleiria.estg.dei.ei.dae.academics.exceptions.MyEntityNotFoundException;
import pt.ipleiria.estg.dei.ei.dae.academics.security.Authenticated;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Path("tags")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class TagService {

    @EJB
    private TagBean tagBean;

    @EJB
    private UserBean userBean;

    @Context
    private SecurityContext securityContext;

    // EP32 - Consultar Todas as tags
    @GET
    public List<TagDTO> getAllTags() {
        return TagDTO.from(tagBean.findAll());
    }

    // EP16 - Criar uma nova tag
    @POST
    @Authenticated
    @RolesAllowed({"Manager", "Administrator"})
    public Response createTag(@Valid TagDTO dto) {
        String userId = securityContext.getUserPrincipal().getName();
        Tag newTag = tagBean.create(dto.getName(), userId);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Tag criada com sucesso");
        response.put("tag", TagDTO.from(newTag));

        return Response.status(Response.Status.CREATED).entity(response).build();
    }

    // EP11 - Subscrever uma tag
    @POST
    @Path("/{tag_id}/subscribe")
    @Authenticated
    @RolesAllowed({"Collaborator", "Manager", "Administrator"})
    public Response subscribeToTag(@PathParam("tag_id") long tagId) throws MyEntityNotFoundException {
        String userId = securityContext.getUserPrincipal().getName();

        Tag tag = tagBean.subscribeUserToTag(userId, tagId);
        User user = userBean.find(userId);
        
        // Initialize lazy collection before converting to DTO
        org.hibernate.Hibernate.initialize(user.getSubscribedTags());

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Tag subscrita com sucesso");
        response.put("tag", TagDTO.from(tag));
        response.put("user", UserDTO.from(user));

        return Response.ok(response).build();
    }

    // Cancelar subscrição de uma tag
    @DELETE
    @Path("/{tag_id}/subscribe")
    @Authenticated
    @RolesAllowed({"Collaborator", "Manager", "Administrator"})
    public Response unsubscribeFromTag(@PathParam("tag_id") long tagId) throws MyEntityNotFoundException {
        String userId = securityContext.getUserPrincipal().getName();

        Tag tag = tagBean.unsubscribeUserFromTag(userId, tagId);
        User user = userBean.find(userId);
        
        // Initialize lazy collection before converting to DTO
        org.hibernate.Hibernate.initialize(user.getSubscribedTags());

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Subscrição da tag cancelada com sucesso");
        response.put("tag", TagDTO.from(tag));
        response.put("user", UserDTO.from(user));

        return Response.ok(response).build();
    }

    // EP17 - Eliminar tag
    @DELETE
    @Path("/{tag_id}")
    @Authenticated
    @RolesAllowed({"Manager", "Administrator"})
    public Response deleteTag(@PathParam("tag_id") long tagId) throws MyEntityNotFoundException {
        String userId = securityContext.getUserPrincipal().getName();
        tagBean.delete(tagId, userId);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Tag eliminada com sucesso");

        return Response.ok(response).build();
    }

    // Ocultar ou mostrar tag
    @PUT
    @Path("/{tag_id}/visibility")
    @Authenticated
    @RolesAllowed({"Manager", "Administrator"})
    public Response updateVisibility(@PathParam("tag_id") long tagId, @Valid VisibilityDTO dto) throws MyEntityNotFoundException {
        String userId = securityContext.getUserPrincipal().getName();
        Tag tag = tagBean.updateVisibility(tagId, dto.getVisible(), userId);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Visibilidade da tag atualizada com sucesso");
        response.put("tag_id", tag.getId());
        response.put("visible", tag.isVisible());

        return Response.ok(response).build();
    }
}