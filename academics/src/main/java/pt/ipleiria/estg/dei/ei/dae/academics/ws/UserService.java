package pt.ipleiria.estg.dei.ei.dae.academics.ws;

import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.EJB;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import pt.ipleiria.estg.dei.ei.dae.academics.dtos.UserDTO;
import pt.ipleiria.estg.dei.ei.dae.academics.ejbs.UserBean;
import pt.ipleiria.estg.dei.ei.dae.academics.entities.User;
import pt.ipleiria.estg.dei.ei.dae.academics.security.Authenticated;
import java.util.List;

@Path("users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Authenticated // Protege todos os endpoints desta classe
public class UserService {
    @EJB
    private UserBean userBean;

    // EP31 - Consultar todos (Apenas Admin)
    @GET
    @RolesAllowed({"Administrador"})
    public List<UserDTO> getAllUsers() {
        return UserDTO.from(userBean.findAll());
    }

    // EP22 - Criar utilizador (Apenas Admin)
    @POST
    @RolesAllowed({"Administrador"})
    public Response createUser(UserDTO dto) {
        try {
            User newUser = userBean.create(
                    dto.getPassword(),
                    dto.getName(),
                    dto.getEmail(),
                    dto.getRole()
            );
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
}