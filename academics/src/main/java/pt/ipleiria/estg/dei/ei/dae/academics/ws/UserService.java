package pt.ipleiria.estg.dei.ei.dae.academics.ws;

import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.EJB;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import pt.ipleiria.estg.dei.ei.dae.academics.dtos.CollaboratorDTO;
import pt.ipleiria.estg.dei.ei.dae.academics.dtos.PublicationDTO;
import pt.ipleiria.estg.dei.ei.dae.academics.dtos.UserDTO;
import pt.ipleiria.estg.dei.ei.dae.academics.ejbs.*;
import pt.ipleiria.estg.dei.ei.dae.academics.entities.*;
import pt.ipleiria.estg.dei.ei.dae.academics.exceptions.MyEntityExistsException;
import pt.ipleiria.estg.dei.ei.dae.academics.security.Authenticated;
import jakarta.persistence.EntityNotFoundException;
import pt.ipleiria.estg.dei.ei.dae.academics.security.Authenticated;

import java.util.List;

@Path("users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Authenticated // Protege todos os endpoints desta classe
public class UserService {
    @EJB
    private UserBean userBean;

    @EJB
    private PublicationBean publicationBean;

    @EJB
    private CollaboratorBean collaboratorBean;


    @EJB
    private ResponsibleBean responsibleBean;


    @EJB
    private AdministratorBean administratorBean;

    @Context
    private SecurityContext securityContext;

    // EP31 - Consultar todos (Apenas Admin)
    @GET
    @RolesAllowed({"Administrador"})
    public List<UserDTO> getAllUsers() {
        return UserDTO.from(userBean.findAll());
    }

    // EP22 - Criar utilizador (Apenas Admin)
    @POST
    @RolesAllowed({"Administrador"})
    public Response createUser(@Valid UserDTO dto) {
        try {
            User newUser;

            switch (dto.getRole()) {
                case Administrador:
                    newUser = administratorBean.create(
                            dto.getPassword(),
                            dto.getName(),
                            dto.getEmail()
                    );
                    break;

                case Responsavel:
                    newUser = responsibleBean.create(
                            dto.getPassword(),
                            dto.getName(),
                            dto.getEmail()
                    );
                    break;

                case Colaborador:
                    newUser = collaboratorBean.create(
                            dto.getPassword(),
                            dto.getName(),
                            dto.getEmail()
                    );
                    break;

                default:
                    return Response.status(Response.Status.BAD_REQUEST).entity("Role inválido").build();
            }

            return Response.status(Response.Status.CREATED).entity(UserDTO.from(newUser)).build();

        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.CONFLICT).entity(e.getMessage()).build();
        }
    }


    // EP26 - Desativar utilizador (Soft delete)
    @PUT
    @Path("/{user_id}/deactivate")
    @RolesAllowed({"Administrator"})
    public Response deactivateUser(@PathParam("user_id") long userId) {
        try {
            userBean.deactivate(userId);
            return Response.ok("{\"message\": \"Utilizador desativado com sucesso\", \"status\": \"inactive\"}").build();
        } catch (EntityNotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND).build(); // [cite: 836]
        }
    }

    //EP07
    @GET
    @Path("/me/posts")
    @Authenticated
    public Response getMyPublications(@QueryParam("page") @DefaultValue("1") int page, @QueryParam("limit") @DefaultValue("10") int limit, @QueryParam("is_visible") Boolean isVisible, @QueryParam("tag") Long tagId) {

        String user_id = securityContext.getUserPrincipal().getName();

        List<PublicationDTO> publications = publicationBean.findMyPublications(user_id, page, limit, isVisible, tagId);


        return Response.ok(publications).build();
    }
}